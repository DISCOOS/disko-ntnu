package org.redcross.sar.map.event;

import java.util.EventListener;


public interface IMapElementListener extends EventListener {

	public void onElementChanged(MapElementEvent e);

}
