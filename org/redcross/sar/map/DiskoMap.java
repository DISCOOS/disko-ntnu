package org.redcross.sar.map;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import com.esri.arcgis.beans.map.MapBean;
import com.esri.arcgis.carto.*;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnAfterScreenDrawEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnExtentUpdatedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMapReplacedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMouseDownEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMouseMoveEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnViewRefreshedEvent;
import com.esri.arcgis.controls.esriControlsBorderStyle;
import com.esri.arcgis.geodatabase.IEnumIDs;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.systemUI.ITool;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.event.MsoLayerEventStack;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.gui.map.DrawDialog;
import org.redcross.sar.gui.map.ElementDialog;
import org.redcross.sar.gui.map.MapFilterBar;
import org.redcross.sar.gui.map.MapStatusBar;
import org.redcross.sar.gui.map.SnapDialog;
import org.redcross.sar.map.command.IDiskoTool;
import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.map.element.DrawFrame;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.MsoFeatureClass;
import org.redcross.sar.map.layer.*;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
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

	private static final long serialVersionUID = 1L;
	private static final double dpi = Toolkit.getDefaultToolkit().getScreenResolution();
	//private static final double pixelSize = 0.000377; //meter
	private static final double pixelSize = 0.00254/dpi; // pixels per meter
	private static final double zoomRatio = 1.25;
	
	private static final double MAX_DRAW_SCALE = 75000;
	private static final double MAX_SNAP_SCALE = 50000;
	
	// properties
	private String mxdDoc = null;
	private IMsoModelIf msoModel = null;
	private IDiskoMapManager mapManager = null;
	private IDiskoTool currentTool = null;
	private int supressDrawing = 0;
	private int notifySuspended = 0;
	private boolean isEditSupportInstalled = false;
	private int currentBase = 0;
	
	// lists
	private MsoLayerSelectionModel msoLayerSelectionModel = null;
	private WMSLayerSelectionModel wmsLayerSelectionModel = null;
	private DefaultMapLayerSelectionModel defaultMapLayerSelectionModel = null;
	private EnumSet<IMsoFeatureLayer.LayerCode> myLayers = null;
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	private List<IMsoFeatureLayer> msoLayers = null;
	
	// listeners
	private List<IDiskoWorkListener> workListeners = null;
	
	// components
	private MapStatusBar mapStatusBar = null;
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
	private ControlEventsAdapter ctrlAdapter = null;
	private MapCompEventsAdapter compAdapter = null;
	
	// elements
	private DrawFrame drawFrame = null;
	
	// flags
	private boolean isInitMode = true;
	
	// mouse tracking
	private long previous = 0;		
	private Timer tracker = null;	
	private Point movePoint = null;
	private Point clickPoint = null;


	/**
	 * Default constructor
	 */
	public DiskoMap(IDiskoMapManager mapManager, 
			IMsoModelIf msoModel, EnumSet<IMsoFeatureLayer.LayerCode> myLayers)
			throws IOException, AutomationException {
		
		// prepare
		this.mapManager = mapManager;
		this.msoModel = msoModel;
		this.myLayers = myLayers;
		this.msoLayerEventStack = new MsoLayerEventStack();
		this.workListeners = new ArrayList<IDiskoWorkListener>();
		this.ctrlAdapter = new ControlEventsAdapter();
		this.tracker = new MapTracker();
		this.progressor = new DiskoMapProgressor(this);
		this.compAdapter = new MapCompEventsAdapter();
		
		// initialize GUI
		initialize();
		
		// load map
		loadMxdDoc(mapManager.getMxdDoc(),true);		
	}

	private void initialize() throws IOException, AutomationException {
		
		// prepare
		setName("diskoMap");
		setBorder(null);
		setBorderStyle(esriControlsBorderStyle.esriNoBorder);
		setShowScrollbars(false);
		suppressResizeDrawing(true, 0);		

		// set disko map progress
		getTrackCancel().setCheckTime(250);
		getTrackCancel().setProgressor(progressor);
		
        // listen to mso client event update events
		msoModel.getEventManager().addClientUpdateListener(this);
                
		// listen to do actions when the map is loaded
		addIMapControlEvents2Listener(ctrlAdapter);

		// listen for component events
		addComponentListener(compAdapter);
		
		// create refresh stack
		refreshStack = new HashMap<String,Runnable>();
		
		// create timer to reduce flickering of mouse pointer over map
		tracker = new MapTracker();
		
		// create points for last click and move events
		movePoint = new Point();
		movePoint.setSpatialReferenceByRef(getSpatialReference());
		clickPoint = new Point();
		clickPoint.setSpatialReferenceByRef(getSpatialReference());
		
	}
	
	private void initLayers() throws java.io.IOException, AutomationException {
		
		// forward
		if(!isMxdDocLoaded()) return;
		
		// add custom layers
		IMap focusMap = getActiveView().getFocusMap();
		ISpatialReference srs = getSpatialReference();
		msoLayers = new ArrayList<IMsoFeatureLayer>();
		
		// get interests as
		ArrayList<IMsoFeatureLayer.LayerCode> list = new ArrayList<IMsoFeatureLayer.LayerCode>(myLayers);
		
		// initialize
		myInterests = EnumSet.noneOf(IMsoManagerIf.MsoClassCode.class);
		
		// loop over my layers
		for(int i=0;i<list.size();i++){
			IMsoFeatureLayer.LayerCode layerCode = list.get(i);
			if(layerCode == IMsoFeatureLayer.LayerCode.POI_LAYER) {
				msoLayers.add(new POILayer(msoModel,srs,msoLayerEventStack));				
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_POI));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_POI);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.AREA_LAYER) {
				msoLayers.add(new AreaLayer(msoModel,srs,msoLayerEventStack));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.ROUTE_LAYER) {
				msoLayers.add(new RouteLayer(msoModel,srs,msoLayerEventStack));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.FLANK_LAYER) {
				msoLayers.add(new FlankLayer(msoModel,srs,msoLayerEventStack));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER) {
				msoLayers.add(new SearchAreaLayer(msoModel,srs,msoLayerEventStack));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER) {
				msoLayers.add(new OperationAreaLayer(msoModel,srs,msoLayerEventStack));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER) {
				msoLayers.add(new OperationAreaMaskLayer(msoModel,srs,msoLayerEventStack));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.UNIT_LAYER) {
				msoLayers.add(new UnitLayer(msoModel,srs,msoLayerEventStack));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT);
			}
		}
		
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
		
	}	
	
	private void initLayer(ILayer l) throws AutomationException, IOException {
		if (l instanceof IFeatureLayer) {
			IFeatureLayer f = (IFeatureLayer)l;
			if (!(f instanceof IMsoFeatureLayer)) {
				f.setSelectable(false);
			}
		}
		else if(l instanceof ILayerStatus) {
			// add the disko step processor to this layer
			((ILayerStatus)l).setStepProgressor(progressor);
		}
		else if(l instanceof GroupLayer) {
			// cast to group layer
			GroupLayer g = (GroupLayer)l;
			// loop over all layers
			for (int i = 0; i < g.getCount(); i++) {
				initLayer(g.getLayer(i));
			}
			
		}
		
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
			IEnvelope extent = null;
			IMsoFeatureLayer msoLayer = null;
			
			// loop over layers
			for (IMsoFeatureLayer it : layers) {
				if (it.isVisible() && it.isDirty()) {
					count++;
					msoLayer = it;
					extent = msoLayer.getDirtyExtent();
				}
			}
			// get extent
			extent = (extent==null ? getExtent() : extent);
			// refresh layer(s)
			if(count==1)
				refreshGraphics(msoLayer, extent);
			else if(count > 1)
				refreshGraphics(null, extent);
			else 
				refreshDrawFrame();
						
		} catch (AutomationException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	public void setMsoLayersVisible(IMsoManagerIf.MsoClassCode classCode, boolean value) throws IOException {
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

	public List<IMsoFeatureLayer> getMsoLayers(IMsoManagerIf.MsoClassCode classCode) {
		ArrayList<IMsoFeatureLayer> result = new ArrayList<IMsoFeatureLayer>();
		for (int i = 0; i < msoLayers.size(); i++) {
			IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
			if (msoFeatureLayer.getClassCode() == classCode) {
				result.add(msoFeatureLayer);
			}
		}
		return result;
	}

	public IMsoFeatureLayer getMsoLayer(IMsoFeatureLayer.LayerCode layerCode) {
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
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}

	@Override
	public void setCurrentToolByRef(ITool tool)
			throws IOException, AutomationException {
		setActiveTool(tool,true);
	}

	public void setActiveTool(ITool tool, boolean allow)
			throws IOException, AutomationException {
		// no change?
		if(currentTool==tool) return;
		// forward
		super.setCurrentToolByRef(tool);
		// update locals
		if (currentTool instanceof IDiskoTool &&
				currentTool != null && tool != currentTool) {
			if(!currentTool.deactivate()) {
				JOptionPane.showMessageDialog(this, "Aktivt verktøy kan ikke deaktiveres","Begrensing",JOptionPane.OK_OPTION);
				return;
			}
		}
		if (tool instanceof IDiskoTool) {
			IDiskoTool t = (IDiskoTool)tool;
			// allowed?
			if(!t.activate(allow)) {
				JOptionPane.showMessageDialog(this, "Verktøy kan ikke velges","Begrensing",JOptionPane.OK_OPTION);
				return;
			}
			currentTool = t;
		}
		else {
			// not a disko tool...
			currentTool = null;
		}
		// forward to draw adapter?
		if(isEditSupportInstalled()) {
			if(currentTool instanceof IDrawTool)
				getDrawAdapter().setDrawTool((IDrawTool)currentTool);
		}
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
	public void setWMSLayerSelectionModel()
		throws IOException, AutomationException {
		wmsLayerSelectionModel = new WMSLayerSelectionModel(this);
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public WMSLayerSelectionModel getWMSLayerSelectionModel()
			throws IOException, AutomationException {
		if (wmsLayerSelectionModel == null) {
			wmsLayerSelectionModel = new WMSLayerSelectionModel(this);
		}
		return wmsLayerSelectionModel;
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public void setDefaultMapLayerSelectionModel()
		throws IOException, AutomationException {
		defaultMapLayerSelectionModel = new DefaultMapLayerSelectionModel(this);
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public DefaultMapLayerSelectionModel getDefaultMapLayerSelectionModel()
			throws IOException, AutomationException {
		if (defaultMapLayerSelectionModel == null) {
			defaultMapLayerSelectionModel = new DefaultMapLayerSelectionModel(this);
		}
		return defaultMapLayerSelectionModel;
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

	public void centerAtPosition(Position p) throws IOException, AutomationException {
		// get esri point
		IPoint point = MapUtil.getEsriPoint(p, getSpatialReference());
		if (!point.isEmpty()) {
			centerAt(point);
		}
	}
	
	public void centerAtSelected() throws IOException, AutomationException {
		IEnvelope env = getSelectionExtent();
		if (env != null) {
			centerAt(MapUtil.getCenter(env));
		}
	}
	
	public void centerAtFeature(IFeature feature) throws IOException, AutomationException {
		if(feature!=null) {
			IEnvelope env = feature.getExtent();
			if(env!=null) {
				centerAt(MapUtil.getCenter(env));
			}			
		}
	}

	public void centerAtMsoObject(IMsoObjectIf msoObject) throws IOException, AutomationException {
		IEnvelope env = getMsoObjectExtent(msoObject);
		if(env!=null) {
			centerAt(MapUtil.getCenter(env));
		}			
	}

	public void zoomToSelected() throws IOException, AutomationException {
		zoomToSelected(zoomRatio);
	}

	public void zoomToSelected(double ratio) throws IOException, AutomationException {
		IEnvelope env = getSelectionExtent();
		if (env != null) {
			setExtent(MapUtil.expand(ratio,env));
		}
	}
	
	public void zoomToFeature(IFeature feature) throws IOException, AutomationException {
		zoomToFeature(feature,zoomRatio); 
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
		zoomToMsoObject(msoObject,zoomRatio);
	}
	
	public void zoomToMsoObject(IMsoObjectIf msoObject, double ratio) throws IOException, AutomationException {
		// is poi?
		if(msoObject instanceof IPOIIf) {
			centerAtMsoObject(msoObject);
		}
		else {
			IEnvelope env = getMsoObjectExtent(msoObject);
			if(env!=null) {
				setExtent(MapUtil.expand(ratio,env));
			}			
		}
	}

	public IEnvelope getMsoObjectExtent(IMsoObjectIf msoObj) throws IOException, AutomationException {
		IEnvelope env = null;
		msoObj = getGeodataMsoObject(msoObj);
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
		}
		return env;
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
				double deltaX = (pixWidth*pixelSize)*scale;
				double deltaY = (pixWidth*pixelSize)*scale;
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
		if(mapStatusBar!=null) {
			mapStatusBar.setSelected(selected ? msoObject : null);
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
		
		if(mapStatusBar!=null)
			mapStatusBar.setSelected(null);
		
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
		if (currentTool instanceof IDrawTool) {
			// cast to draw tool
			IDrawTool tool = (IDrawTool)currentTool;
			// return drawing status
			return tool.isDrawing();
		}
		// is not drawing
		return false;
	}
	
	
	@Override
	public ITool getCurrentTool() throws IOException, AutomationException {
		// forward?
		if(currentTool==null)
			return super.getCurrentTool();
		else
			return (ITool)currentTool;
	}

	public void refreshMapBase() throws IOException, AutomationException {
		// forward
		refresh(esriViewDrawPhase.esriViewGeography, null, getExtent());
	}
	
	public void refreshMapBase(IEnvelope extent)  throws IOException, AutomationException {
		// forward
		refresh(esriViewDrawPhase.esriViewGeography, null, extent);
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
	
	public void refreshMsoLayers(IMsoManagerIf.MsoClassCode classCode) throws IOException,
		AutomationException {
		// forward
		refreshMsoLayers(classCode, getExtent());
		
	}
	public void refreshMsoLayers(IMsoManagerIf.MsoClassCode classCode, IEnvelope extent) throws IOException,
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
					// hide
					setVisible(false);
					// refresh view
					getActiveView().refresh();
					// show
					setVisible(true);
				} catch (AutomationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
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
			//System.out.println("L:A:"+key);
			// create object
			Runnable r = new Runnable() {
				public void run() {
					try {				
						// get key
						String key = phase + "." + String.valueOf(data);
						// refresh view
						getActiveView().partialRefresh(phase, data, extent);
						// is draw frame enabled?
						if(isEditSupportInstalled()) {
							// draw directly onto map?
							if(data==null || !drawFrame.isDirty()) 
								drawFrame.draw();
							else
								drawFrame.refresh();
						}
						// remove from stack
						refreshStack.remove(key);
					} catch (AutomationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
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
		// decrement?
		if(notifySuspended>1)
			notifySuspended--;
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
					IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
					msoFeatureLayer.resumeNotify();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void consumeNotify() {
		/*/ decrement?
		if(notifySuspended>0)
			notifySuspended--;
		// only resume on last decrement
		if(notifySuspended==0){
		*/
			// clear stack
			msoLayerEventStack.consumeAll();
		//}
		
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
			return (MAX_DRAW_SCALE >= getScale());
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
			return (MAX_SNAP_SCALE >= getScale());
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

	public void setMapStatusBar(MapStatusBar buddy) {
		// set buddy
		mapStatusBar = buddy;		
	}

	public MapStatusBar getMapStatusBar() {
		return mapStatusBar;		
	}
	
	public Point getClickPoint() {
		return clickPoint;
	}

	public Point getMovePoint() {
		return movePoint;
	}

	public static JPanel createPanel(IDiskoMap map, 
			MapStatusBar statusBar, MapFilterBar filterBar, Border border) {
		JPanel panel = MapStatusBar.createPanel(
				map, statusBar, BorderLayout.NORTH, border);
		map.setMapStatusBar(statusBar);
		filterBar.setMap(map);
		panel.add(filterBar,BorderLayout.SOUTH);
		return panel;
		
	}
	
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
	        NavBar navBar = Utils.getApp().getNavBar();
			drawDialog = (DrawDialog)navBar.getDrawHostTool().getDialog();
			drawDialog.setLocationRelativeTo(this,DiskoDialog.POS_WEST, true, true);

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
			elementDialog.setLocationRelativeTo(this,DiskoDialog.POS_EAST, false, true);

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
	        snapDialog.setLocationRelativeTo(this,DiskoDialog.POS_WEST, true, true);

		}
		return snapDialog;
	}
	
	
	
	public boolean isInitMode() {
		return isInitMode;
	}
	
	public void setInitMode(boolean isInitMode) {
		this.isInitMode = isInitMode;
	}
	
	/*==========================================================
	 * IDiskoWorkListener methods
	 *========================================================== 
	 */		
	
	public void onWorkCancel(DiskoWorkEvent e) {
		fireOnWorkCancel(e);	
	}
	
	public void onWorkFinish(DiskoWorkEvent e) {
		fireOnWorkFinish(e);	
	}
	
	public void onWorkChange(DiskoWorkEvent e) {
		fireOnWorkChange(e);
	}

	public void addDiskoWorkEventListener(IDiskoWorkListener listener) {
		workListeners.add(listener);
	}
	
	public void removeDiskoWorkEventListener(IDiskoWorkListener listener) {
		workListeners.remove(listener);
	}
    
    private void fireOnWorkCancel(DiskoWorkEvent e)
    {
		// notify workListeners
		for (int i = 0; i < workListeners.size(); i++) {
			workListeners.get(i).onWorkCancel(e);
		}
	}
		
    private void fireOnWorkFinish(DiskoWorkEvent e)
    {
		// notify workListeners
		for (int i = 0; i < workListeners.size(); i++) {
			workListeners.get(i).onWorkFinish(e);
		}
	}
    
    private void fireOnWorkChange(DiskoWorkEvent e)
    {
		// notify workListeners
		for (int i = 0; i < workListeners.size(); i++) {
			workListeners.get(i).onWorkChange(e);
		}
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
					if(!l.isCached()) l.setCached(true);
					// update visible state
					l.setVisible(i==(index-1));
				}
			}
			
			// update current index
			currentBase = index;
			
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
	
	class ControlEventsAdapter extends IMapControlEvents2Adapter {

		private static final long serialVersionUID = 1L;

		@Override
		public void onViewRefreshed(IMapControlEvents2OnViewRefreshedEvent arg0) throws IOException, AutomationException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						if (DiskoMap.this.isVisible() && !DiskoProgressMonitor.getInstance().isInAction()) {
							DiskoMap.this.getTrackCancel().getProgressor().show();
						}
					} catch (AutomationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});			
			//System.out.println(toString() + ":: onViewRefreshed");
		}

		@Override
		public void onAfterScreenDraw(IMapControlEvents2OnAfterScreenDrawEvent arg0) throws IOException, AutomationException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						DiskoMap.this.getTrackCancel().getProgressor().hide();
					} catch (AutomationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});			
			//System.out.println(toString() + ":: onAfterScreenDraw");
		}

		@Override
		public void onMouseDown(IMapControlEvents2OnMouseDownEvent e) throws IOException, AutomationException {
			// update status panel?
			if(mapStatusBar!=null) {
				clickPoint.setX(e.getMapX());
				clickPoint.setY(e.getMapY());
				mapStatusBar.onMouseDown(clickPoint);
			}
		}
		
		@Override
		public void onMouseMove(IMapControlEvents2OnMouseMoveEvent e) throws IOException, AutomationException {
			// update status panel?
			if(mapStatusBar==null) {
				// stop timer?
				if(tracker.isRunning())
					tracker.stop();					
			}
			else {
				// get tic
				long tic = Calendar.getInstance().getTimeInMillis();
				if(tic-previous>190) {
					previous = tic;
					movePoint.setX(e.getMapX());
					movePoint.setY(e.getMapY());
					// start timer?
					if(!tracker.isRunning()) {
						tracker.start();
					}
				}
			}		
		}
		
		public void onMapReplaced(IMapControlEvents2OnMapReplacedEvent e)
               	throws java.io.IOException, AutomationException {
			// update status panel?
			if(mapStatusBar!=null) {
				mapStatusBar.reset();
			}
			// update draw element?
			if(drawFrame!=null) {
				drawFrame.setActiveView(getActiveView());
			}
		}
		
		public void onExtentUpdated(IMapControlEvents2OnExtentUpdatedEvent theEvent)
    		throws java.io.IOException, AutomationException {
			
			// update status panel?
			if(mapStatusBar==null) {
				// stop timer?
				if(tracker.isRunning())
					tracker.stop();					
			}
			else {
				// get tic
				long tic = Calendar.getInstance().getTimeInMillis();
				if(tic-previous>150) {
					previous = tic;
					// start timer?
					if(!tracker.isRunning()) {
						tracker.start();
					}
				}
			}		
			
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
					suppressResizeDrawing(false, 0);
					// turn on
					suppressResizeDrawing(true, 0);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
		}

	}
	
	class MapTracker extends Timer {
		
		private static final long serialVersionUID = 1L;
		
		MapTracker() {
			
			// forward
			super(50, new ActionListener() {
	
				public void actionPerformed(ActionEvent arg0) {
					// notify?
					if(mapStatusBar!=null) {
						try {
							mapStatusBar.onMouseMove(movePoint);
							mapStatusBar.setScale(getMapScale());
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}				
				}});
			
			// one shot and start at once
			setRepeats(false);
			setInitialDelay(0);
			
		}		
	}	
	
	
}
