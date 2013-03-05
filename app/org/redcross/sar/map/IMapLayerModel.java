package org.redcross.sar.map;

import java.util.List;

public interface IMapLayerModel<I> {

	public String getName();

	public int findIndex(I l);

	public int getLayerCount();

	public I getLayer(int i);

	public boolean isIncluded(int i);
	
	public void setIncluded(int i, boolean isIncluded);

	public void setIncluded(int[] index, boolean isIncluded);

	public void setAllSelectable(boolean isIncluded);

	public List<I> getIncluded();

	/**
	 * Sets visibility, true/false, for given layer
	 * @param isVisible
	 * @param index
	 */
	public void setVisible(boolean isVisible, int index);
	
	/**
	 * Loops through list of layers and sets all visible true/false
	 * @param isVisible
	 */
	public void setAllVisible(boolean isVisible);
	
	/**
	 * Loops through vector of chosen layers and sets visibility true/false
	 * @param isVisible
	 * @param index
	 */
	public void setVisible(boolean isVisible, int[] index);
	
	/**
	 * Make included layers equal to visible layers
	 */
	public void synchronize();
	
}
