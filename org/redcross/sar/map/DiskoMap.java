package org.redcross.sar.map;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;

import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esri.arcgis.beans.map.MapBean;
import com.esri.arcgis.carto.*;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnExtentUpdatedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMapReplacedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMouseDownEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMouseMoveEvent;
import com.esri.arcgis.controls.esriControlsBorderStyle;
import com.esri.arcgis.geodatabase.IEnumIDs;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IPolygon;
import com.esri.arcgis.geometry.IPolyline;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.systemUI.ITool;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkRepeater;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.event.MsoLayerEventStack;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.DrawDialog;
import org.redcross.sar.gui.dialog.ElementDialog;
import org.redcross.sar.gui.dialog.SnapDialog;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.map.element.DrawFrame;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.MsoFeatureClass;
import org.redcross.sar.map.layer.*;
import org.redcross.sar.map.layer.IMsoFeatureLayer.LayerCode;
import org.redcross.sar.map.tool.DrawAdapter;
import org.redcross.sar.map.tool.IDiskoTool;
import org.redcross.sar.map.tool.IDrawTool;
import org.redcross.sar.map.tool.SnapAdapter;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.thread.DiskoMapProgressor;
import org.redcross.sar.thread.DiskoProgressMonitor;
import org.redcross.sar.util.mso.Position;

/**
 * This calls extends MapBean to provide user interface for map rendering
 * @author geira
 *
 */
public final class DiskoMap extends MapBean implements IDiskoMap, IMsoUpdateListenerIf {

	//private static final double pixelSize = 0.000377; //meter
	
	private static final long serialVersionUID = 1L;
	
	// static constants
	private static final double DPI = Toolkit.getDefaultToolkit().getScreenResolution();
	private static final double PIXEL_SIZE = 0.00254/DPI; // pixels per meter
	private static final double ZOOM_RATIO = 1.25;
	private static final double MAX_DRAW_SCALE = 75000;
	private static final double MAX_SNAP_SCALE = 50000;
	
	// properties
	private String mxdDoc = null;
	private IMsoModelIf msoModel = null;
	private IDiskoMapManager mapManager = null;
	private IDiskoTool activeTool = null;
	private int supressDrawing = 0;
	private int notifySuspended = 0;
	private int currentBase = 0;
	private boolean isCacheChanged = false;
	private boolean isEditSupportInstalled = false;
	
	// lists
	private MsoLayerSelectionModel msoLayerSelectionModel = null;
	private WmsLayerSelectionModel wmsLayerSelectionModel = null;
	private MapLayerSelectionModel mapLayerSelectionModel = null;
	private EnumSet<LayerCode> layerCodes = null;
	private EnumSet<MsoClassCode> classCodes = null;
	private Map<MsoClassCode,EnumSet<MsoClassCode>> coClassCodes = null;
	private List<IMsoFeatureLayer> msoLayers = null;
	private List<IDiskoMapListener> mapListeners = null;
	
	// components
	private DrawDialog drawDialog = null;
	private ElementDialog elementDialog = null;
	private SnapDialog snapDialog = null;

	// stacks
	private HashMap<String,Runnable> refreshStack = null;
	private MsoLayerEventStack msoLayerEventStack = null;
	
	// progress monitoring
	private DiskoMapProgressor progressor = null;
	
	// adapters
	private DrawAdapter drawAdapter = null;
	private SnapAdapter snapAdapter = null;
	private DiskoWorkRepeater workRepeater = null;
	private ControlEventsAdapter ctrlAdapter = null;
	private MapCompEventsAdapter compAdapter = null;
	
	// elements
	private DrawFrame drawFrame = null;
	
	// flags
	private boolean isInitMode = true;
	
	// mouse tracking
	private long previous = 0;		
	private Point movePoint = null;
	private Point clickPoint = null;


	/**
	 * Default constructor
	 */
	public DiskoMap(IDiskoMapManager mapManager, 
			IMsoModelIf msoModel, EnumSet<LayerCode> myLayers)
			throws IOException, AutomationException {
		
		// prepare
		this.mapManager = mapManager;
		this.msoModel = msoModel;
		this.layerCodes = myLayers;
		this.msoLayerEventStack = new MsoLayerEventStack();
		this.mapListeners = new ArrayList<IDiskoMapListener>();
		
		// adpaters
		this.ctrlAdapter = new ControlEventsAdapter();
		this.compAdapter = new MapCompEventsAdapter();
		this.workRepeater = new DiskoWorkRepeater();		
		
		// initialize GUI
		initialize();
		
		// load map
		loadMxdDoc(mapManager.getMxdDoc(),true);		
	}

	private void initialize() throws IOException, AutomationException {
		
		// prepare
		setName("diskoMap");
		setBorder(null); // BorderFactory.createBevelBorder(BevelBorder.LOWERED)
		setBorderStyle(esriControlsBorderStyle.esriNoBorder);
		setShowScrollbars(false);
		suppressResizeDrawing(true, 0);		

		// set disko map progress
		getTrackCancel().setCheckTime(250);
		getTrackCancel().setProgressor(getProgressor());
		
		// listen to do actions when the map is loaded
		addIMapControlEvents2Listener(ctrlAdapter);

		// listen for component events
		addComponentListener(compAdapter);
		
		// create refresh stack
		refreshStack = new HashMap<String,Runnable>();
		
		// create points for last click and move events
		movePoint = new Point();
		clickPoint = new Point();
		
	}
	
	private void initLayers() throws java.io.IOException, AutomationException {
		
		// forward
		if(!isMxdDocLoaded()) return;
		
        // remove listen to mso client event update events
		msoModel.getEventManager().removeClientUpdateListener(this);
                
		// add custom layers
		IMap focusMap = getActiveView().getFocusMap();
		ISpatialReference srs = getSpatialReference();
		msoLayers = new ArrayList<IMsoFeatureLayer>();
		
		// get interests as
		ArrayList<LayerCode> list = new ArrayList<LayerCode>(layerCodes);
		
		// initialize
		classCodes = EnumSet.noneOf(MsoClassCode.class);
		coClassCodes = new HashMap<MsoClassCode, EnumSet<MsoClassCode>>();
		
		// loop over my layers
		for(int i=0;i<list.size();i++){
			LayerCode layerCode = list.get(i);
			if(layerCode == LayerCode.POI_LAYER) {
				msoLayers.add(new POILayer(msoModel,srs,msoLayerEventStack));				
				if(addClass(MsoClassCode.CLASSCODE_POI)) { 
					addCoClass(MsoClassCode.CLASSCODE_ASSIGNMENT,MsoClassCode.CLASSCODE_POI);
				}
			}
			else if(layerCode == LayerCode.AREA_LAYER) {
				msoLayers.add(new AreaLayer(msoModel,srs,msoLayerEventStack));
				if(addClass(MsoClassCode.CLASSCODE_AREA)) {
					addCoClass(MsoClassCode.CLASSCODE_ASSIGNMENT,MsoClassCode.CLASSCODE_AREA);
				}
			}
			else if(layerCode == LayerCode.ROUTE_LAYER) {
				msoLayers.add(new RouteLayer(msoModel,srs,msoLayerEventStack));
				if(addClass(MsoClassCode.CLASSCODE_ROUTE)) {
					addCoClass(MsoClassCode.CLASSCODE_AREA,MsoClassCode.CLASSCODE_ROUTE);
					addCoClass(MsoClassCode.CLASSCODE_ASSIGNMENT,MsoClassCode.CLASSCODE_ROUTE);
				}
			}
			else if(layerCode == LayerCode.FLANK_LAYER) {
				msoLayers.add(new FlankLayer(msoModel,srs,msoLayerEventStack));
				if(addClass(MsoClassCode.CLASSCODE_ROUTE)) {
					addCoClass(MsoClassCode.CLASSCODE_ASSIGNMENT,MsoClassCode.CLASSCODE_ROUTE);
				}
			}
			else if(layerCode == LayerCode.SEARCH_AREA_LAYER) {
				msoLayers.add(new SearchAreaLayer(msoModel,srs,msoLayerEventStack));
				addClass(MsoClassCode.CLASSCODE_SEARCHAREA);
			}
			else if(layerCode == LayerCode.OPERATION_AREA_LAYER) {
				msoLayers.add(new OperationAreaLayer(msoModel,srs,msoLayerEventStack));
				addClass(MsoClassCode.CLASSCODE_OPERATIONAREA);
			}
			else if(layerCode == LayerCode.OPERATION_AREA_MASK_LAYER) {
				msoLayers.add(new OperationAreaMaskLayer(msoModel,srs,msoLayerEventStack));
				addClass(MsoClassCode.CLASSCODE_OPERATIONAREA);
			}
			else if(layerCode == LayerCode.UNIT_LAYER) {
				msoLayers.add(new UnitLayer(msoModel,srs,msoLayerEventStack));
				addClass(MsoClassCode.CLASSCODE_UNIT);
			}
		}
		
		// add co-classes that affects registered classes 
		for(MsoClassCode code : coClassCodes.keySet()) {
			addClass(code);	
		}
		
		/* =====================================================
		 * 
		 * It is important that the map receives handleMsoChange()
		 * AFTER all layers has received it. this ensures that
		 * all layers is updated before the map handles the event
		 * 
		 * ===================================================== */
		
        // add listen to mso client event update events. 
		msoModel.getEventManager().addClientUpdateListener(this);

		// create a the mso group layer
		GroupLayer msoGroup = new GroupLayer();
		msoGroup.setName("MSO_GROUP_LAYER");
		msoGroup.setCached(true);
		
		// add to focus map
		focusMap.addLayer(msoGroup);

		// add mso layers to group
		for (int i = 0; i < msoLayers.size(); i++) {
			msoGroup.add((ILayer)msoLayers.get(i));
		}

		// init all layers
		for (int i = 0; i < focusMap.getLayerCount(); i++) {
			initLayer(focusMap.getLayer(i));
		}
		
		// initialize mso selection model
		setMsoLayerSelectionModel();
		
	}	
	
	private void initLayer(ILayer l) throws AutomationException, IOException {
		if (l instanceof IFeatureLayer) {
			IFeatureLayer f = (IFeatureLayer)l;
			if (!(f instanceof IMsoFeatureLayer)) {
				f.setSelectable(false);
			}
		}
		else if(l instanceof GroupLayer) {
			// cast to group layer
			GroupLayer g = (GroupLayer)l;
			// loop over all layers
			for (int i = 0; i < g.getCount(); i++) {
				initLayer(g.getLayer(i));
			}
			
		}
		// register step progressor?
		if(l instanceof ILayerStatus) {
			// add the disko step processor to this layer
			((ILayerStatus)l).setStepProgressor(getProgressor());
		}		
	}
	
	private boolean addClass(MsoClassCode code) {
		if(!classCodes.contains(code)) {
			return classCodes.add(code);
		}
		return false;
		
	}
	
	private boolean addCoClass(MsoClassCode from, MsoClassCode to) {
		EnumSet<MsoClassCode> list = null;
		if(!coClassCodes.containsKey(from)) {
			list = EnumSet.noneOf(MsoClassCode.class);
			coClassCodes.put(from, list);
		}
		else {
			list = coClassCodes.get(from);
		}
		if(!list.contains(to)) {
			list.add(to);
			return true;
		}
		return false;
	}
	
	public void handleMsoUpdateEvent(Update e) {
		
		try {
			
			// get mso object
			IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
			
			// refresh layers?
			if (!super.isShowing() || isDrawingSupressed()) { return;	}
			
			// get layers
			List<IMsoFeatureLayer> layers = getMsoLayers(msoObj.getMsoClassCode());

			// initialize
			int count = 0;
			IMsoFeatureLayer msoLayer = null;
			
			// loop over layers
			for (IMsoFeatureLayer it : layers) {
				if (it.isVisible() && it.isDirty()) {
					count++;
					msoLayer = it;
				}
			}
			
			// refresh layer(s)
			if(count==1)
				refreshGraphics(msoLayer, getExtent());
			else if(count > 1)
				refreshGraphics(null, getExtent());
			else 
				refreshDrawFrame();
						
		} catch (AutomationException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	public void setMsoLayersVisible(MsoClassCode classCode, boolean value) throws IOException {
		for (int i = 0; i < msoLayers.size(); i++) {
			IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
			if (msoFeatureLayer.getClassCode() == classCode) {
				msoFeatureLayer.setVisible(value);
			}
		}
	}
	
	public List<IMsoFeatureLayer> getMsoLayers() {
		return new ArrayList<IMsoFeatureLayer>(msoLayers);
	}
	
	public EnumSet<LayerCode> getSupportedMsoLayers() {
		return layerCodes;
	}

	public List<IMsoFeatureLayer> getMsoLayers(MsoClassCode classCode) {
		List<IMsoFeatureLayer> result = new ArrayList<IMsoFeatureLayer>();
		for (int i = 0; i < msoLayers.size(); i++) {
			IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
			if (msoFeatureLayer.getClassCode() == classCode) {
				result.add(msoFeatureLayer);
			}
		}
		// get co classes
		EnumSet<MsoClassCode> relations = coClassCodes.get(classCode);
		if(relations!=null) {
			for(MsoClassCode c : relations) {
				List<IMsoFeatureLayer> list = getMsoLayers(c);
				for(IMsoFeatureLayer it:list) {
					if(!result.contains(it)) result.add(it);					
				}
			}
		}
		return result;
	}

	public IMsoFeatureLayer getMsoLayer(LayerCode layerCode) {
		for (int i = 0; i < msoLayers.size(); i++) {
			IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
			if (msoFeatureLayer.getLayerCode() == layerCode) {
				return msoFeatureLayer;
			}
		}
		return null;
	}

	public boolean isDrawingSupressed() {
		return supressDrawing>0;
	}
	
	public void setSupressDrawing(boolean supress) {
		if(supress)
			supressDrawing++;
		else if(supressDrawing>0)
			supressDrawing--;
	}

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return classCodes.contains(aMsoObject.getMsoClassCode());
	}
	
	@Override
	public ITool getCurrentTool() throws IOException, AutomationException {
		return (ITool)activeTool;
	}

	@Override
	public void setCurrentToolByRef(ITool tool)
			throws IOException, AutomationException {
		// update locals
		if (!(tool instanceof IDiskoTool))
			throw new IllegalArgumentException("Tools must implement the IDiskoTool interface");
		// is valid tool (option 1 := toggle dialog show/hide)
		setActiveTool((IDiskoTool)tool,1);
	}

	@Override
	public IDiskoTool getActiveTool() {
		return activeTool;
	}	
	
	public boolean setActiveTool(IDiskoTool tool, int options)
			throws IOException, AutomationException {
		// no change?
		if(activeTool==tool) return true;
		// update locals
		if (activeTool != null && !activeTool.deactivate()) {
			Utils.showError("Verktøy " + tool.getCaption() 
					+ " kan ikke deaktiveres");
				return false;
		}
		// allowed?
		if(tool!=null && !tool.activate(options)) {
			Utils.showError("Verktøy "
					+ tool.getCaption() + " kan ikke velges");
			return false;
		}
		
		// unregister work listener?
		if(activeTool!=null) {
			activeTool.removeDiskoEventListener(workRepeater);
		}
		
		// forward
		super.setCurrentToolByRef(tool);
		
		// save tool
		activeTool = tool;
		
		// register?
		if(activeTool!=null) {
			
			// register work listener
			activeTool.addDiskoWorkListener(workRepeater);
			
			// get button
			AbstractButton button = activeTool.getButton(); 
			
			// has button?
			if(button!=null) {
				button.setSelected(true);
				button.requestFocus();			
			}
			// is hosted?
			if(activeTool.isHosted()) {
				// ensure selected
				activeTool.getHostTool().setTool(tool,false);
			}
		}
		// forward to draw adapter?
		if(isEditSupportInstalled()) {
			if(activeTool instanceof IDrawTool)
				getDrawAdapter().onActiveToolChanged((IDrawTool)activeTool);
		}
		// success
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public void setMsoLayerSelectionModel()
		throws IOException, AutomationException{
		msoLayerSelectionModel = new MsoLayerSelectionModel(this);
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public MsoLayerSelectionModel getMsoLayerSelectionModel()
			throws IOException, AutomationException {
		if (msoLayerSelectionModel == null) {
			msoLayerSelectionModel = new MsoLayerSelectionModel(this);
		}
		return msoLayerSelectionModel;
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public void setWmsLayerSelectionModel()
		throws IOException, AutomationException {
		wmsLayerSelectionModel = new WmsLayerSelectionModel(this);
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public WmsLayerSelectionModel getWmsLayerSelectionModel()
			throws IOException, AutomationException {
		if (wmsLayerSelectionModel == null) {
			wmsLayerSelectionModel = new WmsLayerSelectionModel(this);
		}
		return wmsLayerSelectionModel;
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public void setMapLayerSelectionModel()
		throws IOException, AutomationException {
		mapLayerSelectionModel = new MapLayerSelectionModel(this);
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public MapLayerSelectionModel getMapLayerSelectionModel()
			throws IOException, AutomationException {
		if (mapLayerSelectionModel == null) {
			mapLayerSelectionModel = new MapLayerSelectionModel(this);
		}
		return mapLayerSelectionModel;
	}

	public IDiskoMapManager getMapManager() {
		return mapManager;
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getSelection()
	 */
	public List<IFeature> getSelection() throws IOException, AutomationException {
		ArrayList<IFeature> selection = new ArrayList<IFeature>();
		int count = getSelectionCount(true);
		if (count > 0) {
			for (int i = 0; i < getLayerCount(); i++) {
				try {
					ILayer layer = getLayer(i);
					if (layer instanceof FeatureLayer) {
						FeatureLayer flayer = (FeatureLayer)layer;
						IEnumIDs enumID = flayer.getSelectionSet().getIDs();
						int oid = enumID.next();
						while (oid != -1) {
							selection.add((IFeature)flayer.getFeatureClass().getFeature(oid));
							oid = enumID.next();
						}
					}
				}
				catch(Exception e) {
					// consume remoteobject exception on IMsoFeatureLayers
				}				
			}
			List layers = getMsoLayers();			
			for(int i=0;i<layers.size();i++) {
				// get mso feature layer
				IMsoFeatureLayer layer = (IMsoFeatureLayer)layers.get(i);
				// loop over all features
				for(int j=0;j<layer.getFeatureCount();j++) {
					IMsoFeature msoFeature = layer.getFeature(j);
					if(msoFeature.isSelected()) {
						selection.add((IFeature)msoFeature);
					}
				}
			}
		}
		
		return selection;
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getSelection()
	 */
	public List<IMsoFeature> getMsoSelection() throws IOException, AutomationException {
		ArrayList<IMsoFeature> selection = new ArrayList<IMsoFeature>();
		int count = getMsoSelectionCount(true);
		if (count > 0) {
			List layers = getMsoLayers();			
			for(int i=0;i<layers.size();i++) {
				// get mso feature layer
				IMsoFeatureLayer layer = (IMsoFeatureLayer)layers.get(i);
				// loop over all features
				for(int j=0;j<layer.getFeatureCount();j++) {
					IMsoFeature msoFeature = layer.getFeature(j);
					if(msoFeature.isSelected()) {
						selection.add(msoFeature);
					}
				}
			}
		}
		
		return selection;
	}
	
	public IEnvelope getSelectionExtent() throws IOException, AutomationException {
		List<IFeature> selection = getSelection();
		if (selection != null && selection.size() > 0) {
			IEnvelope env = new Envelope();
			for (int i = 0; i < selection.size(); i++) {
				env.union(selection.get(i).getExtent());
			}
			return env;
		}
		return null;
	}

	public void centerAt(IPoint p) throws IOException, AutomationException {
		IPoint p2 = MapUtil.getCenter(getExtent());
		// forward?
		if(!MapUtil.is2DEqual(p, p2))
			super.centerAt(p);
	}
	
	public void centerAtPosition(Position p) throws IOException, AutomationException {
		// get esri point
		IPoint point = MapUtil.getEsriPoint(p, getSpatialReference());
		if (!point.isEmpty()) {
			centerAt(point);
		}
	}
	
	public void centerAtSelected() throws IOException, AutomationException {
		IEnvelope env = getSelectionExtent();
		if (env != null && !env.isEmpty()) {
			centerAt(MapUtil.getCenter(env));
		}
	}
	
	public void centerAtFeature(IFeature feature) throws IOException, AutomationException {
		if(feature!=null) {
			IEnvelope env = feature.getExtent();
			if(env!=null && !env.isEmpty()) {
				centerAt(MapUtil.getCenter(env));
			}			
		}
	}

	public void centerAtMsoObject(IMsoObjectIf msoObject) throws IOException, AutomationException {
		IEnvelope env = getMsoObjectExtent(msoObject);
		if(env!=null && !env.isEmpty()) {
			centerAt(MapUtil.getCenter(env));
		}
	}

	public void flashSelected() {
		try {
			List<IFeature> list = getSelection();
			for(IFeature it : list)
				flashFeature(it);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void flashPoint(IPoint p) {
		flashShape(p);
	}
	
	public void flashPosition(Position p) {
		try {
			// get esri point
			IPoint point = MapUtil.getEsriPoint(p, getSpatialReference());
			if (!point.isEmpty()) {
				flashPoint(point);
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void flashFeature(IFeature feature) {
		// forward?
		if(feature!=null) {
			try {
				IGeometry geo = getFlashableGeometry(feature.getShapeCopy(),false);
				if(geo!=null) flashShape(geo);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private IGeometry getFlashableGeometry(IGeometry geo, boolean extent) throws AutomationException, IOException {
		IGeometry flashable = null;
		if(geo!=null && !geo.isEmpty()) {
			if(!extent) {				
				if(geo instanceof GeometryBag)
					return MapUtil.getPolyline((GeometryBag)geo);
				else if(geo instanceof Polygon)
					return MapUtil.getPolyline((Polygon)geo);
				else if(geo instanceof Polyline)
					return (Polyline)geo;
				else if(geo instanceof Point)
					return (Point)geo;
			}
			return MapUtil.expand(1.25, geo.getEnvelope());
		}
		// finished
		return flashable;
	}
	
	public void flashMsoObject(IMsoObjectIf msoObj) {
		try {
			// forward?
			msoObj = getGeodataMsoObject(msoObj);
			if (msoObj != null) {
				List layers = getMsoLayers(msoObj.getMsoClassCode());
				for (int i = 0; i < layers.size(); i++) {
					IMsoFeatureLayer flayer = (IMsoFeatureLayer)layers.get(i);
					MsoFeatureClass msoFC = (MsoFeatureClass)flayer.getFeatureClass();
					flashFeature(msoFC.getFeature(msoObj.getObjectId()));
				}
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void flashEnvelope(IEnvelope extent) {
		flashShape(extent);
	}
	
	public void flashPolygon(IPolygon p) {
		flashShape(p);		
	}
	
	public void flashPolyline(IPolyline p) {
		flashShape(p);				
	}

	private void flashShape(IGeometry shape) {
		try {
			flashShape(shape,3,750,null);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void setExtent(IEnvelope extent) throws IOException, AutomationException {
		/*
		IPoint p1 = MapUtil.getCenter(extent);
		IPoint p2 = MapUtil.getCenter(getExtent());
		boolean contained ((Envelope)getExtent()).contains(extent);
		// any change?
		if(!MapUtil.is2DEqual(p1, p2) || contained) {
			
		}
		*/
		super.setExtent(extent);
	}
	
	public void zoomToSelected() throws IOException, AutomationException {
		zoomToSelected(ZOOM_RATIO);
	}

	public void zoomToSelected(double ratio) throws IOException, AutomationException {
		IEnvelope env = getSelectionExtent();
		if (env != null) {
			setExtent(MapUtil.expand(ratio,env));
		}
	}
	
	public void zoomToFeature(IFeature feature) throws IOException, AutomationException {
		zoomToFeature(feature,ZOOM_RATIO); 
	}

	public void zoomToFeature(IFeature feature, double ratio) throws IOException, AutomationException {
		if(feature!=null) {
			IEnvelope env = feature.getExtent();
			if(env!=null) {
				setExtent(MapUtil.expand(ratio,env));
			}			
		}
	}

	public void zoomToMsoObject(IMsoObjectIf msoObject) throws IOException, AutomationException {
		zoomToMsoObject(msoObject,ZOOM_RATIO);
	}
	
	public void zoomToMsoObject(IMsoObjectIf msoObject, double ratio) throws IOException, AutomationException {
		// is poi?
		if(msoObject instanceof IPOIIf) {
			centerAtMsoObject(msoObject);
		}
		else {
			IEnvelope env = getMsoObjectExtent(msoObject);
			if(env!=null && !env.isEmpty()) {
				setExtent(MapUtil.expand(ratio,env));
			}			
		}
	}

	public IEnvelope getMsoObjectExtent(IMsoObjectIf msoObj) throws IOException, AutomationException {
		/*
		if (msoObj != null) {
			List layers = getMsoLayers(msoObj.getMsoClassCode());
			for (int i = 0; i < layers.size(); i++) {
				IFeatureLayer flayer = (IFeatureLayer)layers.get(i);
				MsoFeatureClass msoFC = (MsoFeatureClass)flayer.getFeatureClass();
				IMsoFeature msoFeature = msoFC.getFeature(msoObj.getObjectId());
				IEnvelope extent = null;
				if(msoObj instanceof IPOIIf) {
					extent = getExtent();
					extent.centerAt((IPoint)msoFeature.getShape());
				}
				else {
					extent = msoFeature.getExtent();
				}
				if (extent != null) {
					if (env == null)
						env = extent.getEnvelope();
					else env.union(extent);
				}
			}
		}*/
		return MapUtil.getMsoExtent(getGeodataMsoObject(msoObj), this, true);
	}
	
	public void zoomToPrintMapExtent(IMsoObjectIf msoObject, double scale, int pixHeigth, int pixWidth) throws IOException, AutomationException {
		msoObject = getGeodataMsoObject(msoObject);
		if (msoObject != null) {
			IEnvelope env = null;
			List layers = getMsoLayers(msoObject.getMsoClassCode());
			for (int i = 0; i < layers.size(); i++) {
				IFeatureLayer flayer = (IFeatureLayer)layers.get(i);
				MsoFeatureClass msoFC = (MsoFeatureClass)flayer.getFeatureClass();
				IMsoFeature msoFeature = msoFC.getFeature(msoObject.getObjectId());
				IEnvelope extent = msoFeature.getExtent();
				
				// calculate extent from scale
				double xMax = extent.getXMax();
				double yMax = extent.getYMax();
				double xMin = extent.getXMin();
				double yMin = extent.getYMin();
				
				//System.out.println("looper "+i+", extent.getXMax(): "+extent.getXMax() + ", extent.getYMax(): "+extent.getYMax() + ", extent.getXMin(): "+extent.getXMin() + "extent.getYMin(): "+extent.getYMin());
				
				// calulate delta values
				double deltaX = (pixWidth*PIXEL_SIZE)*scale;
				double deltaY = (pixWidth*PIXEL_SIZE)*scale;
				double centerX = xMin + (xMax-xMin)/2;
				double centerY = yMin + (yMax-yMin)/2;
				
				//System.out.println("deltaX: "+deltaX + ", deltaY: "+deltaY);
				
				// set extent
				extent.setXMax(centerX+deltaX/2);
				extent.setXMin(centerX-deltaX/2);
				extent.setYMax(centerY+deltaY/2);
				extent.setYMin(centerY-deltaY/2);
				
				//System.out.println("extent.getXMax(): "+extent.getXMax() + ", extent.getYMax(): "+extent.getYMax() + ", extent.getXMin(): "+extent.getXMin() + "extent.getYMin(): "+extent.getYMin());

				// append
				if (extent != null) {
					if (env == null)
						env = extent.getEnvelope();
					else 
						env.union(extent);
				}
			}
			// found extent?
			if (env != null) {
				setExtent(env);
			}
		}
	}

	public void zoomToPrintMapExtent(IMsoObjectIf msoObject, double scale, double mapPrintHeigthSize, double mapPrintWidthSize) throws IOException, AutomationException {
		msoObject = getGeodataMsoObject(msoObject);
		if (msoObject != null) {
			IEnvelope env = null;
			List layers = getMsoLayers(msoObject.getMsoClassCode());

			for (int i = 0; i < layers.size(); i++) {
				
				IFeatureLayer flayer = (IFeatureLayer)layers.get(i);
				MsoFeatureClass msoFC = (MsoFeatureClass)flayer.getFeatureClass();
				IMsoFeature msoFeature = msoFC.getFeature(msoObject.getObjectId());
				IEnvelope extent = msoFeature.getExtent();
				
				if (extent != null) {
					
					//må kalkulere et extent som gir gitt skala				
					//System.out.println("looper "+i+", extent.getXMax(): "+extent.getXMax() + ", extent.getYMax(): "+extent.getYMax() + ", extent.getXMin(): "+extent.getXMin() + "extent.getYMin(): "+extent.getYMin());				
					double centerX = extent.getXMin() + (extent.getXMax()-extent.getXMin())/2;
					double centerY = extent.getYMin() + (extent.getYMax()-extent.getYMin())/2;
					
					
					double deltaX = mapPrintWidthSize * scale;
					double deltaY = mapPrintHeigthSize * scale;
					
					// System.out.println("deltaX: "+deltaX + ", deltaY: "+deltaY);
					
					// set extent
					extent.setXMax(centerX+deltaX/2);
					extent.setXMin(centerX-deltaX/2);
					extent.setYMax(centerY+deltaY/2);
					extent.setYMin(centerY-deltaY/2);
					
					//System.out.println("extent.getXMax(): "+extent.getXMax() + ", extent.getYMax(): "+extent.getYMax() + ", extent.getXMin(): "+extent.getXMin() + "extent.getYMin(): "+extent.getYMin());
					
					// append
					if (env == null)
						env = extent.getEnvelope();
					else 
						env.union(extent);
					
				}
			}
			// found extent?
			if (env != null) {
				setExtent(env);
			}
		}
	}

	public List<IMsoFeatureLayer> setSelected(IMsoObjectIf msoObject, boolean selected)
			throws IOException, AutomationException {
		List<IMsoFeatureLayer> affected = new ArrayList<IMsoFeatureLayer>();
		msoObject = getGeodataMsoObject(msoObject);
		if (msoObject != null) {
			List layers = getMsoLayers(msoObject.getMsoClassCode());
			for (int i = 0; i < layers.size(); i++) {
				IMsoFeatureLayer flayer = (IMsoFeatureLayer)layers.get(i);
				MsoFeatureClass msoFC = (MsoFeatureClass)flayer.getFeatureClass();
				boolean bDirty = false; //(flayer.clearSelected()>0);
				IMsoFeature msoFeature = msoFC.getFeature(msoObject.getObjectId());
				if(msoFeature!=null) {
					flayer.setSelected(msoFeature, selected);
					bDirty = flayer.isDirty();
				}
				if(bDirty)
					affected.add(flayer);
			}
		}
		return affected;
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#setSelected(java.lang.String, java.lang.String, java.lang.Object)
	 */
	public void setSelected(String layerName, String fieldName, Object value)
			throws IOException, AutomationException {
		setSelected(getFeatureLayer(layerName), fieldName, value);
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#setSelected(com.esri.arcgis.carto.FeatureLayer, java.lang.String, java.lang.Object)
	 */
	public void setSelected(FeatureLayer layer, String fieldName, Object value)
			throws IOException, AutomationException {
		String whereclause = "\""+fieldName+"\"=";
		if (value instanceof String) {
			whereclause += "'"+(String)value+"'";
		}
		else if (value instanceof Number) {
			whereclause += value.toString();
		}
		setSelected(layer, whereclause);
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#setSelected(com.esri.arcgis.carto.FeatureLayer, java.lang.String)
	 */
	public void setSelected(FeatureLayer layer, String whereclause)
			throws IOException, AutomationException {
		QueryFilter queryFilter = new QueryFilter();
		queryFilter.setWhereClause(whereclause);
		layer.selectFeatures(queryFilter,com.esri.arcgis.carto.
				esriSelectionResultEnum.esriSelectionResultNew, false);
	}

	public List<IMsoFeatureLayer> clearSelected() 
		throws IOException, AutomationException {
		suspendNotify();
		List msoLayers = getMsoLayers();
		List<IMsoFeatureLayer> cleared = new ArrayList<IMsoFeatureLayer>();
		for (int i = 0; i < msoLayers.size(); i++) {
			IMsoFeatureLayer msoLayer = (IMsoFeatureLayer)msoLayers.get(i);
			if (msoLayer.getSelected().size() > 0) {
				msoLayer.clearSelected();
				cleared.add(msoLayer);
			}
		}
		resumeNotify();
		
		return cleared;
	}
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getFeatureLayer(java.lang.String)
	 */
	public FeatureLayer getFeatureLayer(String name)
			throws IOException, AutomationException {
		// get map in focus
		IMap m = getActiveView().getFocusMap();
		for (int i = 0; i < m.getLayerCount(); i++) {
			ILayer layer = m.getLayer(i);
			if (layer instanceof FeatureLayer && layer.getName().equalsIgnoreCase(name)) {
				return (FeatureLayer)layer;
			}
		}
		return null;
	}

	public String getMxdDoc() {
		return mxdDoc;
	}

	public boolean isDrawing() {
		if (activeTool instanceof IDrawTool) {
			// cast to draw tool
			IDrawTool tool = (IDrawTool)activeTool;
			// return drawing status
			return tool.isDrawing();
		}
		// is not drawing
		return false;
	}
	
	
	public void refreshMapBase() throws IOException, AutomationException {
		// forward
		refresh(esriViewDrawPhase.esriViewGeography, null, getExtent());
	}
	
	public void refreshMapBase(IEnvelope extent)  throws IOException, AutomationException {
		// forward
		if(isCacheChanged) {
			// use IViewRefresh interface?
			if(getActiveView().getFocusMap() instanceof IViewRefresh) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							IViewRefresh view = (IViewRefresh)getActiveView().getFocusMap();
							view.refreshCaches();
						} catch (AutomationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});				
			}
			else {
				// force full refresh
				refresh();
			}
			// reset flag
			isCacheChanged = false;
		}
		else {
			refresh(esriViewDrawPhase.esriViewGeography, null, extent);
		}
	}
	
	public void refreshMsoLayers() throws IOException, AutomationException {
		// forward
		refreshMsoLayers(getExtent());
	}
	
	public void refreshMsoLayers(IEnvelope extent) 
		throws IOException, AutomationException {
		// forward
		refreshMsoLayers(msoLayers,extent);
	}
	
	public void refreshMsoLayers(MsoClassCode classCode) throws IOException,
		AutomationException {
		// forward
		refreshMsoLayers(classCode, getExtent());
		
	}
	public void refreshMsoLayers(MsoClassCode classCode, IEnvelope extent) throws IOException,
		AutomationException {
		// get list
		List<IMsoFeatureLayer> layers = getMsoLayers(classCode);
		// forward
		refreshMsoLayers(layers,extent);
	}
	
	private void refreshMsoLayers(List<IMsoFeatureLayer> layers, IEnvelope extent) 
		throws IOException, AutomationException {
		
		// initialise
		int count = 0;
		boolean isDirtyExtent = (extent == null);
		IMsoFeatureLayer msoLayer = null;
		
		// loop over layers
		for (IMsoFeatureLayer it : layers) {
			if (it.isVisible() && it.isDirty()) {
				count++;
				msoLayer = it;
				if(isDirtyExtent) {
					// get extent
					if(extent==null)
						extent = msoLayer.getDirtyExtent();
					else
						extent.union(msoLayer.getDirtyExtent());
	
				}
			}
		}
		
		// refresh layer(s)
		if(count==1)
			refreshGraphics(msoLayer, extent);
		else if(count > 1)
			refreshGraphics(null, extent);
		else
			refreshDrawFrame();			
	
	}

	
	
	public void refresh() throws IOException, AutomationException {
		
		// consume?
		if(isDrawing() || isDrawingSupressed()) return;
		
		// create object
		Runnable r = new Runnable() {
			public void run() {
				try {
					// trace event
					//System.out.println(getProgressor().toString() + "::refresh(ALL)::started");						
					// forward
					showProgressor(false);
					// hide
					setVisible(false);
					// refresh view
					getActiveView().refresh();
					// show
					setVisible(true);
					// forward
					hideProgressor();
					// trace event
					//System.out.println(getProgressor().toString() + "::refresh(ALL)::finished");						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		SwingUtilities.invokeLater(r);
	}

	public void refreshGeography(final Object data, final IEnvelope extent) {
		// forward
		refresh(esriViewDrawPhase.esriViewGeography,data,extent);
	}
	
	public void refreshGraphics(final Object data, final IEnvelope extent) {
		// forward
		refresh(esriViewDrawPhase.esriViewGraphics,data,extent);
	}
	
	private void refresh(final int phase, final Object data, final IEnvelope extent) {
		// consume?
		if(isDrawing() || isDrawingSupressed()) return;
		// get key
		String key = phase + "." + String.valueOf(data);
		// not in stack?
		if (!refreshStack.containsKey(key)) {
			// create object
			Runnable r = new Runnable() {
				public void run() {
					try {				
						// trace event
						//System.out.println(getProgressor().toString() + "::refresh(DATA)::started");		
						// forward
						showProgressor(false); // setTrackID(TRACK_PROGRESS, true);
						// get key
						String key = phase + "." + String.valueOf(data);
						// refresh view
						//Map map = (Map)getActiveView().getFocusMap();
						//map.animationRefresh(phase, data, extent);
						getActiveView().partialRefresh(phase, data, extent);
						// is draw frame enabled?
						//if(isEditSupportInstalled()) {
						//	// draw directly onto map?
						//	if(data==null || !drawFrame.isDirty()) 
						//		drawFrame.draw();
						//	else
						//		drawFrame.refresh();
						//}
						// remove from stack
						refreshStack.remove(key);
						// forward
						hideProgressor();
						// trace event
						//System.out.println(getProgressor().toString() + "::refresh(DATA)::finished");						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			refreshStack.put(key, r);
			SwingUtilities.invokeLater(r);
		}
	}
	
	private void refreshDrawFrame() {
		// is draw frame enabled?
		if(isEditSupportInstalled()) {
			drawFrame.refresh();
		}		
	}

	public boolean isRefreshPending() {
		return (refreshStack.size()>0);
	}
	
	public List<IMsoFeature> getMsoFeature(IMsoObjectIf msoObj) throws AutomationException, IOException {
		List<IMsoFeature> features = new ArrayList<IMsoFeature>();
		if(msoObj!=null) {
			List<IMsoFeatureLayer> layers = getMsoLayers(msoObj.getMsoClassCode());
			for (int i = 0; i < layers.size(); i++) {
				IMsoFeatureLayer flayer = layers.get(i);
				if (flayer!=null) {
		 			// try to get feature
					MsoFeatureClass msoFC = (MsoFeatureClass)flayer.getFeatureClass();
					IMsoFeature msoFeature = msoFC.getFeature(msoObj.getObjectId());
					// found?
					if(msoFeature!=null){
						features.add(msoFeature);
					}
				}
			}
		}
		return features;
	}
	
	public int isSelected(IMsoObjectIf msoObj) throws AutomationException, IOException {
		if(msoObj==null) return -1;
		int count = 0;
		List<IMsoFeature> features = getMsoFeature(msoObj);
		for (IMsoFeature it : features) {
			// is selected?
			count += it.isSelected() ? 1 : 0;
		}
		return count;
	}
	
	private IMsoObjectIf getGeodataMsoObject(IMsoObjectIf msoObject) {
		if (msoObject instanceof IAssignmentIf) {
			IAssignmentIf assignment = (IAssignmentIf)msoObject;
			msoObject = assignment.getPlannedArea();
		} 
		return msoObject;
	}

	public boolean isNotifySuspended() {
		return (notifySuspended>0);
	}

	public void suspendNotify() {
		// only suspend on first increment
		if(notifySuspended==0) {
			for (int i = 0; i < msoLayers.size(); i++) {
				IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
				msoFeatureLayer.suspendNotify();
			}
		}
		// increment
		notifySuspended++;
	}

	public void resumeNotify() {
		// only resume on last decrement
		if(notifySuspended==1){
			try {
				// reset counter
				notifySuspended = 0;
				// Forward to stack first! This ensures that
				// all events are grouped into maximum two events. The stack
				// also ensures that any DESELECTED_EVENT events are fired before
				// any SELECTED_EVENT events. This is important when listeners are
				// acting on selection events.
				msoLayerEventStack.fireAll();
				// notify layers (no events will be fired because the stack is now empty)
				for (int i = 0; i < msoLayers.size(); i++) {
					msoLayers.get(i).resumeNotify();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// decrement?
		if(notifySuspended>1)
			notifySuspended--;
	}
	
	public void consumeNotify() {
		// only resume on last decrement
		if(notifySuspended==1){
			// clear stack
			msoLayerEventStack.consumeAll();
		}
		// decrement?
		if(notifySuspended==1)
			notifySuspended--;
	}
	
	public int getSelectionCount(boolean update) throws IOException, AutomationException {
		int count = getMsoSelectionCount(update);
		// return selection count
		return getMap().getSelectionCount()+count;
	}
	
	public int getMsoSelectionCount(boolean update) throws IOException, AutomationException {
		int count = 0;
		for (int i = 0; i < msoLayers.size(); i++) {
			IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
			count += msoFeatureLayer.getSelectionCount(update);
		}
		// return selection count
		return count;
	}
	
	public double getScale() {
		try {
			return getScale((IBasicMap)getActiveView().getFocusMap());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return 0;
	}
	
	public double getMaxDrawScale() {
		return MAX_DRAW_SCALE;
	}
	
	public boolean isDrawAllowed() {
		try {
			return (getScale()<=MAX_DRAW_SCALE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return false;
	}
	
	public double getMaxSnapScale() {
		return MAX_SNAP_SCALE;
	}
	
	public boolean isSnapAllowed() {
		try {
			return (getScale()<=MAX_SNAP_SCALE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return false;
	}

	public List<IFeatureLayer> getSnappableLayers() throws IOException, AutomationException {
		IMap focusMap = getActiveView().getFocusMap();
		double scale = getScale((IBasicMap)focusMap);
		ArrayList<IFeatureLayer> layers = new ArrayList<IFeatureLayer>();
		// for scales beyond MAX_SNAP_SCALE, allowing for snapping does not make sence because 
		// drawing is typically done at much lower scales. furthermore, allowing scales beyond
		// MAX_SNAP_SCALE will reduce system performance because of a very large number of indexes features
		// must be buffered. Ultimately, the application may become unresponsive for a very long time
		// halting the system progres.
		if(MAX_SNAP_SCALE>=scale) {
			// loop over all layers
			for (int i = 0; i < focusMap.getLayerCount(); i++) {
				// get layer
				ILayer layer = focusMap.getLayer(i);
				// is a feature layer?
				if (layer instanceof IFeatureLayer) {
					// get shapetype
					IFeatureLayer flayer = (IFeatureLayer) layer;
					if (isSnappable(flayer,scale)) {
						layers.add(flayer);
					}
				}
				else if (layer instanceof GroupLayer) {
					// cast to group layer
					GroupLayer glayer = (GroupLayer) layer;
					// forward
					getSnappableInGroupLayer(layers, glayer, scale);
				}
			}
		}
		return layers;
	}
	
	private void getSnappableInGroupLayer(ArrayList<IFeatureLayer> layers, GroupLayer layer, double scale) throws IOException, AutomationException {
		// get layer count
		int count = layer.getCount();
		// loop over all group layers
		for(int j=0;j<count;j++) {
			// get layer
			ILayer l = layer.getLayer(j);
			if (l instanceof IFeatureLayer) {
				IFeatureLayer flayer = (IFeatureLayer) l;
				if (isSnappable(flayer,scale)) {
					layers.add(flayer);
				}
			}
			// nested groups?
			else if (l instanceof GroupLayer) {
				// cast to group layer
				GroupLayer glayer = (GroupLayer) l;
				// forward
				getSnappableInGroupLayer(layers, glayer, scale);
			}			
		}
	}
	
	private boolean isSnappable(IFeatureLayer layer, double scale) throws IOException, AutomationException {
		int shapeType = layer.getFeatureClass().getShapeType();
		if (shapeType == esriGeometryType.esriGeometryPolyline ||
				shapeType == esriGeometryType.esriGeometryPolygon  ||
				shapeType == esriGeometryType.esriGeometryBag) {
			
			if (	layer.isVisible() && 
					scale > layer.getMaximumScale() &&
					scale < layer.getMinimumScale()) {
				return true;
			}
		}
		return false;
	}
	
	private DiskoMapProgressor getProgressor() {
		if(progressor==null) {
			progressor = new DiskoMapProgressor();
			
			/*
			progressor.addListener(new DiskoMapProgressor.ProgressorListener() {

				public void onChange() {}

				public void onHide() {
					try {
						DiskoProgressMonitor.getInstance().finish();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//showBar(getSouthBar(), "MapFilterBar");
				}

				public void onShow() {
					try {
						DiskoProgressMonitor.getInstance().start("Laster kart");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//showBar(getSouthBar(), "ProgressBar");
				}

			});
			*/
		}
		return progressor;
	}
	
	
	public Point getClickPoint() {
		return clickPoint;
	}

	public Point getMovePoint() {
		return movePoint;
	}

	/*
	public static JPanel createPanel(IDiskoMap map, 
			MapStatusBar statusBar, MapFilterBar filterBar, Border border) {
		JPanel panel = MapStatusBar.createPanel(
				map, statusBar, BorderLayout.NORTH, border);
		map.setMapStatusBar(statusBar);
		filterBar.setMap(map);
		panel.add(filterBar,BorderLayout.SOUTH);
		return panel;
		
	}
	*/
	
	public boolean isEditSupportInstalled() {
		return isEditSupportInstalled;
	}
	
	public void installEditSupport() {
		// install?
		if(!isEditSupportInstalled) {
			// set flag
			isEditSupportInstalled = true;
			// initialize draw adapter
			getDrawAdapter();
			getSnapAdapter();
		}
	}
	
	public DrawFrame getDrawFrame() {
		// not supported?
		if(!isEditSupportInstalled) return null;
		// initialize?
		if(drawFrame==null) {
			try {
				// create new frame
				drawFrame = new DrawFrame(getActiveView());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return drawFrame;
	}
	
	public DrawAdapter getDrawAdapter() {
		// not supported?
		if(!isEditSupportInstalled) return null;
		// initialize?
		if(drawAdapter == null) {
			// create draw adapter
			drawAdapter = new DrawAdapter(Utils.getApp());
			// register objects
			drawAdapter.register(this);
		}
		return drawAdapter;
	}
	
	public DrawDialog getDrawDialog() {
		// not supported?
		if(!isEditSupportInstalled) return null;
		// initialize?
		if(drawDialog == null) {
			
			// get draw dialog
	        NavBarPanel navBar = Utils.getApp().getNavBar();
			drawDialog = (DrawDialog)navBar.getDrawHostTool().getDialog();
			drawDialog.setLocationRelativeTo(this,DefaultDialog.POS_WEST, true, true);

		}
		return drawDialog;
	}
	
	public ElementDialog getElementDialog() {
		// not supported?
		if(!isEditSupportInstalled) return null;
		// initialize?
		if (elementDialog == null) {
			
			// create
			elementDialog = new ElementDialog(Utils.getApp().getFrame());
			elementDialog.setLocationRelativeTo(this,DefaultDialog.POS_EAST, false, true);

		}
		return elementDialog;
	}	
	
	public SnapAdapter getSnapAdapter() {
		// not supported?
		if(!isEditSupportInstalled) return null;
		// initialize?
		if(snapAdapter == null) {
			try {
				snapAdapter = new SnapAdapter();
				snapAdapter.register(this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return snapAdapter;
	}
	
	public SnapDialog getSnapDialog() {
		// not supported?
		if(!isEditSupportInstalled) return null;
		// initialize?
		if(snapDialog == null) {
			
			// create new snap dialog
	        snapDialog = new SnapDialog(Utils.getApp().getFrame());
	        snapDialog.setLocationRelativeTo(this,DefaultDialog.POS_WEST, true, true);

		}
		return snapDialog;
	}
	
	
	
	public boolean isInitMode() {
		return isInitMode;
	}
	
	public void setInitMode(boolean isInitMode) {
		this.isInitMode = isInitMode;
	}
	
    
    public IEnvelope getDirtyExtent() {
    	IEnvelope extent = null;
		try {
			for (IMsoFeatureLayer it : msoLayers) {
				// is visible?
				if(it.isVisible()) {
					// get extent
					IEnvelope e = it.getDirtyExtent();
					// is dirty?
					if(e!=null) {
						// get extent
						if(extent==null)
							extent = e;
						else
							extent.union(e);
					}
				}
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		return extent;
    }

	public int getMapBaseIndex() {
		return currentBase;
	}

	public ILayer getMapBase() {
		try {
			return getLayer(getMapManager().getMapBase(currentBase));
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed!
		return null;
	}
	
	public int getMapBaseCount() {
		return getMapManager().getMapBaseCount();
	}

	/**
	 * Set base map layer
	 * 
	 * @param index 1-based base map layer index.
	 */
	
	public int setMapBase(int index) {
		
		// get current base layer count
		int count = getMapBaseCount();
		
		// not allowed?
		if(count==0 || getMapBaseIndex()==index) return 0;

		try {
			
			// loop over all map base layers
			for(int i=0; i<count;i++) {
				// get next base layer
				ILayer l = getLayer(getMapManager().getMapBase(i+1));
				// found?
				if(l!=null) {
					// force cached?
					if(!l.isCached()) {
						isCacheChanged = true;
						l.setCached(true);
					}
					// update visible state
					l.setVisible(i==(index-1));
				}
			}
			
			// update current index
			currentBase = index;
			
			// update map layer selection model
			setMapLayerSelectionModel();
			
			// return next base layer index
			return index;
			
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return 0;
	}

	public int toggleMapBase() {
		// toogle upwards
		int index = (currentBase<getMapBaseCount()) ? currentBase + 1 : 1;
		// forward
		return setMapBase(index);
	}
	
	private ILayer getLayer(String name)
			throws IOException, AutomationException {
		// get map in focus
		IMap m = getActiveView().getFocusMap();
		for (int i = 0; i < m.getLayerCount(); i++) {
			ILayer layer = m.getLayer(i);
			if (layer.getName().equalsIgnoreCase(name)) {
				return layer;
			}
		}
		return null;
	}
	
	public boolean isMxdDocLoaded() {
		try {
			return getDocumentFilename()!=null && !getDocumentFilename().isEmpty();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return false;
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		// only show map if a document is loaded
		super.setVisible(isVisible && isMxdDocLoaded());
	}

	public boolean checkMxdDoc(String mxddoc) {
		try {
			// forward
			return checkMxFile(mxddoc);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failure
		return false;
	}

	public boolean loadMxdDoc() {
		// forward
		return loadMxdDoc(mapManager.getMxdDoc(),false);
	}

	private boolean loadMxdDoc(String mxdDoc, boolean force) {

		// initialize flag
		boolean bFlag = false;
		
		try {
			
			// allowed?
			if(isVisible() || force) {
			
				// hide map
				setVisible(false);
				
				// load document?
				if(!getDocumentFilename().equals(mxdDoc)) { 
					// validate document
					if(checkMxFile(mxdDoc)) {
						// load document
						loadMxFile(mxdDoc, null, null);
						// set mxd document
						this.mxdDoc = mxdDoc;
						// update spatial references
						movePoint.setSpatialReferenceByRef(getSpatialReference());
						clickPoint.setSpatialReferenceByRef(getSpatialReference());
						// reset map base index
						currentBase = 0;
						// set default base layer
						setMapBase(1);
						// initalize mso layers for the newly loaded map document
						initLayers();						
						// success
						bFlag = true;
					}
				}
				
				// show again
				setVisible(true);
				
			}			
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// finished
		return bFlag;
		
	}
	
	public void showProgressor(boolean autocancel) {
		try {
			// prepare to show progess dialog only? (does not lock the application)
			if(!DiskoProgressMonitor.getInstance().isInAction()) {
				DiskoProgressMonitor.getInstance().setProgressLocationAt(this);
				if(autocancel)
					DiskoProgressMonitor.getInstance().start("Laster kart",0,0,0,0,2000);
				else
					DiskoProgressMonitor.getInstance().start("Laster kart");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}								
	}
	
	public void hideProgressor() {
		try {
			// prepare to hide progress dialg
			DiskoProgressMonitor.getInstance().finish();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	/*==========================================================
	 * IDiskoWorkListener methods
	 *==========================================================*/		

	public void addDiskoWorkListener(IDiskoWorkListener listener) {
		workRepeater.addDiskoWorkListener(listener);
	}
	
	public void removeDiskoWorkEventListener(IDiskoWorkListener listener) {
		workRepeater.removeDiskoWorkListener(listener);
	}
    
	/*==========================================================
	 * Internal classes
	 *==========================================================*/
    
    class ControlEventsAdapter extends IMapControlEvents2Adapter {

		private static final long serialVersionUID = 1L;
		
		@Override
		public void onMouseDown(IMapControlEvents2OnMouseDownEvent e) throws IOException, AutomationException {
			
			// update
			clickPoint.setX(e.getMapX());
			clickPoint.setY(e.getMapY());
			
			// notify later on EDT
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireOnMouseClick();
				}
			});	
			
		}
		
		@Override
		public void onMouseMove(IMapControlEvents2OnMouseMoveEvent e) throws IOException, AutomationException {
			
			// update
			double x = e.getMapX(); 
			double y = e.getMapY();
			
			// get current tic
			long tic = Calendar.getInstance().getTimeInMillis();
			//System.out.println("onMouseMove:="+tic);
			// update?
			if (tic - previous > 100 && (movePoint.isEmpty() || !MapUtil.is2DEqual(movePoint,x,y))) {
				// update point
				movePoint.setX(x);
				movePoint.setY(y);
				previous = tic;
				// notify later on EDT
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						fireOnMouseMove();
					}
				});	
			}
			
		}
		
		public void onMapReplaced(IMapControlEvents2OnMapReplacedEvent e)
               	throws java.io.IOException, AutomationException {

			// notify later on EDT
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					
					try {
						// set spatial references
						movePoint.setSpatialReferenceByRef(getSpatialReference());
						clickPoint.setSpatialReferenceByRef(getSpatialReference());

						// update draw support
						if(drawFrame!=null) drawFrame.setActiveView(getActiveView());
						
						// update current selection models
						setMsoLayerSelectionModel();
						setMapLayerSelectionModel();
						setWmsLayerSelectionModel();
						
						// forward
						fireOnMapReplaced();
					} catch (AutomationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			});	
		}
		
		public void onExtentUpdated(IMapControlEvents2OnExtentUpdatedEvent theEvent)
    		throws java.io.IOException, AutomationException {
			
			// notify later on EDT
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireOnExtendChanged();
				}
			});	
			
		}
		
	}
	
	class MapCompEventsAdapter extends ComponentAdapter {

		@Override
		public void componentShown(ComponentEvent arg0) {
			// load mxd document?
			if(!mapManager.getMxdDoc().endsWith(mxdDoc)) {
				// load default mxd document later
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						loadMxdDoc();
					}
				});					
			}
		}

		/* =============================================================
		 * IMPORTANT: This is a critical hack, do not remove!
		 * 
		 * Without this hack, the system will hang under certain
		 * conditions. The problem is an deadlock and is located in 
		 * ArcGIS Engine.
		 * 
		 * It can be reprodused in the following way: 
		 * 
		 * 1. Comment out both occurences of suppressResizeDrawing() below
		 * 2. Switch to the work prosess "MessageLog"
		 * 3. Toggle NavButton on, the map is shown (cardlayout toggle)
		 * 4. Toggle NavButton off, the map should no be hidden
		 * 5. BUT: The system hangs with the map partial resized and still 
		 * 	  shown. If the debug command "Run -> Suspend" is selected 
		 * 	  and the AWT thread is inspected in debug view you will see 
		 * 	  that the system hangs in call to a class i ArcGIS engine. 
		 * 	  More spesific; during the creation of a ESRI Point. 
		 * 
		 * 		This Point creation is an result of a event sequence invoked 
		 * 	  when preparing to draw the table which now should be known 
		 *    instead of the map.
		 *    
		 * PROBABLE CAUSE: Then the NavButton is toggled, the NavBar is
		 * hidden before the cardlayout is toggled. The reason for this: the 
		 * actionlistener registered on button NavButton by MainMenuPanel is
		 * registered before the actionlistener used in MessageLogPanel to
		 * toggle the cardlayout on m_tablePanel. Because the NavBar is hidden, 
		 * a resize of the MapControl in DiskoMap is executed just before the 
		 * cardlayout toggle (which hides the map). Since a resize results in a
		 * repaint of the map, and resize operations on the MapBean is 
		 * done on another thread than the event dispatch thread (EDT), an 
		 * potential concurrency issue (deadlock) now waiting to happend. 
		 * 		This is what probably happens: The combination of a resize
		 * of the MapControl, and hidding it using cardlayout, which then 
		 * invoces an asynchronous repaint in the MapControl, ultimately 
		 * resulting in deadlock (a concurrency issue) when the new point 
		 * is created on EDT. 
		 * 		The solution below works probably because it changes the 
		 * order of events, compared to earlier, and thus prevents the deadlock.
		 * 
		 * =============================================================
		 */
		
		@Override
		public void componentResized(ComponentEvent arg0) {
			try {
				if(isVisible()) {
					// turn off
					//suppressResizeDrawing(false, 0);
					// turn on
					//suppressResizeDrawing(true, 0);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
		}

	}

	public boolean addDiskoMapListener(IDiskoMapListener listener) {
		if(!mapListeners.contains(listener))
			return mapListeners.add(listener);
		return false;
	}

	public boolean removeDiskoMapListener(IDiskoMapListener listener) {
		if(mapListeners.contains(listener))
			return mapListeners.remove(listener);
		return false;
	}
	
	private void fireOnMouseClick() {
		for(IDiskoMapListener it : mapListeners)
			it.onMouseClick();
	}
	
	private void fireOnMouseMove() {
		//System.out.println("fireOnMouseMove:="+System.currentTimeMillis());
		for(IDiskoMapListener it : mapListeners)
			it.onMouseMove();		
	}
	
	private void fireOnExtendChanged() {
		for(IDiskoMapListener it : mapListeners)
			it.onExtentChanged();		
	}
	
	private void fireOnMapReplaced() {
		for(IDiskoMapListener it : mapListeners)
			it.onExtentChanged();		
	}
}
