package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IMap;

import org.redcross.sar.map.layer.IMsoFeatureLayer;

public class DefaultMapLayerSelectionModel extends FeatureLayerSelectionModel {

	private ArrayList<IFeatureLayer> defaultMapLayers = new ArrayList<IFeatureLayer>();	
	
	//private Enumeration mso = new Enumeration();
	
	public DefaultMapLayerSelectionModel(DiskoMap map) throws IOException {
		super(map);
		initialize();
	}
	
	private void initialize() throws IOException {
				
		IMap focusMap = map.getActiveView().getFocusMap();
		//System.out.println("DefaultMapLayerSelectionModel initialize(): antall lag: " + focusMap.getLayerCount());
		for (int i = 0; i < focusMap.getLayerCount(); i++) {
			ILayer layer = focusMap.getLayer(i);
			if (layer instanceof IMsoFeatureLayer) {
				//System.out.println("IMsoFeatureLayer navn: " + layer.getName());
				continue;
			}
			else if (layer instanceof IFeatureLayer){
				//System.out.println("IFeatureLayerLayer navn: " + layer.getName());
				IFeatureLayer flayer = (IFeatureLayer) layer;			
				defaultMapLayers.add(flayer);
			}
			else{
				//System.out.println("wms? " + layer.getClass());
			}
		}
		//System.out.println("antall defaulte lag: " + defaultMapLayers.size());
		layers = new IFeatureLayer[defaultMapLayers.size()];
		selected = new boolean[defaultMapLayers.size()];
		for (int i = 0; i < selected.length; i++) {
			layers[i] = defaultMapLayers.get(i);
			selected[i] = true;
		}
	}
}
