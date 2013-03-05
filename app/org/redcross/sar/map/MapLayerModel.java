package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.esri.arcgis.carto.GroupLayer;
import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IMap;

public class MapLayerModel extends LayerModel {

	private List<ILayer> mapLayers = new ArrayList<ILayer>();	
	
	protected MapLayerModel(DiskoMap map) throws IOException {
		super(map);
		initialize();
	}
	
	public String getName() {
		return "map";
	}
	
	private void initialize() throws IOException {
				
		IMap focusMap = map.getMapImpl().getActiveView().getFocusMap();
		
		for (int i = 0; i < focusMap.getLayerCount(); i++) {
			ILayer l = focusMap.getLayer(i);
			if(l instanceof IFeatureLayer)
				mapLayers.add(l);
			if(l instanceof GroupLayer) {
				String name = l.getName();
				// is not the MSO group layer?
				if(!name.startsWith("MSO_")) {
					// is a map base layer?
					if(name.startsWith("MAP_")) {
						// get base name
						String base = map.getMapBase()!=null ? map.getMapBase().getName(): null;
						// is current map base?
						if(name.equals(base))
							mapLayers.add(l);
					}
					else
						mapLayers.add(l);
				}
			}
		}
		
		// initialize array
		layers = new ILayer[mapLayers.size()];
		included = new boolean[mapLayers.size()];
		for (int i = 0; i < layers.length; i++) {
			layers[i] = mapLayers.get(i);
			included[i] = true;				
		}
		
	}
}
