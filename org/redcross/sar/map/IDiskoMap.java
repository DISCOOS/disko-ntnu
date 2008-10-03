package org.redcross.sar.map;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.redcross.sar.gui.dialog.DrawDialog;
import org.redcross.sar.gui.dialog.SnapDialog;
import org.redcross.sar.gui.mso.dialog.ElementDialog;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IDiskoLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IDiskoLayer.LayerCode;
import org.redcross.sar.map.tool.MsoDrawAdapter;
import org.redcross.sar.map.tool.IMapTool;
import org.redcross.sar.map.tool.SnapAdapter;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.thread.event.IDiskoWorkListener;
import org.redcross.sar.util.mso.GeoPos;

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
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public interface IDiskoMap {
	
	public enum CoordinateFormat {
		FORMAT_UTM,
		FORMAT_MGRS,
		FORMAT_DEG,
		FORMAT_DEM,
		FORMAT_DES
	}
	
	public IMapTool getActiveTool();
	public boolean setActiveTool(IMapTool tool, int options) throws IOException, AutomationException;
	
	public EnumSet<LayerCode> getSupportedLayers();
	
	public List<IDiskoLayer> getDiskoLayers();
	public IDiskoLayer getDiskoLayer(LayerCode layerCode);
	
	public List<IMsoFeatureLayer> getMsoLayers();
	public List<IMsoFeatureLayer> getMsoLayers(MsoClassCode classCode);
	
	public IMsoFeatureLayer getMsoLayer(LayerCode layerCode);
	
	public List<IMsoFeature> getMsoFeature(IMsoObjectIf msoObj) throws AutomationException, IOException;	
	
	public MsoLayerSelectionModel getMsoLayerSelectionModel() throws IOException, AutomationException;
	public WmsLayerSelectionModel getWmsLayerSelectionModel() throws IOException, AutomationException;	
	public MapLayerSelectionModel getMapLayerSelectionModel() throws IOException, AutomationException;	
	public void setWmsLayerSelectionModel() throws IOException, AutomationException;
	public void setMsoLayerSelectionModel() throws IOException, AutomationException;
	public void setMapLayerSelectionModel() throws IOException, AutomationException;
	
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
	public void flashPoint(IPoint p);
	public void flashPosition(GeoPos p);
	public void flashEnvelope(IEnvelope extent);
	public void flashPolygon(IPolygon p);
	public void flashPolyline(IPolyline p);
	public void flashFeature(IFeature feature);
	public void flashMsoObject(IMsoObjectIf msoObject);
	
	public void centerAt(IPoint p) throws IOException, AutomationException;
	public void centerAtPosition(GeoPos p) throws IOException, AutomationException;
	public void centerAtSelected () throws IOException, AutomationException;
	public void centerAtFeature(IFeature feature) throws IOException, AutomationException;
	public void centerAtMsoObject(IMsoObjectIf msoObject) throws IOException, AutomationException;
	
	public void zoomTo(IGeometry geom,double ratio) throws IOException, AutomationException;
	public void zoomToPosition(GeoPos p,double ratio) throws IOException, AutomationException;
	public void zoomToSelected () throws IOException, AutomationException;
	public void zoomToSelected (double ratio) throws IOException, AutomationException;
	public void zoomToFeature(IFeature feature) throws IOException, AutomationException;
	public void zoomToFeature(IFeature feature, double ratio) throws IOException, AutomationException;
	public void zoomToMsoObject(IMsoObjectIf msoObject) throws IOException, AutomationException;
	public void zoomToMsoObject(IMsoObjectIf msoObject, double ratio) throws IOException, AutomationException;
	
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
	
	public Point getClickPoint();
	public Point getCenterPoint();
	public Point getMovePoint();

	public double getScale();
	
	public double getMaxSnapScale();	
	public boolean isSnapAllowed();
	
	public double getMaxDrawScale();
	public boolean isDrawAllowed();
		
	public void addIMapControlEvents2Listener(IMapControlEvents2Adapter listener) throws IOException, AutomationException;
	public void removeIMapControlEvents2Listener(IMapControlEvents2Adapter listener) throws IOException, AutomationException;
	
	public boolean isEditSupportInstalled();
	public void installEditSupport();
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
	
	public void addDiskoWorkListener(IDiskoWorkListener listener);
	public void removeDiskoWorkEventListener(IDiskoWorkListener listener);
	
	public IEnvelope getDirtyExtent();
	
	public boolean isRefreshPending();
	
	public boolean isVisible();
	public void setVisible(boolean isVisible);

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
	
	
}