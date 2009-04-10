package org.redcross.sar.map.event;

import java.util.Collection;
import java.util.EventListener;

import org.redcross.sar.map.layer.IMsoFeatureLayer;

public interface IMapDataListener extends EventListener {

	public void onDataChanged(Collection<IMsoFeatureLayer> layers);

}
