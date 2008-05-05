package org.redcross.sar.map;

import java.io.IOException;
import java.util.List;

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
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.systemUI.ITool;

public interface IDiskoMap {
	
	public void setActiveTool(ITool tool, boolean allow) throws IOException, AutomationException;
	
	public List<IMsoFeatureLayer> getMsoLayers();
	
	public List<IMsoFeatureLayer> getMsoLayers(IMsoManagerIf.MsoClassCode classCode);
	
	public IMsoFeatureLayer getMsoLayer(IMsoFeatureLayer.LayerCode layerCode);
	
	public List<IMsoFeature> getMsoFeature(IMsoObjectIf msoObj) throws AutomationException, IOException;	
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public MsoLayerSelectionModel getMsoLayerSelectionModel()
			throws IOException, AutomationException;
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public void setMsoLayerSelectionModel()
			throws IOException, AutomationException;
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public WMSLayerSelectionModel getWMSLayerSelectionModel()
			throws IOException, AutomationException;
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public void setWMSLayerSelectionModel()
			throws IOException, AutomationException;
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public DefaultMapLayerSelectionModel getDefaultMapLayerSelectionModel()
			throws IOException, AutomationException;
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getClipLayerSelectionModel()
	 */
	public void setDefaultMapLayerSelectionModel()
			throws IOException, AutomationException;
	
	public IDiskoMapManager getMapManager();

	public List<IFeature> getSelection() throws IOException, AutomationException;
	
	public List<IMsoFeature> getMsoSelection() throws IOException, AutomationException;
	
	public IEnvelope getSelectionExtent() throws IOException, AutomationException;

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#setSelected(java.lang.String, java.lang.String, java.lang.Object)
	 */
	public void setSelected(String layerName, String fieldName, Object value)
			throws IOException, AutomationException;

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#setSelected(com.esri.arcgis.carto.FeatureLayer, java.lang.String, java.lang.Object)
	 */
	public void setSelected(FeatureLayer layer, String fieldName, Object value)
			throws IOException, AutomationException;

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#setSelected(com.esri.arcgis.carto.FeatureLayer, java.lang.String)
	 */
	public void setSelected(FeatureLayer layer, String whereclause)
			throws IOException, AutomationException;
	
	public int isSelected(IMsoObjectIf msoObj) throws AutomationException, IOException;
	
	public List setSelected(IMsoObjectIf msoObject, boolean selected) throws IOException, AutomationException;
	
	public List clearSelected() throws IOException, AutomationException;

	public int getSelectionCount(boolean update) throws IOException, AutomationException;
	
	public int getMsoSelectionCount(boolean update) throws IOException, AutomationException;
	
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
	
	/*public void startEdit(IMsoObjectIf msoObject, boolean selectIt) throws IOException, AutomationException;
	
	public void stopEdit(IMsoObjectIf msoObject) throws IOException, AutomationException;
	
	public boolean isEditing();
	*/
	
	public IEnvelope getMsoObjectExtent(IMsoObjectIf msoObj) throws IOException, AutomationException;
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#getFeatureLayer(java.lang.String)
	 */
	public FeatureLayer getFeatureLayer(String name) throws IOException,
			AutomationException;

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#refreshLayer(com.esri.arcgis.geometry.IEnvelope)
	 */
	public void refreshLayer(Object obj, IEnvelope extent) throws IOException,
			AutomationException;

	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#refresh()
	 */
	public void refresh() throws IOException,
			AutomationException;
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.map.IDiskoMap#refreshSelection(com.esri.arcgis.geometry.IEnvelope)
	 */
	public void refreshSelection(Object obj, IEnvelope extent) throws IOException,
			AutomationException;
	
	public void refreshMsoLayers() throws IOException, AutomationException;
	
	public void refreshMsoLayers(IEnvelope extent) throws IOException, AutomationException;
	
	public void refreshMsoLayers(IMsoManagerIf.MsoClassCode classCodet) throws IOException,
		AutomationException;
	
	public void refreshMsoLayers(IMsoManagerIf.MsoClassCode classCode, IEnvelope extent) throws IOException,
		AutomationException;
	
	public void suspendNotify();	
	
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
	
		
}