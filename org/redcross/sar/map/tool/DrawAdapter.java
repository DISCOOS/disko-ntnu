package org.redcross.sar.map.tool;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.gui.dialog.DrawDialog;
import org.redcross.sar.gui.dialog.ElementDialog;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.gui.panel.ElementPanel.ElementEvent;
import org.redcross.sar.gui.panel.ElementPanel.IElementEventListener;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.element.DrawFrame;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer.LayerCode;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.tool.IDrawTool.DrawMode;
import org.redcross.sar.map.tool.IDrawTool.FeatureType;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.MsoUtils;

import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnExtentUpdatedEvent;
import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.display.esriScreenCache;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author kennetgu
 *
 */
public class DrawAdapter implements IMsoUpdateListenerIf, IMsoLayerEventListener, 
									IElementEventListener , IDiskoWorkListener {

	private int consumeCount = 0;
	private int selectionCount = 0;
	private boolean isDirty = false;
	private boolean isExecutePending = false;
	
	private DiskoMap map = null;
	private IDiskoTool replacedTool = null;
	private IMsoModelIf msoModel = null;
	private IDiskoApplication app = null;

	// states
	private IMsoObjectIf msoOwner = null;
	private IMsoObjectIf msoObject = null;
	private MsoClassCode msoCode = null;	
	
	private Enum element = null;	
	private DrawMode drawMode = null;
	
	private IEnvelope geoFrame = null;

	private AdapterState previous = null;
	
	// edit support
	private DrawFrame drawFrame = null;
	private DrawDialog drawDialog = null;
	private ElementDialog elementDialog = null;	
		
	private EnumSet<MsoClassCode> myInterests = null;	

	private MapControlAdapter mapListener = null;
	private KeyToWorkAdapter keyToWorkAdapter = null;
	
	private ArrayList<IDrawAdapterListener> drawListeners = null;
	private ArrayList<IDiskoWorkListener> workListeners = null;
	
	public DrawAdapter(IDiskoApplication app) {
		
		// prepare
		this.app = app;
		this.myInterests = getMyInterests();
		this.msoModel = app.getMsoModel();
		this.drawListeners = new ArrayList<IDrawAdapterListener> ();
		this.workListeners = new ArrayList<IDiskoWorkListener>();
		this.mapListener = new MapControlAdapter();
		this.keyToWorkAdapter = new KeyToWorkAdapter();
		
		// set draw mode
		drawMode = DrawMode.MODE_UNDEFINED;
		
		// add listeners
		msoModel.getEventManager().addClientUpdateListener(this);
		
		// add global keyevent listeners
		app.getKeyEventDispatcher().addKeyListener(
				KeyEvent.KEY_PRESSED, KeyEvent.VK_ESCAPE, keyToWorkAdapter);
		app.getKeyEventDispatcher().addKeyListener(
				KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER, keyToWorkAdapter);
		
	}
	
	private static EnumSet<MsoClassCode> getMyInterests() {
		EnumSet<MsoClassCode> myInterests = 
			EnumSet.of(MsoClassCode.CLASSCODE_OPERATIONAREA);
		myInterests.add(MsoClassCode.CLASSCODE_SEARCHAREA);
		myInterests.add(MsoClassCode.CLASSCODE_AREA);
		myInterests.add(MsoClassCode.CLASSCODE_ROUTE);
		myInterests.add(MsoClassCode.CLASSCODE_TRACK);
		myInterests.add(MsoClassCode.CLASSCODE_POI);
		myInterests.add(MsoClassCode.CLASSCODE_UNIT);
		return myInterests;
	}
	
	private static EnumSet<LayerCode> getMyLayers() {
		EnumSet<LayerCode> myLayers = 
			EnumSet.of(LayerCode.OPERATION_AREA_LAYER);
		myLayers.add(LayerCode.SEARCH_AREA_LAYER);
		myLayers.add(LayerCode.AREA_LAYER);
		myLayers.add(LayerCode.ROUTE_LAYER);
		myLayers.add(LayerCode.POI_LAYER);
		myLayers.add(LayerCode.UNIT_LAYER);
		return myLayers;
	}
	
	public void register(DiskoMap map) {
		
		// is not supporting this?
		if(!map.isEditSupportInstalled()) return;
		
		// initialize
		Iterator<LayerCode> it = null;
		
		// unregister?
		if(this.map!=null) {
			// remove current listeners
			try {
				it = getMyLayers().iterator();
				while(it.hasNext()) {
					IMsoFeatureLayer msoLayer = this.map.getMsoLayer(it.next());
					if(msoLayer!=null) msoLayer.removeMsoLayerEventListener(this);
				}	
				// remove map control adapater
				this.map.removeIMapControlEvents2Listener(mapListener);
				// remove disko work listener
				map.removeDiskoWorkEventListener(this);
				// add me as listener
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		// register?
		if(map!=null) {
			// add listeners
			try {
				it = getMyLayers().iterator();
				while(it.hasNext()) {
					IMsoFeatureLayer msoLayer = map.getMsoLayer(it.next());
					if(msoLayer!=null)
						msoLayer.addMsoLayerEventListener(this);
				}
				// enable automatic update of draw frame 
				map.addIMapControlEvents2Listener(mapListener);
				// listen work events raised by tools
				map.addDiskoWorkListener(this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
		
		// save map
		this.map = map;
		
		// register draw frame
		register(map.getDrawFrame());
		
		// register draw dialog
		register(map.getDrawDialog());
		
		// register element dialog
		register(map.getElementDialog());
		
	}
	
	private void register(DrawFrame frame) {
		// save frame
		this.drawFrame = frame;
	}
	
	private void register(ElementDialog dialog) {
		// unregister?
		if(this.elementDialog!=null) {
			this.elementDialog.getElementPanel().removeElementListener(this);
		}
		// register?
		if(dialog!=null)
			dialog.getElementPanel().addElementListener(this);
		// save dialog
		this.elementDialog = dialog;
	}
	
	private void register(DrawDialog dialog) {
		// save dialog
		this.drawDialog = dialog;
	}
	
	public void addDrawAdapterListener(IDrawAdapterListener listener) {
		drawListeners.add(listener);
	}
	
	public void removeDrawAdapterListener(IDrawAdapterListener listener) {
		drawListeners.remove(listener);
	}
	
	public void addDiskoWorkEventListener(IDiskoWorkListener listener) {
		workListeners.add(listener);
	}
	
	public void removeDiskoWorkEventListener(IDiskoWorkListener listener) {
		workListeners.remove(listener);
	}
		
	public DrawMode getDrawMode() {
		return drawMode;
	}	
	
	public DrawDialog getDrawDialog() {
		return drawDialog;
	}
	
	public DrawFrame getDrawFrame() {
		return drawFrame;
	}
	
	public Enum getElement() {
		return element;
	}
	
	public IMsoObjectIf getMsoObject() {
		return msoObject;
	}
	
	public IMsoObjectIf getMsoOwner() {
		return msoOwner;
	}
	
	public MsoClassCode getMsoClassCode() {
		return msoCode;
	}
	
	private void reset() {
		// ensure safe execution! (only on EDT)
		if (SwingUtilities.isEventDispatchThread()) {
			try {
				// prepare for work
				setChangeable(false);
				suspendUpdate();
				// clear dirty bit
				isDirty = false;
				// clear any selections
				if (map.isSelected(msoObject) > 0)
					map.setSelected(msoObject, false);
				// reset mso draw data
				setMsoData(null, null, msoCode);
				// reset previous state
				previous = null;
				// resume old mode safely (always do this on EDT)
				resumeMode();
				// reset active tool?
				if (getSelectedTool() != null) getSelectedTool().reset();
				// finalize work
				resumeUpdate();
				setChangeable(true);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					reset();
				}
			});
		}		
	}	
	
	private void resumeMode() {
		
		try {
			
			// get old draw mode
			DrawMode oldDrawMode = this.drawMode;
			
			// only possible if draw dialog is registered
			if(drawDialog==null) return;
	
			// change state
			change(drawMode);
	
			// notify
			fireOnDrawModeChange(drawMode,oldDrawMode, msoObject);	

		}
		catch(Exception e) {
			e.printStackTrace();
		}		

		
	}

	private void setDrawMode(DrawMode drawMode) {
		
		// get old draw mode
		DrawMode oldDrawMode = this.drawMode;
		
		// is draw dialog registered?
		if(drawDialog==null) {
			// enter undefined mode
			change(DrawMode.MODE_UNDEFINED);
		}
		else {
			// infer mode from current state
			change(drawMode);
		}
		// notify
		fireOnDrawModeChange(drawMode,oldDrawMode, msoObject);			
	}

	private void change(DrawMode mode) {
		
		// prepare
		setChangeable(false);
		suspendUpdate();
		
		/* =====================================
		 * 1. Infer mode from state values
		 * =====================================
		 * The following logic is used; Only 
		 * certain draw mode states are allowed
		 * depending on the state variables
		 * msoObject, msoOwner and msoCode.
		 * ? -> CREATE  : Is allways allowed.
		 * ? -> REPLACE : Only allowed if
		 * 				  an msoObject hook 
		 * 				  exists 
		 * ? -> CONTINUE: same as REPLACE
		 * ? -> APPEND  : Only allowed if
		 * 				  an msoOwner hook 
		 * 				  exists
		 * =====================================*/
		
		try {
			
			// infer mode from user selection and feasible draw modes?
			if(isDrawModeInferable(msoOwner,msoObject)) {
				// get inferred mode
				if(DrawMode.MODE_CREATE.equals(mode)) {
					// forward
					loadPrevious(true);
					// get mode
					drawMode = (msoObject != null) ? DrawMode.MODE_REPLACE 
							: (msoOwner != null) ? DrawMode.MODE_APPEND : mode;
				}
				else if(DrawMode.MODE_REPLACE.equals(mode)) {
					// forward
					loadPrevious(true);
					// get mode
					drawMode = (msoObject == null) ? DrawMode.MODE_CREATE : mode;
				}
				else if(DrawMode.MODE_CONTINUE.equals(mode)) {
					// forward
					loadPrevious(true);
					// get mode
					drawMode = (msoObject == null) ? DrawMode.MODE_CREATE : mode;
				}
				else if(DrawMode.MODE_APPEND.equals(mode)) {
					// forward
					savePrevious();
					// get mode
					drawMode = (msoOwner == null) ? DrawMode.MODE_CREATE : mode;
					// set dirty flag
					isDirty = true;
				}
				else if(DrawMode.MODE_LOCKED.equals(mode)) {
					drawMode = mode;
				}
			}
			else {
				// only allow create mode when on inferable
				drawMode = getDefaultDrawMode(msoCode);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// return to default
			drawMode = DrawMode.MODE_UNDEFINED;
		}		
		
		/* =====================================
		 * 2. Constain state values to mode
		 * =====================================
		 * The following logic is used; Only 
		 * certain state variable values are 
		 * allowed depending on the draw mode
		 * msoObject, msoOwner and msoCode.
		 * IF CREATE   -> Both msoObject and
		 * 				  msoOwner must be null
		 * IF REPLACE  -> msoObject must be 
		 * 				  selected
		 * IF CONTINUE -> same as REPLACE
		 * IF APPEND   -> msoObject must be null
		 * =====================================*/
		
		
		try {
			// constrain mso hooks?
			if(isUndefinedMode() || isCreateMode() || isAppendMode()) {
				// clear selection and reset?
				if(msoObject!=null) {
					// clear selection?
					if(map.isSelected(msoObject)>0)
						map.setSelected(msoObject, false);
					// reset hook
					msoObject = null;
				}
				// clear selection and reset?
				if(msoOwner!=null) {
					// clear selection?
					if(map.isSelected(msoOwner)>0) 
						map.setSelected(msoOwner, false);
					// reset hook?
					if(!isAppendMode())
						msoOwner = null;
				}
			}
			else if(isReplaceMode() || isContinueMode()) {
				// select object?
				if(msoObject!=null)
					ensureIsSelected(msoObject,true);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// forward?
		prepareFrame(true);
		
		// forward
		setToolSet(msoCode,getToolSet());
		
		// finished
		resumeUpdate();
		setChangeable(true);
		
	}
	
	private boolean isDrawModeInferable(IMsoObjectIf msoOwn, IMsoObjectIf msoObj) {
		try {
			// flags
			boolean isReplaceable = 
					(msoObj instanceof IOperationAreaIf 
					|| msoObj instanceof ISearchAreaIf
					|| msoObj instanceof IRouteIf 
					|| msoObj instanceof IPOIIf
					|| msoObj instanceof IUnitIf);
			boolean isAppendable = (msoOwn instanceof IAreaIf);
			// replace mode possible?
			if(isReplaceable) {
				// draw mode is inferable
				return map.isSelected(msoObject)>0;
			}
			// append mode possible?
			if(isAppendable) {
				// draw mode is inferable
				return true;
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// draw mode is not possible to infer from mso data
		return false;
	}
	
	private void savePrevious() {
		// save current
		previous = new AdapterState();
	}
	
	private void loadPrevious(boolean autoselect) {
		// use previous?
		if(previous!=null) previous.load(autoselect);
		// only use once
		previous = null;
	}
	
	private void setMsoData(IMsoObjectIf msoOwn, IMsoObjectIf msoObj, MsoClassCode code) {
		// get frame update flag
		isDirty = isDirty || (msoOwner!=msoOwn) || (msoObject!=msoObj) || (msoCode!=code) ;
		// set data
		msoObject = msoObj;
		msoOwner = msoOwn;
		msoCode = code;		
	}
	
	public boolean setMsoFrame() {

		// get dirty flag
		boolean isDirty = (geoFrame!=null);

		try {
						
			// initialize frame
			IEnvelope frame = null;
			
			// get owner envelope?
			if(msoOwner!=null)
				frame = MapUtil.getMsoExtent(msoOwner,map,true);
			else if(msoObject!=null)
				frame = MapUtil.getMsoExtent(msoObject,map,true);

			// set new frame
			geoFrame = frame;
			
			// update dirty flag
			isDirty = isDirty || (geoFrame!=null);
					
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// finished
		return isDirty;
		
	}	
	
	public boolean setFrame(IEnvelope e) throws AutomationException, IOException {
		// update
		geoFrame = (e!=null) ? e.getEnvelope() : null;	
		// refresh
		return setGeoFrame();
	}
	
	public boolean setFrameUnion(IEnvelope e) throws AutomationException, IOException {
		// update
		if(geoFrame==null)
			return setFrame(e);
		if(e!=null) 
			geoFrame.union(e);
		// refresh
		return setGeoFrame();			
	}
	
	private boolean setGeoFrame() throws IOException, AutomationException {
		
		try {
			// has frame?
			if(geoFrame!=null) {
				// get screen display and start drawing on it
				IDisplay display = drawFrame.display(); map.getActiveView().getScreenDisplay();
				// enable draeing
				display.startDrawing(display.getHDC(),(short) esriScreenCache.esriNoScreenCache);
				// expand?
				double d = display.getDisplayTransformation().fromPoints(15);
				// get next frame
				IEnvelope frame = MapUtil.expand(d,d,false,geoFrame.getEnvelope());
				// set text and frame
				drawFrame.setFrame(frame);
				// update display
				display.finishDrawing();
				// activate frame?
				if(getSelectedTool()!=null && getSelectedTool().isShowDrawFrame())
					drawFrame.activate();
			}
			else {
				// hide
				drawFrame.deactivate();
			}
			// success
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failure
		return false;
	}	
	
	public void prepareFrame(boolean refresh) {
		try {
			
			// consume?
			if(Utils.getApp().isLoading()) return;
			
			// get frame?
			if(isDirty) {
				// forward
				if(setMsoFrame()) setGeoFrame();
				// reset flag
				isDirty = false;
			}
			
			// get flag
			boolean isDrawAllowed = isDrawAllowed();
			// get icon selection
			if(isDrawAllowed) {
				// parse state
				if(isCreateMode())
					drawFrame.clearSelectedIcon();
				else if(isReplaceMode())
					drawFrame.setSelectedIcon("replace", true);
				else if(isContinueMode())
					drawFrame.setSelectedIcon("continue", true);					
				else if(isAppendMode()) 
					drawFrame.setSelectedIcon("append", true);					
			}
			// get caption text
			String text = getDescription();
			// update caption text
			drawFrame.setText(text);
			// update visible icons
			drawFrame.setIconVisible("cancel", isDrawAllowed);
			drawFrame.setIconVisible("finish", isDrawAllowed);
			drawFrame.setIconVisible("replace", isDrawAllowed && isReplaceAllowed());
			drawFrame.setIconVisible("continue", isDrawAllowed && isContinueAllowed());
			drawFrame.setIconVisible("append", isDrawAllowed && isAppendAllowed());
			// commit changes
			drawFrame.commit();
			// forward?
			if(refresh) refreshFrame();
			// update property panel?
			if(getSelectedTool()!=null) // && getSelectedTool().isShowDrawFrame()) 
				getSelectedTool().getToolPanel().update();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						
	}
	
	public String getDescription() {
		// initialize
		String caption = DiskoEnumFactory.getText(DrawMode.MODE_UNDEFINED);
		// get draw flag
		boolean isDrawAllowed = map.isDrawAllowed();
		// translate current state to text
		if(!isDrawAllowed) {
			caption = DiskoStringFactory.getText("DRAW_RESTRICTION_ZOOM_IN");
		}
		else {
			// parse state
			if(isCreateMode()) {
				caption = DiskoEnumFactory.getText(DrawMode.MODE_CREATE) + " " + (msoCode != null 
						? DiskoEnumFactory.getText(element): caption);
			}
			else if(isReplaceMode()) {
				caption = DiskoEnumFactory.getText(DrawMode.MODE_REPLACE) + " " + (msoObject != null 
						? MsoUtils.getMsoObjectName(msoObject, 1) : caption);
			}
			else if(isContinueMode()) {
				caption = DiskoEnumFactory.getText(DrawMode.MODE_CONTINUE) + " på " 
						+ (msoObject != null ? MsoUtils.getMsoObjectName(msoObject, 1) : caption);
			}
			else if(isAppendMode()) {
				
				// get first part of caption
				caption = DiskoEnumFactory.getText(DrawMode.MODE_APPEND);
				
				// get feature type
				FeatureType type = (getSelectedTool()!=null) ? getSelectedTool().getFeatureType() : null;
				
				// detect type to append
				if(type==null) 
					caption += " "+ (msoCode != null ? DiskoEnumFactory.getText(msoCode): caption);
				else if(FeatureType.FEATURE_POINT.equals(type))
					caption += " "+ DiskoEnumFactory.getText(MsoClassCode.CLASSCODE_POI);					
				else
					caption += " "+ DiskoEnumFactory.getText(MsoClassCode.CLASSCODE_ROUTE);					
			}
			else if(isLockedMode()) {
				caption = String.format(DiskoEnumFactory.getText(DrawMode.MODE_LOCKED),
						MsoUtils.getMsoObjectName(msoObject!=null ? msoObject : msoOwner, 1));				
			}
		}
		return caption;	
	}
	
	public boolean isUndefinedMode() {
		return DrawMode.MODE_UNDEFINED.equals(drawMode);
	}
	
	public boolean isCreateMode() {
		return DrawMode.MODE_CREATE.equals(drawMode);
	}
	
	public boolean isReplaceMode() {
		return DrawMode.MODE_REPLACE.equals(drawMode);
	}

	public boolean isAppendMode() {
		return DrawMode.MODE_APPEND.equals(drawMode);
	}
	
	public boolean isContinueMode() {
		return DrawMode.MODE_CONTINUE.equals(drawMode);
	}
	
	public boolean isLockedMode() {
		return DrawMode.MODE_LOCKED.equals(drawMode);		
	}

	
	public boolean isCreateAllowed() {
		return isCreateMode() 
			&& (msoObject==null);
	}
	
	public boolean isReplaceAllowed() {
		return !isCreateMode() 
			&& (msoObject!=null);
	}

	public boolean isContinueAllowed() {
		return !isCreateMode() 
			&& (msoObject!=null) 
			&& (FeatureType.FEATURE_POLYLINE.equals(getSelectedTool().getFeatureType()));
	}
	
	public boolean isAppendAllowed() {
		return !isCreateMode() 
			&& (msoOwner!=null);
	}
	
	
	public boolean isDrawAllowed() {
		return !isLockedMode() && map.isDrawAllowed();
	}
	
	public void refreshFrame() {
		// should refresh?
		if(map.isVisible() && !(map.isRefreshPending() || map.isDrawingSupressed() || map.isNotifySuspended())) 
			drawFrame.refresh();
	}
	
	public boolean onMouseDown(int button, int shift, IPoint p) {
		try {
			// do a hit test
			String name = drawFrame.hitIcon(p.getX(), p.getY(), 1);
			// is a icon hit?
			return (name!=null && !name.isEmpty());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return false;
	}
	
	public boolean onMouseUp(int button, int shift, IPoint p) {
		try {
			// do a hit test
			final String name = drawFrame.hitIcon(p.getX(), p.getY(), 1);
			// is a icon hit?
			if(name!=null && !name.isEmpty()) {
				// ensure that this is run on EDT!
				if (SwingUtilities.isEventDispatchThread()) {
					execute(name);
				} else if(!isExecutePending){
					isExecutePending = true;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							execute(name);
						}
					});
				}				
				// finished
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return false;
	}	
	
	private void execute(String command) {
		// execute command
		if ("cancel".equalsIgnoreCase(command)) {
			// forward
			if (cancel()) refreshFrame();
		} else if ("finish".equalsIgnoreCase(command)) {
			// forward
			if (finish()) refreshFrame();
		} else if ("replace".equalsIgnoreCase(command)) {
			// change mode
			setDrawMode(DrawMode.MODE_REPLACE);
		} else if ("continue".equalsIgnoreCase(command)) {
			// change mode
			setDrawMode(DrawMode.MODE_CONTINUE);
		} else if ("append".equalsIgnoreCase(command)) {
			// change mode
			setDrawMode(DrawMode.MODE_APPEND);
		}		
		// reset flag
		isExecutePending = false;		
	}
	
	public boolean finish() {
		
		// consume?
		if(!isChangeable()) return false;
		
		setChangeable(false);
		suspendUpdate();
		
		// initialize
		boolean bFlag = false;
		
		try {
			// get selected tool
			IDrawTool tool = getSelectedTool();
			// only cancel current step?
			if(tool!=null && tool.isActive() && tool.isDirty()) {				
				// This may result in a reentry from the IDrawTool, hence
				// the IsWorking flag is set to consume this
				bFlag = tool.finish();
			}
			else {
				refreshFrame();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		resumeUpdate();
		setChangeable(true);
	
		// finished
		return bFlag;
	}
	
	public boolean cancel() {
		
		// consume?
		if(!isChangeable()) return false;
		
		suspendUpdate();
		
		// initalize
		boolean bFlag = false;
		
		try {
			// get selected tool
			IDrawTool tool = getSelectedTool();
			// only cancel current step?
			if(tool!=null && tool.isActive() && tool.isDirty()) {				
				// reset current changes
				tool.reset();
				tool.activate(0);
			}
			else {
				// deactivate frame?
				if(map.isEditSupportInstalled() && map.isVisible()) {
					// deactivate 
					drawFrame.deactivate();
				}
				bFlag = true;
				// forward
				reset();
				// set replaced tool?
				if(replacedTool!=null) {
					map.setActiveTool(replacedTool, 0);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		resumeUpdate();
		
		// finished
		return bFlag;
	}
	
	private void fireOnWorkFinish(Object data) {
		
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,data,DiskoWorkEvent.EVENT_FINISH);

		// forward
		fireOnWorkPerformed(e);    	
    }
    
    private void fireOnWorkPerformed(DiskoWorkEvent e)
    {
		// notify workListeners
		for (int i = 0; i < workListeners.size(); i++) {
			workListeners.get(i).onWorkPerformed(e);
		}
	}
    
    private void fireOnDrawFinished(DrawMode mode, IMsoObjectIf msoObject)
    {
		// notify drawListeners
		for (int i = 0; i < drawListeners.size(); i++) {
			drawListeners.get(i).onDrawWorkFinished(mode,msoObject);
		}
		
	}
    
    private void fireOnDrawModeChange(DrawMode mode, DrawMode oldMode, IMsoObjectIf msoObject)
    {
		// notify drawListeners
		for (int i = 0; i < drawListeners.size(); i++) {
			drawListeners.get(i).onDrawModeChanged(mode,oldMode,msoObject);
		}
	}
    
    private void fireOnElementChange(Enum element, IMsoObjectIf msoObject)
    {
		// notify drawListeners
		for (int i = 0; i < drawListeners.size(); i++) {
			drawListeners.get(i).onElementChange(element, msoObject);
		}
	}
    
	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */
    
	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}	
	
	public void handleMsoUpdateEvent(Update e) {
		
		// get flags
		int mask = e.getEventTypeMask();
        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
        boolean addedReference = (mask & MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
        boolean removedReference = (mask & MsoEvent.EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;
		
        // get mso object
        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
        
        // add object?
		if (createdObject) {
			msoObjectCreated(msoObj,mask);
		}
		// is object modified?
		if ( (addedReference || removedReference || modifiedObject)) {
			msoObjectChanged(msoObj,mask);
		}
		// delete object?
		if (deletedObject) {
			msoObjectDeleted(msoObj,mask);		
		}
	}

	private void msoObjectCreated(IMsoObjectIf msoObject, int mask) {
		// forward?
		if(elementDialog!=null)
			elementDialog.getElementPanel().msoObjectCreated(msoObject, mask);
	}
	
	private void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		// forward?
		if(elementDialog!=null) {
			elementDialog.getElementPanel().msoObjectChanged(msoObject, mask);
			
			if(this.msoObject==msoObject) {
				// forward
				setup(msoOwner,msoObject,true);
			}
		}
	}

	private void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		// forward?
		if(elementDialog!=null) {
			elementDialog.getElementPanel().msoObjectDeleted(msoObject, mask);
			
			if(this.msoObject==msoObject) {
				// forward
				setup(msoOwner,null,true);
			}
		}
	}	
	
	/* ===========================================
	 * IMsoLayerEventListener implementation
	 * ===========================================
	 */
	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException {
		
		// consume?
		if(isSelectionConsumed() || !e.isFinal()) return;
		
		// prevent reentry
		setConsumeSelection(true);
		
		// update this
		try {

			// initialize to current
			Enum type = element;
			IMsoObjectIf msoObject = null;
			IMsoObjectIf msoOwner = null;

			// get selection list
			List<IMsoObjectIf> selection = e.getSelectedMsoObjects();

			// get element list
			JList elementList = (elementDialog != null) ? elementDialog
					.getElementList()
					: null;

			// has no element list?
			if (elementList == null)
				return;

			// has selected items?
			if (selection != null && selection.size() > 0) {

				// get first selected object
				msoObject = selection.get(0);

				// get owner
				msoOwner = (msoObject == null) ? null
						: MsoUtils.getOwningArea(msoObject);

				// dispatch type of object
				if (msoObject instanceof IOperationAreaIf) {
					// get type
					type = MsoClassCode.CLASSCODE_OPERATIONAREA;
				} else if (msoObject instanceof ISearchAreaIf) {
					// get type
					type = MsoClassCode.CLASSCODE_SEARCHAREA;
				} else if (msoObject instanceof IRouteIf) {
					// initialize
					type = MsoClassCode.CLASSCODE_ROUTE;
					// get owning area
					IAreaIf area = (IAreaIf) msoOwner;
					// found area?
					if (area != null) {
						// get sub type
						type = MsoUtils.getType(msoObject, true);
					}
				} else if (msoObject instanceof IPOIIf) {

					// initialize
					type = MsoClassCode.CLASSCODE_POI;

					// get poi
					IPOIIf poi = (IPOIIf) msoObject;

					// get poi type
					IPOIIf.POIType poiType = poi.getType();

					// get flag
					boolean isAreaPOI = (poiType == IPOIIf.POIType.START)
							|| (poiType == IPOIIf.POIType.VIA)
							|| (poiType == IPOIIf.POIType.STOP);

					// is area poi?
					if (isAreaPOI) {

						// initialize
						type = SearchSubType.PATROL;

						// get owning area
						IAreaIf area = (IAreaIf) msoOwner;

						// get flag
						isAreaPOI = (area != null);

						// found area?
						if (isAreaPOI) {
							IAssignmentIf assignment = area
									.getOwningAssignment();
							if (assignment instanceof ISearchIf) {
								ISearchIf search = (ISearchIf) assignment;
								type = search.getSubType();
							}
						}
					}
				} else if (msoObject instanceof IUnitIf) {
					// get type
					type = MsoClassCode.CLASSCODE_UNIT;
				}
			}
			// forward
			setup(type, msoOwner, msoObject, msoObject!=null || msoOwner != null);
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		// detect selections again
		setConsumeSelection(false);
		
		
	}	
	
	public boolean nextElement() {

		// consume?
		if(!isChangeable()) return false;
		
		// get element list
		JList elementList = (elementDialog!=null) ? elementDialog.getElementList() : null;
		
		// has no element list?
		if(elementList == null) return false;
		
		// get command post
		ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();		
		// get current value
		Enum e = (Enum)elementList.getSelectedValue();
		// initialize?
		if(e==null) {
			e = MsoClassCode.CLASSCODE_OPERATIONAREA;
		}
		// decide
		if (cmdPost.getOperationAreaListItems().size() == 0) {
			if(!MsoClassCode.CLASSCODE_OPERATIONAREA.equals(e))
				e = MsoClassCode.CLASSCODE_OPERATIONAREA;
		}
		else if (cmdPost.getSearchAreaListItems().size() == 0) {
			if(!MsoClassCode.CLASSCODE_SEARCHAREA.equals(e))
				e = MsoClassCode.CLASSCODE_SEARCHAREA;
		}
		else if(!(e instanceof SearchSubType)) {
			e = SearchSubType.PATROL;
		}
		// forward
		selectElement(e);
		// success
		return true;
	}
	
	public boolean selectElement(Enum element) {
		
		// consume?
		if(!isChangeable()) return false;
		
		// get element list
		JList elementList = (elementDialog!=null) ? elementDialog.getElementList() : null;		
		
		// has no element list?
		if(elementList != null && elementList.getSelectedValue()!=element)
			elementList.setSelectedValue(element, true);
		else
			setup(element,msoOwner,msoObject,true);
		
		// success
		return true;
		
	}
	
	public void setup(IMsoObjectIf msoOwn, IMsoObjectIf msoObj, boolean setTool) {
		setup(element,msoOwn,msoObj,setTool);
	}
	
	public void setup(final Enum element, 
			final IMsoObjectIf msoOwn, final IMsoObjectIf msoObj, final boolean setTool) {

		// consume?
		if(!isChangeable()) return;
		
		// only execute gui updates on EDT!
		if (SwingUtilities.isEventDispatchThread()) {

			// forward
			setChangeable(false);
			suspendUpdate();

			// initialize
			Enum e = element;
			MsoClassCode code = null;
			IDiskoTool defaultTool = null;
			IMsoObjectIf msoOwner = null;
			IMsoObjectIf msoObject = null;
			DrawMode mode = DrawMode.MODE_UNDEFINED;

			// get nav bar
			NavBarPanel navBar = app.getNavBar();

			// dispatch element
			if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(element)) {
				// set class code
				code = MsoClassCode.CLASSCODE_OPERATIONAREA;
				// contrain
				msoOwner = null;
				msoObject = constrainToCode(MsoClassCode.CLASSCODE_OPERATIONAREA,msoObj);
				// translate state into draw mode
				mode = translateToDrawMode(msoOwner, msoObject,code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode) 
						? map.getActiveTool() : navBar.getFreeHandTool());
			} else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(element)) {
				// set class code
				code = MsoClassCode.CLASSCODE_SEARCHAREA;
				// contrain
				msoOwner = null;
				msoObject = constrainToCode(MsoClassCode.CLASSCODE_SEARCHAREA,msoObj);
				// translate state into draw mode
				mode = translateToDrawMode(msoOwner, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode) 
						? map.getActiveTool() : navBar.getFreeHandTool());
			} else if (MsoClassCode.CLASSCODE_ROUTE.equals(element)) {
				// set class code
				code = MsoClassCode.CLASSCODE_ROUTE;
				// contrain
				msoOwner = constrainToCode(MsoClassCode.CLASSCODE_AREA,msoOwn);
				msoObject = constrainToCode(MsoClassCode.CLASSCODE_ROUTE,msoObj);
				// translate state into draw mode
				mode = translateToDrawMode(msoOwner, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode) 
						? map.getActiveTool() : navBar.getFreeHandTool());
			} else if (element instanceof SearchSubType) {
				// set class code
				code = (msoObj instanceof IRouteIf || msoObj instanceof IPOIIf) 
					 ? msoObj.getMsoClassCode() : MsoClassCode.CLASSCODE_ROUTE;
				// contrain
				msoOwner = constrainToCode(MsoClassCode.CLASSCODE_AREA,msoOwn);
				msoObject = constrainToCode(code,msoObj);
				// translate state into draw mode
				mode = translateToDrawMode(msoOwner, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode) ? map.getActiveTool() : 
					(msoObj instanceof IPOIIf ? navBar.getPOITool() : navBar.getFreeHandTool()));
			} else if (MsoClassCode.CLASSCODE_POI.equals(element)) {
				// set class code
				code = MsoClassCode.CLASSCODE_POI;
				// forward
				msoOwner = constrainToCode(MsoClassCode.CLASSCODE_AREA,msoOwn);
				msoObject = constrainToCode(MsoClassCode.CLASSCODE_POI,msoObj);
				// translate state into draw mode
				mode = translateToDrawMode(msoOwner, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode) 
						? map.getActiveTool() : navBar.getPOITool());
			} else if (MsoClassCode.CLASSCODE_UNIT.equals(element)) {
				// set class code
				code = MsoClassCode.CLASSCODE_UNIT;
				// constrain
				msoOwner = null;
				msoObject = constrainToCode(MsoClassCode.CLASSCODE_UNIT,msoObj);
				// translate state into draw mode
				mode = translateToDrawMode(msoOwner, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode) 
						? map.getActiveTool() : navBar.getPositionTool());
			} else {
				// forward
				reset();
				// not supported!
				e = null;
			}
			// save element
			DrawAdapter.this.element = e;
			// notify?
			if (e != null) {
				// set mso draw data
				setMsoData(msoOwner, msoObject, code);
				// set drawmode
				setDrawMode(mode);
				// select default tool?
				if (!app.isLoading() && setTool) setDefaultTool(defaultTool);
				// forward
				syncLists();
				// notify
				fireOnElementChange(element, msoObject);
			}
			
			// forward
			setChangeable(true);
			resumeUpdate();
			
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setup(element, msoOwn, msoObj, setTool);
				}
			});
		}		
				
	}
		
	private IMsoObjectIf constrainToCode(final MsoClassCode code, final IMsoObjectIf msoObj) {
		try {
			// reset current section?
			if (msoObj != null) {
				if (!code.equals(msoObj.getMsoClassCode())) {
					// clear any selections
					if (map.isSelected(msoObj) > 0)
						map.setSelected(msoObj, false);
					return null;
				}
			}
			// allowed
			return msoObj;
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failure
		return null;
	}
	
	private DrawMode translateToDrawMode(IMsoObjectIf msoOwn, IMsoObjectIf msoObj, MsoClassCode code) {
		// flags
		boolean isReplaceable = 
				(msoObj instanceof IOperationAreaIf 
				|| msoObj instanceof ISearchAreaIf
				|| msoObj instanceof IRouteIf 
				|| msoObj instanceof IPOIIf
				|| msoObj instanceof IUnitIf);
		boolean isAppendable = (msoOwn instanceof IAreaIf);
		// check for lock mode
		if(msoOwn instanceof IAreaIf) {
			// cast to IAreaIf
			IAreaIf area = (IAreaIf)msoOwn;
			// get assignment
			IAssignmentIf assignment = area.getOwningAssignment();
			// is change allowed?
			if(assignment!=null && AssignmentStatus.FINISHED.compareTo(assignment.getStatus()) <= 0)
				// finished assignments are locked
				return DrawMode.MODE_LOCKED;					
		}
		// replace mode possible?
		if(isReplaceable) {
			return DrawMode.MODE_REPLACE;
		}
		// append mode possible?
		if(isAppendable) {
			return DrawMode.MODE_APPEND;
		}
		// get default draw mode
		return getDefaultDrawMode(code);
	}
	
	private DrawMode getDefaultDrawMode(MsoClassCode code) {
		// get default draw mode for given mso class
		if(MsoClassCode.CLASSCODE_UNIT.equals(code))
			return DrawMode.MODE_REPLACE;
		else
			return DrawMode.MODE_CREATE;
	}
	
	private void syncLists() {
		// set flag
		setConsumeSelection(true);			
		// update lists in element dialog
		if (elementDialog.getElementList().getSelectedValue()!=element)
			elementDialog.getElementList().setSelectedValue(element, false);				
		if(elementDialog.getObjectList().getSelectedValue()!=msoOwner)
			elementDialog.getObjectList().setSelectedValue(msoOwner, false);
		if(elementDialog.getPartList().getSelectedValue()!=msoObject)
			elementDialog.getPartList().setSelectedValue(msoObject, false);				
		// reset flag
		setConsumeSelection(false);		
	}
	
	private boolean isSelectionConsumed() {
		return (selectionCount>0);
	}
	
	private void setConsumeSelection(boolean isSelecting) {
		if(isSelecting)
			selectionCount++;
		else if(selectionCount>0)
			selectionCount--;
	}
	
	private Object[] getToolSet() {
		
		// initialize
		Object[] attributes = null;
		
		// dispatch element
		if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(msoCode)) {
			// get attributes
			attributes = new Object[] {drawMode,msoObject};
		}
		else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(msoCode)) {
			// get attributes
			attributes = new Object[] {drawMode,msoObject};
		}
		else if (MsoClassCode.CLASSCODE_ROUTE.equals(msoCode)) {
			// get owning area
			IAreaIf area = (IAreaIf)msoOwner;
			// initialize with default value
			SearchSubType type = element instanceof SearchSubType ? (SearchSubType)element : SearchSubType.PATROL;
			// get current type?
			if(area!=null) {
				// is search sub type?
				if(area.getOwningAssignment() instanceof ISearchIf) {
					ISearchIf search = (ISearchIf)area.getOwningAssignment();
					type = search.getSubType();
				}
			}
			// flag
			boolean drawPolygon = (type == SearchSubType.AIR || 
					type == SearchSubType.LINE || 
					type == SearchSubType.MARINE ||
					type == SearchSubType.URBAN);
			// get attributes
			attributes = new Object[] {drawMode,drawPolygon,type,msoOwner,msoObject};
		}
		else if (MsoClassCode.CLASSCODE_POI.equals(msoCode)) {
			// get owning area
			IAreaIf area = (IAreaIf)msoOwner;
			// has area?
			boolean isAreaPOI = (area!=null);
			// get attributes
			attributes = new Object[] {drawMode,isAreaPOI,msoOwner,msoObject};
		}
		else if (MsoClassCode.CLASSCODE_UNIT.equals(element)) {
			// get attributes
			attributes = new Object[] {drawMode,msoObject};
		}			
				
		// finished
		return attributes;
		
	}	
	
	public void setToolSet(final MsoClassCode code, final Object[] attributes) {
		
		// consume?
		if(drawDialog==null) return;
		
		// setup tools safely (always do this on the EDT)
		if (SwingUtilities.isEventDispatchThread()) {
			// set batch mode
			drawDialog.setBatchUpdate(true);
			// dispatch type of data
			if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(code)) {
				// set attributes
				drawDialog.setAttribute(DiskoToolType.POI_TOOL, null,"POITYPES");
				drawDialog.setAttribute(attributes[0], "SETDRAWMODE");
				drawDialog.setAttribute(true, "DRAWPOLYGON");
				drawDialog.setAttribute(null, "SEARCHSUBTYPE");
				// get mso object
				IMsoObjectIf msoObj = (IMsoObjectIf) attributes[1];
				// initialize
				EnumSet<FeatureType> features = EnumSet.noneOf(FeatureType.class);				
				// select enabled tools
				if (msoObj == null)
					features = EnumSet.allOf(FeatureType.class);
				else {
					features = EnumSet.of(
							FeatureType.FEATURE_POLYGON,
							FeatureType.FEATURE_POLYLINE);					
				}
				// set tooltypes
				drawDialog.enableToolTypes(features);
				// set mso draw data
				drawDialog.setMsoDrawData(null, msoObj,
						MsoClassCode.CLASSCODE_OPERATIONAREA);
			} else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(code)) {
				// set attibutes
				drawDialog.setAttribute(DiskoToolType.POI_TOOL, null, "POITYPES");
				drawDialog.setAttribute(attributes[0], "SETDRAWMODE");
				drawDialog.setAttribute(true, "DRAWPOLYGON");
				drawDialog.setAttribute(null, "SEARCHSUBTYPE");
				// get mso object
				IMsoObjectIf msoObj = (IMsoObjectIf) attributes[1];
				// initialize
				EnumSet<FeatureType> features = EnumSet.noneOf(FeatureType.class);				
				// select enabled tools
				if (msoObj == null)
					features = EnumSet.allOf(FeatureType.class);
				else {
					features = EnumSet.of(
							FeatureType.FEATURE_POLYGON,
							FeatureType.FEATURE_POLYLINE);					
				}
				// set tooltypes
				drawDialog.enableToolTypes(features);
				// set mso draw data
				drawDialog.setMsoDrawData(null, msoObj,
						MsoClassCode.CLASSCODE_SEARCHAREA);
			} else if (MsoClassCode.CLASSCODE_ROUTE.equals(code)) {
				// get mso objects
				IMsoObjectIf msoOwn = (IMsoObjectIf) attributes[3];
				IMsoObjectIf msoObj = (IMsoObjectIf) attributes[4];
				// get poi types
				POIType[] poiTypes = MsoUtils.getAvailablePOITypes(code,msoObj!=null ? msoObj : msoOwn);
				// set attibutes
				drawDialog.setAttribute(DiskoToolType.POI_TOOL, poiTypes,
						"POITYPES");
				drawDialog.setAttribute(attributes[0], "SETDRAWMODE");
				drawDialog.setAttribute(attributes[1], "DRAWPOLYGON");
				drawDialog.setAttribute(attributes[2], "SEARCHSUBTYPE");				
				// initialize
				EnumSet<FeatureType> features = EnumSet.noneOf(FeatureType.class);				
				// select enabled tools
				if (msoObj == null)
					features = EnumSet.allOf(FeatureType.class);
				else if (msoObj instanceof IPOIIf){
					features = EnumSet.of(FeatureType.FEATURE_POINT);
				}
				else {
					features = EnumSet.of(
							FeatureType.FEATURE_POLYGON,
							FeatureType.FEATURE_POLYLINE);					
				}
				// set tooltypes
				drawDialog.enableToolTypes(features);				
				// set mso draw data
				drawDialog.setMsoDrawData(msoOwn, msoObj,
						MsoClassCode.CLASSCODE_ROUTE);
			} else if (MsoClassCode.CLASSCODE_POI.equals(code)) {
				// iniitialize
				POIType[] poiTypes = null;
				// get mso objects
				IMsoObjectIf msoOwn = (IMsoObjectIf) attributes[2];
				IMsoObjectIf msoObj = (IMsoObjectIf) attributes[3];
				// get valid tool feature types
				EnumSet<FeatureType> features = EnumSet.noneOf(FeatureType.class);
				// get flags
				boolean isAreaPOI = (Boolean) attributes[1];
				// get attributes
				if (isAreaPOI) {
					// get available poi types
					poiTypes = MsoUtils.getAvailablePOITypes(MsoClassCode.CLASSCODE_ROUTE,msoObj!=null ? msoObj : msoOwn);
					// get allowed tool feature types
					if (msoObj == null)
						features = EnumSet.allOf(FeatureType.class);
					else if (msoObj instanceof IPOIIf){
						features = EnumSet.of(FeatureType.FEATURE_POINT);
					}
					else {
						features = EnumSet.of(
								FeatureType.FEATURE_POLYGON,
								FeatureType.FEATURE_POLYLINE);					
					}
				} else {
					// get available poi types
					poiTypes = MsoUtils.getAvailablePOITypes(code,msoObj);
					// get allowed tool feature types
					features = EnumSet.of(FeatureType.FEATURE_POINT);
				}
				// set attibutes
				drawDialog.setAttribute(attributes[0], "SETDRAWMODE");
				drawDialog.setAttribute(DiskoToolType.POI_TOOL, poiTypes,"POITYPES");
				drawDialog.setAttribute(false, "DRAWPOLYGON");
				drawDialog.setAttribute(null, "SEARCHSUBTYPE");
				// limit tool selection
				drawDialog.enableToolTypes(features);
				// set mso draw data
				drawDialog.setMsoDrawData(msoOwn, msoObj,
						MsoClassCode.CLASSCODE_POI);
			} else if (MsoClassCode.CLASSCODE_UNIT.equals(code)) {
				// set attibutes
				drawDialog.setAttribute(attributes[0], "SETDRAWMODE");
				drawDialog.setAttribute(false, "DRAWPOLYGON");
				drawDialog.setAttribute(null, "SEARCHSUBTYPE");
				drawDialog.setAttribute(DiskoToolType.POI_TOOL, null,"POITYPES");
				// get mso object
				IMsoObjectIf msoObj = (IMsoObjectIf) attributes[1];
				// enable position tools only
				drawDialog.enableToolType(DiskoToolType.POSITION_TOOL);
				// set mso draw data
				drawDialog.setMsoDrawData(null, msoObj,
						MsoClassCode.CLASSCODE_UNIT);
			} else {
				// diable all tools
				drawDialog.enableTools(false);
			}
			// forward
			drawDialog.getToolCaption();
			// reset batch mode, consuming any changes
			drawDialog.setBatchUpdate(false);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setToolSet(code, attributes);
				}
	
			});
		}		
		
	}	
	
	public IDrawTool getSelectedTool() {
		return drawDialog.getSelectedTool();
	}
	
	public void setSelectedTool(IDrawTool tool, boolean activate) {
		// update draw dialog
		drawDialog.setSelectedTool(tool,activate);		
	}
	
	public void onActiveToolChanged(IDrawTool tool) {
		// update?
		if(DrawMode.MODE_APPEND.equals(drawMode))
			resumeMode();
	}
	
	private void setDefaultTool(IDiskoTool tool) {
		// only allow draw tools be selected
		if(tool instanceof IDrawTool) {
			// initialize tools
			IDrawTool next = (IDrawTool)tool;
			IDrawTool current = getSelectedTool();
			// only change if selected draw tool feature type is different
			next = (current!=null && current.getFeatureType()==next.getFeatureType() ? current : next);
			// change?
			if(next!=null) {
				// get current tool
				replacedTool = map.getActiveTool();
				// forward
				drawDialog.setSelectedTool(next,true);
			}
		}
	}
	
	/*==========================================================
	 * ElementPanel.IElementEventListener
	 *========================================================== 
	 */	
	
	public void onElementSelected(ElementEvent e) {
		
		// consume?
		if(isSelectionConsumed() || !isChangeable()) return;

		// forward
		suspendUpdate();
		
		try {
			// is class element?
			if(e.isClassElement() || e.isObjectElement()) {
				
				// clear selected?
				if(map.getSelectionCount(true)>0) {
					map.clearSelected();
				}

				// force setup?
				if(e.isClassElement()) {
					// get element
					Enum element = (Enum)e.getElement();
					// force setup
					setup(element,msoOwner,msoObject,true);
				}
				
			}	
			else {
				
				// get mso object
				IMsoObjectIf msoObj = (IMsoObjectIf)e.getElement();
				
				// select object?
				if(msoObj!=null)
					ensureIsSelected(msoObject,true);
				
			}
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		// forward
		resumeUpdate();
		
	}

	public void onElementCenterAt(ElementEvent e) {
		
		// is not class element?
		if(!e.isClassElement()) {
		
			// suspend map update
			suspendUpdate();
			
			// forward
			try {
				
				// initialize
				IEnvelope extent = null;
				IMsoObjectIf msoSelect = null;
				
				// get mso object
				IMsoObjectIf msoObject = (IMsoObjectIf)e.getElement();
				
				// is area?
				if(msoObject instanceof IAreaIf) {
					// cast to IAreaIF
					IAreaIf area = (IAreaIf)msoObject;
					// get all routes
					Collection<IMsoObjectIf> c = area.getAreaGeodataItems();
					// loop over all objects
					for(IMsoObjectIf it:c) {
						if(msoSelect==null)
							msoSelect = it;
						// get extent
						IEnvelope env = map.getMsoObjectExtent(it);
						// do a union?
						if (env != null) {
							if (extent == null)
								extent = env.getEnvelope();
							else extent.union(env);
						}
					}
				}
				else {
					msoSelect = msoObject;
					extent = map.getMsoObjectExtent(msoSelect);
				}
				
				// has extent?
				if(extent!=null) {
					// hide dialog
					elementDialog.setVisible(false);
					// set extent of object
					map.setExtent(MapUtil.expand(1.25, extent));
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			// resume map update
			resumeUpdate();
		}
	}

	public void onElementEdit(ElementEvent e) {

		// consume?
		if(isSelectionConsumed() || !isChangeable()) return;
		
		// is not class element?
		if(!e.isClassElement()) {
		
			// prepare
			setChangeable(false);
			suspendUpdate();
										
			
			// forward
			try {
				
				// initialize
				Enum element = null;					
				IEnvelope extent = null;
				IMsoObjectIf msoSelect = null;
				IMsoObjectIf msoOwner = this.msoOwner;
				
				// get selected mso object
				IMsoObjectIf msoObject = (IMsoObjectIf)e.getElement();
				
				// is area?
				if(msoObject instanceof IAreaIf) {
					
					// replace current owner with this
					msoOwner = msoObject;
					
					// reset object
					msoObject = null;
					
					// cast to IAreaIf
					IAreaIf area = (IAreaIf)msoOwner;
					
					// get all routes
					Collection<IMsoObjectIf> c = area.getAreaGeodataItems();
					
					// loop over all objects
					for(IMsoObjectIf it:c) {
						// get extent
						IEnvelope env = map.getMsoObjectExtent(it);
						// do a union?
						if (env != null) {
							if (extent == null)
								extent = env.getEnvelope();
							else extent.union(env);
						}
					}

					
					// has assignment?
					if(area.getOwningAssignment() instanceof ISearchIf) {
						
						// cast to ISearchIf
						ISearchIf search = (ISearchIf)area.getOwningAssignment();
						
						// get sub type
						element = search.getSubType();

					}
					
				}
				else {
					// set selection
					msoSelect = msoObject;
					extent = map.getMsoObjectExtent(msoObject);
					// get element
					element = (msoObject==null) ? null : msoObject.getMsoClassCode();
				}
				
				// forward
				setup(element,msoOwner,msoObject,true);
				
				// expand extent
				extent = MapUtil.expand(1.25, extent);
				
				// set extent of object
				map.setExtent(extent);
								
				// select object?
				if(msoSelect!=null)
					ensureIsSelected(msoSelect,true);
				
				// hide dialog
				elementDialog.setVisible(false);
				
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
			// finished
			resumeUpdate();			
			setChangeable(true);
		}
		
	}		
	
	public void onElementDelete(ElementEvent e) {
		
		// is not class element?
		if(!e.isClassElement()) {
			
			// get mso object
			final IMsoObjectIf msoObj = (IMsoObjectIf)e.getElement();
			
			// prompt user before executing request
			Runnable r = new Runnable() {
				public void run() {
					
					// get default value
					String message = MsoUtils.getDeleteMessage(msoObj);		
					// allowed to delete this?
					if(MsoUtils.isDeleteable(msoObj)) {
						// forward
						int ans =  Utils.showConfirm("Bekreft sletting",message,JOptionPane.YES_NO_OPTION);
						// delete?
						if(ans == JOptionPane.YES_OPTION) { 
							// forwar
							suspendUpdate();							
							// get options
							int options = (msoObj instanceof IAreaIf ? 1 : 0);
							// try to delete
							boolean bFlag = MsoUtils.delete(msoObj, options); 
							// success?
							if(bFlag) {
								// do cleanup
								reset();
								// notify change?
								fireOnWorkFinish(msoObj);
							} else {
								// delete failed
								Utils.showError("Sletting kunne ikke utføres");
							}
							// forward
							resumeUpdate();
						}
					}
					else {			
						Utils.showWarning("Begrensning", message);
					}
				}									
			};
			SwingUtilities.invokeLater(r);
		}
			
	}
	
	/* ==========================================================
	 * IDiskoWorkListener interface
	 * ========================================================== */	
	
	public void onWorkPerformed(DiskoWorkEvent e) {
		
		// is selected tool?
		if(e.getSource() == getSelectedTool()) {
			
			// initialize
			boolean resume = false;
			
			// get tool
			IDrawTool tool = getSelectedTool();
			
			// dispatch type
			if(e.isFinish()) {
				// get mso object
				IMsoObjectIf msoObject = tool.getMsoObject();
				// select object
				if(msoObject!=null && map!=null) {
					// prevent reentry
					setChangeable(false);
					// suspend map update
					suspendUpdate();
					try {
						// clear current selected
						map.clearSelected();
						// select next object
						map.setSelected(msoObject, true);
					} catch (Exception ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
					// resume map update
					resumeUpdate();
					// allow reentry
					setChangeable(true);
				}
				// notify
				fireOnWorkFinish(msoObject);
				fireOnDrawFinished(drawMode, msoObject);
				// prepare for next
				setup(tool.getMsoOwner(),msoObject,false);
			}
			else if(e.isCancel()) {
				// resume 
				if(map!=null) {
					try {
						// get flag
						resume = (map.getCurrentTool() instanceof SelectFeatureTool);
					}
					catch(Exception ex) {
						ex.printStackTrace();
					}
				}			
			}
			// forward
			if(resume) resumeMode();
		}
	}

	/*==========================================================
	 * Private methods
	 *========================================================== 
	 */	
    	
	private List<IMsoFeatureLayer> ensureIsSelected(IMsoObjectIf msoObj, boolean unique) throws AutomationException, IOException {
		// initialize
		List<IMsoFeatureLayer> list = new ArrayList<IMsoFeatureLayer>();
		// get flags
		boolean isSelected = (map.isSelected(msoObject)>0);
		// clear current selection?
		if(unique && map.getSelectionCount(false)>0 && !isSelected) {
			list.addAll(map.clearSelected());
		}		
		// select object?
		if(msoObj!=null && !isSelected) {
			list.addAll(map.setSelected(msoObj, true));
		}
		// finished
		return list.size()==0 ? null : list;
	}
	
	private void suspendUpdate() {
		// suspend map update
		if(map!=null) {
			try {
				map.suspendNotify();				
				map.setSupressDrawing(true);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}
	
	private void resumeUpdate() {
		// resume map update
		if(map!=null) {
			try {
				map.setSupressDrawing(false);
				map.refreshMsoLayers();
				map.resumeNotify();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}
	
	public boolean isChangeable() {
		return (consumeCount==0);
	}
	
	public void setChangeable(boolean isChangeable) {
		if(!isChangeable)
			consumeCount++;
		else if(consumeCount>0)
			consumeCount--;
	}

	
	/*==========================================================
	 * Interfaces
	 *========================================================== 
	 */	
    
    
	public interface IDrawAdapterListener {
		public void onDrawModeChanged(DrawMode mode, DrawMode oldMode, IMsoObjectIf msoObject);
		public void onDrawWorkFinished(DrawMode mode, IMsoObjectIf msoObject);
		public void onElementChange(Enum element, IMsoObjectIf msoObject);
	}
	
	/*==========================================================
	 * Private classes
	 *========================================================== 
	 */	
	
	/**
	 * Class implementing the IMapControlEvents2Adapter intefaces
	 * 
	 * Is used to catch events that is used to draw the tool 
	 * geometries on the map
	 * 
	 *
	 */
	class MapControlAdapter extends IMapControlEvents2Adapter {

		private static final long serialVersionUID = 1L;
		
		@Override
		public void onExtentUpdated(IMapControlEvents2OnExtentUpdatedEvent e) throws IOException, AutomationException {
			// forward
			super.onExtentUpdated(e);
			// forward
			prepareFrame(true);
		}

	}
	
	class AdapterState {

		// states
		private IMsoObjectIf msoOwner = null;
		private IMsoObjectIf msoObject = null;
		private MsoClassCode msoCode = null;	
		
		private Enum element = null;	
		private DrawMode drawMode = null;
		
		AdapterState() {
			save();
		}
		
		public void save() {
			this.msoOwner = DrawAdapter.this.msoOwner;
			this.msoObject = DrawAdapter.this.msoObject;
			this.msoCode = DrawAdapter.this.msoCode;
			this.element = DrawAdapter.this.element;
			this.drawMode = DrawAdapter.this.drawMode;
		}
		
		public void load(boolean autoselect) {
			// autoselect first element in owner? 
			if(autoselect && this.msoObject==null && this.msoOwner!=null) {
				// get first route
				if(MsoClassCode.CLASSCODE_AREA.equals(this.msoOwner.getMsoClassCode())) {
					// cast to IAreaIf
					IAreaIf area = (IAreaIf)this.msoOwner;
					// get areas
					List<IMsoObjectIf> routes = new ArrayList<IMsoObjectIf>(area.getAreaGeodataItems());
					// get first route?
					if(routes.size()>0) {
						this.msoObject = routes.get(0);
					}
				}
			}
			// forward
			DrawAdapter.this.setMsoData(this.msoOwner,this.msoObject,this.msoCode);
			// update the rest
			DrawAdapter.this.element = this.element;
			DrawAdapter.this.drawMode = this.drawMode;
		}
	}
	
	class KeyToWorkAdapter extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			// can process event?
			if(map!=null && map.isVisible() && getSelectedTool() instanceof IDrawTool) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					// forward
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// forward
							cancel();
						}
					});
					break;
				case KeyEvent.VK_ENTER:
					// forward
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// forward
							finish();
						}
					});
				}
			}				
		}
	}		

	
}
