package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.interop.AutomationException;

public abstract class FeatureLayerSelectionModel {
	
	protected DiskoMap map = null;
	protected IFeatureLayer[] layers = null;
	protected boolean[] selected = null;
	
	public FeatureLayerSelectionModel(DiskoMap map) throws IOException {
		this.map = map;
	}
	
	public int getLayerCount() {
		return layers.length;
	}
	
	public IFeatureLayer getFeatureLayer(int i) {
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
		ArrayList<IFeatureLayer> result = new ArrayList<IFeatureLayer>();
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
	public void setLayerVisibility(boolean visible, int index) 
		throws IOException, AutomationException{
			//System.out.println("setLayerVisibility");
			IFeatureLayer flayer = layers[index];			
			flayer.setVisible(visible);
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
				IFeatureLayer flayer = layers[i];			
				flayer.setVisible(visible);
			}
	}	
	
	/**
	 * Loops through vector of chosen layers and sets visibility true/false
	 * @param visible
	 * @param index
	 * @throws IOException
	 * @throws AutomationException
	 */
	public void setLayerVisibility(boolean visible, int[] index) 
		throws IOException, AutomationException{
			//System.out.println("setLayerVisibility");
			for (int i = 0; i < index.length; i++){
				int idx = index[i];
				IFeatureLayer flayer = layers[idx];			
				flayer.setVisible(visible);
			}
	}	
	
}
