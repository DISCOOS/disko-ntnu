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
import com.esri.arcgis.controls.IMapControlEvents2OnExtentUpdatedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMapReplacedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMouseDownEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMouseMoveEvent;
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

import org.redcross.sar.gui.map.MapFilterBar;
import org.redcross.sar.gui.map.MapStatusBar;
import org.redcross.sar.map.command.IDiskoTool;
import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.MsoFeatureClass;
import org.redcross.sar.map.layer.*;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
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
	
	private String mxdDoc = null;
	private IMsoModelIf msoModel = null;
	private IDiskoMapManager mapManager = null;
	private MsoLayerSelectionModel msoLayerSelectionModel = null;
	private WMSLayerSelectionModel wmsLayerSelectionModel = null;
	private DefaultMapLayerSelectionModel defaultMapLayerSelectionModel = null;
	private IDiskoTool currentTool = null;
	private boolean supressDrawing = false;
	private boolean isNotifySuspended = false;
	protected EnumSet<IMsoFeatureLayer.LayerCode> myLayers = null;
	protected EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	private List<AbstractMsoFeatureLayer> msoLayers = null;
	private HashMap<String,Runnable> refreshStack = null;
	private MapStatusBar mapStatusBar = null;
	private MapFilterBar mapFilterBar = null;
	
	private long previous = 0;
	
	private Timer update = null;
	
	private Point movePoint = null;
	private Point clickPoint = null;


	/**
	 * Default constructor
	 */
	public DiskoMap(String mxdDoc, IDiskoMapManager mapManager, 
			IMsoModelIf msoModel, EnumSet<IMsoFeatureLayer.LayerCode> myLayers)
			throws IOException, AutomationException {
		this.mxdDoc = mxdDoc;
		this.mapManager = mapManager;
		this.msoModel = msoModel;
		this.myLayers = myLayers;
		
		initialize();
	}

	private void initialize() throws IOException, AutomationException {
		setName("diskoMap");
		setShowScrollbars(false);
		setBorderStyle(com.esri.arcgis.controls.esriControlsBorderStyle.esriNoBorder);
		setBorder(null);

		//setDocumentFilename(mxdDoc);
		loadMxFile(mxdDoc, null, null);

		// initialize layers
        initLayers();
		
		// listen to do actions when the map is loaded
		addIMapControlEvents2Listener(new IMapControlEvents2Adapter() {
			private static final long serialVersionUID = 1L;
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
					if(update.isRunning())
						update.stop();					
				}
				else {
					// get tic
					long tic = Calendar.getInstance().getTimeInMillis();
					if(tic-previous>190) {
						previous = tic;
						movePoint.setX(e.getMapX());
						movePoint.setY(e.getMapY());
						// start timer?
						if(!update.isRunning()) {
							update.start();
						}
					}
				}		
			}
			public void onMapReplaced(IMapControlEvents2OnMapReplacedEvent e)
                   	throws java.io.IOException, AutomationException {
				initLayers();
				// update status panel?
				if(mapStatusBar!=null) {
					mapStatusBar.reset();
				}
			}
			public void onExtentUpdated(IMapControlEvents2OnExtentUpdatedEvent theEvent)
        		throws java.io.IOException, AutomationException {
				// update status panel?
				if(mapStatusBar==null) {
					// stop timer?
					if(update.isRunning())
						update.stop();					
				}
				else {
					// get tic
					long tic = Calendar.getInstance().getTimeInMillis();
					if(tic-previous>150) {
						previous = tic;
						// start timer?
						if(!update.isRunning()) {
							update.start();
						}
					}
				}		
			}
			
		});

		addComponentListener(new java.awt.event.ComponentListener() {

			public void componentHidden(ComponentEvent arg0) {
				// NOP
				
			}

			public void componentMoved(ComponentEvent arg0) {
				// NOP
				
			}

			public void componentResized(ComponentEvent arg0) {
				try {
					if(isVisible()) {
						// turn off
						suppressResizeDrawing(false, 0);
						// force a refres
						//refresh();
						// resume
						suppressResizeDrawing(true, 0);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
			}

			public void componentShown(ComponentEvent arg0) {
				// NOP
				
			}
			
		});

		// create refresh stack
		refreshStack = new HashMap<String,Runnable>();
		
		// create timer to prevent flickering of mouse pointer over map
		update = new Timer(50, new ActionListener() {

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
			}
			
		});
		
		// one shot and start at once
		update.setRepeats(false);
		update.setInitialDelay(0);
		
		// create points for last click and move events
		clickPoint = new Point();		
		movePoint = new Point();		
		clickPoint.setSpatialReferenceByRef(getSpatialReference());
		movePoint.setSpatialReferenceByRef(getSpatialReference());
		
	}
	
	private void initLayers() throws java.io.IOException, AutomationException {
		// add custom layers
		IMap focusMap = getActiveView().getFocusMap();
		ISpatialReference srs = getSpatialReference();
		msoLayers = new ArrayList<AbstractMsoFeatureLayer>();
		
		// get interests as
		ArrayList<IMsoFeatureLayer.LayerCode> list = new ArrayList<IMsoFeatureLayer.LayerCode>(myLayers);
		
		// initialize
		myInterests = EnumSet.noneOf(IMsoManagerIf.MsoClassCode.class);
		
		// loop over my layers
		for(int i=0;i<list.size();i++){
			IMsoFeatureLayer.LayerCode layerCode = list.get(i);
			if(layerCode == IMsoFeatureLayer.LayerCode.POI_LAYER) {
				msoLayers.add(new POILayer(msoModel,srs));				
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_POI));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_POI);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.AREA_LAYER) {
				msoLayers.add(new AreaLayer(msoModel,srs));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.ROUTE_LAYER) {
				msoLayers.add(new RouteLayer(msoModel,srs));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.FLANK_LAYER) {
				msoLayers.add(new FlankLayer(msoModel,srs));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER) {
				msoLayers.add(new SearchAreaLayer(msoModel,srs));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER) {
				msoLayers.add(new OperationAreaLayer(msoModel,srs));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER) {
				msoLayers.add(new OperationAreaMaskLayer(msoModel,srs));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA);
			}
			else if(layerCode == IMsoFeatureLayer.LayerCode.UNIT_LAYER) {
				msoLayers.add(new UnitLayer(msoModel,srs));
				if(!myInterests.contains(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT));
					myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT);
			}
		}

		IMsoEventManagerIf msoEventManager = msoModel.getEventManager();
		msoEventManager.addClientUpdateListener(this);

		for (int i = 0; i < msoLayers.size(); i++) {
			IFeatureLayer layer = (IFeatureLayer)msoLayers.get(i);
			focusMap.addLayer(layer);
			layer.setCached(true);
		}

		// set all featurelayers not selectabel
		for (int i = 0; i < focusMap.getLayerCount(); i++) {
			ILayer layer = focusMap.getLayer(i);
			if (layer instanceof IFeatureLayer) {
				IFeatureLayer flayer = (IFeatureLayer)layer;
				if (!(flayer instanceof IMsoFeatureLayer)) {
					flayer.setSelectable(false);
				}
			}
		}
	}
	
	public void handleMsoUpdateEvent(Update e) {
		
		try {
			
			// get mso object
			IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
			
			/*
			// Get editing features
			List<IMsoFeature> edits = getEditing();
			
			// is editing?
			if(edits.size()>0) {
				
				// get flags
				int mask = e.getEventTypeMask();
		        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
		        boolean addedReference = (mask & MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
		        boolean removedReference = (mask & MsoEvent.EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;
				
				// is object modified?
				if (addedReference || removedReference || modifiedObject) {
					
	 				// dispatch
	 				if(msoObj.getMsoClassCode() == IMsoManagerIf.MsoClassCode.CLASSCODE_POI) { 					
	 					
	 					// cast to poi
	 					IPOIIf poi = (IPOIIf)msoObj; 
	 					
						// get poi type
						IPOIIf.POIType type = poi.getType();
						
						// get flag
						boolean isAreaPoi = (type == IPOIIf.POIType.START) || 
							(type == IPOIIf.POIType.VIA) || (type == IPOIIf.POIType.STOP);
						
						// check area?
						if(isAreaPoi) {
							// get as list
							List<IAreaIf> areas = new ArrayList<IAreaIf>(msoModel.getMsoManager().getCmdPost().getAreaList().getItems());
							// look for and area that contains poi
							for(int i=0;i<areas.size();i++) {
								if(areas.get(i).getAreaPOIs().contains(poi)) {
		 							startEdit(msoObj,false);		 										 										
		 							break;
								}
							}
						}
						else {
							// look for and operation or search area in edit mode
							for(int i=0;i<edits.size();i++) {
								// get mso object
								IMsoObjectIf item = edits.get(i).getMsoObject();
								// get flag
								boolean bStartEdit = (item.getMsoClassCode() == IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA) ||
									(item.getMsoClassCode() == IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA);
								// start editing on poi?
								if(bStartEdit) {
		 							startEdit(msoObj,false);		 										 										
		 							break;
								}
							}
						}
	 				}
				}
			}
			*/
			
			// refresh layers?
			if (!super.isShowing() || supressDrawing) { return;	}
			
			// get layers
			List layers = getMsoLayers(msoObj.getMsoClassCode());
			if(layers.size()>0) {
				IMsoFeatureLayer flayer = (IMsoFeatureLayer)layers.get(0);
				if (flayer.isDirty()) {
					refreshLayer(flayer, getExtent());
				}
			}
			
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
	
	public List getMsoLayers() {
		return msoLayers;
	}

	public List getMsoLayers(IMsoManagerIf.MsoClassCode classCode) {
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

	public void setSupressDrawing(boolean supress) {
		supressDrawing = supress;
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

	public List setSelected(IMsoObjectIf msoObject, boolean selected)
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
			mapStatusBar.setSelected(msoObject);
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

	public List clearSelected() 
		throws IOException, AutomationException {
		boolean bflag = isNotifySuspended;
		// suspend?
		if(!bflag)
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
		// resume?
		if(!bflag)
			resumeNotify();
		
		if(mapStatusBar!=null)
			mapStatusBar.setSelected(null);
		
		return cleared;
	}

	/*
	public void startEdit(IMsoObjectIf msoObject, boolean selectIt) throws IOException, AutomationException {
		
		List refList = null;
		msoObject = getGeodataMsoObject(msoObject);
		if (msoObject == null) {
			for (int i = 0; i < msoLayers.size(); i++) {
				// get layer
				IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
				// start editing layer
				msoFeatureLayer.startEdit(null);
			}
		}
		else {
			// get class code
			IMsoManagerIf.MsoClassCode classCode = msoObject.getMsoClassCode();
			// loop over all mso layers
			for (int i = 0; i < msoLayers.size(); i++) {
				IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
				if (msoFeatureLayer.getClassCode() == classCode) {
					refList = msoFeatureLayer.startEdit(msoObject);
					// has reference list?
					if (refList!=null) {
						// loop over referenced mso objects
					    for (Iterator it=refList.iterator(); it.hasNext(); ) {
					    	// get mso object
					        IMsoObjectIf msoObj = (IMsoObjectIf)it.next();
					        // recurse
					        startEdit(msoObj,false);				        
					    }
						
					}
				}
				else {
					// start editing layer
					msoFeatureLayer.startEdit(null);
				}
			}
			// select object?
			if (selectIt) {
				clearSelected();
				setSelected(msoObject, true);							
			}
		}
		
	}
	
	public void stopEdit(IMsoObjectIf msoObject) throws IOException, AutomationException {
		List refList = null;
		msoObject = getGeodataMsoObject(msoObject);
		if (msoObject == null) {
			for (int i = 0; i < msoLayers.size(); i++) {
				IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
				msoFeatureLayer.stopEdit(null);
			}
		}
		else {
			// get class code
			IMsoManagerIf.MsoClassCode classCode = msoObject.getMsoClassCode();
			// loop over all mso layers
			for (int i = 0; i < msoLayers.size(); i++) {
				IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
				if (msoFeatureLayer.getClassCode() == classCode) {
					refList = msoFeatureLayer.stopEdit(msoObject);
					// has reference list?
					if (refList!=null) {
						// loop over referenced mso objects
					    for (Iterator it=refList.iterator(); it.hasNext(); ) {
					    	// get mso object
					        IMsoObjectIf msoObj = (IMsoObjectIf)it.next();
					        // recurse
					        stopEdit(msoObj);				        
					    }
						
					}
				}
				else {
					// enable layer
					msoFeatureLayer.setEnabled(true);
				}
			}
		}
		
	}
	
	public boolean isEditing() {
		for (int i = 0; i < msoLayers.size(); i++) {
			IMsoFeatureLayer flayer = msoLayers.get(i);
			if (flayer.isEditing()) {
				return true;
			}
		}
		return false;
	}
	*/
	
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
	
	public void refresh() throws IOException, AutomationException {
		
		// consume?
		if(isDrawing()) return;
		
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

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#refreshLayer(com.esri.arcgis.geometry.IEnvelope)
	 */
	public void refreshLayer(final Object obj, final IEnvelope extent) {
		// consume?
		if(isDrawing()) return;
		// get key
		String key = String.valueOf(obj);
		//System.out.println("L:T:"+key);
		// not in stack?
		if (!refreshStack.containsKey(key)) {
			//System.out.println("L:A:"+key);
			// create object
			Runnable r = new Runnable() {
				public void run() {
					try {				
						// get key
						String key = String.valueOf(obj);
						//System.out.println("L:R:M"+key+":"+DiskoMap.this.hashCode());
						// refresh view
						getActiveView().partialRefresh(
								esriViewDrawPhase.esriViewGraphics, obj, extent);
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

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#refreshSelection(com.esri.arcgis.geometry.IEnvelope)
	 */
	public void refreshSelection(final Object obj, final IEnvelope extent) {
		// consume?
		if(isDrawing()) return;
		// get key
		String key = String.valueOf(obj);
		//System.out.println("S:T:"+key);
		// not in stack?
		if (!refreshStack.containsKey(key)) {
			//System.out.println("S:A:"+key);
			// create object
			Runnable r = new Runnable() {
				public void run() {
					try {
						// get key
						String key = String.valueOf(obj);
						//System.out.println("S:R:"+key);
						// refresh view
						getActiveView().partialRefresh(
								esriViewDrawPhase.esriViewGraphics, obj, extent);
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

	public void refreshMsoLayers() throws IOException,
		AutomationException {
		refreshMsoLayers(getExtent());
	}
	
	public void refreshMsoLayers(IEnvelope extent) throws IOException,
		AutomationException {
		int count = 0;
		IMsoFeatureLayer msoLayer = null;
		for (IMsoFeatureLayer it : msoLayers) {
			if (it.isDirty()) {
				count++;
				msoLayer = it;
			}
		}
		// refresh layer(s)
		if(count==1)
			refreshLayer(msoLayer, extent);
		else if(count > 1)
			refreshLayer(null, extent);

	}
	
	public void refreshMsoLayers(IMsoManagerIf.MsoClassCode classCode) throws IOException,
		AutomationException {
		refreshMsoLayers(classCode, getExtent());
		
	}
	public void refreshMsoLayers(IMsoManagerIf.MsoClassCode classCode, IEnvelope extent) throws IOException,
		AutomationException {
		List layers = getMsoLayers(classCode);
		for (int i = 0; i < layers.size(); i++) {
			IMsoFeatureLayer flayer = (IMsoFeatureLayer)layers.get(i);
			if (flayer.isDirty()) {
				refreshLayer(flayer, extent);
			}
		}
	}
	
	/*
	private List<IMsoFeature> getEditing() throws AutomationException, IOException {
		ArrayList<IMsoFeature> edits = new ArrayList<IMsoFeature>();
		for (int i = 0; i < msoLayers.size(); i++) {
			IMsoFeatureLayer flayer = msoLayers.get(i);
			if (flayer.isEditing()) {
				ArrayList<IMsoFeature> list = flayer.getEditing();
				if(list.size()>0){
					edits.addAll(list);
				}
			}
		}
		return edits;
	}
	*/
	
	public List getMsoFeature(IMsoObjectIf msoObj) throws AutomationException, IOException {
		List<IMsoFeature> features = new ArrayList<IMsoFeature>();
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

	public void suspendNotify() {
		isNotifySuspended = true;
		for (int i = 0; i < msoLayers.size(); i++) {
			IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
			msoFeatureLayer.suspendNotify();
		}
	}

	public void resumeNotify() {
		for (int i = 0; i < msoLayers.size(); i++) {
			IMsoFeatureLayer msoFeatureLayer = (IMsoFeatureLayer)msoLayers.get(i);
			msoFeatureLayer.resumeNotify();
		}
		isNotifySuspended = false;
	}

	public boolean isNotifySuspended() {
		return isNotifySuspended;
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

	public List getSnappableLayers() throws IOException, AutomationException {
		IMap focusMap = getActiveView().getFocusMap();
		double scale = getScale((IBasicMap)focusMap);
		ArrayList<IFeatureLayer> layers = new ArrayList<IFeatureLayer>();
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
			else if (layer instanceof GroupLayer) {
				// cast to group layer
				GroupLayer glayer = (GroupLayer) layer;
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
			
			if (layer.isVisible() && scale > layer.getMaximumScale() &&
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
	
}
