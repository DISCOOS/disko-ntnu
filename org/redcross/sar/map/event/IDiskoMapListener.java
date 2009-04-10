package org.redcross.sar.map.event;

import java.util.EventListener;

public interface IDiskoMapListener extends EventListener {

	public void onMouseClick(DiskoMapEvent e);
	public void onMouseMove(DiskoMapEvent e);
	public void onExtentChanged(DiskoMapEvent e);
	public void onMapReplaced(DiskoMapEvent e);
	public void onSelectionChanged(DiskoMapEvent e);


}
