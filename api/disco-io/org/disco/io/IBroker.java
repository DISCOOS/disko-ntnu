package org.disco.io;

import java.util.Collection;

import org.disco.io.event.IBrokerListener;

public interface IBroker<E extends IEntity> extends ISession {
	
	public E getEntity(Object cue);
	public Collection<E> getAllEntities();
	public Collection<E> getKnownEntities();
	public Collection<E> getUnknownEntities();
	public void addBrokerListener(IBrokerListener listener);
	public void removeBrokerListener(IBrokerListener listener);
	
	public boolean isInspected(IPacket packet);
	public Object getCue(IPacket packet);
	
	public ISession getSession();

}
