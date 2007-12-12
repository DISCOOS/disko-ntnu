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
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkEventListener;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.event.DiskoWorkEvent.DiskoWorkEventType;
import org.redcross.sar.gui.DrawDialog;
import org.redcross.sar.gui.ElementDialog;
import org.redcross.sar.gui.ElementPanel;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.gui.ElementPanel.ElementEvent;
import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.map.command.SelectFeatureTool;
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

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author kennetgu
 *
 */
public class DrawAdapter implements 
	IMsoUpdateListenerIf, IMsoLayerEventListener, 
	ElementPanel.IElementEventListener, IDiskoWorkEventListener {

	public enum DrawMode {
		UNDEFINED,
		CREATE,
		APPEND,
		REPLACE
	}
	
	private boolean isWorking = false;
	private boolean isSelecting = false;
	
	private Enum element = null;
	
	private DiskoMap map = null;
	private IDrawTool currentTool = null;
	private IMsoModelIf msoModel = null;
	private IDiskoApplication app = null;
	
	private IMsoObjectIf currentMsoOwn = null;
	private IMsoObjectIf currentMsoObj = null;
	private IMsoFeature currentMsoFeature = null;
	
	private DrawDialog drawDialog = null;
	private ElementDialog elementDialog = null;	
		
	private DrawMode drawMode = null;
	
	private EnumSet<MsoClassCode> myInterests = null;	

	private ArrayList<IDrawAdapterListener> drawListeners = null;
	private ArrayList<IDiskoWorkEventListener> workListeners = null;
	
	public DrawAdapter(IDiskoApplication app) {
		
		// prepare
		this.app = app;
		this.myInterests = getMyInterests();
		this.msoModel = app.getMsoModel();
		this.drawListeners = new ArrayList<IDrawAdapterListener> ();
		this.workListeners = new ArrayList<IDiskoWorkEventListener>();
		
		// set draw mode
		drawMode = DrawMode.UNDEFINED;
		
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
		
		// initialize
		Iterator<LayerCode> it = null;
		
		// unregister?
		if(this.map!=null) {
			it = getMyLayers().iterator();
			while(it.hasNext()) {
				IMsoFeatureLayer msoLayer = this.map.getMsoLayer(it.next());
				if(msoLayer!=null)
					msoLayer.removeDiskoLayerEventListener(this);
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
		}		
		// set draw mode
		enterMode(DrawMode.UNDEFINED);
		// save map
		this.map = map;
	}
	
	public void register(ElementDialog dialog) {
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
	
	public void register(DrawDialog dialog) {
		// unregister?
		if(this.drawDialog!=null) {
			this.drawDialog.removeDiskoWorkEventListener(this);
		}
		// register?
		if(dialog!=null)
			dialog.addDiskoWorkEventListener(this);
		// set draw mode
		enterMode(DrawMode.UNDEFINED);
		// save dialog
		this.drawDialog = dialog;
	}
	
	public void addDrawAdapterListener(IDrawAdapterListener listener) {
		drawListeners.add(listener);
	}
	
	public void removeDrawAdapterListener(IDrawAdapterListener listener) {
		drawListeners.remove(listener);
	}
	
	public void addDiskoWorkEventListener(IDiskoWorkEventListener listener) {
		workListeners.add(listener);
	}
	
	public void removeDiskoWorkEventListener(IDiskoWorkEventListener listener) {
		workListeners.remove(listener);
	}
		
	public DrawMode getDrawMode() {
		return drawMode;
	}

	public Enum getElement() {
		return element;
	}
	
	public IMsoObjectIf getMsoObject() {
		return currentMsoObj;
	}
	
	public IMsoObjectIf getMsoOwner() {
		return currentMsoOwn;
	}
	
	private void enterMode(DrawMode drawMode) {
		// get old draw mode
		DrawMode oldDrawMode = this.drawMode;
		// is draw dialog registered?
		if(drawDialog==null) {
			// enter undefined mode
			this.drawMode=DrawMode.UNDEFINED;
		}
		else {
			// resume mode first?
			if(!DrawMode.CREATE.equals(this.drawMode)) resumeMode();
			// enter mode
			this.drawMode=drawMode;
		}
		// notify
		fireOnDrawModeChange(drawMode,oldDrawMode);			
	}
	
	private void resumeMode() {
		// get old draw mode
		DrawMode oldDrawMode = this.drawMode;
		// only possible is draw dialog is registered
		if(drawDialog==null) return;
		// get flag
		boolean isUpdateMode = DrawMode.REPLACE.equals(drawMode) 
			&& (currentMsoFeature!=null ? currentMsoFeature.isSelected():false);
		// update tools
		drawDialog.setAttribute(isUpdateMode, "ISUPDATEMODE");
		// resume default state
		drawMode=DrawMode.CREATE;
		// notify
		fireOnDrawModeChange(drawMode,oldDrawMode);			
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
    
    private void fireOnDrawModeChange(DrawMode mode, DrawMode oldMode)
    {
		// notify drawListeners
		for (int i = 0; i < drawListeners.size(); i++) {
			drawListeners.get(i).onDrawModeChange(mode,oldMode);
		}
	}
    
    private void fireOnElementChange(Enum element)
    {
		// notify drawListeners
		for (int i = 0; i < drawListeners.size(); i++) {
			drawListeners.get(i).onElementChange(element);
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
		if(elementDialog!=null)
			elementDialog.getElementPanel().msoObjectDeleted(msoObject, mask);
	}	
	
	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}	
	
	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException {
		
		// consume?
		if(isWorking()) return;
		
		// update this
		try {
			
			// get mso layer
			IMsoFeatureLayer msoLayer = (IMsoFeatureLayer)e.getSource();
			
			// get selection list
			List selection = msoLayer.getSelected();
			
			// has selected items?
			if (selection != null && selection.size() > 0) {
				
				// set flag
				isSelecting = true;
				
				// get first mso feature
				currentMsoFeature = (IMsoFeature)selection.get(0);
				
				// save current selected object
				currentMsoObj = (currentMsoFeature == null) ? null : currentMsoFeature.getMsoObject();
				
				// get owner
				currentMsoOwn = (currentMsoObj == null) ? null : MsoUtils.getOwningArea(currentMsoObj);
				
				// get element list
				JList elementList = (elementDialog!=null) ? elementDialog.getElementList() : null;
				
				// has no element list?
				if(elementList == null) return;
				
				// dispatch type of object
				if (currentMsoObj instanceof IOperationAreaIf) {
					// select type?
					if (elementList.getSelectedValue()!=MsoClassCode.CLASSCODE_OPERATIONAREA) {
						elementList.setSelectedValue(MsoClassCode.CLASSCODE_OPERATIONAREA, true);
					}
				}
				else if (currentMsoObj instanceof ISearchAreaIf) {
					// select type?
					if (elementList.getSelectedValue()!=MsoClassCode.CLASSCODE_SEARCHAREA) {
						elementList.setSelectedValue(MsoClassCode.CLASSCODE_SEARCHAREA, false);
					}
					else {
						setup(MsoClassCode.CLASSCODE_SEARCHAREA);
					}
				}
				else if (currentMsoObj instanceof IRouteIf) {
					// get owning area
					IAreaIf area = (IAreaIf)currentMsoOwn;
					// found area?
					if(area!=null) {
						// get sub type
						Enum type = MsoUtils.getType(currentMsoObj,true);
						// select type?
						if (elementList.getSelectedValue()!=type) {
							elementList.setSelectedValue(type, false);
						}
						else {
							setup(type);
						}							
					}
					else {
						setup(SearchSubType.PATROL);
					}
				}
				else if (currentMsoObj instanceof IPOIIf) {
					
					// get poi
					IPOIIf poi = (IPOIIf)currentMsoObj;
					
					// get poi type
					IPOIIf.POIType poiType = poi.getType();
					
					// get flag
					boolean isAreaPOI = (poiType == IPOIIf.POIType.START) || 
						(poiType == IPOIIf.POIType.VIA) || (poiType == IPOIIf.POIType.STOP);
					
					// is area poi?
					if(isAreaPOI) {

						// get owning area
						IAreaIf area = (IAreaIf)currentMsoOwn;
						
						// get flag
						isAreaPOI = (area!=null);
						
						// found area?
						if(isAreaPOI) {
							IAssignmentIf assignment = area.getOwningAssignment();
							if (assignment instanceof ISearchIf) {
								ISearchIf search = (ISearchIf)assignment;
								SearchSubType type = search.getSubType();
								// select type?
								if (elementList.getSelectedValue()!=type) {
									elementList.setSelectedValue(type, false);
								}
								else {
									setup(type);
								}
							}							
							else {
								setup(SearchSubType.PATROL);
							}
						}
						else {
							setup(SearchSubType.PATROL);
						}
					}
				}
				// reset flag
				isSelecting = false;
			}
		} catch (RuntimeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}	
	
	public boolean selectElement() {

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
			setup(element);
		
		// success
		return true;
		
	}
	
	private void setup(Enum element) {
		
		// only possible is draw dialog is registered
		if(drawDialog==null) return;
		
		try {
		
			// get nav bar
	        NavBar navBar = app.getNavBar();
	        
			// dispatch element
			if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(element)) {
				// reset current?
				if(currentMsoObj!=null){
					if(!MsoClassCode.CLASSCODE_OPERATIONAREA.equals(currentMsoObj.getMsoClassCode())){
						reset();
					}
				}				
				// get attributes
				Object[] attributes = {currentMsoObj instanceof IOperationAreaIf,currentMsoObj};
				// set draw mode
				enterMode((Boolean)attributes[0] ? DrawMode.REPLACE : DrawMode.CREATE);
				// setup tools
				drawDialog.setToolSet(MsoClassCode.CLASSCODE_OPERATIONAREA,attributes);
				setDefaultTool(navBar.getFreeHandTool());
			}
			else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(element)) {
				// reset current?
				if(currentMsoObj!=null){
					if(!MsoClassCode.CLASSCODE_SEARCHAREA.equals(currentMsoObj.getMsoClassCode())){
						reset();
					}
				}
				// get attributes
				Object[] attributes = {currentMsoObj instanceof ISearchAreaIf,currentMsoObj};
				// set draw mode
				enterMode((Boolean)attributes[0] ? DrawMode.REPLACE : DrawMode.CREATE);
				// setup tools
				drawDialog.setToolSet(MsoClassCode.CLASSCODE_SEARCHAREA,attributes);
				setDefaultTool(navBar.getFreeHandTool());
			}
			else if (MsoClassCode.CLASSCODE_ROUTE.equals(element)) {
				// reset current?
				if(currentMsoObj!=null){
					if(!MsoClassCode.CLASSCODE_ROUTE.equals(currentMsoObj.getMsoClassCode())){
						reset();
					}
				}
				// get owning area
				IAreaIf area = (IAreaIf)currentMsoOwn;
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
				Object[] attributes = {currentMsoObj instanceof IRouteIf,drawPolygon,type,currentMsoObj,area};
				// set draw mode
				enterMode((Boolean)attributes[0] ? DrawMode.REPLACE : DrawMode.CREATE);
				// forward
				drawDialog.setToolSet(MsoClassCode.CLASSCODE_ROUTE, attributes);
				setDefaultTool(navBar.getFreeHandTool());
			}
			else if(element instanceof SearchSubType) {
				// reset current?
				if(currentMsoOwn!=null){
					if(!MsoClassCode.CLASSCODE_AREA.equals(currentMsoOwn.getMsoClassCode())){
						reset();
					}
				}
				// get requested sub type
				SearchSubType type = (SearchSubType)element;
				// get owning area
				IAreaIf area = (IAreaIf)currentMsoOwn;
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
				Object[] attributes = {currentMsoOwn instanceof IAreaIf, drawPolygon,type,currentMsoOwn,currentMsoObj};
				// set draw mode
				enterMode((Boolean)attributes[0] ? DrawMode.APPEND : DrawMode.CREATE);
				// forward
				drawDialog.setToolSet(MsoClassCode.CLASSCODE_ROUTE, attributes);
				setDefaultTool(navBar.getFreeHandTool());
				
			}
			else if (MsoClassCode.CLASSCODE_POI.equals(element)) {
				// reset current?
				if(currentMsoObj!=null){
					if(!MsoClassCode.CLASSCODE_POI.equals(currentMsoObj.getMsoClassCode())){
						reset();
					}
				}
				// get owning area
				IAreaIf area = (IAreaIf)currentMsoOwn;
				// has area?
				boolean isAreaPOI = (area!=null);
				// get attributes
				Object[] attributes = {currentMsoObj instanceof IPOIIf,isAreaPOI,currentMsoOwn,currentMsoObj};
				// set draw mode
				enterMode((Boolean)attributes[0] ? DrawMode.REPLACE : DrawMode.CREATE);
				// setup tools
				drawDialog.setToolSet(MsoClassCode.CLASSCODE_POI,attributes);
				setDefaultTool(navBar.getPOITool());
			}
			else if (MsoClassCode.CLASSCODE_UNIT.equals(element)) {
				// reset current?
				if(currentMsoObj!=null){
					if(!MsoClassCode.CLASSCODE_UNIT.equals(currentMsoObj.getMsoClassCode())){
						reset();
					}
				}
				// get attributes
				Object[] attributes = {currentMsoObj instanceof IUnitIf,currentMsoObj};
				// setup tools
				drawDialog.setToolSet(MsoClassCode.CLASSCODE_UNIT,attributes);
				setDefaultTool(navBar.getPositionTool());
			}			
			else {
				// forward
				reset();
				// not supported!
				element = null;
			}
			// notify?
			if(element!=null)
				fireOnElementChange(element);
			// save element
			this.element = element;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
				
	}
	
	private void setDefaultTool(IDrawTool tool) {
		// is selecting?
		if(!isSelecting && tool!=null) {
			// activate directly?
			if(currentTool==null || !currentTool.getType().equals(tool.getType())) {
				currentTool = tool;
				drawDialog.setActiveTool(currentTool);				
			}
		}
	}
	
	private void reset() {
		suspendUpdate();
		setIsWorking();
		try {
			map.clearSelected();
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
		currentMsoFeature = null;
		currentMsoObj = null;
		currentMsoOwn = null;
		setIsNotWorking();
		resumeUpdate();
	}
	
	public void onElementSelected(ElementEvent e) {
		
		// is class element?
		if(e.isClassElement()) {
			
			// get element
			Enum element = (Enum)e.getElement();
			
			// clear selection?
			if(!isSelecting) {
				try {
					map.clearSelected();
				} catch (AutomationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			// forward
			setup(element);
			
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
				
				// expand extent
				extent = MapUtil.expand(1.25, extent);
				// set extent of object
				map.setExtent(extent);
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
				
				// get mso object
				currentMsoObj = (IMsoObjectIf)e.getElement();
				
				// is area?
				if(currentMsoObj instanceof IAreaIf) {
					
					// save as owner
					currentMsoOwn = currentMsoObj;
					
					// reset object
					currentMsoObj = null;
					
					// cast to IAreaIf
					IAreaIf area = (IAreaIf)currentMsoOwn;
					
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
					msoSelect = currentMsoObj;
					extent = map.getMsoObjectExtent(currentMsoObj);
					// get element
					element = (currentMsoObj==null) ? null : currentMsoObj.getMsoClassCode();
				}
				
				// forward
				setup(element);
				
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
						}
						else {
							// do a general delete
							MsoUtils.delete(msoObject);
						}
						// notify change?
						fireOnWorkChange(this, msoObject, null);
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
		// return to create mode
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
		public void onDrawModeChange(DrawMode mode, DrawMode oldMode);
		public void onElementChange(Enum element);
	}

	
}
