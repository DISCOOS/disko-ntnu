package org.redcross.sar.io;

import org.disco.io.IBroker;
import org.disco.io.IEntity;
import org.disco.io.IOManager;
import org.disco.io.ISession;
import org.disco.io.aprs.APRSBroker;
import org.disco.io.aprs.APRSPacket;
import org.disco.io.aprs.APRSStation;
import org.disco.io.event.EntityEvent;
import org.disco.io.event.IManagerListener;
import org.disco.io.event.ProtocolEvent;
import org.disco.io.event.SessionEvent;
import org.redcross.sar.data.event.ISourceListener;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;
import org.redcross.sar.util.mso.Position;

public class APRSMapper {
	
	private static IOManager m_io;
	private static IMsoModelIf m_mso;
	private static final EventManager listener = new EventManager();
	
	/*========================================================
  	 * The singleton code
  	 *========================================================*/

	private static APRSMapper m_this;
	
	/**
	 * Get singleton instance of class
	 *
	 * @return Returns singleton instance of class
	 */
  	public static synchronized APRSMapper getInstance() {
  		if (m_this == null) {
  			// it's ok, we can call this constructor
  			m_this = new APRSMapper();
  			// instances
  			m_io = IOManager.getInstance();
  			m_mso = MsoModelImpl.getInstance();
  			// add listeners
  			m_io.addManagerListener(listener);
  			m_mso.addSourceListener(listener);
  		}
  		return m_this;
  	}

	/**
	 * Method overridden to protect singleton
	 *
	 * @throws CloneNotSupportedException
	 * @return Returns nothing. Method overridden to protect singleton
	 */
  	public Object clone() throws CloneNotSupportedException{
  		throw new CloneNotSupportedException();
  		// that'll teach 'em
  	}
  	
	/*========================================================
  	 * private classes
  	 *========================================================*/
  	
  	private static class EventManager implements ISourceListener<UpdateList>, IManagerListener {
  		
  		public void onSourceChanged(SourceEvent<UpdateList> e) {
			if(e.getInformation().isClearAllEvent()) {
				// loop over all identities
				for(IBroker<?> broker : m_io.getBrokers()) {
					for(IEntity it : broker.getKnownEntities()) {
						it.setIdentity(null);
					}
				}
			}
		}
  		
  		public void onOpen(SessionEvent e) { /*NOP*/ }
		public void onClose(SessionEvent e) { /*NOP*/ }
		public void onSessionAdded(ISession session) { /*NOP*/ }
		public void onSessionRemoved(ISession session) { /*NOP*/ }
		public void onTransmit(ISession session, ProtocolEvent e) { /*NOP*/ }

		public void onReceive(ISession session, ProtocolEvent e) {
			// only parse APRS packets...
			if(e.getPacket() instanceof APRSPacket) {
				// cast to APRS name space
				APRSBroker broker = (APRSBroker)session;
				APRSPacket packet = (APRSPacket)e.getPacket();
				// parse packet type
				if(packet.isPositionSet()) {
					// get command post
					ICmdPostIf cmdPost = m_mso.getMsoManager().getCmdPost();
					// create position
					Position p = new Position("WPT",packet.getPosition().getPosition());
					// Search for station 
					IEntity entity = broker.getEntity(packet.getSSID());
					// found station?
					if(entity!=null) {
						// cast to APRSStation
						APRSStation station = (APRSStation)entity;
						// is identified or retry identification?
						if(entity.isIdentified() || identify(entity)) {
							// cast entity to MSO namespace
							String objID = (String)station.getIdentity();
							// get unit
							IUnitIf unit = cmdPost.getUnitList().getItem(objID);
							// found?
							if(unit!=null) {
								unit.suspendClientUpdate();
								unit.logPosition(p, packet.getTime());
								unit.resumeClientUpdate(true);
							}
						}
						else {
							// TODO: Show i map
							System.out.println("entity is still not known");
						}
					}
				}
			}
		}
		
		public void onEntityDetected(IBroker<?> broker, EntityEvent e) {
			// try to identify entity
			identify(e.getEntity());
		}  		
		
		private boolean identify(IEntity e) {
			// only parse APRS stations...
			if(e instanceof APRSStation) {
				// get ssid of station
				String ssid = (String)e.getCue();
				// has ssid?
				if(ssid!=null) {
					// loop over all units
					for(Object it : m_mso.getItems(IUnitIf.class)) {
						IUnitIf unit = (IUnitIf)it;
						if(ssid.endsWith(unit.getToneID()) ||
								ssid.endsWith(unit.getCallSign())) {
							// unit is identified
							e.setIdentity(unit.getObjectId());
							// success
							return true;
						}
					}
				}
			}			
			// failed
			return false;
		}

		public void onCurrentSessionChanged(ISession session) { /* NOP */ }
		public void onBufferOverflow(ISession session, ProtocolEvent e) { /* NOP */}
  	}
  
}
