package org.disco.io.event;

import java.util.EventListener;

public interface IProtocolListener extends EventListener {

	public void onReceive(ProtocolEvent e);
	public void onTransmit(ProtocolEvent e);
	public void onBufferOverflow(ProtocolEvent e);
	
}
