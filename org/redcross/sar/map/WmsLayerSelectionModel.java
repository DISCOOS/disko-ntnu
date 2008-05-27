package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;

import com.esri.arcgis.carto.GroupLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IMap;
import com.esri.arcgis.carto.IWMSLayer;
import com.esri.arcgis.carto.IWMSMapLayer;

public class WmsLayerSelectionModel extends LayerSelectionModel {
	
	private ArrayList<ILayer> wmsLayers = new ArrayList<ILayer>();
	
	public WmsLayerSelectionModel(DiskoMap map) throws IOException {
		super(map);
		initialize();
	}
	
	public String getName() {
		return "wms";
	}
	
	private void initialize() throws IOException {
		IMap focusMap = map.getActiveView().getFocusMap();
		for (int i = 0; i < focusMap.getLayerCount(); i++) {
			ILayer l = focusMap.getLayer(i);
			if(l instanceof IWMSLayer || l instanceof IWMSMapLayer)
				wmsLayers.add(l);
			else if(l instanceof GroupLayer) {
				String name = l.getName();
				// is valid layer?
				if(!(name.startsWith("MSO_") || name.startsWith("MAP_")))
					wmsLayers.add(l);
			}
		}
		layers = new ILayer[wmsLayers.size()];
		selected = new boolean[wmsLayers.size()];
		for (int i = 0; i < selected.length; i++) {
			layers[i] = wmsLayers.get(i);
			selected[i] = wmsLayers.get(i).isVisible();
		}
	}	
	
}
