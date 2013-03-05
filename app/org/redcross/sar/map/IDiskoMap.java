package org.redcross.sar.map;

import java.awt.Point;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.redcross.sar.gui.dialog.DrawDialog;
import org.redcross.sar.gui.dialog.ElementDialog;
import org.redcross.sar.gui.dialog.SnapDialog;
import org.redcross.sar.map.event.IDiskoMapListener;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMapLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer.LayerCode;
import org.redcross.sar.map.tool.MsoDrawAdapter;
import org.redcross.sar.map.tool.IMapTool;
import org.redcross.sar.map.tool.SnapAdapter;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.work.event.IFlowListener;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IPolygon;
import com.esri.arcgis.geometry.IPolyline;
import com.esri.arcgis.geometry.ISpatialReference;
//import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public interface IDiskoMap {

	public enum CoordinateFormat {
		FORMAT_UTM,
		FORMAT_MGRS,
		FORMAT_DEG,
		FORMAT_DEM,
		FORMAT_DES
	}
	
	public Object getMapImpl();

	public boolean connect(IMsoModelIf model);
	public MsoFeatureBinder getMsoBinder();
	
	public boolean isActive();
	public boolean activate();
	public boolean deactivate();

	public boolean execute(boolean showProgress, boolean wait);

	public IMapTool getActiveTool();
	public boolean setActiveTool(IMapTool tool, int options) throws IOException, AutomationException;

	public List<Enum<?>> getSupportedLayers();

	public List<IMapLayer> getLayers();
	public IMapLayer getLayer(Enum<?> layerCode);

	public List<IMsoFeatureLayer> getMsoLayers();
	public List<IMsoFeatureLayer> getMsoLayers(MsoClassCode classCode);

	public IMsoFeatureLayer getMsoLayer(Enum<LayerCode> layerCode);

	public List<IMsoFeature> getMsoFeature(IMsoObjectIf msoObj) throws AutomationException, IOException;

	public MsoLayerModel getMsoLayerModel() throws IOException, AutomationException;
	public WmsLayerModel getWmsLayerModel() throws IOException, AutomationException;
	public MapLayerModel getMapLayerModel() throws IOException, AutomationException;
	public void setWmsLayerModel() throws IOException, AutomationException;
	public void setMsoLayerModel() throws IOException, AutomationException;
	public void setMapLayerModel() throws IOException, AutomationException;

	public IDiskoMapManager getMapManager();

	public List<IFeature> getSelection() throws IOException, AutomationException;
	public List<IMsoFeature> getMsoSelection() throws IOException, AutomationException;
	public IEnvelope getSelectionExtent() throws IOException, AutomationException;

	public void setSelected(String layerName, String fieldName, Object value) throws IOException, AutomationException;
	public void setSelected(IFeatureLayer layer, String fieldName, Object value) throws IOException, AutomationException;
	public void setSelected(IFeatureLayer layer, String whereclause) throws IOException, AutomationException;

	public int isSelected(IMsoObjectIf msoObj) throws AutomationException, IOException;
	public int getSelectionCount(boolean update) throws IOException, AutomationException;
	public int getMsoSelectionCount(boolean update) throws IOException, AutomationException;
	public List<IMsoFeatureLayer> setSelected(IMsoObjectIf msoObject, boolean selected) throws IOException, AutomationException;
	public List<IMsoFeatureLayer> clearSelected() throws IOException, AutomationException;

	public void flashSelected();
	public void flash(IPoint p);
	public void flash(GeoPos p);
	public void flash(IEnvelope extent);
	public void flash(IPolygon p);
	public void flash(IPolyline p);
	public void flash(IFeature feature);
	public void flash(IMsoObjectIf msoObject);

	public void centerAt(IPoint p) throws IOException, AutomationException;
	public void centerAt(GeoPos p) throws IOException, AutomationException;
	public void centerAtSelected() throws IOException, AutomationException;
	public void centerAt(IFeature feature) throws IOException, AutomationException;
	public void centerAt(IMsoObjectIf msoObject) throws IOException, AutomationException;

	public void zoomTo(IGeometry geom,double ratio) throws IOException, AutomationException;
	public void zoomTo(GeoPos p,double ratio) throws IOException, AutomationException;
	public void zoomToSelected() throws IOException, AutomationException;
	public void zoomToSelected(double ratio) throws IOException, AutomationException;
	public void zoomTo(IFeature feature) throws IOException, AutomationException;
	public void zoomTo(IFeature feature, double ratio) throws IOException, AutomationException;
	public void zoomTo(IMsoObjectIf msoObject) throws IOException, AutomationException;
	public void zoomTo(IMsoObjectIf msoObject, double ratio) throws IOException, AutomationException;

	public IEnvelope getMsoObjectExtent(IMsoObjectIf msoObj) throws IOException, AutomationException;

	public IFeatureLayer getFeatureLayer(String name) throws IOException, AutomationException;

	public void refresh() throws IOException, AutomationException;
	public void refreshGraphics(Object data, IEnvelope extent) throws IOException,  AutomationException;
	public void refreshGeography(Object data, IEnvelope extent) throws IOException,  AutomationException;

	public void refreshMapBase() throws IOException, AutomationException;
	public void refreshMapBase(IEnvelope extent) throws IOException, AutomationException;
	public void refreshMsoLayers() throws IOException, AutomationException;
	public void refreshMsoLayers(IEnvelope extent) throws IOException, AutomationException;
	public void refreshMsoLayers(MsoClassCode code) throws IOException, AutomationException;
	public void refreshMsoLayers(MsoClassCode code, IEnvelope extent) throws IOException, AutomationException;

	public void suspendNotify();
	public void consumeNotify();
	public void resumeNotify();
	public boolean isNotifySuspended();

	public boolean isDrawingSupressed();
	public void setSupressDrawing(boolean supress);

	public List<IFeatureLayer> getSnappableLayers() throws IOException, AutomationException;

	public IPoint getClickPoint();
	public IPoint getCenterPoint();
	public IPoint getMovePoint();

	public double getMapScale();

	public double getMaxSnapScale();
	public boolean isSnapAllowed();

	public double getMaxDrawScale();
	public boolean isDrawAllowed();

	public void addIMapControlEvents2Listener(IMapControlEvents2Adapter listener) throws IOException, AutomationException;
	public void removeIMapControlEvents2Listener(IMapControlEvents2Adapter listener) throws IOException, AutomationException;

	public boolean isEditSupportInstalled();
	public void installEditSupport(EnumSet<MsoClassCode> editable);
	public MsoDrawAdapter getDrawAdapter();
	public DrawDialog getDrawDialog();
	public DrawFrame getDrawFrame();
	public ElementDialog getElementDialog();
	public SnapAdapter getSnapAdapter();
	public SnapDialog getSnapDialog();

	public boolean isInitMode();
	public void setInitMode(boolean isInitMode);

	public IEnvelope getFullExtent() throws IOException, AutomationException;

	public IEnvelope getExtent() throws IOException, AutomationException;
	public void setExtent(IEnvelope e) throws IOException, AutomationException;

	public ISpatialReference getSpatialReference() throws IOException, AutomationException;

	public void addWorkFlowListener(IFlowListener listener);
	public void removeWorkEventListener(IFlowListener listener);

	public IEnvelope getDirtyExtent();

	public boolean isRefreshPending();

	public boolean isVisible();
	public void setVisible(boolean isVisible);
	public boolean isShowing();

	public ILayer getMapBase();
	public int getMapBaseIndex();
	public int getMapBaseCount();
	public int toggleMapBase();
	public int setMapBase(int index);

	public boolean loadMxdDoc();
	public boolean isMxdDocLoaded();

	public void showProgressor(boolean autocancel);
	public void hideProgressor();

	public boolean addDiskoMapListener(IDiskoMapListener listener);
	public boolean removeDiskoMapListener(IDiskoMapListener listener);
	
	public void setMapScale(double scale) throws IOException, AutomationException;
	
	public IEnvelope trackRectangle() throws IOException, AutomationException;
	public IGeometry trackLine() throws IOException, AutomationException;
	public IGeometry trackPolygon() throws IOException, AutomationException;
	public IGeometry trackCircle() throws IOException, AutomationException;
	
	/** Calculates a map distance corresponding to a point (1/72) distance */
	public double fromPoints(int pointDistance); 
	
	/** Calculates a distance in points (1/72 inch) corresponding to the map distance */
	public double toPoints(int mapDistance); 
	
	/** Calculates device coordinates corresponding to the map point */
	public Point fromMapPoint(IPoint mapPoint);
	
	/** Calculates a point in map coordinates corresponding to the device point */
	public IPoint toMapPoint(Point devicePoint);
	
}