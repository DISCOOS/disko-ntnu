package org.redcross.sar.map.event;

import java.util.EventListener;
import java.util.List;

import org.redcross.sar.map.layer.IMapLayer;


public interface IMapDataListener extends EventListener {

	@SuppressWarnings("unchecked")
	public void onDataChanged(List<IMapLayer> layers);

}
