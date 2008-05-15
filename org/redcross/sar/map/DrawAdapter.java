/**
 * 
 */
package org.redcross.sar.map;

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
import org.redcross.sar.event.DiskoWorkEvent.DiskoWorkEventType;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.map.DrawDialog;
import org.redcross.sar.gui.map.ElementDialog;
import org.redcross.sar.gui.map.ElementPanel;
import org.redcross.sar.gui.map.ElementPanel.ElementEvent;
import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.map.command.SelectFeatureTool;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.command.IDrawTool.DrawMode;
import org.redcross.sar.map.command.IDrawTool.FeatureType;
import org.redcross.sar.map.element.DrawFrame;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer.LayerCode;
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
import com.esri.arcgis.systemUI.ITool;

/**
 * @author kennetgu
 *
 */
public class DrawAdapter implements 
	IMsoUpdateListenerIf, IMsoLayerEventListener, 
	ElementPanel.IElementEventListener, IDiskoWorkListener {

	private int workCount = 0;
	private boolean isSelecting = false;
	private boolean isDirty = false;
	
	private DiskoMap map = null;
	private IDrawTool currentTool = null;
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
		
		// set draw mode
		drawMode = DrawMode.MODE_UNDEFINED;
		
		// add listeners
		msoModel.getEventManager().addClientUpdateListener(this);
		
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
				// add map control adapater
				this.map.removeIMapControlEvents2Listener(mapListener);
				// remove map as listener
				removeDiskoWorkEventListener(map);
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
				// add map control adapter to enable draw frame updates
				map.addIMapControlEvents2Listener(mapListener);
				// add map as work listener
				addDiskoWorkEventListener(map);
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
		// unregister?
		if(this.drawDialog!=null) {
			this.drawDialog.removeDiskoWorkEventListener(this);
		}
		// register?
		if(dialog!=null)
			dialog.addDiskoWorkEventListener(this);
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
	
	private void reset() throws AutomationException, IOException {
		// prepare for work
		setIsWorking();
		suspendUpdate();
		// clear dirty bit
		isDirty = false;
		// clear any selections
		if(map.isSelected(msoObject)>0) 
			map.setSelected(msoObject, false);
		// reset mso draw data
		setMsoDrawData(null,null,msoCode);
		// reset previous state
		previous = null;
		// resume old mode
		resumeMode();
		// reset active tool?
		if((currentTool!=null)) getActiveTool().reset();
		// finalize work
		resumeUpdate();
		setIsNotWorking();
	}	
	
	private void resumeMode() {
		
		try {
			
			// get old draw mode
			DrawMode oldDrawMode = this.drawMode;
			
			// only possible if draw dialog is registered
			if(drawDialog==null) return;
	
			// change state
			change(drawMode);
	
			// update tools
			drawDialog.setAttribute(drawMode, "SETDRAWMODE");
			
			// notify
			fireOnDrawModeChange(drawMode,oldDrawMode, msoObject);	

		}
		catch(Exception e) {
			e.printStackTrace();
		}		

		
	}

	private void SetDrawMode(DrawMode drawMode) {
		
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
		
		// set working
		setIsWorking();
		
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
			
			// get flag
			boolean isInferable = (msoObject!=null) ? (map.isSelected(msoObject)>0 ? true : (msoOwner!=null)) : (msoOwner!=null); 
			
			// infer mode from user selection and feasible draw modes?
			if(isInferable) {
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
			}
			else
				// return to default
				drawMode = DrawMode.MODE_CREATE;
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
			// initialize
			List<IMsoFeatureLayer> layers = null;
			// constrain mso hooks?
			if(isUndefinedMode() || isCreateMode() || isAppendMode()) {
				// clear selection and reset?
				if(msoObject!=null) {
					// clear selection?
					if(map.isSelected(msoObject)>0)
						layers = map.setSelected(msoObject, false);
					// reset hook
					msoObject = null;
				}
				// clear selection and reset?
				if(msoOwner!=null) {
					// clear selection?
					if(map.isSelected(msoOwner)>0) 
						layers = map.setSelected(msoOwner, false);
					// reset hook?
					if(!isAppendMode())
						msoOwner = null;
				}
			}
			else if(isReplaceMode() || isContinueMode()) {
				// select object?
				if(msoObject!=null && map.isSelected(msoObject)==0) {
					layers = map.setSelected(msoObject, true);
				}
			}
			// refresh map?
			if(layers != null) {
				map.refreshMsoLayers();
			}			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// forward?
		prepareFrame(true);
		
		// setup tools
		setToolSet(msoCode,getToolSet());
		
		// set not working
		setIsNotWorking();
		
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
	
	private void setMsoDrawData(IMsoObjectIf msoOwn, IMsoObjectIf msoObj, MsoClassCode code) {
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
				frame = MapUtil.getMsoEnvelope(msoOwner,map);
			else if(msoObject!=null)
				frame = MapUtil.getMsoEnvelope(msoObject,map);

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
	
	public void setFrame(IEnvelope e) throws AutomationException, IOException {
		// update
		geoFrame = (e!=null) ? e.getEnvelope() : null;	
		// refresh
		drawFrame(false);
	}
	
	public void setFrameUnion(IEnvelope e) throws AutomationException, IOException {
		// update
		if(geoFrame==null)
			setFrame(e);
		else {
			if(e!=null) geoFrame.union(e);
			// refresh
			drawFrame(false);			
		}
	}
	
	private void drawFrame(boolean refresh) throws IOException, AutomationException {
		
		try {
			// has frame?
			if(geoFrame!=null) {
				// get screen display and start drawing on it
				IDisplay display = drawFrame.display(); map.getActiveView().getScreenDisplay();
				// refresh?
				if(!refresh)
					display.startDrawing(display.getHDC(),(short) esriScreenCache.esriNoScreenCache);
				// expand?
				double d = display.getDisplayTransformation().fromPoints(15);
				// get next frame
				IEnvelope frame = MapUtil.expand(d,d,false,geoFrame.getEnvelope());
				// set text and frame
				drawFrame.setFrame(frame);
				// update invalidation rectangle?
				if(!refresh) display.finishDrawing();
				// activate frame
				drawFrame.activate();
			}
			else {
				// hide
				drawFrame.deactivate();
			}
			// forward?
			if(refresh) refreshFrame();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void prepareFrame(boolean refresh) {
		try {
			
			// consume?
			if(Utils.getApp().isLoading()) return;
			
			// get frame?
			if(isDirty) {
				// forward
				if(setMsoFrame()) drawFrame(false);
				// reset flag
				isDirty = false;
			}
			
			// get flag
			boolean isDrawAllowed = map.isDrawAllowed();
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
			// update caption text
			drawFrame.setText(getDescription());
			// update visible icons
			drawFrame.setIconVisible("cancel", isDrawAllowed);
			drawFrame.setIconVisible("finish", isDrawAllowed);
			drawFrame.setIconVisible("replace", isDrawAllowed && isReplaceAllowed());
			drawFrame.setIconVisible("continue", isDrawAllowed && isContinueAllowed());
			drawFrame.setIconVisible("append", isDrawAllowed && isAppendAllowed());
			// forward?
			if(refresh) refreshFrame();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						
	}
	
	public String getDescription() {
		// initialize
		String undef = "<" + DiskoEnumFactory.getText(DrawMode.MODE_UNDEFINED) + ">";
		String caption = undef;
		boolean isDrawAllowed = map.isDrawAllowed();
		if(!isDrawAllowed) {
			caption = "Zoom inn";
		}
		else {
			// parse state
			if(isCreateMode()) {
				caption = DiskoEnumFactory.getText(DrawMode.MODE_CREATE) + " " + (msoCode != null 
						? DiskoEnumFactory.getText(element): undef);
			}
			else if(isReplaceMode()) {
				caption = DiskoEnumFactory.getText(DrawMode.MODE_REPLACE) + " " + (msoObject != null 
						? MsoUtils.getMsoObjectName(msoObject, 1) : undef);
			}
			else if(isContinueMode()) {
				caption = DiskoEnumFactory.getText(DrawMode.MODE_CONTINUE) + " på " 
						+ (msoObject != null ? MsoUtils.getMsoObjectName(msoObject, 1) : undef);
			}
			else if(isAppendMode()) {
				caption = DiskoEnumFactory.getText(DrawMode.MODE_APPEND) + " "+ (msoCode != null 
						? DiskoEnumFactory.getText(msoCode): undef);
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
			&& (FeatureType.FEATURE_POLYLINE.equals(getActiveTool().getFeatureType()) 
					|| FeatureType.FEATURE_POLYGON.equals(getActiveTool().getFeatureType()));
	}
	
	public boolean isAppendAllowed() {
		return !isCreateMode() 
			&& (msoOwner!=null) 
			&& (FeatureType.FEATURE_POLYLINE.equals(getActiveTool().getFeatureType()) 
			|| FeatureType.FEATURE_POLYGON.equals(getActiveTool().getFeatureType()));
	}
	
	public void refreshFrame() throws AutomationException, IOException {
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
			String name = drawFrame.hitIcon(p.getX(), p.getY(), 1);
			// is a icon hit?
			if(name!=null && !name.isEmpty()) {
				// execute command
				if("cancel".equalsIgnoreCase(name)) {
					// forward?
					if((currentTool!=null)) getActiveTool().cancel();
					// finished
					return true;
				}
				else if("finish".equalsIgnoreCase(name)) {
					// forward?
					if((currentTool!=null)) getActiveTool().apply();
					// finished
					return true;
				}
				else if("replace".equalsIgnoreCase(name)) {
					// change mode
					SetDrawMode(DrawMode.MODE_REPLACE);
					// finished
					return true;
				}
				else if("continue".equalsIgnoreCase(name)) {
					// change mode
					SetDrawMode(DrawMode.MODE_CONTINUE);
					// finished
					return true;
				}
				else if("append".equalsIgnoreCase(name)) {
					// change mode
					SetDrawMode(DrawMode.MODE_APPEND);
					// finished
					return true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return false;
	}	
	
	public boolean finish() {
		// TODO: in case of future use...
		return false;
	}
	
	public boolean cancel() {
		boolean bFlag = false;
		try {
			// success
			// deactive?
			if(map.isEditSupportInstalled() && map.isVisible()) {
				// deactivate 
				drawFrame.deactivate();
			}
			bFlag = true;
			// forward
			reset();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return bFlag;
	}
	
	private void fireOnWorkChange(Object worker, IMsoObjectIf msoObj, Object data) {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(this,
				worker,msoObj,data,DiskoWorkEventType.TYPE_CHANGE);

		// forward
		fireOnWorkChange(e);    	
    }
    
    private void fireOnWorkChange(DiskoWorkEvent e)
    {
		// notify workListeners
		for (int i = 0; i < workListeners.size(); i++) {
			workListeners.get(i).onWorkChange(e);
		}
	}
    
    private void fireOnDrawFinished(DrawMode mode, IMsoObjectIf msoObject)
    {
		// notify drawListeners
		for (int i = 0; i < drawListeners.size(); i++) {
			drawListeners.get(i).onDrawFinished(mode,msoObject);
		}
		
	}
    
    private void fireOnDrawModeChange(DrawMode mode, DrawMode oldMode, IMsoObjectIf msoObject)
    {
		// notify drawListeners
		for (int i = 0; i < drawListeners.size(); i++) {
			drawListeners.get(i).onDrawModeChange(mode,oldMode,msoObject);
		}
	}
    
    private void fireOnElementChange(Enum element, IMsoObjectIf msoObject)
    {
		// notify drawListeners
		for (int i = 0; i < drawListeners.size(); i++) {
			drawListeners.get(i).onElementChange(element, msoObject);
		}
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
		if(elementDialog!=null)
			elementDialog.getElementPanel().msoObjectChanged(msoObject, mask);
	}

	private void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		// forward?
		if(elementDialog!=null) {
			elementDialog.getElementPanel().msoObjectDeleted(msoObject, mask);
			if(this.msoObject==msoObject) {
				cancel();
			}
		}
	}	
	
	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}	
	
	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException {
		
		// consume?
		if(isWorking() || !e.isFinal()) return;
		
		// set working
		setIsWorking();
		
		// forward
		if((currentTool!=null)) getActiveTool().reset();
		
		// update this
		try {
			
			// initialize to current
			Enum type = element;
			IMsoObjectIf msoObject = null;
			IMsoObjectIf msoOwner = null;
			
			// get selection list
			List<IMsoObjectIf> selection = e.getSelectedMsoObjects();
			
			// get element list
			JList elementList = (elementDialog!=null) ? elementDialog.getElementList() : null;
			
			// has no element list?
			if(elementList == null) return;
			
			// has selected items?
			if (selection != null && selection.size() > 0) {
				
				// get first selected object
				msoObject = selection.get(0);
				
				// get owner
				msoOwner = (msoObject == null) ? null : MsoUtils.getOwningArea(msoObject);
				
				// dispatch type of object
				if (msoObject instanceof IOperationAreaIf) {
					// get type
					type = MsoClassCode.CLASSCODE_OPERATIONAREA;
				}
				else if (msoObject instanceof ISearchAreaIf) {
					// get type
					type = MsoClassCode.CLASSCODE_SEARCHAREA;
				}
				else if (msoObject instanceof IRouteIf) {
					// initialize
					type = MsoClassCode.CLASSCODE_ROUTE;
					// get owning area
					IAreaIf area = (IAreaIf)msoOwner;
					// found area?
					if(area!=null) {
						// get sub type
						type = MsoUtils.getType(msoObject,true);
					}
				}
				else if (msoObject instanceof IPOIIf) {
					
					// initialize
					type = MsoClassCode.CLASSCODE_POI;
					
					// get poi
					IPOIIf poi = (IPOIIf)msoObject;
					
					// get poi type
					IPOIIf.POIType poiType = poi.getType();
					
					// get flag
					boolean isAreaPOI = (poiType == IPOIIf.POIType.START) || 
						(poiType == IPOIIf.POIType.VIA) || (poiType == IPOIIf.POIType.STOP);
					
					// is area poi?
					if(isAreaPOI) {

						// initialize
						type = SearchSubType.PATROL;
						
						// get owning area
						IAreaIf area = (IAreaIf)msoOwner;
						
						// get flag
						isAreaPOI = (area!=null);
						
						// found area?
						if(isAreaPOI) {
							IAssignmentIf assignment = area.getOwningAssignment();
							if (assignment instanceof ISearchIf) {
								ISearchIf search = (ISearchIf)assignment;
								type = search.getSubType();
							}							
						}
					}
				}
				else if(msoObject instanceof IUnitIf) {
					// get type
					type = MsoClassCode.CLASSCODE_UNIT;
				}
			}
			// forward
			setup(type,msoObject,msoOwner,msoOwner!=null);
		} catch (RuntimeException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}		
		// set not working
		setIsNotWorking();		
	}	
	
	public boolean nextElement() {

		// consume?
		if(isWorking()) return false;
		
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
		if(isWorking()) return false;
		
		// get element list
		JList elementList = (elementDialog!=null) ? elementDialog.getElementList() : null;		
		
		// has no element list?
		if(elementList != null && elementList.getSelectedValue()!=element)
			elementList.setSelectedValue(element, true);
		else
			setup(element,msoObject,msoOwner,true);
		
		// success
		return true;
		
	}
	
	public void setup(Enum element, IMsoObjectIf msoObj, IMsoObjectIf msoOwn, boolean setTool) {
		
		try {
		
			// initialize
			MsoClassCode code = null;
			IDrawTool defaultTool = null;
			DrawMode mode = DrawMode.MODE_UNDEFINED;
			
			// get nav bar
	        NavBar navBar = app.getNavBar();
	        
			// dispatch element
			if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(element)) {
				// reset current?
				if(msoObj!=null){
					if(!MsoClassCode.CLASSCODE_OPERATIONAREA.equals(msoObj.getMsoClassCode())){
						reset();
					}
				}				
				// set flag
				boolean isReplaceMode = msoObj instanceof IOperationAreaIf;
				// set draw mode
				mode = (isReplaceMode ? DrawMode.MODE_REPLACE : DrawMode.MODE_CREATE);
				// set class code
				code = MsoClassCode.CLASSCODE_OPERATIONAREA;
				// select default tool
				defaultTool = navBar.getFreeHandTool();
			}
			else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(element)) {
				// reset current?
				if(msoObj!=null){
					if(!MsoClassCode.CLASSCODE_SEARCHAREA.equals(msoObj.getMsoClassCode())){
						reset();
					}
				}
				// set flag
				boolean isReplaceMode = msoObj instanceof ISearchAreaIf;
				// set draw mode
				mode = (isReplaceMode ? DrawMode.MODE_REPLACE : DrawMode.MODE_CREATE);
				// set class code
				code = MsoClassCode.CLASSCODE_SEARCHAREA;
				// select default tool
				defaultTool = navBar.getFreeHandTool();
			}
			else if (MsoClassCode.CLASSCODE_ROUTE.equals(element)) {
				// reset current?
				if(msoObj!=null){
					if(!MsoClassCode.CLASSCODE_ROUTE.equals(msoObj.getMsoClassCode())){
						reset();
					}
				}
				// set flag
				boolean isReplaceMode = msoObj instanceof IRouteIf;
				// set draw mode
				mode = (isReplaceMode ? DrawMode.MODE_REPLACE : DrawMode.MODE_CREATE);
				// set class code
				code = MsoClassCode.CLASSCODE_ROUTE;
				// select default tool
				defaultTool = navBar.getFreeHandTool();
			}
			else if(element instanceof SearchSubType) {
				// reset current?
				if(msoOwn!=null){
					if(!MsoClassCode.CLASSCODE_AREA.equals(msoOwn.getMsoClassCode())){
						reset();
					}
				}
				// get update mode
				boolean isReplaceMode = (msoOwn instanceof IAreaIf 
						|| msoObj instanceof IRouteIf || msoObj instanceof IPOIIf);
				// set draw mode
				mode = (isReplaceMode ? ((msoObj instanceof IRouteIf || msoObj instanceof IPOIIf) ? 
								DrawMode.MODE_REPLACE : DrawMode.MODE_APPEND) : DrawMode.MODE_CREATE);
				// set class code
				code = (msoObj!=null) ? msoObj.getMsoClassCode() : MsoClassCode.CLASSCODE_ROUTE;
				// select default tool
				defaultTool = (msoObj instanceof IPOIIf ? navBar.getPOITool() : navBar.getFreeHandTool());				
			}
			else if (MsoClassCode.CLASSCODE_POI.equals(element)) {
				// reset current?
				if(msoObj!=null){
					if(!MsoClassCode.CLASSCODE_POI.equals(msoObj.getMsoClassCode())){
						reset();
					}
				}
				// get flag
				boolean isReplaceMode = msoObj instanceof IPOIIf;
				// set draw mode
				mode = (isReplaceMode ? DrawMode.MODE_REPLACE : DrawMode.MODE_CREATE);
				// set class code
				code = MsoClassCode.CLASSCODE_POI;
				// select default tool
				defaultTool = navBar.getPOITool();
			}
			else if (MsoClassCode.CLASSCODE_UNIT.equals(element)) {
				// reset current?
				if(msoObj!=null){
					if(!MsoClassCode.CLASSCODE_UNIT.equals(msoObj.getMsoClassCode())){
						reset();
					}
				}
				// get flag
				boolean isReplaceMode = msoObj instanceof IUnitIf;
				// set draw mode
				mode = (isReplaceMode ? DrawMode.MODE_REPLACE : DrawMode.MODE_CREATE);
				// set class code
				code = MsoClassCode.CLASSCODE_UNIT;
				// select default tool
				defaultTool = navBar.getPositionTool();
			}			
			else {
				// forward
				reset();
				// not supported!
				element = null;
			}
			// save element
			this.element = element;
			// notify?
			if(element!=null) {
				// set mso draw data
				setMsoDrawData(msoOwn, msoObj, code);
				// set drawmode
				SetDrawMode(mode);
				// select default tool?
				if(setTool) setDefaultTool(defaultTool);
				// forward
				syncLists();
				// notify
				fireOnElementChange(element,msoObj);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
				
	}
	
	private void syncLists() {
		// set flag
		isSelecting = true;			
		// update lists in element dialog
		if (elementDialog.getElementList().getSelectedValue()!=element)
			elementDialog.getElementList().setSelectedValue(element, false);				
		if(elementDialog.getObjectList().getSelectedValue()!=msoOwner)
			elementDialog.getObjectList().setSelectedValue(msoOwner, false);
		if(elementDialog.getPartList().getSelectedValue()!=msoObject)
			elementDialog.getPartList().setSelectedValue(msoObject, false);				
		// reset flag
		isSelecting = false;		
	}
	
	private Object[] getToolSet() {
		
		// initialize
		Object[] attributes = null;
		
		// dispatch element
		if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(element)) {
			// get attributes
			attributes = new Object[] {drawMode,msoObject};
		}
		else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(element)) {
			// get attributes
			attributes = new Object[] {drawMode,msoObject};
		}
		else if (MsoClassCode.CLASSCODE_ROUTE.equals(element)) {
			// get owning area
			IAreaIf area = (IAreaIf)msoOwner;
			// initialize with default value
			SearchSubType type = SearchSubType.PATROL;
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
		else if(element instanceof SearchSubType) {
			// add poi?
			if(msoObject!=null && MsoClassCode.CLASSCODE_POI.equals(msoObject.getMsoClassCode())) {
				// get owning area
				IAreaIf area = (IAreaIf)msoOwner;
				// has area?
				boolean isAreaPOI = (area!=null);
				// get attributes
				attributes = new Object[] {drawMode,isAreaPOI,msoOwner,msoObject};
			}
			else {
				// get requested sub type
				SearchSubType type = (SearchSubType)element;
				// get owning area
				IAreaIf area = (IAreaIf)msoOwner;
				// get current type?
				if(area!=null) {
					// is search sub type?
					if(area.getOwningAssignment() instanceof ISearchIf) {
						ISearchIf search = (ISearchIf)area.getOwningAssignment();
						// replace!
						type = search.getSubType();
					}
				}
				// flag
				boolean drawPolygon = (type == SearchSubType.AIR || 
						type == SearchSubType.LINE || 
						type == SearchSubType.MARINE ||
						type == SearchSubType.URBAN);
				// get attributes
				attributes = new Object[] {drawMode, drawPolygon,type,msoOwner,msoObject};
			}
		}
		else if (MsoClassCode.CLASSCODE_POI.equals(element)) {
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
	
	public void setToolSet(MsoClassCode code, Object[] attributes) {
		
		// consume?
		if(drawDialog==null) return;
		
		// set batch mode
		drawDialog.setBatchUpdate(true);
		
		// dispatch type of data
		if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(code)) {
			// get poi types
			POIType[] poiTypes = { POIType.GENERAL, POIType.INTELLIGENCE,
					 POIType.OBSERVATION, POIType.FINDING, POIType.SILENT_WITNESS };
			// set attibutes
			drawDialog.setAttribute(DiskoToolType.POI_TOOL,poiTypes,"POITYPES");
			drawDialog.setAttribute(attributes[0],"SETDRAWMODE");
			drawDialog.setAttribute(true,"DRAWPOLYGON");
			drawDialog.setAttribute(null,"SEARCHSUBTYPE");
			// get mso object
			IMsoObjectIf msoObj = (IMsoObjectIf)attributes[1];
			// enable all tools
			drawDialog.enableToolTypes(EnumSet.of(FeatureType.FEATURE_POLYGON,
					FeatureType.FEATURE_POLYLINE));
			// set mso draw data
			drawDialog.setMsoDrawData(null, msoObj, MsoClassCode.CLASSCODE_OPERATIONAREA);
		}
		else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(code)) {
			// get poi types
			POIType[] poiTypes = { POIType.GENERAL, POIType.INTELLIGENCE,
					 POIType.OBSERVATION, POIType.FINDING, POIType.SILENT_WITNESS };
			// set attibutes
			drawDialog.setAttribute(DiskoToolType.POI_TOOL,poiTypes,"POITYPES");
			drawDialog.setAttribute(attributes[0],"SETDRAWMODE");
			drawDialog.setAttribute(true,"DRAWPOLYGON");
			drawDialog.setAttribute(null,"SEARCHSUBTYPE");
			// get mso object
			IMsoObjectIf msoObj = (IMsoObjectIf)attributes[1];
			// enable all tools
			drawDialog.enableToolTypes(EnumSet.of(FeatureType.FEATURE_POLYGON,
					FeatureType.FEATURE_POLYLINE));
			// set mso draw data
			drawDialog.setMsoDrawData(null, msoObj, MsoClassCode.CLASSCODE_SEARCHAREA);
		}
		else if (MsoClassCode.CLASSCODE_ROUTE.equals(code)) {
			// get poi types
			POIType[] poiTypes = { POIType.START, POIType.VIA, POIType.STOP };
			// set attibutes
			drawDialog.setAttribute(DiskoToolType.POI_TOOL,poiTypes,"POITYPES");
			drawDialog.setAttribute(attributes[0],"SETDRAWMODE");
			drawDialog.setAttribute(attributes[1],"DRAWPOLYGON");
			drawDialog.setAttribute(attributes[2],"SEARCHSUBTYPE");
			// get mso objects
			IMsoObjectIf msoOwn = (IMsoObjectIf)attributes[3];
			IMsoObjectIf msoObj = (IMsoObjectIf)attributes[4];
			// enable all tools?
			if(msoObj==null)
				drawDialog.enableToolTypes(EnumSet.allOf(FeatureType.class));
			else{
				drawDialog.enableToolTypes(EnumSet.of(FeatureType.FEATURE_POLYGON,
						FeatureType.FEATURE_POLYLINE));
			}
			// set mso draw data
			drawDialog.setMsoDrawData(msoOwn, msoObj, MsoClassCode.CLASSCODE_ROUTE);
		}
		else if (MsoClassCode.CLASSCODE_POI.equals(code)) {
			// get mso objects
			IMsoObjectIf msoOwn = (IMsoObjectIf)attributes[2];
			IMsoObjectIf msoObj = (IMsoObjectIf)attributes[3];
			// initialize 
			POIType[] types = null;
			EnumSet<FeatureType> features = EnumSet.noneOf(FeatureType.class);
			// get flags
			boolean isAreaPOI = (Boolean)attributes[1];			
			// get attributes
			if(isAreaPOI) {
				types = new POIType[] { POIType.START, POIType.VIA, POIType.STOP };
				features = EnumSet.allOf(FeatureType.class);
			}
			else {
				types = new POIType[] { POIType.GENERAL, POIType.INTELLIGENCE,
						 POIType.OBSERVATION, POIType.FINDING, POIType.SILENT_WITNESS };
				features = EnumSet.of(FeatureType.FEATURE_POINT);
			}
			// set attibutes
			drawDialog.setAttribute(attributes[0],"SETDRAWMODE");
			drawDialog.setAttribute(DiskoToolType.POI_TOOL,types,"POITYPES");
			drawDialog.setAttribute(false,"DRAWPOLYGON");
			drawDialog.setAttribute(null,"SEARCHSUBTYPE");
			// limit tool selection
			drawDialog.enableToolTypes(features);
			// set mso draw data
			drawDialog.setMsoDrawData(msoOwn, msoObj, MsoClassCode.CLASSCODE_POI);
		}
		else if (MsoClassCode.CLASSCODE_UNIT.equals(code)) {
			// set attibutes
			drawDialog.setAttribute(attributes[0],"SETDRAWMODE");
			drawDialog.setAttribute(false,"DRAWPOLYGON");
			drawDialog.setAttribute(null,"SEARCHSUBTYPE");
			// get mso object
			IMsoObjectIf msoObj = (IMsoObjectIf)attributes[1];
			// enable position tools only
			drawDialog.enableToolType(DiskoToolType.POSITION_TOOL);
			// set mso draw data
			drawDialog.setMsoDrawData(null, msoObj, MsoClassCode.CLASSCODE_UNIT);
		}
		else {
			// diable all tools
			drawDialog.enableTools(false);
		}
		// forward
		drawDialog.getToolCaption();
			
		// reset batch mode
		drawDialog.setBatchUpdate(false);
		
	}	
	
	public IDrawTool getActiveTool() {
		return currentTool;
	}
	
	public void setDrawTool(IDrawTool tool) {
		// any change?
		if(currentTool!=tool) {
			// update hook
			currentTool = tool;
			// update draw frame?
			if(tool!=null && map.isEditSupportInstalled()) prepareFrame(drawFrame.isActive());
		}
	}
	
	private void setDefaultTool(IDrawTool tool) {
		// is selecting?
		if(tool!=null) {
			// get current tool
			ITool mapTool = null;
			try {
				mapTool = map.getCurrentTool();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
}
			// activate directly?
			if(mapTool==null || !mapTool.equals(tool.getType())) {
				drawDialog.setActiveTool(tool);				
			}
		}
	}
	
	/*==========================================================
	 * ElementPanel.IElementEventListener
	 *========================================================== 
	 */	
	
	public void onElementSelected(ElementEvent e) {
		
		// consume?
		if(isSelecting) return;
		
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
					setup(element,msoObject,msoOwner,true);
				}
				
			}	
			else {
				
				map.clearSelected();
				map.setSelected((IMsoObjectIf)e.getElement(),true);
				
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
					// expand extent
					extent = MapUtil.expand(1.25, extent);
					// set extent of object
					map.setExtent(extent);
					// hide dialog
					elementDialog.setVisible(false);
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
		if(isSelecting) return;
		
		// is not class element?
		if(!e.isClassElement()) {
		
			// prevent reentry
			setIsWorking();
			
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
				setup(element,msoObject,msoOwner,true);
				
				// expand extent
				extent = MapUtil.expand(1.25, extent);
				
				// suspend map update
				suspendUpdate();
											
				// set extent of object
				map.setExtent(extent);
								
				// select object
				map.clearSelected();
				map.setSelected(msoSelect, true);
				
				// resume map update
				resumeUpdate();
				
				// hide dialog
				elementDialog.setVisible(false);
				
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			// finished working
			setIsNotWorking();
		}
		
	}		
	
	public void onElementDelete(ElementEvent e) {
		
		// is not class element?
		if(!e.isClassElement()) {
			
			// get mso object
			final IMsoObjectIf msoObject = (IMsoObjectIf)e.getElement();
			
			// prompt user before executing request
			Runnable r = new Runnable() {

				public void run() {
					
					// prompt user
				int ans = JOptionPane.showConfirmDialog(app.getFrame(),
				                "Dette vil slette valgt objekt. Vil du fortsette?",
				                "Bekreft sletting", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					
					// do a rollback
					if(ans == JOptionPane.OK_OPTION) {
						// dispatch type
						if(msoObject instanceof IAreaIf) {
							// cast to IAreaIf
							IAreaIf area = (IAreaIf)msoObject;
							// get owning assignment
							IAssignmentIf assignment = area.getOwningAssignment(); 
							// delete assignment
							if(assignment!=null)
								MsoUtils.deleteAssignment(assignment);
							else
								MsoUtils.deleteArea(area);
							// cleanup
							cancel();
						}
						else {
							// do a general delete
							if(MsoUtils.delete(msoObject)) {
								// do cleanup
								cancel();
								// notify change?
								fireOnWorkChange(this, msoObject, null);
							}
							else {
								// failed
								JOptionPane.showMessageDialog(app.getFrame(),
						                "Sletting kunne utføres",
						                "Feilmelding", JOptionPane.OK_OPTION);
							}								
						}
					}
				}
				
			};
			SwingUtilities.invokeLater(r);
		}
			
	}
	
	/*==========================================================
	 * IDiskoWorkListener interface
	 *========================================================== 
	 */	
	
	public void onWorkChange(DiskoWorkEvent e) {
		// get mso object
		IMsoObjectIf msoObject = e.getMsoObject();
		// select object
		if(msoObject!=null && map!=null) {
			// prevent reentry
			setIsWorking();
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
			setIsNotWorking();
		}
		// update mso draw data
		setMsoDrawData((msoObject == null) ? null : MsoUtils.getOwningArea(msoObject), msoObject, msoCode);
		// notify
		fireOnDrawFinished(drawMode, msoObject);
		// resume old mode
		resumeMode();
		// forward
		fireOnWorkChange(e);
	}

	public void onWorkCancel(DiskoWorkEvent e) { 
		// initialize
		boolean resume = false;
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
		// forward
		if(resume) resumeMode();
	}
	
	public void onWorkFinish(DiskoWorkEvent e) { /* Not in use */ }
	
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
	
    public boolean isWorking() {
		return workCount>0;
	}

    private void setIsWorking() {
    	workCount++;
	}
	
    private void setIsNotWorking() {
    	if(workCount>0)
    		workCount--;;
    }

	/*==========================================================
	 * Interfaces
	 *========================================================== 
	 */	
    
    
	public interface IDrawAdapterListener {
		public void onDrawModeChange(DrawMode mode, DrawMode oldMode, IMsoObjectIf msoObject);
		public void onDrawFinished(DrawMode mode, IMsoObjectIf msoObject);
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
		
		private boolean wasSelected = false;
		
		AdapterState() {
			save();
		}
		
		public void save() {
			this.msoOwner = DrawAdapter.this.msoOwner;
			this.msoObject = DrawAdapter.this.msoObject;
			this.msoCode = DrawAdapter.this.msoCode;
			this.element = DrawAdapter.this.element;
			this.drawMode = DrawAdapter.this.drawMode;
			try {
				wasSelected = (map.isSelected(msoObject)>0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
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
						wasSelected = true;
					}
				}
			}
			// reselect?
			if(this.msoObject!=null) {
				try {
					map.setSelected(msoObject,wasSelected);
					map.refreshMsoLayers();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}						
			}
			// forward
			DrawAdapter.this.setMsoDrawData(this.msoOwner,this.msoObject,this.msoCode);
			// update the rest
			DrawAdapter.this.element = this.element;
			DrawAdapter.this.drawMode = this.drawMode;
		}
	}
	
}
