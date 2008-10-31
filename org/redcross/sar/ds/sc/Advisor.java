package org.redcross.sar.ds.sc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redcross.sar.ds.AbstractDs;
import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.modeldriver.IModelDriverIf;
import org.redcross.sar.mso.ICommitManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;
import org.redcross.sar.thread.AbstractWork;
import org.redcross.sar.thread.IWorkLoop.LoopState;

public class Advisor extends AbstractDs<Clue,Action,EventObject> {

	/**
	 * Listen for MSO events
	 */
	protected final EnumSet<MsoClassCode> m_msoInterests;

	/**
	 * Model driver
	 */
	protected IModelDriverIf m_driver;

	/**
	 * MSO model
	 */
	protected IMsoModelIf m_model;

	/**
	 * Commit manager
	 */
	protected ICommitManagerIf m_comitter;

	/**
	 * Map of attributes to update
	 */
	protected final Map<MsoClassCode,List<String>> m_attributes;

	/**
	 * The work that is looped
	 */
	protected final LoopWork m_loopWork;


	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public Advisor(String oprID) throws Exception {

		// forward
		super(Action.class, oprID, 1000, 500);

		// prepare
		m_msoInterests = getMsoInterests();
		m_attributes = getMsoAttributes();
		m_model = MsoModelImpl.getInstance();
		m_comitter = (ICommitManagerIf)m_model;
		m_driver = m_model.getModelDriver();
		m_loopWork = new LoopWork(500);
		m_workLoop.schedule(m_loopWork);

	}

	/* ============================================================
	 * Public methods
	 * ============================================================ */

	public Action getAction(Clue id) {
		return getDsObject(id);
	}

	public List<Action> getItems() {
		return getDsObjects();
	}

	public Map<Clue,Action> getActions() {
		return getDsMap();
	}

	public synchronized boolean load() {
		// forward
		clear();
		// load lists from available assignments
		if(MsoModelImpl.getInstance().getMsoManager().operationExists()) {
			// TODO: evaluate MSO model state
			// TODO: evaluate ETE

			// notify
			fireAdded();
			fireArchived();
		}
		// finished
		return m_dsObjs.size()>0;
	}

	/* ===========================================
	 * Protected methods
	 * =========================================== */

	protected void execute(List<Action> changed, long tic, long timeOut) {

		// forward
		evaluate(changed, tic, timeOut);

	}

	protected void schedule(Map<Clue,Object[]> changes) {
		// any data to submit?
		if(changes.size()>0) {
			// TODO: Schedule work
		}
	}


	/* ===========================================
	 * Helper methods
	 * =========================================== */

	private static EnumSet<MsoClassCode> getMsoInterests() {
		return null;
	}

	private static Map<MsoClassCode,List<String>> getMsoAttributes() {
		return null;
	}

	/* ============================================================
	 * Estimation implementation
	 * ============================================================ */

	private void evaluate(List<Action> changed, long tic, long timeOut) {

		// initialize heavy count
		int heavyCount = 0;

		// initialize local lists
		List<Action> modified = new ArrayList<Action>(m_dsObjs.size());
		List<Action> heavySet = new ArrayList<Action>(m_dsObjs.size());
		List<Action> estimated = new ArrayList<Action>(m_dsObjs.size());
		Map<Clue,Object[]> actions = new HashMap<Clue, Object[]>(m_dsObjs.size());

		// get current work set
		List<Action> workSet = getWorkSet(changed,false);

		// loop over costs in work set
		for(Action cost : workSet) {

			// ensure that the MAX_WORK_TIME is only exceeded once
			if(System.currentTimeMillis()-tic>timeOut)
				break;

			// execute work cycle
			try {
				// add to heavy list?
				if(heavyCount > 0 && false) {
					heavySet.add(cost);
				}
				else {


				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// forward
		schedule(actions);

		// remove estimated from work set
		workSet.removeAll(estimated);

		// set remaining as next work set
		addToResidue(workSet);

		// update heavy list
		setHeavySet(heavySet);

		// notify listeners
		fireModified(modified,0);

	}



	private List<Action> getWorkSet(List<Action> changed, boolean progress) {

		// create new work set
		List<Action> list = new ArrayList<Action>(m_dsObjs.size());

		if(progress) {

			// get costs ready to progress
			for(Action it: m_dsObjs.values()) {
				if(!changed.contains(it)) {

				}
			}

		}
		else {

			// add the rest
			for(Action it: m_heavySet) {
				// add to work list?
				if(!(list.contains(it)))
					list.add(it);
			}

			// add the rest from last time to prevent
			// starvation of costs in the back of m_dsObjs.values()
			for(Action it: m_residueSet) {
				if(!list.contains(it))
					list.add(it);
			}

			// add the missing
			for(Action it : changed) {

				// add to work list?
				if(!(list.contains(it)))
					list.add(it);

			}

		}

		// finished
		return list;

	}

	/* =========================================================================
	 * Anonymous classes
	 * ========================================================================= */

    protected final IMsoUpdateListenerIf m_msoAdapter = new IMsoUpdateListenerIf() {

    	@Override
    	public EnumSet<MsoClassCode> getInterests() {
    		return m_msoInterests;
    	}

    	@Override
    	public void handleMsoUpdateEvent(UpdateList list) {

    		// consume?
    		if(!m_oprID.equals(m_driver.getActiveOperationID()) || m_dsObjs.isEmpty()) return;

    		// not a clear all event?
    		if(!list.isClearAllEvent()) {

    			// loop over all events
    			for(Update e : list.getEvents(m_msoInterests)) {

    				// consume?
    				if(!UpdateMode.LOOPBACK_UPDATE_MODE.equals(e.getUpdateMode())) {

    					// get flags
    			        boolean deletedObject  = e.isDeleteObjectEvent();
    			        boolean modifiedObject = e.isModifyObjectEvent();
    			        boolean modifiedReference = e.isChangeReferenceEvent();

    			        // initialize dirty flag
    			        boolean isDirty = false;

    					// is object modified?
    					if (deletedObject || modifiedObject || modifiedReference) {
    						// forward
    						EventObject found = getExisting(e);
    						// add to queue?
    						if(found==null)
    							isDirty = m_queue.add(e);
    						else if(found instanceof MsoEvent.Update){
    							// make union
    							isDirty = ((MsoEvent.Update)found).union(e);
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

    	private int m_timeOut;

		public LoopWork(int timeOut) throws Exception {
			// forward
			super(true,false,ThreadType.WORK_ON_LOOP,"",0,false,false,true);
			// prepare
			m_timeOut = timeOut;
		}

		/* ============================================================
		 * IDiskoWork implementation
		 * ============================================================ */

		public Void doWork() {

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

			// notify changes
			fireArchived();
			fireAdded();

			// handle updates in queue
			List<Action> changed = update(tic,m_timeOut/2);

			// forward
			execute(changed,tic,m_timeOut/2);

			// finished
			return null;

		}

		/* ============================================================
		 * Helper methods
		 * ============================================================ */

		private List<Action> update(long tic, int timeOut) {

			// initialize
			int count = 0;
			List<Action> workSet = new ArrayList<Action>(m_queue.size());

			// loop over all updates
			while(m_queue.peek()!=null) {

				// ensure that half MAX_WORK_TIME is only exceeded once?
				if(tic>0 && System.currentTimeMillis()-tic>timeOut)
					break;

				// get next update event
				EventObject next = m_queue.poll();

				// translate
				if(next instanceof MsoEvent.Update) {

					// cast to MsoEvent.Update
					MsoEvent.Update  e = (MsoEvent.Update)next;

					// increment update counter
					count++;

					// get flags
					boolean createdObject  = e.isCreateObjectEvent();
			        boolean deletedObject  = e.isDeleteObjectEvent();
			        boolean modifiedObject =   e.isModifyObjectEvent()
		        							|| e.isChangeReferenceEvent();

			        // get MSO object
			        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();

			        // initialize action
					Action object = null;

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
				else if(next instanceof DsEvent.Update) {
					// TODO: Handle decision support update events
				}

			}

			// finished
			return workSet;

		}

    	protected Action msoObjectCreated(IMsoObjectIf msoObj, Update e) {
    		// TODO: Implement
    		return null;
    	}

    	protected Action msoObjectChanged(IMsoObjectIf msoObj, Update e) {
    		// TODO: Implement
    		return null;
    	}

    	protected Action msoObjectDeleted(IMsoObjectIf msoObj, Update e) {
    		// TODO: Implement
    		return null;

    	}

    }

}
