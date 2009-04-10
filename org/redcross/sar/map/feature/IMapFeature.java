package org.redcross.sar.map.feature;

import org.redcross.sar.map.event.IMapFeatureListener;

public interface IMapFeature {

	public boolean isVisible();
	public void setVisible(boolean isVisible);

	public void addMapFeatureListener(IMapFeatureListener listener);
	public void removeMapFeatureListener(IMapFeatureListener listener);

}
