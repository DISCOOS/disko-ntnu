package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.OperationAreaMaskLayer;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.IMap;
import com.esri.arcgis.interop.AutomationException;

public class MsoLayerModel extends LayerModel {

	private List<IMsoFeatureLayer> msoLayers = new ArrayList<IMsoFeatureLayer>();

	protected MsoLayerModel(DiskoMap map) throws IOException {
		super(map);
		initialize();
	}

	public String getName() {
		return "mso";
	}

	public IMsoFeatureLayer getLayer(int index) {
		return (IMsoFeatureLayer)super.getLayer(index);
	}

	public boolean isSelectable(int i) {
		try {
			return msoLayers.get(i).isSelectable();
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public void setSelectable(int i, boolean isSelectable) {
		try {
			msoLayers.get(i).setSelectable(isSelectable);
			if(!isSelectable) {
				msoLayers.get(i).clearSelected();
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setSelectable(int[] index, boolean isSelectable)
		throws IOException, AutomationException{
			for (int i = 0; i < index.length; i++) {
				int idx = index[i];
				setSelectable(idx, isSelectable);
			}
	}

	public void setAllSelectable(boolean isSelectable) {
		for (int i = 0; i < included.length; i++) {
			setSelectable(i, isSelectable);
		}
	}

	public List<IMsoFeatureLayer> getSelectable() {
		List<IMsoFeatureLayer> result = new ArrayList<IMsoFeatureLayer>();
		for (IMsoFeatureLayer it : msoLayers) {
			try {
				if (it.isSelectable()) {
					result.add(it);
				}
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}

	private void initialize() throws IOException {

		IMap focusMap = map.getMapImpl().getActiveView().getFocusMap();
		for (int i = 0; i < focusMap.getLayerCount(); i++) {
			addMsoFeatureLayer(msoLayers,focusMap.getLayer(i),true);
		}

		layers = new IFeatureLayer[msoLayers.size()];
		included = new boolean[msoLayers.size()];
		for (int i = 0; i < included.length; i++) {
			// set layer
			layers[i] = msoLayers.get(i);
			// operation layers is by default not selected
			if(layers[i] instanceof OperationAreaMaskLayer)
				included[i] = false;
			else
				included[i] = true;
		}
	}

}
