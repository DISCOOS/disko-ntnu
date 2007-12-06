package org.redcross.sar.event;

import java.util.EventListener;

public interface IDiskoWorkEventListener extends EventListener {
	
	public void onWorkChange(DiskoWorkEvent e);
	
	public void onWorkCancel(DiskoWorkEvent e);

	public void onWorkFinish(DiskoWorkEvent e);
	
}
