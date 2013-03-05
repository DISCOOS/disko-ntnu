package org.disco.io.event;

import java.util.EventListener;

import org.disco.io.ISession;

public interface ISessionListener extends EventListener {

	public void onOpen(SessionEvent e);
	public void onClose(SessionEvent e);
	public void onReceive(ISession session, ProtocolEvent e);
	public void onTransmit(ISession session, ProtocolEvent e);
	public void onBufferOverflow(ISession session, ProtocolEvent e);
	
}
