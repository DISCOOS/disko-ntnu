package org.redcross.sar.ds.ete;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.redcross.sar.app.Utils;
import org.redcross.sar.ds.IDsIf;
import org.redcross.sar.ds.IDsObjectIf;
import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.ds.event.IDsUpdateListenerIf;
import org.redcross.sar.ds.event.DsEvent.DsEventType;
import org.redcross.sar.modeldriver.IModelDriverIf;
import org.redcross.sar.mso.ICommitManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.committer.IUpdateHolderIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.MsoCompareRoute;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.thread.IDiskoWork;
import org.redcross.sar.util.except.CommitException;
import org.redcross.sar.util.mso.Route;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

public class RouteCostEstimator extends AbstractDiskoWork<Boolean> 
								implements IDsIf<RouteCost>, 
								           IMsoUpdateListenerIf {

	private static final long DUTY_CYCLE_TIME = 2000;		// Invoke every DUTY_CYCLE_TIME
	
	/**
	 * Listen for Updates of assignments, routes and units
	 */
	private final EnumSet<MsoClassCode> msoInterests = 
		EnumSet.of(MsoClassCode.CLASSCODE_ASSIGNMENT, 
				   MsoClassCode.CLASSCODE_ROUTE,
				   MsoClassCode.CLASSCODE_UNIT);
	
	/**
	 * Route Comparator
	 */
	private final MsoCompareRoute m_comparator = new MsoCompareRoute();
	
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
	 * Local list of assignments. 
	 */
	//private final Map<IAssignmentIf,AssignmentStatus> m_changeList = 
	//	new HashMap<IAssignmentIf,AssignmentStatus>();
	
	/**
	 * Local list of MSO route objects. 
	 */
	private final Map<IAssignmentIf,List<IRouteIf>> m_routes = 
		new HashMap<IAssignmentIf,List<IRouteIf>>();
	
	/**
	 * Local list of costs
	 */
	private final Map<IAssignmentIf,RouteCost> m_costs = 
		new HashMap<IAssignmentIf, RouteCost>();
	
	/**
	 * Local list of assignments
	 */
	private final Map<RouteCost, IAssignmentIf> m_assignments = 
		new HashMap<RouteCost, IAssignmentIf>();
	
	/**
	 * Local list of archived costs
	 */
	private final Map<IAssignmentIf,RouteCost> m_archived = 
		Collections.synchronizedMap(new HashMap<IAssignmentIf, RouteCost>());
	
	/**
	 * Update event queue. Buffer between received update and
	 * handling thread. Only updates that is interesting will
	 * be added.
	 */
	private final ConcurrentLinkedQueue<Update> m_queue = 
		new ConcurrentLinkedQueue<Update>();
	
	/**
	 * Local list of cost that requires a full spatial estimation 
	 * (isReplaced() is <code>true</code>, indicating that the 
	 * route is replaced and a full estimation must be executed, which
	 * is time consuming)
	 */
	private final List<RouteCost> m_heavySet =  new ArrayList<RouteCost>();
	
	/**
	 * Update event queue. Buffer between received update and
	 * handling thread. Only updates that is interesting will
	 * be added.
	 */
	private final List<RouteCost> m_residueSet = 
		new ArrayList<RouteCost>();
			
	/**
	 * Estimator id
	 */
	private final String m_oprID;
	
	/**
	 * Model driver
	 */
	private final IModelDriverIf m_driver;
	
	/**
	 * MSO model 
	 */
	private final IMsoModelIf m_model;
	
	/**
	 * Commit manager
	 */
	private final ICommitManagerIf m_comitter;
	
	/**
	 * Work pool hook
	 */
	private DiskoWorkPool m_workPool = null;
	
	/**
	 * List of update listeners.
	 */
	private final List<IDsUpdateListenerIf> m_listeners = 
		new ArrayList<IDsUpdateListenerIf>();	
	
	/* ============================================================
	 * Constructors
	 * ============================================================ */
	
	public RouteCostEstimator(String oprID) throws Exception {
		// forward
		super(true,false,WorkOnThreadType.WORK_ON_NEW,
				"Estimerer",0,false,false,true,DUTY_CYCLE_TIME);
		
		// prepare
		m_oprID = oprID;
		m_workPool = DiskoWorkPool.getInstance();
		m_model = MsoModelImpl.getInstance();
		m_comitter = (ICommitManagerIf)m_model;
		m_driver = m_model.getModelDriver();
		
		// add listener
		MsoModelImpl.getInstance().getEventManager().addClientUpdateListener(this);
		
	}	
	
	/* ============================================================
	 * Public methods
	 * ============================================================ */
	
	public String getOprID() {
		return m_oprID;
	}
	
	public RouteCost getCost(IAssignmentIf assignment) {
		return m_costs.get(assignment);
	}
	
	public List<RouteCost> getItems() {
		return new ArrayList<RouteCost>(m_costs.values());
	}
	
	public Map<IAssignmentIf,RouteCost> getCosts() {
		return m_costs;
	}
	
	public synchronized boolean load() {
		// forward
		clear();
		// load lists from available assignments
		if(MsoModelImpl.getInstance().getMsoManager().operationExists()) {
			// forward
			for(IAssignmentIf it : MsoModelImpl.getInstance().getMsoManager().getCmdPost().getAssignmentListItems()) {				
				if(setEstimate(it)) {
					addToResidue(getCost(it));
				}
				IUnitIf msoUnit = it.getOwningUnit();
				if(msoUnit!=null) {
					if(logPosition(msoUnit)) {
						addToResidue(getCost(it));
					}
				}
			}
		}
		// finished
		return m_costs.size()>0;
	}
	
	public synchronized void clear() {
		for(RouteCost it : m_costs.values()) {
			fireRemoved(it);
		}
		m_costs.clear();
		m_routes.clear();
		m_assignments.clear();		
		m_archived.clear();
		m_heavySet.clear();
		m_residueSet.clear();
		m_queue.clear();
	}
	
	public boolean addUpdateListener(IDsUpdateListenerIf listener) {
		if(!m_listeners.contains(listener))
			return m_listeners.add(listener);
		return false;
	}

	public boolean removeUpdateListener(IDsUpdateListenerIf listener) {
		if(m_listeners.contains(listener))
			return m_listeners.remove(listener);
		return false;
	}
	
	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */

	public boolean hasInterestIn(IMsoObjectIf aMsoObject, UpdateMode mode) {
		// consume?
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)
				|| !m_oprID.equals(m_driver.getActiveOperationID())
				|| m_costs.isEmpty()) return false;		
		// check against interests
		return msoInterests.contains(aMsoObject.getMsoClassCode());
	}	

	public void handleMsoUpdateEvent(Update e) {
			
		// not a clear all event?
		if(!e.isClearAllEvent()) {
		
			// get flags
	        boolean deletedObject  = e.isDeleteObjectEvent();
	        boolean modifiedObject = e.isModifyObjectEvent();
	        boolean modifiedReference = e.isChangeReferenceEvent();
	        boolean resume = false;
	        
			// is object modified?
			if (deletedObject || modifiedObject || modifiedReference) {
				// forward
				Update existing = getExisting(e);
				// add to queue?
				if(existing==null)
					resume = m_queue.add(e);
				else {
					// make union
					resume = existing.union(e);
				}
			}
			
			// resume work? 
			if(resume && isSuspended()) {
				// this ensures faster service if work is suspended
				m_workPool.resume(this);
			}
			
		}
		
	}	

	/* ===========================================
	 * Private methods
	 * ===========================================
	 */
	
	private void fireAdded(RouteCost cost) {
		fireUpdateEvent(new DsEvent.Update(this,DsEventType.ADDED_EVENT,0,new IDsObjectIf[]{cost}));
	}
	
	private void fireModified(List<RouteCost> costs, int flags) {
		IDsObjectIf[] data = new IDsObjectIf[costs.size()]; 
		costs.toArray(data);
		fireUpdateEvent(new DsEvent.Update(this,DsEventType.MODIFIED_EVENT,flags,data));
	}
	
	private void fireRemoved(RouteCost cost) {
		fireUpdateEvent(new DsEvent.Update(this,DsEventType.REMOVED_EVENT,0,new IDsObjectIf[]{cost}));
	}
	
	private void fireUpdateEvent(DsEvent.Update e) {
		for(IDsUpdateListenerIf it : m_listeners) {
			it.handleDsUpdateEvent(e);
		}
	}
	private Update getExisting(Update e) {
		Object s = e.getSource();
		for(Update it : m_queue) {
			if(it.getSource().equals(s)) {
				return it;
			}
		}
		return null;
	}
	
	private RouteCost msoObjectChanged(IMsoObjectIf msoObj, Update e) {
		
		// initialize
		RouteCost cost = null;
		
		// translate
		if(msoObj instanceof IAssignmentIf) {
			// cast to IAssignmentIf
			IAssignmentIf assignment = (IAssignmentIf)msoObj;
			// get routes?
			if(e.isChangeReferenceEvent()) {
				// forward
				if(setEstimate(assignment)) {
					cost = getCost(assignment);
				}
			}			
			// get start time or is finished?
			if(e.isModifyObjectEvent()) {
				// forward
				if(updateArguments(assignment)) {
					cost = getCost(assignment);
				}
			}			
		}
		else if(msoObj instanceof IRouteIf) {
			// cast to IRouteIf
			IRouteIf msoRoute = (IRouteIf)msoObj;
			// get assignment
			IAssignmentIf assignment = getAssignment(msoRoute);
			// has assignment?
			if(assignment!=null) {
				// forward
				if(setEstimate(assignment)) {
					cost = getCost(assignment);
				}
			}
		}			
		else if(msoObj instanceof IUnitIf) {
			// cast to IUnitIf 
			IUnitIf msoUnit = (IUnitIf)msoObj;
			// forward
			if(logPosition(msoUnit)) {
				cost = getCost(msoUnit.getActiveAssignment());
			}
		}
		return cost;
	}

	private RouteCost msoObjectDeleted(IMsoObjectIf msoObj) {

		// initialize
		RouteCost cost = null;
		
		// translate
		if(msoObj instanceof IAssignmentIf) {
			// cast to IAssignmentIf
			IAssignmentIf assignment = (IAssignmentIf)msoObj;
			// forward
			removeEstimate(assignment);
		}
		else if(msoObj instanceof IRouteIf) {
			// cast to IRouteIf
			IRouteIf msoRoute = (IRouteIf)msoObj;
			// get assignment
			IAssignmentIf assignment = getAssignment(msoRoute);
			// has assignment registered?
			if(assignment!=null && !assignment.hasBeenDeleted()) {
				// forward
				if(setEstimate(assignment)) {
					cost = getCost(assignment);
				}
			}
		}		
		return cost;
	}	
		
	private boolean updateArguments(IAssignmentIf assignment) {
		
		// initialize
		boolean bFlag = false;
		
		// get status
		AssignmentStatus status = assignment.getStatus();
				
		// translate status change to action
		switch(status) {
		case DRAFT:
		case READY:
		case QUEUED:
		case ASSIGNED:
		case EXECUTING:
			
			// forward
			bFlag = setOffset(assignment);
			
			break;
		case FINISHED:
		case REPORTED:
			
			/* =================================================================
			 * Only update if status has changed to a legal state finish state. 
			 * If an assignment is finished or reported, it can not be 
			 * reactivated again.
			 * 
			 * When status is changed to FINISHED or REPORTED, the assignment
			 * estimate is archived.
			 * 
			 * ================================================================= */
			
			// forward
			archiveEstimate(assignment, null);

			break;
		}
		
		// finished
		return bFlag;
		
	}
	
	private List<IRouteIf> getRouteSequence(IAssignmentIf assignment) {		
		
		// initialize
		List<IRouteIf> list = new ArrayList<IRouteIf>();
		
		// add all routes
		for(IMsoObjectIf it : assignment.getPlannedArea().getAreaGeodataItems()) {
			if(it instanceof IRouteIf) {
				if(!list.contains(it)) {
					list.add((IRouteIf)it);
				}
			}
		}
		
		// sort routes in ascending order after area sequence number
		Collections.sort(list,m_comparator);
		
		// finished
		return list;	
	}
	
	private Route getRoute(String id, List<IRouteIf> list) {
		Route route = new Route(id);
		for(IRouteIf it : list) {
			// get segment
			Route segment = it.getGeodata();
			// has segment?
			if(segment!=null) {
				route.addAll(segment);
			}
		}
		return route;
	}
	
	private boolean setEstimate(IAssignmentIf assignment) {
		
		// initialize
		boolean bFlag = false;
		
		// route has assignment?
		if(assignment!=null) {

			// resume?
			if(!IAssignmentIf.FINISHED_AND_REPORTED_SET
					.contains(assignment.getStatus())) {
				
				// get sorted route list
				List<IRouteIf> list = getRouteSequence(assignment);
				
				// update list
				m_routes.put(assignment,list);
				
				// forward
				bFlag = setRoute(assignment,list);
				
				// forward
				bFlag |= updateArguments(assignment);
												
			}
			else {
				// archive
				archiveEstimate(assignment,null);
			}			
		}
		
		// finished
		return bFlag;
		
	}
	
	private void archiveEstimate(IAssignmentIf assignment, Track track) {
		if(assignment!=null) {
			RouteCost cost = m_costs.get(assignment);
			if(cost!=null && !cost.isArchived()) {
				cost.archive();
			}
		}
	}	
	
	private void removeEstimate(IAssignmentIf assignment) {
		if(assignment!=null) {
			// get cost
			RouteCost cost = m_costs.get(assignment);
			m_routes.remove(assignment);
			if(cost!=null) {			
				m_costs.remove(assignment);
				m_assignments.remove(cost);
				m_heavySet.remove(cost);
				m_residueSet.remove(cost);
				fireRemoved(cost);
			}
		}
	}	
	
	private boolean setRoute(IAssignmentIf assignment, List<IRouteIf> list) {
		
		// initialize flag
		boolean bFlag = false;
		
		// get current cost
		RouteCost cost = m_costs.get(assignment);
		
		// get current route
		Route route = getRoute(assignment.getObjectId(),list);
		
		// create cost?
		if(cost==null) {
			cost = createCost(assignment,route);
			bFlag = true;
		}
		else {
			// is route changed?
			if(!cost.getRoute().equals(route)) {
				cost.setRoute(route);
				bFlag = true;
			}
		}
		
		// estimate on next work cycle?
		if(bFlag) cost.resume();		
		
		// finished
		return bFlag;
		
	}
	
	private RouteCost createCost(IAssignmentIf assignment, Route route) {
		// create new route cost object
		RouteCost cost = new RouteCost(assignment,route,
				0,Utils.getApp().getMapManager().getPrintMap());
		// add to costs and assignments
		m_costs.put(assignment, cost);	
		m_assignments.put(cost,assignment);	
		// notify
		fireAdded(cost);
		// finished
		return cost;
	}
	
	private IAssignmentIf getAssignment(IRouteIf msoRoute) {
		for(IAssignmentIf it : m_routes.keySet()) {
			List<IRouteIf> list = m_routes.get(it); 
			if(list.contains(msoRoute)) 
				return it;
		}
		return null;
	}
	
	private IAssignmentIf getAssignment(RouteCost cost) {
		 return m_assignments.get(cost);		
	}
	
	private boolean setOffset(IAssignmentIf assignment) {
		
		// initialize
		boolean bFlag = false;
		
		// assignment exists?
		if(assignment!=null) {
			
			// get cost
			RouteCost cost = m_costs.get(assignment);
			
			// valid operation?
			if(!(cost==null || cost.isArchived())) {
				
				// get status
				AssignmentStatus status = assignment.getStatus();
						
				// translate status change to action
				switch(status) {
				case DRAFT:
				case READY:
				case QUEUED:
				case ASSIGNED:					
					// only set start time once
					if(cost.getStartTime()==null) {
						bFlag = cost.setStartTime(Calendar.getInstance());
					}
					break;
				/*
				case ASSIGNED:
					// set start time
					bFlag = cost.setStartTime(assignment.getTimeAssigned());
					break;
				*/
				case EXECUTING:					
					// set start time
					bFlag = cost.setStartTime(assignment.getTimeStarted());
					break;
				}
				// set last known position
				IUnitIf msoUnit = assignment.getOwningUnit();
				if(msoUnit!=null) {
					bFlag |= logPosition(msoUnit);
				}
			}
		}
		return bFlag;
	}
	
	private boolean logPosition(IUnitIf msoUnit) {
		// initialize
		boolean bFlag = false;
		
		// unit exists?
		if(msoUnit!=null) {
			// get position
			TimePos p = msoUnit.getLastKnownPosition();
			// has position?
			if(p!=null) {
				// get active assignment
				IAssignmentIf assignment = msoUnit.getActiveAssignment();
				// has active assignment?
				if(assignment!=null) {
					// get current route cost
					RouteCost cost = m_costs.get(assignment);
					// update offset position?
					if(cost!=null) {
						try {
							cost.setLastKnownPosition(p);
							bFlag = true;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		// finished
		return bFlag;
	}
	

	/* ============================================================
	 * IDiskoWork implementation
	 * ============================================================ */
	
	@Override
	public Boolean doWork() {
		
		/* =============================================================
		 * DESCRIPTION: This method is listening for updates made in
		 * IAssignmentIf and IRouteIf MSO objects. Only one Update is
		 * handled in each work cycle if spatial changes in routes are 
		 * made. This ensures that the system remains responsive since 
		 * spatial route estimation is time consuming (ArcGIS geodata
		 * lookup). A concurrent FIFO queue is used to  store new 
		 * MSO Update events between work cycles.

		 * ALGORITHM: The following algorithm is implemented
		 * 1. Get next Update if exists. Update if new route is 
		 *    created or existing is changed spatially. 
		 * 2. Update all route cost estimates (fast if no spatial changes)
		 * 3. Update all estimated current positions of costs which is not changed
		 * 
		 * DUTY CYCLE MANAGEMENT: MAX_WORK_TIME is the cutoff work duty 
		 * cycle time. The actual duty cycle time may become longer, 
		 * but not longer than a started update or estimate step. 
		 * update() is only allowed to exceed MAX_WORK_TIME/2. The 
		 * remaining time is given to estimate(). This ensures that 
		 * update() will not starve estimate() during long update 
		 * sequences.
		 * 
		 * ============================================================= */
		
		
		// get start tic
		long tic = System.currentTimeMillis();
		
		// handle updates in queue
		List<RouteCost> changed = update(tic);
		
		// calculate estimated based on changes
		estimate(changed,tic);
		
		// calculate progress
		progress(changed,tic);
		
		// finished
		return true;
		
	}
	
	private List<RouteCost> update(long tic) {

		// initialize
		int count = 0;
		List<RouteCost> workSet = new ArrayList<RouteCost>(m_queue.size());
		
		// get available work time
		long maxTime = m_availableTime/2;
		
		// loop over all updates
		while(m_queue.peek()!=null) {
								
			// ensure that half MAX_WORK_TIME is only exceeded once
			if(System.currentTimeMillis()-tic>maxTime)
				break;
			
			// get next update event
			Update e = m_queue.poll();
			
			// increment update counter
			count++;
			
			// get flags
	        boolean deletedObject  = e.isDeleteObjectEvent();
	        boolean modifiedObject = e.isModifyObjectEvent();
	        boolean modifiedReference = e.isChangeReferenceEvent();
	        
	        // get MSO object
	        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
			
	        // initialize cost
			RouteCost cost = null;
			
			// is object modified?
			if (!deletedObject && (modifiedObject || modifiedReference)) {
				cost = msoObjectChanged(msoObj,e);
			}
			
			// delete object?
			if (deletedObject) {
				cost = msoObjectDeleted(msoObj);
			}
			
			// add to work set?
			if(cost!=null && !workSet.contains(cost)) {
				workSet.add(cost);				
			}
					
		}
		
		// finished
		return workSet;
		
	}	
	
	private void estimate(List<RouteCost> changed, long tic) {
		
		// initialize heavy count
		int heavyCount = 0;
		
		// initialize local lists
		List<RouteCost> heavySet = new ArrayList<RouteCost>(m_costs.size());
		List<RouteCost> estimated = new ArrayList<RouteCost>(m_costs.size());
		Map<IAssignmentIf,Calendar> estimates = new HashMap<IAssignmentIf, Calendar>(m_costs.size()); 
		
		// get current work set
		List<RouteCost> workSet = getWorkSet(changed,false);
		
		// loop over costs in work set
		for(RouteCost cost : workSet) {
			
			// ensure that the MAX_WORK_TIME is only exceeded once
			if(System.currentTimeMillis()-tic>m_availableTime)
				break;
			
			// execute work cycle
			try {
				// add to heavy list?
				if(heavyCount > 0 && cost.isSpatialChanged()) {
					heavySet.add(cost);				
				}
				else {

					// increment heavy count?
					if(cost.isSpatialChanged()) heavyCount++;
					
					// forward
					cost.estimate();

					// update assignment?
					IAssignmentIf assignment = getAssignment(cost);
					if(assignment!=null) {
						// get estimates
						Calendar eta = cost.eta();
						Calendar current = assignment.getTimeEstimatedFinished();
						// update estimate?
						if(!equal(eta,current)) {
														
							// add to updates
							estimates.put(assignment, eta);

						}
						// add to estimated
						estimated.add(cost);
					}
				}				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// forward
		submit(estimates);
		
		// remove estimated from work set
		workSet.removeAll(estimated);
		
		// set remaining as next work set
		setWorkResidue(workSet);
		
		// update heavy list
		setHeavySet(heavySet);		
		
	}			
	
	private void progress(List<RouteCost> changed, long tic) {
		
		// get current work set
		List<RouteCost> workSet = getWorkSet(changed,true);
		
		// loop over costs in work set
		for(RouteCost cost : workSet) {
			
			// ensure that the MAX_WORK_TIME is only exceeded once
			if(System.currentTimeMillis()-tic>m_availableTime)
				break;
			
			// forward
			try {
				cost.progress();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		// notify
		fireModified(workSet,1);		
		
	}		
	
	private void submit(Map<IAssignmentIf,Calendar> changes) {
		// any data to submit?
		if(changes.size()>0) {
			try {
				// create unsafe work
				IDiskoWork<Void> work = new UpdateWork(changes);
				// schedule work
				DiskoWorkPool.getInstance().schedule(work);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean equal(Calendar t1, Calendar t2) {
		return t1 == t2 || (t1 != null && t1.equals(t2));
	}
	
	private List<RouteCost> getWorkSet(List<RouteCost> changed, boolean progress) {
		
		// create new work set 
		List<RouteCost> list = new ArrayList<RouteCost>(m_costs.size());
		
		if(progress) {
			
			// get costs ready to progress
			for(RouteCost it: m_costs.values()) {
				if(!changed.contains(it)) {
					IAssignmentIf assignment = getAssignment(it);
					if(!(assignment == null || it.isDirty() || it.isArchived())) {
						IUnitIf unit = assignment.getOwningUnit();
						if(unit!=null && UnitStatus.WORKING.equals(unit.getStatus())) {
							list.add(it);
						}
					}
				}
			}
			
		}
		else {
		
			// add the rest
			for(RouteCost it: m_heavySet) {
				// add to work list?
				if(!(it.isSuspended() || it.isArchived() || list.contains(it)))			
					list.add(it);
			}
			
			// add the rest from last time to prevent
			// starvation of costs in the back of m_costs.values() 
			for(RouteCost it: m_residueSet) {
				if(!list.contains(it))
					list.add(it);
			}		
			
			// add the missing, and remove all archived costs 
			for(RouteCost it : changed) {
				
				// add to work list?
				if(!(it.isSuspended() || it.isArchived() || list.contains(it)))
					list.add(it);
				
				// remove archived costs
				if(it.isArchived()) {
					m_archived.put(getAssignment(it), it);
					if(list.contains(it)) 
						list.remove(it);
				}
				
			}
			
		}
		
		// finished
		return list;
		
	}
	
	private void addToResidue(RouteCost cost) {
		if(cost!=null && !m_residueSet.contains(cost)) {
			m_residueSet.add(cost);
		}
	}
	
	private void setWorkResidue(List<RouteCost> list) {
		m_residueSet.clear();
		m_residueSet.addAll(list);
	}
	
	private void setHeavySet(List<RouteCost> list) {
		m_heavySet.clear();
		m_heavySet.addAll(list);
	}
	
	/* =========================================================================
	 * Inner classes
	 * ========================================================================= */
	
    public class UpdateWork extends AbstractDiskoWork<Void> {
		
    	Map<IAssignmentIf,Calendar> m_changes;
    	
		public UpdateWork(Map<IAssignmentIf,Calendar> changes) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					null,0,false,true,false,0);
			// prepare
			m_changes = changes;
		}
		
		public Void doWork()  {
			
			/* ========================================================
			 * IMPORTANT:  An assignment should only be added to 
			 * updates for commit if and only if the assignment is 
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
			 * For instance, lets look at an example where an assignment 
			 * is created locally and thus, not committed to SARA yet. 
			 * If this thread performs a full commit on all changed objects,
			 * the concurrent work sets are also committed. This may not
			 * be what the user expects to happen, the user may instead
			 * want to rollback the changes. This will not be possible 
			 * any longer because this thread already has committed all 
			 * the changes. Hence, these precautions should be is in the 
			 * concurrent work sets best interest, an the least invasive 
			 * ones. 
			 * ======================================================== */
			
			// initialize local lists
			List<RouteCost> costs = new ArrayList<RouteCost>(m_changes.size());
			List<IUpdateHolderIf> updates = new ArrayList<IUpdateHolderIf>(m_changes.size());				
			
			// loop over all assignments
			for(IAssignmentIf it : m_changes.keySet()) {
			
				// add cost to list
				costs.add(getCost(it));
				
				// get estimate
				Calendar eta = m_changes.get(it); 
				
				// update assignment
				it.setTimeEstimatedFinished(eta);				
				
				// add to updates?
				if(it.isCreated()) {
					// get update holder set
					IUpdateHolderIf holder = m_comitter.getUpdates(it);
					// has updates?
					if(holder!=null) {
						// is modified?
						if(!holder.isCreated() && holder.isModified()) {
							// set partial update
							holder.setPartial(it.getTimeEstimatedFinishedAttribute().getName());
							// add to updates?										
							updates.add(holder);
						}
					}
				}			
			}
			
			// notify listeners
			fireModified(costs,0);
			
			try {
				// commit changes?
				if(updates.size()>0) {
					m_comitter.commit(updates);
				}
			} catch (CommitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			// finished
			return null;
		}
		
	}
}
