package org.redcross.sar.map;

import java.io.IOException;
import java.util.List;

import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.map.DrawDialog;
import org.redcross.sar.gui.map.ElementDialog;
import org.redcross.sar.gui.map.MapStatusBar;
import org.redcross.sar.gui.map.SnapDialog;
import org.redcross.sar.map.element.DrawFrame;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.carto.FeatureLayer;
import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.systemUI.ITool;

public interface IDiskoMap extends IDiskoWorkListener {
	
	public enum CoordinateFormat {
		FORMAT_UTM,
		FORMAT_MGRS,
		FORMAT_DEG,
		FORMAT_DES
	}
	
	public void setActiveTool(ITool tool, boolean allow) throws IOException, AutomationException;
	
	public List<IMsoFeatureLayer> getMsoLayers();
	
	public List<IMsoFeatureLayer> getMsoLayers(IMsoManagerIf.MsoClassCode classCode);
	
	public IMsoFeatureLayer getMsoLayer(IMsoFeatureLayer.LayerCode layerCode);
	
	public List<IMsoFeature> getMsoFeature(IMsoObjectIf msoObj) throws AutomationException, IOException;	
	
	public MsoLayerSelectionModel getMsoLayerSelectionModel() throws IOException, AutomationException;
	public WMSLayerSelectionModel getWMSLayerSelectionModel() throws IOException, AutomationException;	
	public DefaultMapLayerSelectionModel getDefaultMapLayerSelectionModel() throws IOException, AutomationException;	
	public void setWMSLayerSelectionModel() throws IOException, AutomationException;
	public void setMsoLayerSelectionModel() throws IOException, AutomationException;
	public void setDefaultMapLayerSelectionModel() throws IOException, AutomationException;
	
	public IDiskoMapManager getMapManager();
	
	public List<IFeature> getSelection() throws IOException, AutomationException;
	public List<IMsoFeature> getMsoSelection() throws IOException, AutomationException;
	public IEnvelope getSelectionExtent() throws IOException, AutomationException;

	public void setSelected(String layerName, String fieldName, Object value) throws IOException, AutomationException;
	public void setSelected(FeatureLayer layer, String fieldName, Object value) throws IOException, AutomationException;
	public void setSelected(FeatureLayer layer, String whereclause) throws IOException, AutomationException;	

	public int isSelected(IMsoObjectIf msoObj) throws AutomationException, IOException;
	public int getSelectionCount(boolean update) throws IOException, AutomationException;	
	public int getMsoSelectionCount(boolean update) throws IOException, AutomationException;
	public List<IMsoFeatureLayer> setSelected(IMsoObjectIf msoObject, boolean selected) throws IOException, AutomationException;	
	public List<IMsoFeatureLayer> clearSelected() throws IOException, AutomationException;
	
	public void centerAtPosition(Position p) throws IOException, AutomationException;
	public void centerAtSelected () throws IOException, AutomationException;
	public void centerAtFeature(IFeature feature) throws IOException, AutomationException;
	public void centerAtMsoObject(IMsoObjectIf msoObject) throws IOException, AutomationException;
	
	public void zoomToSelected () throws IOException, AutomationException;
	public void zoomToSelected (double ratio) throws IOException, AutomationException;
	public void zoomToFeature(IFeature feature) throws IOException, AutomationException;
	public void zoomToFeature(IFeature feature, double ratio) throws IOException, AutomationException;
	public void zoomToMsoObject(IMsoObjectIf msoObject) throws IOException, AutomationException;
	public void zoomToMsoObject(IMsoObjectIf msoObject, double ratio) throws IOException, AutomationException;
	
	public IEnvelope getMsoObjectExtent(IMsoObjectIf msoObj) throws IOException, AutomationException;
	
	public FeatureLayer getFeatureLayer(String name) throws IOException, AutomationException;

	public void refresh() throws IOException, AutomationException;
	public void refreshGraphics(Object data, IEnvelope extent) throws IOException,  AutomationException;
	public void refreshGeography(Object data, IEnvelope extent) throws IOException,  AutomationException;
	
	public void refreshMapBase() throws IOException, AutomationException;	
	public void refreshMapBase(IEnvelope extent) throws IOException, AutomationException;	
	public void refreshMsoLayers() throws IOException, AutomationException;
	public void refreshMsoLayers(IEnvelope extent) throws IOException, AutomationException;	
	public void refreshMsoLayers(IMsoManagerIf.MsoClassCode code) throws IOException, AutomationException;
	public void refreshMsoLayers(IMsoManagerIf.MsoClassCode code, IEnvelope extent) throws IOException, AutomationException;	
	
	public void suspendNotify();	
	public void consumeNotify();	
	public void resumeNotify();		
	public boolean isNotifySuspended();
	
	public boolean isDrawingSupressed();		
	public void setSupressDrawing(boolean supress);
	
	public List<IFeatureLayer> getSnappableLayers() throws IOException, AutomationException;
	
	public void setMapStatusBar(MapStatusBar buddy);
	public MapStatusBar getMapStatusBar();
	
	public Point getClickPoint();
	
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
	public DrawAdapter getDrawAdapter();	
	public DrawDialog getDrawDialog();
	public DrawFrame getDrawFrame();
	public ElementDialog getElementDialog();	
	public SnapAdapter getSnapAdapter();
	public SnapDialog getSnapDialog();
	
	public boolean isInitMode();
	public void setInitMode(boolean isInitMode);
	
	public IEnvelope getExtent() throws IOException, AutomationException;
	public void setExtent(IEnvelope e) throws IOException, AutomationException;
	
	public ISpatialReference getSpatialReference() throws IOException, AutomationException;
	
	public void addDiskoWorkEventListener(IDiskoWorkListener listener);
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
	
}