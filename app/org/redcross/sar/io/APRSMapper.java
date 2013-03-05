package org.redcross.sar.io;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
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
import org.redcross.sar.AbstractService;
import org.redcross.sar.Application;
import org.redcross.sar.data.IDataSource;
import org.redcross.sar.data.event.ISourceListener;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.event.MsoEvent.ChangeList;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.IWorkLoop;

public class APRSMapper extends AbstractService implements IIOMapper<APRSBroker> {
	
	/** The bound MSO source */
	private IMsoModelIf m_model;
	
	/** local event manager for ISourceListener and IManagerListener */
	private final EventManager listener = new EventManager();
	
	/** shared reference to the IO manager singleton */
	private static final IOManager m_io = IOManager.getInstance();
	
	/** shared reference to the class logger singleton */
	private static final Logger logger = Logger.getLogger(APRSMapper.class);
	
	/** The work on the work loop */
	protected final LoopWork work;
	
	/** queue of events pending*/
	protected final ConcurrentLinkedQueue<QueuedEvent> m_queue = new ConcurrentLinkedQueue<QueuedEvent>();
	
	/*========================================================
  	 * Constructors
  	 *========================================================*/

  	public APRSMapper(String oprID) throws Exception {
  		
  		// forward
  		super(oprID, 1000, 0.1);
  		
  		// prepare
		work = new LoopWork(500);
		
		// schedule work on loop
		m_workLoop.schedule(work);
		
		// connect to MSO model?
		IMsoModelIf model = Application.getInstance().getMsoModel();
		if(oprID.equalsIgnoreCase(model.getID())) {
			if(!connect(Application.getInstance().getMsoModel())) {
	  			throw new IllegalArgumentException("Could not connect to " + model);				
			}
		}
  	}

	/*========================================================
  	 * IIOMapper implementation
  	 *========================================================*/
  
	/*========================================================
  	 * IService implementation
  	 *========================================================*/
	
	public boolean init() {
		// TODO: 
 		return false;
	}
  	
	/*========================================================
  	 * Protected methods
  	 *========================================================*/
	
	protected boolean doConnect(IDataSource<?> source) {
		// allowed?
		if(source instanceof IMsoModelIf) {

			// prepare
			m_model = (IMsoModelIf)source;

			// add listeners
			m_io.addManagerListener(listener);
			m_model.addSourceListener(listener);

			// finished
			return true;
		}
		return false;
	}

	protected boolean doDisconnect() {
		// allowed?
		if(m_model!=null) {
			// remove listener
			m_io.removeManagerListener(listener);
			m_model.removeSourceListener(listener);
			// cleanup
			m_model = null;
			// finished
			return true;
		}
		return false;
	}
	
	/*========================================================
  	 * private classes
  	 *========================================================*/
  	
	private class QueuedEvent {
		Object[] args;
		EventObject event;
		public QueuedEvent(EventObject event, Object... args) {
			this.event = event;
			int size = args.length;
			if(size>0) {
				this.args= new Object[size];
				for(int i=0;i<size;i++)
					this.args[i] = args[i];
			}
		}
	}
	
	
  	private class EventManager implements ISourceListener<ChangeList>, IManagerListener {
  		
  		public void onOpen(SessionEvent e) { 
  			if(e.getSource() instanceof APRSBroker) {
  				logger.info("Session " + e.getSource().getName() + " opened");
  			}
  		}
		public void onClose(SessionEvent e) {
  			if(e.getSource() instanceof APRSBroker) {
  				logger.info("Session " + e.getSource().getName() + " closed");
  			}
		}
		
  		public void onSourceChanged(SourceEvent<ChangeList> e) {
  			if(e.getData().isClearAllEvent()) {
  				m_queue.add(new QueuedEvent(e));
  			}
		}
  		
		public void onReceive(ISession session, ProtocolEvent e) {			
  			m_queue.add(new QueuedEvent(e,session));
		}
		
		public void onEntityDetected(IBroker<?> broker, EntityEvent e) {
  			m_queue.add(new QueuedEvent(e,broker));
		}  		
		
		public void onSessionAdded(ISession session) { /*NOP*/ }
		public void onSessionRemoved(ISession session) { /*NOP*/ }
		public void onCurrentSessionChanged(ISession session) { /* NOP */ }
		public void onTransmit(ISession session, ProtocolEvent e) { /*NOP*/ }
		public void onBufferOverflow(ISession session, ProtocolEvent e) { /* NOP */}
  	}
  	
  	/* =========================================================================
	 * Inner classes
	 * ========================================================================= */

    private class LoopWork extends AbstractWork {

    	private long m_requestedWorkTime;

		public LoopWork(long requestedWorkTime) throws Exception {
			// forward
			super(0,true,false,WorkerType.UNSAFE,"",0,false,false,true);
			// prepare
			m_requestedWorkTime = requestedWorkTime;
		}

		/* ============================================================
		 * IDiskoWork implementation
		 * ============================================================ */

		public Void doWork(IWorkLoop loop) {

			/* =============================================================
			 * DESCRIPTION: This method is listening for events received
			 * from IO Manager and MSO Model.
			 * 
			 * A concurrent FIFO queue is used to store events 
			 * between work cycles.

			 * ALGORITHM: The following algorithm is implemented
			 * 1. Get queued events
			 * 2. Dispatch events to appropriate handlers
			 * 3. Execute actions as necessary.
			 *
			 * DUTY CYCLE MANAGEMENT: TIMEOUT is the cutoff work duty
			 * cycle time. The actual duty cycle time may become longer,
			 * but not longer than a started update or execute step.
			 * update() is only allowed to exceed TIMEOUT/2. The
			 * remaining time is given to execute(). This ensures that
			 * update() will not starve execute() during long update
			 * sequences.
			 *
			 * ============================================================= */

			// get start tic
			long tic = System.currentTimeMillis();

			// calculate timeout
			long workTime = Math.min(loop.getWorkTime(), m_requestedWorkTime);
			
			// handle updates in queue
			List<QueuedEvent> changed = update(tic,workTime/2);

			// forward
			execute(changed,tic,workTime/2);

			// finished
			return null;

		}

		/* ============================================================
		 * Helper methods
		 * ============================================================ */

		private List<QueuedEvent> update(long tic, long timeOut) {

			// initialize
			List<QueuedEvent> workSet = new ArrayList<QueuedEvent>(m_queue.size());

			// loop over all updates
			while(m_queue.peek()!=null) {

				// ensure that half MAX_WORK_TIME is only exceeded once?
				if(tic>0 && System.currentTimeMillis()-tic>timeOut)
					break;

				// get next update event
				QueuedEvent e = m_queue.poll();

				// add to work set?
				if(e!=null && !workSet.contains(e)) {
					workSet.add(e);
				}

			}

			// finished
			return workSet;

		}
		
		
		@SuppressWarnings("unchecked")
		private void execute(List<QueuedEvent> events, long tic, long timeOut) {
			// dispatch
			for(QueuedEvent it : events) {
				EventObject e = it.event;
				if(e instanceof ProtocolEvent && ((ProtocolEvent)e).isRX()) {
						onReceive((ISession)it.args[0], (ProtocolEvent)e);
				}
				else if(e instanceof EntityEvent) {
					onEntityDetected((IBroker<?>)it.args[0],(EntityEvent)e);
				}
				else if(e instanceof SourceEvent) {
					onSourceChanged((SourceEvent<ChangeList>)e);
				}
			}
		}
		
		private void onSourceChanged(SourceEvent<ChangeList> e) {
			/* If a clear-all event has occurred, the MSO model
			 * that this APRS mapper is mapping entities to, is
			 * no longer valid. Hence, all entities identified so 
			 * fare are by definition void, and therefore must be
			 * reset to null */
			if(e.getData().isClearAllEvent()) {
				// loop over all identities
				for(IBroker<?> broker : m_io.getBrokers()) {
					for(IEntity it : broker.getKnownEntities()) {
						it.setIdentity(null);
					}
				}
			}
		}
		
		private void onReceive(ISession session, ProtocolEvent e) {
			// only parse APRS packets...
			if(e.getPacket() instanceof APRSPacket) {
				// cast to APRS name space
				APRSPacket packet = (APRSPacket)e.getPacket();
				// parse packet type
				if(packet.isPositionSet()) {
					setPosition((APRSBroker)session,packet);
				} else {
					logger.info("RX: " + e.getPacket().getMessage());
				}
			} else {
				logger.info("RX: " + e.getPacket().getMessage());
			}
		}
		
		private void onEntityDetected(IBroker<?> broker, EntityEvent e) {
			// try to identify entity
			identify(e.getEntity());
		}  		
		
		private void setPosition(APRSBroker broker, APRSPacket packet) {
			// get command post
			ICmdPostIf cmdPost = m_model.getMsoManager().getCmdPost();
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
					// cast entity to MSO name space
					String objID = (String)station.getIdentity();
					// get unit
					IUnitIf unit = cmdPost.getUnitList().getObject(objID);
					// found?
					if(unit!=null) {
						unit.suspendChange();
						unit.logPosition(p, packet.getTime());
						unit.resumeChange(true);
					} else {
						logger.warn("APRSStation " + station.getCue() + " is identified but not found in unit list");
					}
				}
				else {
					logger.info("TODO: Update position of " +
							"unknown station in map (" + 
							entity.getCue() + " changed position)");
				}
			} else {
				logger.warn("Entity " + entity.getCue() + " detected but not found in Broker");
			}			
		}
		
		private boolean identify(IEntity e) {
			// only parse APRS stations...
			if(e instanceof APRSStation) {
				// get station identity cue 
				String cue = (String)e.getCue();
				// has cue?
				if(cue!=null) {
					// loop over all units
					for(Object it : m_model.getItems(IUnitIf.class)) {
						IUnitIf unit = (IUnitIf)it;
						if(isUnit(unit,cue)) {
							// unit is identified
							e.setIdentity(unit.getObjectId());
							// log
							logger.info("Entity " + e.getCue() 
									+ " was identified as " 
									+ MsoUtils.getUnitName(unit, false));
							// success
							return true;
						}
					}
				}
			}			
			// log
			logger.info("Entity " + e.getCue() + " was not identified");
			// failed
			return false;
		}		
		
		private boolean isUnit(IUnitIf unit, String cue) {
			return cue.equalsIgnoreCase(unit.getTrackingID()) 
				|| cue.endsWith(unit.getToneID())
				|| cue.endsWith(unit.getCallSign());			
		}

    }

}
