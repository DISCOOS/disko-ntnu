package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.redcross.sar.map.layer.IMsoFeatureLayer;

import com.esri.arcgis.carto.GroupLayer;
import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IWMSLayer;
import com.esri.arcgis.carto.IWMSMapLayer;
import com.esri.arcgis.interop.AutomationException;

public abstract class LayerModel implements IMapLayerModel<ILayer> {

	protected DiskoMap map;
	protected ILayer[] layers;
	protected boolean[] included;
	
	public abstract String getName();
	
	protected static final Logger logger = Logger.getLogger(LayerModel.class);

	protected LayerModel(DiskoMap map) throws IOException {
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

	public boolean isIncluded(int i) {
		if (i < included.length) {
			return included[i];
		}
		return false;
	}

	public void setIncluded(int i, boolean isIncluded) {
		if (i < included.length) {
			included[i] = isIncluded;
			if(!isIncluded) {
				try {
					layers[i].setVisible(false);
				} catch (AutomationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void setIncluded(int[] index, boolean isIncluded) {
			for (int i = 0; i < index.length; i++) {
				int idx = index[i];
				setIncluded(idx, isIncluded);
			}
	}

	public void setAllSelectable(boolean isIncluded) {
		for (int i = 0; i < included.length; i++) {
			setIncluded(i, isIncluded);
		}
	}

	public List<ILayer> getIncluded() {
		List<ILayer> result = new ArrayList<ILayer>();
		for (int i = 0; i < layers.length; i++) {
			if (included[i]) {
				result.add(layers[i]);
			}
		}
		return result;
	}

	/**
	 * Sets visibility, true/false, for given layer
	 * @param isVisible
	 * @param index
	 */
	public void setVisible(boolean isVisible, int index) {
		try {
			layers[index].setVisible(isVisible);
		} catch (Exception e) {
			logger.error("Invocation of setVisible() failed",e);
		}
	}

	/**
	 * Loops through list of layers and sets all visible true/false
	 * @param isVisible
	 */
	public void setAllVisible(boolean isVisible) {
		for (int i = 0; i < layers.length; i++){
			setVisible(isVisible,i);
		}
	}

	/**
	 * Loops through vector of chosen layers and sets visibility true/false
	 * @param isVisible
	 * @param index
	 */
	public void setVisible(boolean isVisible, int[] index) {
			for (int i = 0; i < index.length; i++){
				setVisible(isVisible,index[i]);
			}
	}

	/**
	 * Make included layers equal to visible layers
	 */
	public void synchronize() {
		try {
			if(layers!=null) {
				for(int i=0;i<layers.length;i++) {
					included[i] = layers[i].isVisible();
				}
			}
		} catch (Exception e) {
			logger.error("Invocation of isVisible() failed",e);
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
