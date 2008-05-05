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
import org.redcross.sar.gui.map.DrawDialog;
import org.redcross.sar.gui.map.ElementDialog;
import org.redcross.sar.gui.map.ElementPanel;
import org.redcross.sar.gui.map.ElementPanel.ElementEvent;
import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.map.command.SelectFeatureTool;
import org.redcross.sar.map.command.IDrawTool.DrawMode;
import org.redcross.sar.map.command.IDrawTool.FeatureType;
import org.redcross.sar.map.element.DrawFrame;
import org.redcross.sar.map.feature.IMsoFeature;
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
import com.esri.arcgis.geometry.IGeometry;
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

	private boolean isWorking = false;
	private boolean isSelecting = false;
	
	private Enum element = null;
	
	private DiskoMap map = null;
	private IDrawTool currentTool = null;
	private IMsoModelIf msoModel = null;
	private IDiskoApplication app = null;
	
	private IMsoObjectIf msoOwner = null;
	private IMsoObjectIf msoObject = null;
	private MsoClassCode msoCode = null;
	
	private IEnvelope geoFrame = null;
	
	private DrawFrame drawFrame = null;
	private DrawDialog drawDialog = null;
	private ElementDialog elementDialog = null;	
		
	private DrawMode drawMode = null;
	
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
			it = getMyLayers().iterator();
			while(it.hasNext()) {
				IMsoFeatureLayer msoLayer = this.map.getMsoLayer(it.next());
				if(msoLayer!=null) msoLayer.removeDiskoLayerEventListener(this);
			}	
			// remove current listener
			try {
				this.map.removeIMapControlEvents2Listener(mapListener);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		// register?
		if(map!=null) {
			// get iterator
			it = getMyLayers().iterator();
			while(it.hasNext()) {
				IMsoFeatureLayer msoLayer = map.getMsoLayer(it.next());
				if(msoLayer!=null)
					msoLayer.addDiskoLayerEventListener(this);
			}
			// add listener to used to draw tool geometries
			try {
				map.addIMapControlEvents2Listener(mapListener);
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
		
		// set draw mode
		SetDrawMode(DrawMode.MODE_UNDEFINED);
		
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
		// clear any selections
		if(map.isSelected(msoObject)>0) map.setSelected(msoObject, false);
		// reset mso draw data
		setMsoDrawData(null,null,msoCode);
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
		// forward
		updateFrame(true);
		// notify
		fireOnDrawModeChange(drawMode,oldDrawMode, msoObject);			
	}

	private void change(DrawMode mode) {
		
		// set working
		setIsWorking();
		
		try {
			
			// get flag
			boolean isInferable = (msoObject!=null) ? map.isSelected(msoObject)>0 : (msoOwner!=null); 
			
			// infer mode from user selection and feasible draw modes?
			if(isInferable) {
				// get inferred mode
				if(DrawMode.MODE_CREATE.equals(mode)) {
					drawMode = (msoObject != null) ? DrawMode.MODE_REPLACE 
							: (msoOwner != null) ? DrawMode.MODE_APPEND : mode;
				}
				else if(DrawMode.MODE_REPLACE.equals(mode)) {
					drawMode = (msoObject == null) ? DrawMode.MODE_CREATE : mode;
				}
				else if(DrawMode.MODE_CONTINUE.equals(mode)) {
					drawMode = (msoObject == null) ? DrawMode.MODE_CREATE : mode;
				}
				else if(DrawMode.MODE_APPEND.equals(mode)) {
					drawMode = (msoOwner == null) ? DrawMode.MODE_CREATE : mode;
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
		try {
			// constrain mso hooks?
			if(DrawMode.MODE_UNDEFINED.equals(drawMode) ||
					DrawMode.MODE_CREATE.equals(drawMode) || 
					DrawMode.MODE_APPEND.equals(drawMode)) {
				// clear selection?
				if(msoObject!=null && map.isSelected(msoObject)>0) {
					// clear selection
					map.setSelected(msoObject, false);
					// reset hooks
					msoObject = null;
				}
				// clear selection?
				if(msoOwner!=null && map.isSelected(msoOwner)>0) {
					// clear selection
					map.setSelected(msoOwner, false);
					// reset hooks
					msoOwner = null;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// set not working
		setIsNotWorking();
		
	}
	
	
	private void setMsoDrawData(IMsoObjectIf msoObj, IMsoObjectIf msoOwn, MsoClassCode code) {
		// get frame update flag
		boolean isDirty = (msoOwner!=msoOwn);
		// set data
		msoObject = msoObj;
		msoOwner = msoOwn;
		msoCode = code;
		// update frame?
		if(isDirty) {
			if(getFrame()) {
				try {
					drawFrame(true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}
	
	private boolean getFrame() {

		// get dirty flag
		boolean isDirty = (geoFrame!=null);

		try {
						
			// reset frame
			geoFrame = null;
			
            // forward?
			if(msoOwner!=null) {			
				// parse
				if(msoOwner instanceof IAreaIf) {
					// initialize frame
					IEnvelope frame = null;
					// cast to IAreaIf
					IAreaIf area = (IAreaIf)msoOwner;
					// get geometry bag of all lines
					IGeometry geoArea = MapUtil.getEsriGeometryBag(
							area.getAreaGeodata().getClone(),
			        		MsoClassCode.CLASSCODE_ROUTE, 
			        		map.getSpatialReference());
					// get geometry bag of all points
					IGeometry geoPOI = MapUtil.getEsriGeometryBag(
							area.getAreaPOIs(), 
							map.getSpatialReference());
					// create frame
					if(geoArea!=null) {
						frame = geoArea.getEnvelope();
					}
					if(geoPOI!=null) {
						if(frame==null)
							frame = geoArea.getEnvelope();
						else
							frame.union(geoPOI.getEnvelope());
					}
					// set new frame
					geoFrame = frame;
				}
			}
			
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
	
	private void drawFrame(boolean refresh) throws IOException, AutomationException {
		
		try {
			// has frame?
			if(geoFrame!=null) {
				// get screen display and start drawing on it
				IDisplay display = drawFrame.display(); map.getActiveView().getScreenDisplay();
				// refresh?
				if(!refresh)
					display.startDrawing(display.getHDC(),(short) esriScreenCache.esriNoScreenCache);
				// get frame to invalidate
				//IEnvelope invalidate = drawElement.getFrame();				
				// expand?
				double d = display.getDisplayTransformation().fromPoints(15);
				// get next frame
				IEnvelope frame = MapUtil.expand(d,d,false,geoFrame.getEnvelope());
				// forward
				updateFrame(false);							
				// set text and frame
				drawFrame.setFrame(frame);
				// update invalidation rectangle?
				if(!refresh) display.finishDrawing();
				// activate frame
				drawFrame.activate();
			}
			else {
				// hide?
				if(refresh) drawFrame.deactivate();
			}
			// forward?
			if(refresh) refreshDrawFrame();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	private void updateFrame(boolean redraw) {
		// is installed?
		
		try {
			// initialize
			String undef = "<" + Utils.getEnumText(DrawMode.MODE_UNDEFINED) + ">";
			String caption = undef;
			boolean isCreateMode = false;
			boolean isPointFeature = (!isToolActive()) ? false 
					: FeatureType.FEATURE_POINT.equals(getActiveTool().getFeatureType());
			// parse state
			if(DrawMode.MODE_CREATE.equals(drawMode)) {
				drawFrame.clearSelectedIcon();
				caption = Utils.getEnumText(DrawMode.MODE_CREATE) + " " + (msoCode != null 
						? Utils.getEnumText(msoCode): undef);
				// prepare to hide illegal icons
				isCreateMode = true;
			}
			else if(DrawMode.MODE_REPLACE.equals(drawMode)) {
				drawFrame.setSelectedIcon("replace", true);
				caption = Utils.getEnumText(DrawMode.MODE_REPLACE) + " " + (msoObject != null 
						? MsoUtils.getMsoObjectName(msoObject, 1) : undef);
			}
			else if(DrawMode.MODE_CONTINUE.equals(drawMode)) {
				drawFrame.setSelectedIcon("continue", true);					
				caption = Utils.getEnumText(DrawMode.MODE_CONTINUE) + " på " 
						+ (msoObject != null ? MsoUtils.getMsoObjectName(msoObject, 1) : undef);
			}
			else if(DrawMode.MODE_APPEND.equals(drawMode)) {
				drawFrame.setSelectedIcon("append", true);					
				caption = Utils.getEnumText(DrawMode.MODE_APPEND) + " "+ (msoCode != null 
						? Utils.getEnumText(msoCode): undef);
			}
			// update caption text
			drawFrame.setText(caption);
			// update visible icons
			drawFrame.setIconVisible("replace", !isCreateMode);
			drawFrame.setIconVisible("append", !isCreateMode && !isPointFeature);
			drawFrame.setIconVisible("continue", !isCreateMode && !isPointFeature);
			// forward?
			if(redraw) refreshDrawFrame();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						
	}
	
	public void refreshDrawFrame() throws AutomationException, IOException {
		if(!(map.isDrawingSupressed() || map.isNotifySuspended())) 
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
					// forward
					if((currentTool!=null)) getActiveTool().setAttribute(drawMode,"SETDRAWMODE");
					// finished
					return true;
				}
				else if("continue".equalsIgnoreCase(name)) {
					// change mode
					SetDrawMode(DrawMode.MODE_CONTINUE);
					// forward
					if((currentTool!=null)) getActiveTool().setAttribute(drawMode,"SETDRAWMODE");
					// finished
					return true;
				}
				else if("append".equalsIgnoreCase(name)) {
					// change mode
					SetDrawMode(DrawMode.MODE_APPEND);
					// forward
					if((currentTool!=null)) getActiveTool().setAttribute(drawMode,"SETDRAWMODE");
					// finished
					return true;
				}
				else if("delete".equalsIgnoreCase(name)) {
					// change mode
					SetDrawMode(DrawMode.MODE_DELETE);
					// forward
					if((currentTool!=null)) getActiveTool().setAttribute(drawMode,"SETDRAWMODE");
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
			// forward
			reset();
			// deactive
			drawFrame.deactivate();
			// success
			bFlag = true;
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
			if(msoObject==msoObject) {
				cancel();
			}
		}
	}	
	
	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}	
	
	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException {
		
		// consume?
		if(isWorking()) return;
		
		// set working
		setIsWorking();
		
		// forward
		setMsoDrawData(null,null,null);
		
		// forward
		if((currentTool!=null)) getActiveTool().reset();
		
		// update this
		try {
			
			// initialize to current
			Enum type = element;
			IMsoObjectIf msoObject = this.msoObject;
			IMsoObjectIf msoOwner = this.msoOwner;
			
			// get mso layer
			IMsoFeatureLayer msoLayer = (IMsoFeatureLayer)e.getSource();
			
			// get selection list
			List selection = msoLayer.getSelected();
			
			// get element list
			JList elementList = (elementDialog!=null) ? elementDialog.getElementList() : null;
			
			// has no element list?
			if(elementList == null) return;
			
			// set flag
			isSelecting = true;
			
			// has selected items?
			if (selection != null && selection.size() > 0) {
				
				// get first mso feature
				IMsoFeature msoFeature = (IMsoFeature)selection.get(0);
				
				// save current selected object
				msoObject = (msoFeature == null) ? null : msoFeature.getMsoObject();
				
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
			// set flag
			boolean force = false;
			// select type?
			if (elementList.getSelectedValue()!=type)
				elementList.setSelectedValue(type, false);				
			else force = true;
			if(elementDialog.getObjectList().getSelectedValue()!=msoOwner)
				elementDialog.getObjectList().setSelectedValue(msoOwner, false);				
			else force = true;
			if(elementDialog.getPartList().getSelectedValue()!=msoObject)
				elementDialog.getPartList().setSelectedValue(msoObject, false);				
			else force = true;			
			// force setup?
			if(force) setup(type,msoObject,msoOwner,msoOwner!=null);
			// reset flag
			isSelecting = false;
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
	
	private void setup(Enum element, IMsoObjectIf msoObj, IMsoObjectIf msoOwn, boolean setTool) {
		
		// only possible is draw dialog is registered
		if(drawDialog==null) return;
		
		try {
		
			// initialize
			MsoClassCode code = null;
			Object[] attributes = null;
			DrawMode mode = DrawMode.MODE_UNDEFINED;
			IDrawTool defaultTool = null;
			
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
				// get attributes
				Object[] attr = {mode,msoObj};
				// update
				attributes = attr;
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
				// get attributes
				Object[] attr = {mode,msoObj};
				// update attribute
				attributes = attr;
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
				// get owning area
				IAreaIf area = (IAreaIf)msoOwn;
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
				// set flag
				boolean isReplaceMode = msoObj instanceof IRouteIf;
				// set draw mode
				mode = (isReplaceMode ? DrawMode.MODE_REPLACE : DrawMode.MODE_CREATE);
				// get attributes
				Object[] attr = {mode,drawPolygon,type,msoObj,area};
				// update attributes
				attributes = attr;
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
				// get requested sub type
				SearchSubType type = (SearchSubType)element;
				// get owning area
				IAreaIf area = (IAreaIf)msoOwn;
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
				// get update mode
				boolean isReplaceMode = (msoOwn instanceof IAreaIf || msoObj instanceof IRouteIf);
				// set draw mode
				mode = (isReplaceMode ? ((msoObj instanceof IRouteIf) ? 
								DrawMode.MODE_REPLACE : DrawMode.MODE_APPEND) : DrawMode.MODE_CREATE);
				// get attributes
				Object[] attr = {mode, drawPolygon,type,msoOwn,msoObj};
				// update attributes
				attributes = attr;
				// set class code
				code = MsoClassCode.CLASSCODE_ROUTE;
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
				// get owning area
				IAreaIf area = (IAreaIf)msoOwn;
				// has area?
				boolean isAreaPOI = (area!=null);
				// get flag
				boolean isReplaceMode = msoObj instanceof IPOIIf;
				// set draw mode
				mode = (isReplaceMode ? DrawMode.MODE_REPLACE : DrawMode.MODE_CREATE);
				// get attributes
				Object[] attr = {mode,isAreaPOI,msoOwn,msoObj};
				// update attributes
				attributes = attr;
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
				// get attributes
				Object[] attr = {mode,msoObj};
				// update attributes
				attributes = attr;
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
			// notify?
			if(element!=null) {
				// set mso draw data
				setMsoDrawData(msoObj, msoOwn, code);
				// set drawmode
				SetDrawMode(mode);
				// setup tools
				drawDialog.setToolSet(code,attributes);
				// select default tool?
				if(setTool) setDefaultTool(defaultTool);
				// notify
				fireOnElementChange(element,msoObj);
			}
			// save element
			this.element = element;
			// set tool active?
			//if(map.isEditSupportInstalled()) getDrawDialog().setActiveTool(getActiveTool());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
				
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
			if(tool!=null && map.isEditSupportInstalled()) updateFrame(drawFrame.isActive());
		}
	}
	
	private boolean isToolActive() {
		return (currentTool!=null) ? currentTool.isActive() : false;
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
	
	public void onElementSelected(ElementEvent e) {
		
		// is class element?
		if(e.isClassElement() || e.isObjectElement()) {
			
			// clear selection?
			if(!isSelecting) {
				try {
					// clear selected?
					if(map.getSelectionCount(true)>0) {
						suspendUpdate();
						map.clearSelected();
						resumeUpdate();
					}

				} catch (AutomationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
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
			
			// clear selection?
			if(!isSelecting) {
				try {
					suspendUpdate();
					map.clearSelected();
					map.setSelected((IMsoObjectIf)e.getElement(),true);
					resumeUpdate();
				} catch (AutomationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}
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

		// is not class element?
		if(!e.isClassElement()) {
		
			// suspend map update
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
			// resume map update
			resumeUpdate();
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
			} catch (AutomationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// resume map update
			resumeUpdate();
			// allow reentry
			setIsNotWorking();
		}
		// update mso draw data
		setMsoDrawData(msoObject, (msoObject == null) ? null : MsoUtils.getOwningArea(msoObject), msoCode);
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
		return isWorking;
	}

    private void setIsWorking() {
    	isWorking = true;
	}
	
    private void setIsNotWorking() {
    	isWorking = false;
    }

	public interface IDrawAdapterListener {
		public void onDrawModeChange(DrawMode mode, DrawMode oldMode, IMsoObjectIf msoObject);
		public void onDrawFinished(DrawMode mode, IMsoObjectIf msoObject);
		public void onElementChange(Enum element, IMsoObjectIf msoObject);
	}
	
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
			drawFrame(true);
		}

	}
	
}
