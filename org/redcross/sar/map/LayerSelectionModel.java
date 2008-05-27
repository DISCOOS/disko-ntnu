package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.redcross.sar.map.layer.IMsoFeatureLayer;

import com.esri.arcgis.carto.GroupLayer;
import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IWMSLayer;
import com.esri.arcgis.carto.IWMSMapLayer;
import com.esri.arcgis.interop.AutomationException;

public abstract class LayerSelectionModel {
	
	protected DiskoMap map = null;
	protected ILayer[] layers = null;
	protected boolean[] selected = null;
	
	public abstract String getName();
	
	public LayerSelectionModel(DiskoMap map) throws IOException {
		this.map = map;
	}
	
	public int findIndex(ILayer l) {
		for(int i=0;i<layers.length;i++) {
			if(layers[i]==l)
				return i;
		}
		return -1;
	}
	
	public int getLayerCount() {
		return layers.length;
	}
	
	public ILayer getLayer(int i) {
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
		List<ILayer> result = new ArrayList<ILayer>();
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
	public void setVisible(boolean visible, int index) 
		throws IOException, AutomationException{
			layers[index].setVisible(visible);
	}		
	
	/**
	 * Loops through list of layers and sets all visible true/false
	 * @param visible
	 * @throws IOException
	 * @throws AutomationException
	 */
	public void setAllVisible(boolean visible) 
		throws IOException, AutomationException{
			//System.out.println("setLayerVisibility");
			for (int i = 0; i < layers.length; i++){
				layers[i].setVisible(visible);
			}
	}	
	
	/**
	 * Loops through vector of chosen layers and sets visibility true/false
	 * @param visible
	 * @param index
	 * @throws IOException
	 * @throws AutomationException
	 */
	public void setVisible(boolean visible, int[] index) 
		throws IOException, AutomationException{
			for (int i = 0; i < index.length; i++){
				int idx = index[i];
				layers[idx].setVisible(visible);
			}
	}	
	
	public void update() {
		try {
			if(layers!=null) {
				for(int i=0;i<layers.length;i++) {
					selected[i] = layers[i].isVisible();
				}
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void addGroupLayer(List<GroupLayer> list, ILayer l) throws AutomationException, IOException {
		if (l instanceof GroupLayer)
			list.add((GroupLayer)l);
	}
	
	protected void addFeatureLayer(List<IFeatureLayer> list, ILayer l, boolean recurse) throws AutomationException, IOException {
		if (l instanceof IFeatureLayer)
			list.add((IFeatureLayer)l);
		else if(recurse && l instanceof GroupLayer) {
			// cast to group layer
			GroupLayer g = (GroupLayer)l;
			// loop over all layers
			for (int i = 0; i < g.getCount(); i++) {
				addFeatureLayer(list, g.getLayer(i),recurse);
			}			
		}
	}
	
	protected void addMsoFeatureLayer(List<IMsoFeatureLayer> list, ILayer l, boolean recurse) throws AutomationException, IOException {
		if (l instanceof IMsoFeatureLayer)
			list.add((IMsoFeatureLayer)l);
		else if(recurse && l instanceof GroupLayer) {
			// cast to group layer
			GroupLayer g = (GroupLayer)l;
			// loop over all layers
			for (int i = 0; i < g.getCount(); i++) {
				addMsoFeatureLayer(list, g.getLayer(i),recurse);
			}			
		}
	}
	
	protected void addWmsLayer(List<ILayer> list, ILayer l, boolean recurse) throws AutomationException, IOException {
		if (l instanceof IWMSLayer || l instanceof IWMSMapLayer)
			list.add(l);
		else if(recurse && l instanceof GroupLayer) {
			// cast to group layer
			GroupLayer g = (GroupLayer)l;
			// loop over all layers
			for (int i = 0; i < g.getCount(); i++) {
				addWmsLayer(list, g.getLayer(i),recurse);
			}			
		}
	}
	
}
