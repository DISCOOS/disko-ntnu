package org.redcross.sar.ds;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.redcross.sar.Application;
import org.redcross.sar.data.IDataSource;
import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.IMsoTransactionManagerIf;
import org.redcross.sar.mso.IDispatcherIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoChangeListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Change;
import org.redcross.sar.mso.event.MsoEvent.ChangeList;
import org.redcross.sar.mso.work.AbstractMsoWork;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.WorkPool;
import org.redcross.sar.work.IWorkLoop.LoopState;

public abstract class AbstractDsMso<M extends IMsoObjectIf, T
		extends IDsObject> extends AbstractDs<M, T, MsoEvent.Change> {

	protected final Logger m_logger = Logger.getLogger(AbstractDsMso.class);
	
	/**
	 * Listen for Updates of assignments, routes and units
	 */
	protected final EnumSet<MsoClassCode> m_interests;

	/* ============================================================
	 * Declaration of local lists
	 * ============================================================
	 *
	 * This solves the problem of concurrent modification of lists
	 * in MSO model. MSO is not thread safe, and MSO is updated
	 * from more then one thread. As long as all work i done on
	 * DiskoWorkPool, concurrency is ensured. DiskoWorkPool manages,
	 * both unsafe (locks application and shows progress) and safe
	 * work. Estimation is potentially a time consuming process and
	 * has real-time requirements. Hence, the user should not be aware
	 * of this task running. Thus, estimation must be done on a safe
	 * thread, managed by DiskoWorkPool, because this does not
	 * lock the application.
	 *
	 * Beware though, safe work may still be unsafe. It is the
	 * programmer that is in charge of implementing work that is
	 * thread safe, which is indicated to the DiskoWorkPool when
	 * scheduled. This property is exploited here.
	 *
	 * The algorithm is not thread safe according to the DiskoWorkPool
	 * rules. Only work that does not invoke methods on Swing and MSO
	 * model is thread safe by definition. This algorithm access MSO
	 * model during estimation. MSO objects may still be changed or
	 * deleted concurrently with access to these objects during
	 * estimation. These changes however, will be detected later and
	 * fixed through the Update Event Queue. It's really not a
	 * problem for either the system nor the algorithm, because MSO
	 * object are not written to, only read from, thus not really
	 * violating the system.
	 *
	 * ============================================================ */

	/**
	 * Model driver
	 */
	protected IDispatcherIf m_driver;

	/**
	 * MSO model
	 */
	protected IMsoModelIf m_model;

	/**
	 * Commit manager
	 */
	protected IMsoTransactionManagerIf m_manager;

	/**
	 * List of attribute to update
	 */
	protected final Map<MsoClassCode,List<String>> m_attributes;

	/**
	 * The work that is looped
	 */
	protected final LoopWork m_work;

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public AbstractDsMso(Class<T> dataClass, String oprID,
			EnumSet<MsoClassCode> interests, long requestedDutyCycle,
			double requestedUtilization, Map<MsoClassCode,List<String>> attributes) throws Exception {

		// forward
		super(dataClass, oprID, requestedDutyCycle, requestedUtilization);

		// prepare
		m_interests = interests;
		m_attributes = attributes;
		m_work = new LoopWork((int)(requestedDutyCycle*requestedUtilization));
		
		// schedule work on loop
		m_workLoop.schedule(m_work);

		// connect to MSO model
		connect(Application.getInstance().getMsoModel());

	}

	/* ============================================================
	 * Required methods
	 * ============================================================ */

	public abstract boolean load();

	protected abstract T msoObjectCreated(IMsoObjectIf msoObj, MsoEvent.Change e);

	protected abstract T msoObjectChanged(IMsoObjectIf msoObj, MsoEvent.Change e);

	protected abstract T msoObjectDeleted(IMsoObjectIf msoObj, MsoEvent.Change e);

	protected abstract void execute(List<T> changed, long tic, long timeOut);

	/* ============================================================
	 * Public methods
	 * ============================================================ */

	/* ============================================================
	 * Protected methods
	 * ============================================================ */

	protected boolean doConnect(IDataSource<?> source) {
		// allowed?
		if(source instanceof IMsoModelIf) {

			// prepare
			m_model = (IMsoModelIf)source;
			m_manager = (IMsoTransactionManagerIf)m_model;
			m_driver = m_model.getDispatcher();

			// listen for changes
			m_model.getEventManager().addChangeListener(m_msoAdapter);

			// finished
			return true;
		}
		return false;
	}

	protected boolean doDisconnect() {
		// allowed?
		if(m_model!=null) {
			// remove listener
			m_model.getEventManager().removeChangeListener(m_msoAdapter);
			// initialize
			m_model = null;
			m_manager = null;
			m_driver = null;
			// finished
			return true;
		}
		return false;
	}	
	
	@Override
	protected T getDsObject(Object id) {
		if(id instanceof IMsoObjectIf) {
			return super.getDsObject(id.toString());
		}
		else {
			return super.getDsObject(id);
		}
	}

	protected void schedule(Map<M,Object[]> changes) {
		// any data to submit?
		if(changes.size()>0) {
			try {
				// create unsafe work
				CommitWork work = new CommitWork(changes,m_attributes);
				// schedule work
				WorkPool.getInstance().schedule(work);
			} catch (Exception e) {
				m_logger.error("Failed to schedule work",e);
			}
		}
	}

	/* =========================================================================
	 * Anonymous classes
	 * ========================================================================= */

    protected final IMsoChangeListenerIf m_msoAdapter = new IMsoChangeListenerIf() {

    	@Override
    	public EnumSet<MsoClassCode> getInterests() {
    		return m_interests;
    	}

    	@Override
    	public void handleMsoChangeEvent(ChangeList list) {

    		// consume?
    		if(!getID().equals(m_driver.getCurrentOperationID()) || m_dsObjs.isEmpty()) return;

    		// not a clear all event?
    		if(!list.isClearAllEvent()) {

    			// loop over all events
    			for(Change e : list.getEvents(m_interests)) {

    				// consume?
    				if(!e.isLoopbackMode()) {

    					// get flags
    			        boolean deletedObject  = e.isDeleteObjectEvent();
    			        boolean modifiedObject = e.isModifyObjectEvent();
    			        boolean modifiedReference = e.isModifiedReferenceEvent();

    			        // initialize dirty flag
    			        boolean isDirty = false;

    					// is object modified?
    					if (deletedObject || modifiedObject || modifiedReference) {
    						// forward
    						MsoEvent.Change existing = getExisting(e);
    						// add to queue?
    						if(existing==null)
    							isDirty = m_queue.add(e);
    						else {
    							// make union
    							isDirty = existing.union(e);
    						}
    						// decide action on changes
    		 				if(isDirty && isLoopState(LoopState.SUSPENDED)) {
    							// this ensures faster service if work is suspended
    							resume();
    						}
    					}
    				}
    			}
    		}
    	}


    };

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
			 * DESCRIPTION: This method is listening for updates made in
			 * IAssignmentIf and IRouteIf MSO objects. Only one Update is
			 * handled in each work cycle if spatial changes in routes are
			 * made. This ensures that the system remains responsive since
			 * spatial route estimation is time consuming (ArcGIS geodata
			 * lookup). A concurrent FIFO queue is used to store new
			 * MSO Update events between work cycles.

			 * ALGORITHM: The following algorithm is implemented
			 * 1. Get next Update if exists. Update if new route is
			 *    created or existing is changed spatially.
			 * 2. Update all route cost estimates (fast if no spatial changes)
			 * 3. Update all estimated current positions of costs which is not changed
			 *
			 * DUTY CYCLE MANAGEMENT: TIMEOUT is the cutoff work duty
			 * cycle time. The actual duty cycle time may become longer,
			 * but not longer than a started update or estimate step.
			 * update() is only allowed to exceed TIMEOUT/2. The
			 * remaining time is given to estimate(). This ensures that
			 * update() will not starve estimate() during long update
			 * sequences.
			 *
			 * ============================================================= */

			// get start tic
			long tic = System.currentTimeMillis();

			// calculate timeout
			long workTime = Math.min(loop.getWorkTime(), m_requestedWorkTime);
			
			// notify changes
			fireArchived();
			fireAdded();

			// handle updates in queue
			List<T> changed = update(tic,workTime/2);

			// forward
			execute(changed,tic,workTime/2);

			// finished
			return null;

		}

		/* ============================================================
		 * Helper methods
		 * ============================================================ */

		private List<T> update(long tic, long timeOut) {

			// initialize
			int count = 0;
			List<T> workSet = new ArrayList<T>(m_queue.size());

			// loop over all updates
			while(m_queue.peek()!=null) {

				// ensure that half MAX_WORK_TIME is only exceeded once?
				if(tic>0 && System.currentTimeMillis()-tic>timeOut)
					break;

				// get next update event
				MsoEvent.Change e = m_queue.poll();

				// increment update counter
				count++;

				// get flags
				boolean createdObject  = e.isCreateObjectEvent();
		        boolean deletedObject  = e.isDeleteObjectEvent();
		        boolean modifiedObject =   e.isModifyObjectEvent()
	        							|| e.isModifiedReferenceEvent();

		        // get MSO object
		        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();

		        // initialize cost
				T object = null;

				// is object created?
				if(!deletedObject && createdObject) {
					object = msoObjectCreated(msoObj,e);
				}

				// is object modified?
				if (!deletedObject && modifiedObject) {
					object = msoObjectChanged(msoObj,e);
				}

				// delete object?
				if (deletedObject) {
					object = msoObjectDeleted(msoObj,e);
				}

				// add to work set?
				if(object!=null && !workSet.contains(object)) {
					workSet.add(object);
				}

			}

			// finished
			return workSet;

		}

    }

    private class CommitWork extends AbstractMsoWork {

    	final Map<M,Object[]> m_changes;
    	final Map<MsoClassCode,List<String>> m_attributes;

		public CommitWork(Map<M,Object[]> changes, Map<MsoClassCode,List<String>> attributes) throws Exception {
			// forward
			super(false,true,"",0,false,true);
			// prepare
			m_changes = changes;
			m_attributes = attributes;
		}

		@SuppressWarnings("unchecked")
		public Void doWork(IWorkLoop loop)  {

			/* ========================================================
			 * IMPORTANT:  An IMsoObjectIf should only be added to
			 * updates for commit if and only if the IMsoObjectIf is
			 * created (committed to SARA). Furthermore, only the
			 * changed attribute should be committed. Concurrent work
			 * sets (potentially at least one per work process) should
			 * be affected in the same manner as a update event from
			 * the server would do.
			 *
			 * REASON: This ensures that concurrent work sets is not
			 * changed in a way that is unpredicted by the user that
			 * is working on these work sets.
			 *
			 * For instance, lets look at an example where an IMsoObjectIf
			 * is created locally and thus, not committed to SARA yet.
			 * If this thread performs a full commit on all changed objects,
			 * the concurrent work sets are also committed. This may not
			 * be what the user expects to happen, the user may instead
			 * want to rollback the changes. This will not be possible
			 * any longer because this thread already has committed all
			 * the changes. Hence, these precautions should be in the
			 * concurrent work sets best interest, an the least invasive
			 * ones.
			 * ======================================================== */

			// initialize local list
			List<IChangeRecordIf> changes = new ArrayList<IChangeRecordIf>(m_changes.size());

			// loop over all assignments
			for(M it : m_changes.keySet()) {

				// get values to update
				Object[] values = m_changes.get(it);

				// get attributes
				List<String> attributes = m_attributes.get(it.getClassCode());

				// has attributes?
				if(attributes!=null) {

					// update attributes
					for(int i=0;i<m_attributes.size();i++) {

						// get attribute name
						String name = attributes.get(i);

						// get attribute
						IMsoAttributeIf attr = it.getAttributes().get(name);

						// has attribute?
						if(attr!=null) {
							// update attribute
							attr.set(values[i]);

							// add to updates?
							if(it.isCreated()) {
								// get update holder set
								IChangeRecordIf rs = m_manager.getChanges(it);
								// has updates?
								if(rs!=null) {
									// is modified?
									if(!rs.isObjectCreated() && rs.isObjectModified()) {
										// set partial update
										rs.setFilter(name);
										// add to updates?
										changes.add(rs);
									}
								}
							}
						}
					}
				}
			}

			try {
				// commit changes?
				if(changes.size()>0) {
					m_manager.commit(changes);
				}
			} catch (TransactionException e) {
				m_logger.error("Failed to commit changes",e);
			}

			// finished
			return null;

		}

	}

}
