package org.redcross.sar.map.event;

import java.util.EventListener;

public interface IMapLayerEventListener extends EventListener {

	public void onSelectionChanged(MapLayerEvent e);

}
