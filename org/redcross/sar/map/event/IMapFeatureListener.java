package org.redcross.sar.map.event;

import java.util.EventListener;

import javax.swing.event.ChangeEvent;

public interface IMapFeatureListener extends EventListener {

	public void onFeatureChanged(ChangeEvent e);

}
