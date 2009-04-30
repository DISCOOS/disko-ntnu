package org.redcross.sar.ds.mso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.redcross.sar.Application;
import org.redcross.sar.data.IData;
import org.redcross.sar.data.IDataSource;
import org.redcross.sar.ds.AbstractDs;
import org.redcross.sar.ds.ICue;
import org.redcross.sar.ds.IDsObject;
import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.mso.IMsoTransactionManagerIf;
import org.redcross.sar.mso.IDispatcherIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.IWorkLoop.LoopState;

public class MsoAdvisor extends AbstractDs<ICue,IDsObject,EventObject> {

	/**
	 * ID generation counter
	 */
	private static int m_nextID = 0;

	/**
	 * Listen for MSO events
	 */
	protected final EnumSet<MsoClassCode> m_interests;

	/**
	 * Model driver
	 */
	protected IDispatcherIf m_dispatcher;

	/**
	 * MSO model
	 */
	protected IMsoModelIf m_model;

	/**
	 * Commit manager
	 */
	protected IMsoTransactionManagerIf m_comitter;

	/**
	 * Map of attributes to update
	 */
	protected final Map<MsoClassCode,List<String>> m_attributes;

	/**
	 * The work that is looped
	 */
	protected final LoopWork m_work;

	/**
	 * The assignment production level
	 */
	protected final Map<AssignmentStatus,Production> m_production = new HashMap<AssignmentStatus,Production>();

	/**
	 * Production status level set
	 */
	protected final EnumSet<AssignmentStatus> PRODUCTION_SET = EnumSet.allOf(AssignmentStatus.class);

	/**
	 * The unit execution level
	 */
	protected final Map<UnitStatus,Execution> m_execution = new HashMap<UnitStatus,Execution>();

	/**
	 * Production status level set
	 */
	protected final EnumSet<UnitStatus> EXECUTION_SET = EnumSet.allOf(UnitStatus.class);
  	
  	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public MsoAdvisor(String oprID) throws Exception {

		// forward
		super(IDsObject.class, oprID, 1000, 500);

		// prepare
		m_interests = getMsoInterests();
		m_attributes = getMsoAttributes();
		m_work = new LoopWork(500);
		
		// schedule work on loop
		m_workLoop.schedule(m_work);

		// connect to MSO model?
		IMsoModelIf model = Application.getInstance().getMsoModel();
		if(oprID.equalsIgnoreCase(model.getID())) {
			if(!connect(Application.getInstance().getMsoModel())) {
	  			throw new IllegalArgumentException("Could not connect to " + model);				
			}
		}

	}

	
	
	/* ============================================================
	 * Public methods
	 * ============================================================ */

	public synchronized boolean load() {
		// forward
		clear();
		// is data bound?
		if(isDataSourceBound()) {
			// load lists from available assignments
			if(m_model.getMsoManager().operationExists()) {
				// get command post
				ICmdPostIf cmdPost = m_model.getMsoManager().getCmdPost();
				// create levels
				createProduction(cmdPost.getAssignmentListItems());
				createExecution(cmdPost.getUnitListItems());
				// notify
				fireAdded();
				fireArchived();
			}
			// finished
			return m_dsObjs.size()>0;
		}
		// failed
		return false;
	}

	public Production getLevel(AssignmentStatus status) {
		return m_production.get(status);
	}

	public Execution getLevel(UnitStatus status) {
		return m_execution.get(status);
	}

	/* ===========================================
	 * Protected methods
	 * =========================================== */

	protected boolean doConnect(IDataSource<?> source) {
		// allowed?
		if(source instanceof IMsoModelIf) {

			// prepare
			m_model = (IMsoModelIf)source;
			m_comitter = (IMsoTransactionManagerIf)m_model;
			m_dispatcher = m_model.getDispatcher();

			// listen for changes
			m_model.getEventManager().addClientUpdateListener(m_msoAdapter);

			// finished
			return true;
		}
		return false;
	}

	protected boolean doDisconnect() {
		// allowed?
		if(m_model!=null) {
			// remove listener
			m_model.getEventManager().removeClientUpdateListener(m_msoAdapter);
			// initialize
			m_model = null;
			m_comitter = null;
			m_dispatcher = null;
			// finished
			return true;
		}
		return false;
	}
	
	protected void createProduction(Collection<IAssignmentIf> list) {
		// clear current levels
		m_production.clear();
		// loop over production set
		for(AssignmentStatus it : PRODUCTION_SET) {
			createLevel(list,it);
		}
	}

	protected void createLevel(Collection<IAssignmentIf> list, AssignmentStatus status) {
		// create production level
		Production level = new Production(createID(),"AssignmentStatus."+status.name(),5,status);
		// add level
		m_production.put(status,level);
		// schedule assignments
		level.getInput().schedule(list);
		// register level as cue
		add(level,level);

	}

	protected void createExecution(Collection<IUnitIf> list) {
		// clear current levels
		m_execution.clear();
		// loop over production set
		for(UnitStatus it : EXECUTION_SET) {
			createLevel(list,it);
		}
	}

	protected void createLevel(Collection<IUnitIf> list, UnitStatus status) {
		// create production level
		Execution level = new Execution(createID(),"UnitStatus."+status.name(),5,status);
		// add level
		m_execution.put(status,level);
		// schedule assignments
		level.getInput().schedule(list);
		// register level as cue
		add(level,level);

	}

	protected void execute(List<IDsObject> changed, long tic, long timeOut) {

		// forward
		evaluate(changed, tic, timeOut);

	}

	protected void schedule(IAssignmentIf msoObj) {
		// loop over production levels
		for(Production it : m_production.values()) {
			it.getInput().schedule(msoObj);
		}
	}

	protected void schedule(IUnitIf msoObj) {
		// loop over execution levels
		for(Execution it : m_execution.values()) {
			it.getInput().schedule(msoObj);
		}
	}

	/* ===========================================
	 * Helper methods
	 * =========================================== */

	private static EnumSet<MsoClassCode> getMsoInterests() {
		EnumSet<MsoClassCode> set = EnumSet.of(MsoClassCode.CLASSCODE_ASSIGNMENT);
		set.add(MsoClassCode.CLASSCODE_UNIT);
		return set;
	}

	private static Map<MsoClassCode,List<String>> getMsoAttributes() {
		return null;
	}

	private static ID createID() {
		ID id = new ID(m_nextID);
		m_nextID++;
		return id;
	}

	/* ============================================================
	 * Estimation implementation
	 * ============================================================ */

	private void evaluate(List<IDsObject> changed, long tic, long timeOut) {

		// initialize heavy count
		int heavyCount = 0;

		// initialize local lists
		List<IDsObject> modified = new ArrayList<IDsObject>(m_dsObjs.size());
		List<IDsObject> heavySet = new ArrayList<IDsObject>(m_dsObjs.size());

		// get current work set
		List<IDsObject> workSet = getWorkSet(changed);

		// loop over costs in work set
		for(IDsObject it : workSet) {

			// ensure that the MAX_WORK_TIME is only exceeded once
			if(System.currentTimeMillis()-tic>timeOut)
				break;

			// execute work cycle
			try {
				// add to heavy list?
				if(heavyCount > 0 && false) {
					heavySet.add(it);
				}
				else {

					// forward
					it.calculate();

					// is modified
					modified.add(it);

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// remove modified from work set
		workSet.removeAll(modified);

		// set remaining as next work set
		addToResidue(workSet);

		// update heavy list
		setHeavySet(heavySet);

		// notify listeners
		fireModified(modified,0);

	}

	private List<IDsObject> getWorkSet(List<IDsObject> changed) {

		// initialize
		List<IDsObject> list = new ArrayList<IDsObject>(m_dsObjs.size());

		/* ==========================================
		 * add the residue progress work set from
		 * last work cycle to prevent starvation.
		 * ========================================== */
		for(IDsObject it: m_residueSet) {
			if(it.isDirty() && include(it,list)) list.add(it);
		}

		// add the changed
		for(IDsObject it : changed) {
			if(it.isDirty() && include(it,list)) list.add(it);
		}

		// add the rest
		for(IDsObject it : m_dsObjs.values()) {
			if(it.isDirty() && include(it,list)) list.add(it);
		}

		// remove from residue
		m_residueSet.removeAll(list);

		// finished
		return list;

	}

	/* =========================================================================
	 * Anonymous classes
	 * ========================================================================= */

    protected final IMsoUpdateListenerIf m_msoAdapter = new IMsoUpdateListenerIf() {

    	@Override
    	public EnumSet<MsoClassCode> getInterests() {
    		return m_interests;
    	}

    	@Override
    	public void handleMsoUpdateEvent(UpdateList list) {

    		// consume?
    		if(!getID().equals(m_dispatcher.getActiveOperationID()) || m_dsObjs.isEmpty()) return;

    		// not a clear all event?
    		if(!list.isClearAllEvent()) {

    			// loop over all events
    			for(Update e : list.getEvents(m_interests)) {

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
    };

	/* =========================================================================
	 * Inner classes
	 * ========================================================================= */

    public static class ID implements IData {

    	private final int m_number;

    	public ID(int number) {
    		// prepare
    		m_number = number;
    	}

		public int getNumber(int number) {
			return m_number;
		}

		@Override
		public int compareTo(IData data) {
			if(!(data instanceof ID))
				throw new IllegalArgumentException("Objects not comparable");
			// compare
			return m_number - ((ID)data).m_number;
		}

		public DsClassCode getClassCode() {
			return DsClassCode.CLASSCODE_CUE;
		}

    }

    private class LoopWork extends AbstractWork {

    	private int m_timeOut;

		/* ============================================================
		 * Constructors
		 * ============================================================ */

		public LoopWork(int timeOut) throws Exception {
			// forward
			super(0,true,false,ThreadType.WORK_ON_LOOP,"",0,false,false,true);
			// prepare
			m_timeOut = timeOut;
		}

		/* ============================================================
		 * IDiskoWork implementation
		 * ============================================================ */

		public Void doWork() {

			// get start tic
			long tic = System.currentTimeMillis();

			// notify changes
			fireArchived();
			fireAdded();

			// handle updates in queue
			List<IDsObject> changed = update(tic,m_timeOut/2);

			// forward
			execute(changed,tic,m_timeOut/2);

			// finished
			return null;

		}

		/* ============================================================
		 * Helper methods
		 * ============================================================ */

		private List<IDsObject> update(long tic, int timeOut) {

			// initialize
			int count = 0;
			List<IDsObject> workSet = new ArrayList<IDsObject>(m_queue.size());

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

			        // initialize list of changed cues
			        Collection<IDsObject> dirtyList = null;

					// is object created?
					if(!deletedObject && createdObject) {
						dirtyList = msoObjectCreated(msoObj,e);
					}

					// is object modified?
					if (!deletedObject && modifiedObject) {
						dirtyList = msoObjectChanged(msoObj,e);
					}

					// delete object?
					if (deletedObject) {
						dirtyList = msoObjectDeleted(msoObj,e);
					}

					// add to work set?
					if(dirtyList!=null) {
						for(IDsObject it : dirtyList) {
							if(!workSet.contains(it)) {
								workSet.addAll(dirtyList);
							}
						}
					}

				}
				else if(next instanceof DsEvent.Update) {
					// TODO: Handle decision support update events
				}

			}

			// finished
			return workSet;

		}

    	protected Collection<IDsObject> msoObjectCreated(IMsoObjectIf msoObj, Update e) {
    		// initialize
    		Collection<IDsObject> isDirty = new Vector<IDsObject>();
    		// translate
    		if(msoObj instanceof IAssignmentIf) {
    			schedule((IAssignmentIf)msoObj);
    			isDirty.addAll(m_production.values());
    		}
    		// finished
    		return isDirty;
    	}

    	protected Collection<IDsObject> msoObjectChanged(IMsoObjectIf msoObj, Update e) {
    		// initialize
    		Collection<IDsObject> isDirty = new Vector<IDsObject>();
    		// translate
    		if(msoObj instanceof IAssignmentIf) {
    			schedule((IAssignmentIf)msoObj);
    			isDirty.addAll(m_production.values());
    		}
    		// finished
    		return isDirty;
    	}

    	protected Collection<IDsObject> msoObjectDeleted(IMsoObjectIf msoObj, Update e) {
    		// initialize
    		Collection<IDsObject> isDirty = new Vector<IDsObject>();
    		// translate
    		if(msoObj instanceof IAssignmentIf) {
    			schedule((IAssignmentIf)msoObj);
    			isDirty.addAll(m_production.values());
    		}
    		// finished
    		return isDirty;
    	}

    }



}
