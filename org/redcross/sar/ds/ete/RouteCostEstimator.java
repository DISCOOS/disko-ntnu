package org.redcross.sar.ds.ete;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.redcross.sar.app.Utils;
import org.redcross.sar.ds.IDsIf;
import org.redcross.sar.modelDriver.IModelDriverIf;
import org.redcross.sar.mso.ICommitManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.committer.IUpdateHolderIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.MsoCompareRoute;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.except.CommitException;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.Route;
import org.redcross.sar.util.mso.Track;

public class RouteCostEstimator extends AbstractDiskoWork<Boolean> 
								implements IDsIf<Boolean>, 
								           IMsoUpdateListenerIf {

	private static final long DUTY_CYCLE_TIME = 2000;	// Invoke every DUTY_CYCLE_TIME
	
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
	private final Map<IAssignmentIf,AssignmentStatus> m_statusList = 
		new HashMap<IAssignmentIf,AssignmentStatus>();
	
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
	private final List<RouteCost> m_workSet = 
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
	
	/**
	 * Return estimated time enroute using current offset
	 */	
	public synchronized int ete(IAssignmentIf assignment) {
		// get cost
		RouteCost cost = m_costs.get(assignment);
		// has cost?
		if(cost!=null) {
			// found
			return cost.ete();
		}
		// no cost found
		return -1;
	}
	
	/**
	 * Return estimated time of arrival from current offset position
	 * @param IAssignmentIf assignment - Assignment to get estimate for
	 */	
	public synchronized Calendar eta(IAssignmentIf assignment) {
		// get cost
		RouteCost cost = m_costs.get(assignment);
		// has cost?
		if(cost!=null) {
			// found
			return cost.eta();
		}
		// no cost found
		return null;
	}
	
	/**
	 * Estimated time of arrival at point from current offset position (unit position or start)
	 * @param Position to - Closest arrival point on route
	 * @param IAssignmentIf assignment - Assignment to get estimate for
	 * @return Estimated time of arrival at point from registered start time (t0), and
	 * current offset position. The offset position is updated from unit position changes. 
	 * If no unit position is found, start is used. The start time is set to execution start time  
	 * of assignment. If assignment is not started, then estimation start time is used.
	 */ 	
	public synchronized Calendar eta(Position to, IAssignmentIf assignment) {
		// get cost
		RouteCost cost = m_costs.get(assignment);
		// has cost?
		if(cost!=null) {
			int i = -1;
			try {
				cost.findNearest(to.getGeoPos());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// found?
			if(i>=0) {
				// get estimate
				return cost.eta(i);
			}
		}
		// no cost found
		return null;
	}
	
	/**
	 * Estimated time of arrival from current offset position (unit position or start)
	 * @param Calendar t0 - Start time of travel
	 * @param IAssignmentIf assignment - Assignment to get estimate for
	 * @return Return estimated time of arrival from current offset position. The offset 
	 * position is updated from unit position changes. If no unit position is found, 
	 * start is used.  
	 * <b>IMPORTANT</b>: This estimate is only a time-shift of current estimate. 
	 * Because the estimated cost is dependent on time, the returned estimate be comes 
	 * less accurate with increasing time shift.
	 */	
	public synchronized Calendar eta(Calendar t0, IAssignmentIf assignment) {
		// get cost
		RouteCost cost = m_costs.get(assignment);
		// has cost?
		if(cost!=null) {
			// get estimated time enroute
			int ete = cost.ete();
			// add seconds to calendar
			t0.add(Calendar.SECOND, ete);
			// finished
			return t0;
		}
		// no cost found
		return null;
	}

	
	/**
	 * Estimated time of arrival at closest position on route from current offset 
	 * position (unit position or start)
	 * @param Calendar t0 - Start time of travel
	 * @param Position at - Closest arrival point on route
	 * @param IAssignmentIf assignment - Assignment to get estimate for
	 * @return Return estimated time of arrival at closest position on route from current 
	 * offset position. The offset position is updated from unit position changes. If no unit 
	 * position is found, start is used. 
	 * <b>IMPORTANT</b>: This estimate is only a time-shift of 
	 * current estimate. Because the estimated cost is dependent on time, the returned estimate 
	 * be comes less accurate with increasing time shift.
	 */	
	public synchronized Calendar eta(Calendar t0, Position at, IAssignmentIf assignment) {
		// get cost
		RouteCost cost = m_costs.get(assignment);
		// has cost?
		if(cost!=null) {
			int i = -1;
			try {
				cost.findNearest(at.getGeoPos());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// found?
			if(i>=0) {
				// get estimated time enroute
				int ete = cost.ete(i);
				// add seconds to calendar
				t0.add(Calendar.SECOND, ete);
				// finished
				return t0;				
			}
		}
		// no cost found
		return null;
	}
	
	/* ===========================================
	 * Public methods
	 * ===========================================
	 */
	
	public RouteCost getCost(IAssignmentIf assignment) {
		return m_costs.get(assignment);
	}
	
	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return msoInterests.contains(aMsoObject.getMsoClassCode());
	}	

	public synchronized void handleMsoUpdateEvent(Update e) {
			
		// consume?
		if(!m_oprID.equals(m_driver.getActiveOperationID())) return;
		
		// not a clear all event?
		if(!e.isClearAllEvent()) {
		
			// get flags
	        boolean deletedObject  = e.isDeleteObjectEvent();
	        boolean modifiedObject = e.isModifyObjectEvent();
	        boolean modifiedReference = e.isChangeReferenceEvent();
	        boolean resume = false;
	        
			// is object modified?
			if (deletedObject || modifiedObject || modifiedReference) {
				resume = m_queue.add(e);
			}
			
			/*
			// estimate expired? 
			if(resume) {
				// this ensures faster service when estimates expires on updates
				m_workPool.resume(this);
			}
			*/
			
		}
		
	}	

	private boolean msoObjectChanged(IMsoObjectIf msoObj, Update e) {
		
		// initialize
		boolean bFlag = false;
		
		// translate
		if(msoObj instanceof IAssignmentIf) {
			// cast to IAssignmentIf
			IAssignmentIf assignment = (IAssignmentIf)msoObj;
			// get routes?
			if(e.isChangeReferenceEvent()) {
				// forward
				bFlag = setEstimate(assignment);
			}
			
			// get start time or is finished?
			if(e.isModifyObjectEvent()) {
				// forward
				setAssignmentStatus(assignment);				
				// set flag
				bFlag = true;
			}			
		}
		else if(msoObj instanceof IRouteIf) {
			// cast to IRouteIf
			IRouteIf msoRoute = (IRouteIf)msoObj;
			// get assignment
			IAssignmentIf assignment = getRouteAssignment(msoRoute);
			// has assignment?
			if(assignment!=null) {
				// forward
				bFlag = setEstimate(assignment);
			}
		}			
		else if(msoObj instanceof IUnitIf) {
			// cast to IUnitIf 
			IUnitIf msoUnit = (IUnitIf)msoObj;
			// forward
			setPosition(msoUnit);
		}
		return bFlag;
	}

	private boolean msoObjectDeleted(IMsoObjectIf msoObj) {

		// initialize
		boolean bFlag = false;
		
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
			IAssignmentIf assignment = getRouteAssignment(msoRoute);
			// has assignment registered?
			if(assignment!=null) {
				// forward
				bFlag = setEstimate(assignment);
			}
		}		
		return bFlag;
	}	
		
	private boolean setAssignmentStatus(IAssignmentIf assignment) {
		
		// initialize
		boolean bFlag = false;
		
		// get status
		AssignmentStatus next = assignment.getStatus();
		AssignmentStatus current = m_statusList.get(assignment);
				
		// translate status change to action
		switch(next) {
		case READY:
		case QUEUED:
		case ASSIGNED:
		case EXECUTING:
			
			/* =================================================================
			 * Only update if status has changed to a legal state. If an
			 * assignment is finished or reported, it can not be reactivated.
			 * 
			 * Offset (current position of executing unit and time at this point) 
			 * is only set when assignment status is changed to EXECUTING
			 * ================================================================= */
			
			// any change?
			if(!(next.equals(current) || 
				 IAssignmentIf.FINISHED_AND_REPORTED_SET.contains(current))) {
				// set flag
				m_statusList.put(assignment,next);
				// forward?
				setOffset(assignment);
			}			
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
			
			// any change?
			if(!IAssignmentIf.FINISHED_AND_REPORTED_SET.contains(current)) {
				// update
				m_statusList.put(assignment,next);
				// forward
				archiveEstimate(assignment, null);
			}			
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
			if(!IAssignmentIf
					.FINISHED_AND_REPORTED_SET.contains(
							assignment.getStatus())) {
				
				// get sorted route list
				List<IRouteIf> list = getRouteSequence(assignment);
				
				// update list
				m_routes.put(assignment,list);
				
				// forward
				bFlag = setRoute(assignment,list);
				
				// forward
				setAssignmentStatus(assignment);
												
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
				cost.archive(null);
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
		RouteCost cost = new RouteCost(route,0,Utils.getApp().getMapManager().getPrintMap());
		// add to costs and assignments
		m_costs.put(assignment, cost);	
		m_assignments.put(cost,assignment);	
		// finished
		return cost;
	}
	
	private IAssignmentIf getRouteAssignment(IRouteIf msoRoute) {
		for(IAssignmentIf it : m_routes.keySet()) {
			List<IRouteIf> list = m_routes.get(it); 
			if(list.contains(msoRoute)) 
				return it;
		}
		return null;
	}
	
	private void setOffset(IAssignmentIf assignment) {
		if(assignment!=null) {
			// can set offset?
			if(AssignmentStatus.EXECUTING.equals(assignment.getStatus())){
				// get cost
				RouteCost cost = m_costs.get(assignment);
				// has cost?
				if(cost!=null) {
					// set start time and position
					cost.shift(assignment.getTimeStarted(),0);
					// get owning unit
					IUnitIf unit = assignment.getOwningUnit();
					// set position?
					if(unit!=null) {
						// get position
						Position p = unit.getPosition();
						if(p!=null) {
							try {
								int offset = cost.findNearest(p.getGeoPos());
								cost.setStartPosition(offset!=-1?offset:0);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
	
	private void setPosition(IUnitIf msoUnit) {
		if(msoUnit!=null) {
			// get position
			Position p = msoUnit.getPosition();
			// has position?
			if(p!=null) {
				// get active assignment
				IAssignmentIf assignment = msoUnit.getActiveAssignment();
				// has active assignment?
				if(assignment!=null) {
					// get current route cost
					RouteCost cost = m_costs.get(assignment);
					// update offset position?
					if(cost==null) {
						try {
							int offset = cost.findNearest(p.getGeoPos());
							cost.setStartPosition(offset!=-1?offset:0);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public void load() {
		if(MsoModelImpl.getInstance().getMsoManager().operationExists()) {
			// forward
			for(IAssignmentIf it : MsoModelImpl.getInstance().getMsoManager().getCmdPost().getAssignmentListItems()) {
				setEstimate(it);
			}
		}
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
		 * spatial route estimation is time consuming (accessing ArcGIS
		 * geodata). A concurrent FIFO queue is used to  store new 
		 * MSO Update events between work cycles.

		 * ALGORITHM: The following algorithm is implemented
		 * 1. Get next Update event if exists. Update if new route is 
		 *    created or existing is changed spatially. 
		 * 2. Update all route cost estimates (fast if no spatial changes)
		 * 
		 * DUTY CYCLE MANAGEMENT: MAX_WORK_TIME is the cutoff work duty 
		 * cycle time. The actual duty cycle time may become longer, 
		 * but not longer than a started update or estimate step. 
		 * update() is only allowed to exceed MAX_WORK_TIME/2. The 
		 * remaining time is given to estimate(). This ensures that 
		 * update() will not starve estimate() during large update 
		 * sequences.
		 * 
		 * ============================================================= */
		
		
		// ensure concurrent updates of estimator
		synchronized(this) {
			
			// get start tic
			long tic = System.currentTimeMillis();
			
			// handle updates in queue
			update(tic);
			
			// calculate estimated based on changes
			estimate(tic);
			
			/*
			SimpleDateFormat format = new SimpleDateFormat("hh:MM:ss");
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(System.currentTimeMillis());
			System.out.println("RouteCostEstimator:: " + format.format(c.getTime()));
			*/
			
			// this method will return when finished, or if
			// MAX_WORK_TIME is exceeded.

		}
		
		// finished
		return true;
	}
	
	private boolean update(long tic) {

		// initialize
		int count = 0;
		
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
			
			// is object modified?
			if (!deletedObject && (modifiedObject || modifiedReference)) {
				msoObjectChanged(msoObj,e);
			}
			
			// delete object?
			if (deletedObject) {
				msoObjectDeleted(msoObj);
			}			
					
		}
		
		// finished
		return count>0;
		
	}	
	
	private void estimate(long tic) {
		
		// initialize heavy count
		int heavyCount = 0;
		
		// initialize local lists
		List<IUpdateHolderIf> updates = new ArrayList<IUpdateHolderIf>(10);
		List<RouteCost> heavySet = new ArrayList<RouteCost>(m_costs.size());
		List<RouteCost> estimated = new ArrayList<RouteCost>(m_costs.size());
		
		// get current work set
		List<RouteCost> workSet = getWorkSet();
		
		// suspend updates when updating estimates
		MsoModelImpl.getInstance().suspendClientUpdate();
		
		// loop over all cost
		for(RouteCost cost : workSet) {
			
			// ensure that the MAX_WORK_TIME is only exceeded once
			if(System.currentTimeMillis()-tic>m_availableTime)
				break;
			
			// execute work cycle
			try {
				// add to heavy list?
				if(heavyCount > 0 && cost.isReplaced()) {
					heavySet.add(cost);				
				}
				else {

					// increment heavy count?
					if(cost.isReplaced()) heavyCount++;
					
					// forward
					cost.estimate();

					// update assignment?
					IAssignmentIf assignment = m_assignments.get(cost);
					if(assignment!=null) {
						// get estimates
						Calendar eta = cost.eta();
						Calendar current = assignment.getTimeEstimatedFinished();
						// update estimate?
						if(eta!=null &&(current!=eta || 
								current.getTimeInMillis()!=eta.getTimeInMillis())) {
							
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
							
							// update assignment
							assignment.setTimeEstimatedFinished(eta);
							
							SimpleDateFormat format = new SimpleDateFormat("hh:MM:ss");
							//Calendar c = Calendar.getInstance();
							//c.setTimeInMillis(System.currentTimeMillis());
							System.out.println("RouteCostEstimator:: " + MsoUtils.getAssignmentName(assignment, 1) + " " + format.format(eta.getTime()));			
							
							// add to updates?
							if(assignment.isCreated()) {
								// get update holder set
								IUpdateHolderIf holder = m_comitter.getUpdates(assignment);
								// has updates?
								if(holder!=null) {
									// is modified?
									if(holder.isModified()) {
										// set partial update
										holder.setPartial(assignment.getTimeEstimatedFinishedAttribute().getName());
										// add to updates?										
										updates.add(holder);
									}
								}
							}
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
		
		try {
			// commit changes?
			if(updates.size()>0) {
				m_comitter.commit(updates);
			}
		} catch (CommitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// resume updates
		MsoModelImpl.getInstance().resumeClientUpdate();				
		
		// remove estimated from work set
		workSet.removeAll(estimated);
		
		// set remaining as next work set
		setWorkSet(workSet);
		
		// update heavy list
		setHeavySet(heavySet);
		
		
	}			
	
	private List<RouteCost> getWorkSet() {
		
		// create new work set 
		List<RouteCost> list = new ArrayList<RouteCost>(m_costs.size());
		
		// add the rest
		for(RouteCost it: m_heavySet) {
			// add to work list?
			if(!(it.isSuspended() || it.isArchived()))			
				list.add(it);
		}
		
		// start with heavy work
		//list.addAll(m_heavySet);
		
		// add the rest from last time to prevent
		// starvation of costs in the back of m_costs.values() 
		for(RouteCost it: m_workSet) {
			list.add(it);
		}		
		
		// add the missing, and remove all archived costs 
		for(IAssignmentIf it : m_costs.keySet()) {						
			
			// get cost
			RouteCost cost = m_costs.get(it);
			
			// add to work list?
			if(!(cost.isSuspended() || cost.isArchived() || list.contains(cost)))
				list.add(cost);
			
			// remove archived costs
			if(cost.isArchived()) {
				m_archived.put(it, cost);
				if(list.contains(cost)) 
					list.remove(cost);
			}
			
		}
		
		// replace current work set
		setWorkSet(list);
		
		// finished
		return list;
		
	}
	
	private void setWorkSet(List<RouteCost> list) {
		m_workSet.clear();
		m_workSet.addAll(list);
	}
	
	private void setHeavySet(List<RouteCost> list) {
		m_heavySet.clear();
		m_heavySet.addAll(list);
	}
	
}
