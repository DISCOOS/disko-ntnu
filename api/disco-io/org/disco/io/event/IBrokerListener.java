package org.disco.io.event;

import java.util.EventListener;

import org.disco.io.IBroker;

public interface IBrokerListener extends EventListener {
	
	public void onEntityDetected(IBroker<?> broker, EntityEvent e);

}
