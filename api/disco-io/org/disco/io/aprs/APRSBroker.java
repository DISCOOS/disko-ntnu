package org.disco.io.aprs;

import java.util.HashMap;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.disco.io.Broker;
import org.disco.io.IPacket;
import org.disco.io.ISession;
import org.disco.io.Session;
import org.disco.io.event.IBrokerListener;

public class APRSBroker extends Broker<APRSStation> implements ISession {

	protected EventListenerList listeners = new EventListenerList();
	protected Map<String,APRSStation> stations = new HashMap<String,APRSStation>(); 
	
	public static final String APRS_IS_LOGIN = "\ruser %1$s pass %2$s filter %3$s\n";	
	
	public APRSBroker(Session session) {
		super(session);
	}
	
	public void addBrokerListener(IBrokerListener listener) {
		listeners.add(IBrokerListener.class, listener);		
	}

	public void removeBrokerListener(IBrokerListener listener) {
		listeners.remove(IBrokerListener.class, listener);				
	}

	@Override
	protected APRSStation createStation(IPacket packet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getCue(IPacket packet) {
		if(isInspected(packet)) {
			APRSPacket aprs = (APRSPacket)packet;
			return aprs.getCue();
			
		}
		return null;
	}

	@Override
	public boolean isInspected(IPacket packet) {
		// TODO Auto-generated method stub
		return false;
	}
		

		

}
