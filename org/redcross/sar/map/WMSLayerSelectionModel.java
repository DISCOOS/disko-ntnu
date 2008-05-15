package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IMap;
import com.esri.arcgis.carto.WMSMapLayer;
import com.esri.arcgis.carto.esriViewDrawPhase;
import com.esri.arcgis.interop.AutomationException;

public class WMSLayerSelectionModel {
	
	private ArrayList<WMSMapLayer> wmsLayers = new ArrayList<WMSMapLayer>();
	protected DiskoMap map = null;
	protected WMSMapLayer[] layers = null;
	protected boolean[] selected = null;
	
	public WMSLayerSelectionModel(DiskoMap map) throws IOException {
		this.map = map;
		initialize();
	}
	
	private void initialize() throws IOException {
		//System.out.println("WMSLayerSelectionModel initialize");		
		IMap focusMap = map.getActiveView().getFocusMap();
		for (int i = 0; i < focusMap.getLayerCount(); i++) {
			ILayer layer = focusMap.getLayer(i);
			if (layer instanceof WMSMapLayer){
				//System.out.println("WMSMapLayer navn: " + layer.getName());
				WMSMapLayer wmsLayer = (WMSMapLayer) layer;
				wmsLayers.add(wmsLayer);			
			}
		}
		//System.out.println("antall wms lag: " + wmsLayers.size());
		layers = new WMSMapLayer[wmsLayers.size()];
		selected = new boolean[wmsLayers.size()];
		for (int i = 0; i < selected.length; i++) {
			layers[i] = wmsLayers.get(i);
			selected[i] = wmsLayers.get(i).isVisible();
			//System.out.println("feiler her??");
		}
	}	
	
	public int getLayerCount() {
		return layers.length;
	}
	
	public WMSMapLayer getFeatureLayer(int i) {
		if (i < layers.length) {
			return layers[i];
		}
		return null;
	}
	
	public boolean isSelected(int i) {
		if (i < selected.length) {
			return selected[i];
		}
		return false;
	}
	
	public void setSelected(int i, boolean b) {
		if (i < selected.length) {
			selected[i] = b;
		}
	}
	
	public void setAllSelected(boolean b) {
		for (int i = 0; i < selected.length; i++) {
			selected[i] = b;
		}
	}
	
	public List getSelected() {
		ArrayList<WMSMapLayer> result = new ArrayList<WMSMapLayer>();
		for (int i = 0; i < layers.length; i++) {
			if (selected[i]) {
				result.add(layers[i]);
			}
		}
		return result;
	}
	
	/**
	 * Sets visibility, true/false, for given layer
	 * @param visible
	 * @param index
	 * @throws IOException
	 * @throws AutomationException
	 */
	public void setlayerVisibility(boolean visible, int index) 
		throws IOException, AutomationException{
			//System.out.println("setLayerVisibility");
			WMSMapLayer wmslayer = layers[index];			
			wmslayer.setVisible(visible);
			map.refresh(esriViewDrawPhase.esriViewGeography, wmslayer, map.getExtent());
	}		
	
	/**
	 * Loops through list of layers and sets all visible true/false
	 * @param visible
	 * @throws IOException
	 * @throws AutomationException
	 */
	public void setAllLayerVisibility(boolean visible) 
		throws IOException, AutomationException{
			//System.out.println("setLayerVisibility");
			for (int i = 0; i < layers.length; i++){
				WMSMapLayer flayer = layers[i];			
				flayer.setVisible(visible);
				map.refresh(esriViewDrawPhase.esriViewGeography, flayer, map.getExtent());
			}
	}	
	
	/**
	 * Loops through vector of chosen layers and sets visibility true/false
	 * @param visible
	 * @param index
	 * @throws IOException
	 * @throws AutomationException
	 */
	public void setlayerVisibility(boolean visible, int[] index) 
		throws IOException, AutomationException{
			//System.out.println("setLayerVisibility");
			for (int i = 0; i < index.length; i++){
				int idx = index[i];
				WMSMapLayer flayer = layers[idx];			
				flayer.setVisible(visible);
				map.refresh(esriViewDrawPhase.esriViewGeography, flayer, map.getExtent());
			}
			
	}	
	
	
	
}
