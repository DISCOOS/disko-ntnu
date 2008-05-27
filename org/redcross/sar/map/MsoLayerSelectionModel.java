package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;

import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.OperationAreaMaskLayer;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.IMap;

public class MsoLayerSelectionModel extends LayerSelectionModel {
	
	private ArrayList<IMsoFeatureLayer> msoLayers = new ArrayList<IMsoFeatureLayer>();
	
	public MsoLayerSelectionModel(DiskoMap map) throws IOException {
		super(map);
		initialize();
	}
	
	public String getName() {
		return "mso";
	}
	
	private void initialize() throws IOException {
		
		IMap focusMap = map.getActiveView().getFocusMap();
		for (int i = 0; i < focusMap.getLayerCount(); i++) {
			addMsoFeatureLayer(msoLayers,focusMap.getLayer(i),true);
		}
		
		layers = new IFeatureLayer[msoLayers.size()];
		selected = new boolean[msoLayers.size()];
		for (int i = 0; i < selected.length; i++) {
			// set layer
			layers[i] = msoLayers.get(i);
			// operation layers is by default not selected
			if(layers[i] instanceof OperationAreaMaskLayer)
				selected[i] = false;
			else
				selected[i] = true;				
		}
	}
	
}
