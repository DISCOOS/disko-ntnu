package org.redcross.sar.map.tool;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.redcross.sar.Application;
import org.redcross.sar.IApplication;
import org.redcross.sar.gui.dialog.DrawDialog;
import org.redcross.sar.gui.dialog.ElementDialog;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.gui.menu.NavMenu;
import org.redcross.sar.gui.panel.ElementPanel.ElementEvent;
import org.redcross.sar.gui.panel.ElementPanel.IElementEventListener;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.DrawFrame;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.IMsoLayerEventListener;
import org.redcross.sar.map.event.MsoLayerEvent;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMapLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
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
import org.redcross.sar.mso.event.IMsoChangeListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.event.IFlowListener;
import org.redcross.sar.work.event.FlowEvent;

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
public class MsoDrawAdapter implements IMsoChangeListenerIf, IMsoLayerEventListener,
									IElementEventListener , IFlowListener {

	private int consumeCount = 0;
	private int selectionCount = 0;
	private boolean isDirty = false;
	private boolean isExecutePending = false;

	private DiskoMap map;
	private IMapTool replacedTool;
	private IMsoModelIf msoModel;
	private IApplication app;

	// states
	private IMsoObjectIf msoOwner;
	private IMsoObjectIf msoObject;
	private MsoClassCode msoCode;

	private Enum<?> element;
	private DrawMode drawMode;

	private IEnvelope geoFrame;

	private AdapterState previous;

	// edit support
	private DrawFrame drawFrame;
	private DrawDialog drawDialog;
	private ElementDialog elementDialog;

	private List<Enum<?>> layers;
	private EnumSet<MsoClassCode> editable;

	private MapControlAdapter mapListener;
	private KeyToWorkAdapter keyToWorkAdapter;

	private ArrayList<IDrawAdapterListener> drawListeners;
	private ArrayList<IFlowListener> workListeners;

	public MsoDrawAdapter() {
		this(getSupport());
	}
		
	public MsoDrawAdapter(EnumSet<MsoClassCode> editable) {
		
		// prepare
		this.app = Application.getInstance();
		this.editable = editable;
		this.layers = MsoUtils.translate(editable);
		this.msoModel = app.getMsoModel();
		this.drawListeners = new ArrayList<IDrawAdapterListener>();
		this.workListeners = new ArrayList<IFlowListener>();
		this.mapListener = new MapControlAdapter();
		this.keyToWorkAdapter = new KeyToWorkAdapter();

		// set draw mode
		drawMode = DrawMode.MODE_UNDEFINED;

		// add listeners
		msoModel.getEventManager().addChangeListener(this);

		// add global key event listeners
		app.getKeyEventDispatcher().addKeyListener(
				KeyEvent.KEY_PRESSED, KeyEvent.VK_ESCAPE, keyToWorkAdapter);
		app.getKeyEventDispatcher().addKeyListener(
				KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER, keyToWorkAdapter);
		app.getKeyEventDispatcher().addKeyListener(
				KeyEvent.KEY_PRESSED, KeyEvent.VK_DELETE, keyToWorkAdapter);

	}
	
	public static EnumSet<MsoClassCode> getSupport() {
		EnumSet<MsoClassCode> editable =
			EnumSet.of(MsoClassCode.CLASSCODE_OPERATIONAREA);
		editable.add(MsoClassCode.CLASSCODE_SEARCHAREA);
		editable.add(MsoClassCode.CLASSCODE_AREA);
		editable.add(MsoClassCode.CLASSCODE_ROUTE);
		editable.add(MsoClassCode.CLASSCODE_TRACK);
		editable.add(MsoClassCode.CLASSCODE_POI);
		editable.add(MsoClassCode.CLASSCODE_UNIT);
		return editable;
	}
	
	public void register(DiskoMap map) {

		// is not supporting this?
		if(!map.isEditSupportInstalled()) return;

		// unregister?
		if(this.map!=null) {
			// remove current listeners
			try {
				for(Enum<?> it : layers) {
					IMapLayer layer = this.map.getLayer(it);
					if(layer instanceof IMsoFeatureLayer) { 
						((IMsoFeatureLayer)layer).removeMsoLayerEventListener(this);
					}
				}
				// remove map control adapter
				this.map.removeIMapControlEvents2Listener(mapListener);
				// remove DISKO work listener
				map.removeWorkEventListener(this);
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
				for(Enum<?> it : layers) {
					IMapLayer layer = map.getLayer(it);
					if(layer instanceof IMsoFeatureLayer)
						((IMsoFeatureLayer)layer).addMsoLayerEventListener(this);
				}
				// enable automatic update of draw frame
				map.addIMapControlEvents2Listener(mapListener);
				// listen work events raised by tools
				map.addWorkFlowListener(this);
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
	
	public boolean isSupported(MsoClassCode code) {
		return editable.contains(code);
	}

	public void addDrawAdapterListener(IDrawAdapterListener listener) {
		drawListeners.add(listener);
	}

	public void removeDrawAdapterListener(IDrawAdapterListener listener) {
		drawListeners.remove(listener);
	}

	public void addWorkFlowListener(IFlowListener listener) {
		workListeners.add(listener);
	}

	public void removeWorkFlowListener(IFlowListener listener) {
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

	public Enum<?> getElement() {
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
			// prepare for work
			setChangeable(false);
			suspendUpdate();
			// clear dirty bit
			isDirty = false;
			// reset mso draw data
			setMsoData(null, null, msoCode);
			// reset previous state
			previous = null;
			// resume old mode safely (always do this on EDT)
			resumeMode();
			// reset active tool?
			if (getSelectedDrawTool() != null) getSelectedDrawTool().reset();
			// ensure that object is selected
			boolean refresh = (ensureIsSelected(msoObject, true)!=null);
			// finalize work
			resumeUpdate(refresh);
			setChangeable(true);
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

		// initialize
		boolean refresh = false;

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
					if(map.isSelected(msoObject)>0) {
						map.setSelected(msoObject, false);
						refresh = true;
					}
					// reset hook
					msoObject = null;
				}
				// clear selection and reset?
				if(msoOwner!=null) {
					// clear selection?
					if(map.isSelected(msoOwner)>0) {
						map.setSelected(msoOwner, false);
						refresh = true;
					}
					// reset hook?
					if(!isAppendMode())
						msoOwner = null;
				}
			}
			else if(isReplaceMode() || isContinueMode()) {
				// select object?
				if(msoObject!=null) {
					refresh = ensureIsSelected(msoObject,true)!=null;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// forward
		setToolSet(msoCode,getToolSet());

		// finished
		resumeUpdate(refresh);
		setChangeable(true);

		// forward?
		if(!refresh) prepareFrame(true);

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
				IDisplay display = drawFrame.display(); //map.getActiveView().getScreenDisplay();
				// enable drawing
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
				if(getSelectedDrawTool()!=null && getSelectedDrawTool().isShowDrawFrame())
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
			if(Application.getInstance().isLoading()) return;

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
			if(getSelectedDrawTool()!=null)
				getSelectedDrawTool().getToolPanel().update();
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
				caption = DiskoEnumFactory.getText(DrawMode.MODE_CONTINUE) + " p� "
						+ (msoObject != null ? MsoUtils.getMsoObjectName(msoObject, 1) : caption);
			}
			else if(isAppendMode()) {

				// get first part of caption
				caption = DiskoEnumFactory.getText(DrawMode.MODE_APPEND);

				// get feature type
				FeatureType type = (getSelectedDrawTool()!=null) ? getSelectedDrawTool().getFeatureType() : null;

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
			&& (FeatureType.FEATURE_POLYLINE.equals(getSelectedDrawTool().getFeatureType()));
	}

	public boolean isAppendAllowed() {
		return !isCreateMode()
			&& (msoOwner!=null);
	}


	public boolean isDrawAllowed() {
		return !isLockedMode() && map.isDrawAllowed();
	}

	public boolean isWorkPending() {
		// get selected tool
		IDrawTool tool = getSelectedDrawTool();
		// check
		return tool!=null && tool.isActive() && tool.isDirty();
	}

	public void refreshFrame() {
		// should refresh?
		//if(map.isVisible() && !(map.isRefreshPending() || map.isDrawingSupressed() || map.isNotifySuspended()))
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
		return finish(true);
	}

	public boolean finish(boolean onWorkPool) {

		// consume?
		if(!isChangeable()) return false;

		setChangeable(false);
		suspendUpdate();

		// initialize
		boolean bFlag = false;

		try {
			// get selected tool
			IDrawTool tool = getSelectedDrawTool();
			// only cancel current step?
			if(tool!=null && tool.isActive() && tool.isDirty()) {
				// synchronized execution
				synchronized(tool) {
					// set flag
					boolean workPoolFlag = tool.setWorkPoolMode(onWorkPool);
					// This may result in a reentry from the IDrawTool, hence
					// the IsWorking flag is set to consume this
					bFlag = tool.finish();
					// resume flag
					tool.setWorkPoolMode(workPoolFlag);
				}
			}
			else {
				refreshFrame();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		resumeUpdate(false);
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
			IDrawTool tool = getSelectedDrawTool();
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
		FlowEvent e = new FlowEvent(this,data,FlowEvent.EVENT_FINISH);

		// forward
		fireOnWorkPerformed(e);

    }

    private void fireOnWorkPerformed(FlowEvent e)
    {
		// notify workListeners
		for (int i = 0; i < workListeners.size(); i++) {
			workListeners.get(i).onFlowPerformed(e);
		}
	}

    private void fireOnDrawFinished(DrawMode mode, IMsoObjectIf msoObject)
    {
		// notify drawListeners
		for (IDrawAdapterListener it : drawListeners) {
			it.onDrawWorkFinished(mode,msoObject);
		}

	}

    private void fireOnDrawModeChange(DrawMode mode, DrawMode oldMode, IMsoObjectIf msoObject)
    {
		// notify drawListeners
		for (int i = 0; i < drawListeners.size(); i++) {
			drawListeners.get(i).onDrawModeChanged(mode,oldMode,msoObject);
		}
	}

    private void fireOnElementChange(Enum<?> element, IMsoObjectIf msoObject)
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

	public EnumSet<MsoClassCode> getInterests() {
		return editable;
	}

	public void handleMsoChangeEvent(MsoEvent.ChangeList events) {

        // clear all?
        if(events.isClearAllEvent()) {
        	// forward
        	msoObjectDeleted(this.msoObject,0);
        }
        else {

			// loop over all events
			for(MsoEvent.Change e : events.getEvents(editable)) {

		        // get mso object
		        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();

				// consume loopback updates
				if(!e.isLoopbackMode()) {

					// get mask
					int mask = e.getMask();

					// get flags
			        boolean createdObject  = (mask & MsoEvent.MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
			        boolean deletedObject  = (mask & MsoEvent.MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
			        boolean modifiedObject = (mask & MsoEvent.MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
			        boolean addedReference = (mask & MsoEvent.MsoEventType.ADDED_RELATION_EVENT.maskValue()) != 0;
			        boolean removedReference = (mask & MsoEvent.MsoEventType.REMOVED_RELATION_EVENT.maskValue()) != 0;

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
			}
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
				setup(msoObject,true);
			}
		}
	}

	private void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		// forward?
		if(elementDialog!=null) {

			// forward
			elementDialog.getElementPanel().msoObjectDeleted(msoObject, mask);

			if(this.msoObject==msoObject) {
				// forward
				setup(null,true);
			}
		}
	}

	/* ===========================================
	 * IMsoLayerEventListener implementation
	 * ===========================================
	 */
	public void onSelectionChanged(MsoLayerEvent e) {

		// consume?
		if(isSelectionConsumed() || !e.isFinal()) return;

		// prevent reentry
		setConsumeSelection(true);

		// update this
		try {

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
			if (selection != null && selection.size() > 0)
				// use first selected object only
				setup(selection.get(0),true);
			else
				// forward
				setup(null,false);

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

		// no model available?
		if(!msoModel.getMsoManager().operationExists()) return false;

		// get command post
		ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();

		// get current value
		Enum<?> e = (Enum<?>)elementList.getSelectedValue();
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

	public boolean selectElement(Enum<?> element) {

		// consume?
		if(!isChangeable()) return false;

		// get element list
		JList elementList = (elementDialog!=null) ? elementDialog.getElementList() : null;

		// has no element list?
		if(elementList != null && elementList.getSelectedValue()!=element)
			elementList.setSelectedValue(element, true);
		else
			setup(element,msoObject,true);

		// success
		return true;

	}

	public void setup(IMsoObjectIf msoObj, boolean setTool) {
		setup(msoObj==null ? element : msoObj.getClassCode(),msoObj,setTool);
	}

	public void setup(
			final Enum<?> element,
			final IMsoObjectIf msoObj,
			final boolean setTool) {

		// consume?
		if(!isChangeable()) return;

		// only execute gui updates on EDT!
		if (SwingUtilities.isEventDispatchThread()) {

			// initialize
			boolean refresh = false;
			Enum<?> e = element;
			MsoClassCode code = null;
			IMapTool defaultTool = null;
			IMsoObjectIf msoOwner = null;
			IMsoObjectIf msoObject = null;
			DrawMode mode = DrawMode.MODE_UNDEFINED;

			// forward
			setChangeable(false);
			suspendUpdate();

			// get nav bar
			NavMenu navBar = app.getNavMenu();

			// dispatch element
			if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(element)) {
				// set class code
				code = MsoClassCode.CLASSCODE_OPERATIONAREA;
				// constrain
				msoObject = constrainToCode(MsoClassCode.CLASSCODE_OPERATIONAREA,msoObj);
				// translate state into draw mode
				mode = translateToDrawMode(null, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode)
						? map.getActiveTool() : navBar.getFreeHandTool());
			} else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(element)) {
				// set class code
				code = MsoClassCode.CLASSCODE_SEARCHAREA;
				// constrain
				msoObject = constrainToCode(MsoClassCode.CLASSCODE_SEARCHAREA,msoObj);
				// translate state into draw mode
				mode = translateToDrawMode(null, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode)
						? map.getActiveTool() : navBar.getFreeHandTool());
			} else if (MsoClassCode.CLASSCODE_ROUTE.equals(element)) {
				// set class code
				code = MsoClassCode.CLASSCODE_ROUTE;
				// constrain
				msoObject = constrainToCode(MsoClassCode.CLASSCODE_ROUTE,msoObj);
				msoOwner = constrainToCode(MsoClassCode.CLASSCODE_AREA,
						msoObj instanceof IAreaIf ? msoObj : MsoUtils.getOwningArea(msoObject));
				// translate state into draw mode
				mode = translateToDrawMode(msoOwner, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode)
						? map.getActiveTool() : navBar.getFreeHandTool());
			} else if (element instanceof SearchSubType) {
				// set class code
				code = (msoObj instanceof IRouteIf || msoObj instanceof IPOIIf)
					 ? msoObj.getClassCode() : MsoClassCode.CLASSCODE_ROUTE;
				// constrain
				msoObject = constrainToCode(code,msoObj);
				msoOwner = constrainToCode(MsoClassCode.CLASSCODE_AREA,
						msoObj instanceof IAreaIf ? msoObj : MsoUtils.getOwningArea(msoObject));
				// translate state into draw mode
				mode = translateToDrawMode(msoOwner, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode) ? map.getActiveTool() :
					(msoObj instanceof IPOIIf ? navBar.getPOITool() : navBar.getFreeHandTool()));
			} else if (MsoClassCode.CLASSCODE_POI.equals(element)) {
				// set class code
				code = MsoClassCode.CLASSCODE_POI;
				// constrain
				msoObject = constrainToCode(MsoClassCode.CLASSCODE_POI,msoObj);
				msoOwner = constrainToCode(MsoClassCode.CLASSCODE_AREA,
						msoObj instanceof IAreaIf ? msoObj : MsoUtils.getOwningArea(msoObject));
				// translate state into draw mode
				mode = translateToDrawMode(msoOwner, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode)
						? map.getActiveTool() : navBar.getPOITool());
			} else if (MsoClassCode.CLASSCODE_UNIT.equals(element)) {
				// set class code
				code = MsoClassCode.CLASSCODE_UNIT;
				// constrain
				msoObject = constrainToCode(MsoClassCode.CLASSCODE_UNIT,msoObj);
				// translate state into draw mode
				mode = translateToDrawMode(null, msoObject, code);
				// select default tool
				defaultTool = (DrawMode.MODE_LOCKED.equals(mode)
						? map.getActiveTool() : navBar.getPositionTool());
			} else {
				// forward
				reset();
				// not supported!
				e = null;
			}
			// supported?
			if(isSupported(code)) {
				// save element
				MsoDrawAdapter.this.element = e;
				// notify?
				if (e != null) {
					// set MSO data
					setMsoData(msoOwner, msoObject, code);
					// select object?
					if(msoObject!=null) {
						refresh = (ensureIsSelected(msoObject,true)!=null);
					}
					// set draw mode
					setDrawMode(mode);
					// select default tool?
					if (!app.isLoading() && setTool) setDefaultTool(defaultTool);
					// forward
					syncLists();
					// notify
					fireOnElementChange(element, msoObject);
				}
			}

			// forward
			setChangeable(true);
			resumeUpdate(refresh);

		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setup(element, msoObj, setTool);
				}
			});
		}

	}

	private IMsoObjectIf constrainToCode(final MsoClassCode code, final IMsoObjectIf msoObj) {
		try {
			// reset current section?
			if (msoObj != null) {
				if (!code.equals(msoObj.getClassCode())) {
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
				drawDialog.setAttribute(MapToolType.POI_TOOL, null,"POITYPES");
				drawDialog.setAttribute(attributes[0], "SETDRAWMODE");
				drawDialog.setAttribute(true, "DRAWPOLYGON");
				drawDialog.setAttribute(null, "SUBTYPE");
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
				drawDialog.setMsoData(null, msoObj,
						MsoClassCode.CLASSCODE_OPERATIONAREA);
			} else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(code)) {
				// set attibutes
				drawDialog.setAttribute(MapToolType.POI_TOOL, null, "POITYPES");
				drawDialog.setAttribute(attributes[0], "SETDRAWMODE");
				drawDialog.setAttribute(true, "DRAWPOLYGON");
				drawDialog.setAttribute(null, "SUBTYPE");
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
				drawDialog.setMsoData(null, msoObj,
						MsoClassCode.CLASSCODE_SEARCHAREA);
			} else if (MsoClassCode.CLASSCODE_ROUTE.equals(code)) {
				// get mso objects
				IMsoObjectIf msoOwn = (IMsoObjectIf) attributes[3];
				IMsoObjectIf msoObj = (IMsoObjectIf) attributes[4];
				// get poi types
				POIType[] poiTypes = MsoUtils.getAvailablePOITypes(code,msoObj!=null ? msoObj : msoOwn);
				// set attibutes
				drawDialog.setAttribute(MapToolType.POI_TOOL, poiTypes,
						"POITYPES");
				drawDialog.setAttribute(attributes[0], "SETDRAWMODE");
				drawDialog.setAttribute(attributes[1], "DRAWPOLYGON");
				drawDialog.setAttribute(attributes[2], "SUBTYPE");

				// select enabled tools
				EnumSet<FeatureType> features = EnumSet.allOf(FeatureType.class);

				/*
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
				*/
				// set tooltypes
				drawDialog.enableToolTypes(features);
				// set mso draw data
				drawDialog.setMsoData(msoOwn, msoObj,
						MsoClassCode.CLASSCODE_ROUTE);
			} else if (MsoClassCode.CLASSCODE_POI.equals(code)) {
				// iniitialize
				POIType[] poiTypes = null;
				SearchSubType searchSubType = null;
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
					if(msoObj==null)
						poiTypes = MsoUtils.getAvailablePOITypes(MsoClassCode.CLASSCODE_ROUTE, msoOwn);
					else
						poiTypes = new POIType[]{((IPOIIf)msoObj).getType()};
					// get search sub type
					searchSubType = msoOwn instanceof ISearchIf ? ((ISearchIf)msoOwn).getSubType() : null;
					// get allowed tool feature types
					if (msoObj == null)
						features = EnumSet.allOf(FeatureType.class);
					else if (msoOwn != null){
						features = EnumSet.of(
								FeatureType.FEATURE_POLYGON,
								FeatureType.FEATURE_POLYLINE);
					}
					else {
						features = EnumSet.of(FeatureType.FEATURE_POINT);
					}
				} else {
					// get available poi types
					poiTypes = MsoUtils.getAvailablePOITypes(code,msoObj);
					// get allowed tool feature types
					features = EnumSet.of(FeatureType.FEATURE_POINT);
				}
				// set attibutes
				drawDialog.setAttribute(attributes[0], "SETDRAWMODE");
				drawDialog.setAttribute(MapToolType.POI_TOOL, poiTypes,"POITYPES");
				drawDialog.setAttribute(false, "DRAWPOLYGON");
				drawDialog.setAttribute(searchSubType, "SUBTYPE");
				// limit tool selection
				drawDialog.enableToolTypes(features);
				// set mso draw data
				drawDialog.setMsoData(msoOwn, msoObj,
						MsoClassCode.CLASSCODE_POI);
			} else if (MsoClassCode.CLASSCODE_UNIT.equals(code)) {
				// set attibutes
				drawDialog.setAttribute(attributes[0], "SETDRAWMODE");
				drawDialog.setAttribute(false, "DRAWPOLYGON");
				drawDialog.setAttribute(null, "SUBTYPE");
				drawDialog.setAttribute(MapToolType.POI_TOOL, null,"POITYPES");
				// get mso object
				IMsoObjectIf msoObj = (IMsoObjectIf) attributes[1];
				// enable position tools only
				drawDialog.enableToolType(MapToolType.POSITION_TOOL);
				// set mso draw data
				drawDialog.setMsoData(null, msoObj,
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

	public IDrawTool getSelectedDrawTool() {
		return drawDialog.getSelectedTool();
	}

	public void setSelectedDrawTool(IDrawTool tool, boolean activate) {
		// update draw dialog
		drawDialog.setSelectedTool(tool,activate);
	}

	public boolean isDrawToolSupported(IDrawTool tool) {
		return (tool instanceof IMsoTool);
	}

	public void onActiveToolChanged(IDrawTool tool) {
		// update?
		if(DrawMode.MODE_APPEND.equals(drawMode))
			resumeMode();
	}

	private void setDefaultTool(IMapTool tool) {
		// only allow draw tools be selected
		if(tool instanceof IDrawTool) {
			// initialize tools
			IDrawTool next = (IDrawTool)tool;
			IDrawTool current = getSelectedDrawTool();
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

		// initialize
		boolean refresh = false;

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
					refresh = true;
				}

				// force setup?
				if(e.isClassElement()) {
					// get element
					Enum<?> element = (Enum<?>)e.getElement();
					// force setup
					setup(element,msoObject,true);
				}

			}
			else {

				// get mso object
				IMsoObjectIf msoObj = (IMsoObjectIf)e.getElement();

				// select object?
				if(msoObj!=null) {
					refresh = ensureIsSelected(msoObject,true)!=null;
				}

			}
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		// forward
		resumeUpdate(refresh);

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
			resumeUpdate(false);
		}
	}

	public void onElementEdit(ElementEvent e) {

		// consume?
		if(isSelectionConsumed() || !isChangeable()) return;

		// is not class element?
		if(!e.isClassElement()) {

			// prepare
			suspendUpdate();

			// initialize
			IEnvelope extent = null;

			// forward
			setup((IMsoObjectIf)e.getElement(),true);

			// hide dialog
			elementDialog.setVisible(false);

			// finished
			resumeUpdate(false);

			// forward
			try {

				// get extent
				if(this.msoOwner!=null)
					extent = map.getMsoObjectExtent(this.msoOwner);
				else if(this.msoObject!=null)
					extent = map.getMsoObjectExtent(this.msoObject);

				// set extent of object?
				if(extent!=null)
					map.zoomTo(extent,1.25);
				else
					map.refreshMsoLayers();

			}
			catch(Exception ex) {
				ex.printStackTrace();
			}

		}

	}

	public void onElementDelete(ElementEvent e) {

		// is not class element?
		if(!e.isClassElement()) {

			// get MSO object
			final IMsoObjectIf msoObj = (IMsoObjectIf)e.getElement();

			// handle later
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					delete(msoObj);
				}
			});

		}

	}

	public void delete() {

		// can process event?
		if(map!=null && map.isVisible()) {
			try {
				// get selected elements
				List<IMsoFeature> list = map.getMsoSelection();
				// any selections?
				if(list.size()>0)  {
					// forward
					delete(list.get(0).getMsoObject());
				}
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/* ==========================================================
	 * IWorkListener interface
	 * ========================================================== */

	public void onFlowPerformed(FlowEvent e) {

		// is selected tool?
		if(e.getSource() == getSelectedDrawTool()) {

			// initialize
			boolean resume = false;

			// get tool
			IDrawTool tool = getSelectedDrawTool();

			// supported tool?
			if(tool instanceof IMsoTool) {

				// dispatch type
				if(e.isFinish()) {
					// get MSO object
					IMsoObjectIf msoObject = ((IMsoTool)tool).getMsoObject();
					// select object
					if(msoObject!=null && map!=null) {
						// prevent reentry
						setChangeable(false);
						// suspend map update
						suspendUpdate();
						// ensure that object is selected
						boolean refresh = (ensureIsSelected(msoObject, true)!=null);
						// resume map update
						resumeUpdate(refresh);
						// allow reentry
						setChangeable(true);
					}
					// notify
					fireOnWorkFinish(msoObject);
					fireOnDrawFinished(drawMode, msoObject);
					// prepare for next
					setup(msoObject,false);
				}
				else if(e.isCancel()) {
					// resume
					if(map!=null) {
						try {
							// get flag
							resume = (map.getActiveTool() instanceof SelectTool);
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
	}

	/*==========================================================
	 * Private methods
	 *==========================================================
	 */

	private void delete(IMsoObjectIf msoObj) {

		// get default value
		String message = MsoUtils.getDeleteMessage(msoObj);

		// allowed to delete this?
		if(MsoUtils.isDeleteable(msoObj)) {
			// forward
			int ans =  Utils.showConfirm("Bekreft sletting",message,JOptionPane.YES_NO_OPTION);
			// delete?
			if(ans == JOptionPane.YES_OPTION) {
				// forward
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
					Utils.showError("Sletting kunne ikke utf�res");
				}
				// forward
				resumeUpdate();
			}
		}
		else {
			Utils.showWarning("Begrensning", message);
		}
	}


	private List<IMsoFeatureLayer> ensureIsSelected(IMsoObjectIf msoObj, boolean unique) {
		try {
			// initialize
			List<IMsoFeatureLayer> list = new ArrayList<IMsoFeatureLayer>();
			// get flags
			boolean isSelected = msoObj!=null && (map.isSelected(msoObj)>0);
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
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
		resumeUpdate(true);
	}

	private void resumeUpdate(boolean refresh) {
		if(map!=null) {
			try {
				map.setSupressDrawing(false);
				if(refresh) map.refreshMsoLayers();
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
		public void onElementChange(Enum<?> element, IMsoObjectIf msoObject);
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

		private Enum<?> element = null;
		private DrawMode drawMode = null;

		AdapterState() {
			save();
		}

		public void save() {
			this.msoOwner = MsoDrawAdapter.this.msoOwner;
			this.msoObject = MsoDrawAdapter.this.msoObject;
			this.msoCode = MsoDrawAdapter.this.msoCode;
			this.element = MsoDrawAdapter.this.element;
			this.drawMode = MsoDrawAdapter.this.drawMode;
		}

		public void load(boolean autoselect) {
			// autoselect first element in owner?
			if(autoselect && this.msoObject==null && this.msoOwner!=null) {
				// get first route
				if(MsoClassCode.CLASSCODE_AREA.equals(this.msoOwner.getClassCode())) {
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
			MsoDrawAdapter.this.setMsoData(this.msoOwner,this.msoObject,this.msoCode);
			// update the rest
			MsoDrawAdapter.this.element = this.element;
			MsoDrawAdapter.this.drawMode = this.drawMode;
		}
	}

	class KeyToWorkAdapter extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			// can process event?
			if(map!=null && map.isVisible() && getSelectedDrawTool() instanceof IDrawTool) {
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
					break;
				case KeyEvent.VK_DELETE:
					// forward
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// forward
							delete();
						}
					});
				}
			}
		}
	}


}
