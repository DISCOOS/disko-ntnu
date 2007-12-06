package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;

import org.redcross.sar.map.layer.IMsoFeatureLayer;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IMap;

public class MsoLayerSelectionModel extends FeatureLayerSelectionModel {
	
	private ArrayList<IFeatureLayer> msoLayers = new ArrayList<IFeatureLayer>();
	
	public MsoLayerSelectionModel(DiskoMap map) throws IOException {
		super(map);
		initialize();
	}
	
	private void initialize() throws IOException {
		//System.out.println("MsoLayerSelectionModel: initialize()");		
		IMap focusMap = map.getActiveView().getFocusMap();
		for (int i = 0; i < focusMap.getLayerCount(); i++) {
			ILayer layer = focusMap.getLayer(i);		
			if (layer instanceof IMsoFeatureLayer){
				//System.out.println("IMsoFeatureLayer navn: " + layer.getName());	
				IMsoFeatureLayer msoLayer = (IMsoFeatureLayer) layer;
				msoLayers.add(msoLayer);
			}			
		}
		layers = new IFeatureLayer[msoLayers.size()];
		selected = new boolean[msoLayers.size()];
		for (int i = 0; i < selected.length; i++) {
			layers[i] = msoLayers.get(i);
			selected[i] = true;
		}
	}
	
}
