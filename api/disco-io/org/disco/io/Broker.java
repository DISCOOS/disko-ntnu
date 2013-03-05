package org.disco.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.disco.io.event.EntityEvent;
import org.disco.io.event.IBrokerListener;
import org.disco.io.event.ISessionListener;
import org.disco.io.event.ProtocolEvent;

public abstract class Broker<E extends IEntity> implements IBroker<E> {

	protected Session session;
	protected Map<Object,E> stations = new HashMap<Object,E>(); 
	
	protected EventListenerList listeners = new EventListenerList();
	
	public Broker(Session session) {
		this.session = session;
	}

	public ISession getSession() {
		return session;
	}
	
	public E getEntity(Object cue) {
		return stations.get(cue);
	}
	
	public Collection<E> getAllEntities() {
		List<E> items = new ArrayList<E>();
		items.addAll(stations.values());
		return items;
	}

	public Collection<E> getKnownEntities() {
		List<E> items = new ArrayList<E>();
		for(E it : stations.values()) {
			if(it.isIdentified()) {
				items.addAll(stations.values());
			}			
		}
		return items;
	}	
	
	public Collection<E> getUnknownEntities() {
		List<E> items = new ArrayList<E>();
		for(E it : stations.values()) {
			if(!it.isIdentified()) {
				items.addAll(stations.values());
			}			
		}
		return items;
	}	
			
	public void addBrokerListener(IBrokerListener listener) {
		listeners.add(IBrokerListener.class, listener);		
	}

	public void removeBrokerListener(IBrokerListener listener) {
		listeners.remove(IBrokerListener.class, listener);				
	}
	
	public abstract Object getCue(IPacket packet);
		
	protected void fireOnReceive(ProtocolEvent e) {
		// forward?
		if(isInspected(e.getPacket())) {
			// intercept
			savePacket(getCue(e.getPacket()),e.getPacket());
			// forward
			session.fireOnReceive(e);
		}
	}

	protected void fireOnTransmit(ProtocolEvent e) {
		// forward?
		if(isInspected(e.getPacket())) {
			// intercept
			savePacket(getCue(e.getPacket()),e.getPacket());
			// forward
			session.fireOnTransmit(e);
		}
	}
	
    protected void fireOnEntityDetected(IEntity station) {
        
    	// create event
    	EntityEvent e = new EntityEvent(this,station);
    	
    	// get listeners
    	IBrokerListener[] list = listeners.getListeners(IBrokerListener.class);
    	
    	// notify listeners
    	for(IBrokerListener it:list) {
    		it.onEntityDetected(this, e);
    	}
    	
    }
	
	protected void savePacket(Object cue, IPacket packet) {
		// try to get station
		E station = stations.get(cue);
		// new station?
		if(station==null) {
			// create and add
			station = createStation(packet);
			stations.put(station.getCue(),station);
			// add packet to station
			station.addPacket(packet);
			// notify
			fireOnEntityDetected(station);
		}
		else {
			// add packet to station
			station.addPacket(packet);			
		}
	}
	
	protected abstract E createStation(IPacket packet);

	/* ================================================================
	 * ISession wrapper
	 * ================================================================ */
	
	public void addSessionListener(ISessionListener listener) {
		session.addSessionListener(listener);
	}

	public boolean close() throws Throwable {
		return session.close();
	}

	public IResponse execute(String command) {
		return session.execute(command);
	}

	public ILink getLink() {
		return session.getLink();
	}

	public String getMode() {
		return session.getMode();
	}

	public String getName() {
		return session.getName();
	}

	public IParser getParser() {
		return session.getParser();
	}

	public IProtocol getProtocol() {
		return session.getProtocol();
	}

	public String getType() {
		return session.getType();
	}

	public boolean isCommandMode() {
		return session.isCommandMode();
	}

	public boolean isOpen() {
		return session.isOpen();
	}

	public boolean isTransmitMode() {
		return session.isTransmitMode();
	}

	public void removeSessionListener(ISessionListener listener) {
		session.removeSessionListener(listener);
	}

	public String setCommandMode() {
		return session.setCommandMode();
	}

	public String setTransmitMode() {
		return session.setTransmitMode();
	}

	public boolean transmit(String message) {
		return session.transmit(message);
	}

}
