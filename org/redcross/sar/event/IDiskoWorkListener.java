package org.redcross.sar.event;

import java.util.EventListener;

public interface IDiskoWorkListener extends EventListener {
	
	public void onWorkPerformed(DiskoWorkEvent e);
	
}
