package org.disco.io.event;

import java.util.EventListener;

import org.disco.io.IBroker;
import org.disco.io.ISession;

public interface IManagerListener extends EventListener {
	
	public void onOpen(SessionEvent e);
	public void onClose(SessionEvent e);
	public void onSessionAdded(ISession session);
	public void onSessionRemoved(ISession session);
	public void onReceive(ISession session, ProtocolEvent e);
	public void onTransmit(ISession session, ProtocolEvent e);
	public void onBufferOverflow(ISession session, ProtocolEvent e);
	public void onEntityDetected(IBroker<?> broker, EntityEvent e);
	public void onCurrentSessionChanged(ISession session);

}
