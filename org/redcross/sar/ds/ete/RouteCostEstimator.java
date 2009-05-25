package org.redcross.sar.ds.ete;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redcross.sar.Application;
import org.redcross.sar.ds.AbstractDsMso;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.MsoCompareRoute;
import org.redcross.sar.util.mso.Route;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

public class RouteCostEstimator extends AbstractDsMso<IAssignmentIf,RouteCost> {

	public enum ETEClassCode {
		CLASSCODE_ROUTECOST
	}
	
	/**
	 * Route Comparator
	 */
	private final MsoCompareRoute m_comparator = new MsoCompareRoute();

	/**
	 * Local list of MSO route objects.
	 */
	private final Map<IAssignmentIf,List<IRouteIf>> m_routes =
		new HashMap<IAssignmentIf,List<IRouteIf>>();

	/**
	 *
	 *
	 */
	private final Map<IAssignmentIf,AssignmentStatus> m_states =
		new HashMap<IAssignmentIf,AssignmentStatus>();

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public RouteCostEstimator(String oprID) throws Exception {

		// forward
		super(RouteCost.class, oprID, EnumSet.of(MsoClassCode.CLASSCODE_ASSIGNMENT,
				 MsoClassCode.CLASSCODE_ROUTE, MsoClassCode.CLASSCODE_UNIT),
				 1000,500,getAttributes());

	}

	/* ============================================================
	 * Public methods
	 * ============================================================ */

	public RouteCost getCost(IAssignmentIf assignment) {
		return getDsObject(assignment);
	}

	public List<RouteCost> getItems() {
		return getDsObjects();
	}

	public Map<IAssignmentIf,RouteCost> getCosts() {
		return getDsMap();
	}

	public synchronized boolean load() {
		// forward
		clear();
		// load lists from available assignments
		if(m_model.getMsoManager().operationExists()) {
			// forward
			for(IAssignmentIf it : m_model.getMsoManager().getCmdPost().getAssignmentListItems()) {
				if(setEstimate(it)) {
					RouteCost c = getCost(it);
					if(!c.isArchived()) {
						addToResidue(c);
					}
				}
				IUnitIf msoUnit = it.getOwningUnit();
				if(msoUnit!=null) {
					if(logPosition(msoUnit)) {
						RouteCost c = getCost(it);
						if(!c.isArchived()) {
							addToResidue(c);
						}
					}
				}
			}
		}
		// notify
		fireAdded();
		fireArchived();
		// finished
		return m_dsObjs.size()>0;
	}

	public synchronized void clear() {
		// forward
		super.clear();
		// cleanup
		m_routes.clear();
	}

	/* ===========================================
	 * Required methods
	 * =========================================== */

	protected RouteCost msoObjectCreated(IMsoObjectIf msoObj, Update e) { return null; }

	protected RouteCost msoObjectChanged(IMsoObjectIf msoObj, Update e) {

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

		// finished
		return cost;
	}

	protected RouteCost msoObjectDeleted(IMsoObjectIf msoObj, Update e) {

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
			if(assignment!=null && !assignment.isDeleted()) {
				// forward
				if(setEstimate(assignment)) {
					cost = getCost(assignment);
				}
			}
		}

		// finished
		return cost;
	}

	protected void execute(List<RouteCost> changed, long tic, long timeOut) {

		// calculate estimated based on changes
		estimate(changed, tic, timeOut);

		// ensure that the MAX_WORK_TIME is only exceeded once
		if(System.currentTimeMillis()-tic<timeOut) {

			// calculate progress
			progress(changed, tic, timeOut);

		}

	}

	/* ===========================================
	 * Helper methods
	 * =========================================== */

	private static Map<MsoClassCode,List<String>> getAttributes() {
		// prepare
		Map<MsoClassCode,List<String>> map = new HashMap<MsoClassCode, List<String>>(1);
		List<String> list = new ArrayList<String>(1);
		list.add("timeestimatedfinished");
		map.put(MsoClassCode.CLASSCODE_ASSIGNMENT, list);
		return map;
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
		case ALLOCATED:
		case EXECUTING:

			// forward
			bFlag = setOffset(assignment);

			break;
		case ABORTED:
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

		// sort routes in ascending order on area sequence number
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

			// get sorted route list
			List<IRouteIf> list = getRouteSequence(assignment);

			// update list
			m_routes.put(assignment,list);

			// forward
			bFlag = setRoute(assignment,list);

			// forward
			bFlag |= updateArguments(assignment);

		}

		// finished
		return bFlag;

	}

	private void archiveEstimate(IAssignmentIf assignment, Track track) {
		if(assignment!=null) {
			RouteCost cost = m_dsObjs.get(assignment);
			if(cost!=null && !cost.isArchived()) {
				// log last position?
				IUnitIf msoUnit = assignment.getOwningUnit();
				if(msoUnit!=null) {
					cost.setLastKnownPosition(msoUnit.getLastKnownPosition());
				}
				// archive cost
				cost.archive();
				// save change
				m_archived.put(assignment, cost);
				m_added.remove(assignment);
			}
		}
	}

	private void removeEstimate(IAssignmentIf assignment) {
		// get cost
		m_routes.remove(assignment);
		// forward
		remove(assignment);
	}

	private boolean setRoute(IAssignmentIf assignment, List<IRouteIf> list) {

		// initialize flag
		boolean bFlag = false;

		// get current cost
		RouteCost cost = m_dsObjs.get(assignment);

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
		RouteCost cost = new RouteCost(assignment,route,0,Application.getInstance().getMapManager().getPrintMap());
		// forward
		add(assignment,cost);
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
		 return m_idObjs.get(cost);
	}

	private boolean setOffset(IAssignmentIf assignment) {

		// initialize
		boolean bFlag = false;

		// assignment exists?
		if(assignment!=null) {

			// get cost
			RouteCost cost = m_dsObjs.get(assignment);

			// valid operation?
			if(!(cost==null || cost.isArchived())) {

				// get status
				AssignmentStatus status = assignment.getStatus();

				// set dirty flag
				bFlag = !status.equals(m_states.put(assignment, status));

				// translate status change to action
				switch(status) {
				case DRAFT:
				case READY:
				case QUEUED:
				case ALLOCATED:
					// reset temporal and sample data?
					if(bFlag) cost.clear(false,true,true);
					// set start time
					bFlag |= cost.setStartTime(Calendar.getInstance());
					break;
				case EXECUTING:
					// set start time
					bFlag |= cost.setStartTime(assignment.getTime(AssignmentStatus.EXECUTING));
					// set last known position
					IUnitIf msoUnit = assignment.getOwningUnit();
					if(msoUnit!=null) {
						bFlag |= logPosition(msoUnit);
					}
					break;
				}
			}
		}
		// finished
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
				// is executing the active assignment?
				if(assignment!=null && AssignmentStatus.EXECUTING.equals(assignment.getStatus())) {
					// get current route cost
					RouteCost cost = m_dsObjs.get(assignment);
					// update offset position?
					if(cost!=null) {
						try {
							bFlag = cost.setLastKnownPosition(p);
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
	 * Estimation implementation
	 * ============================================================ */

	private void estimate(List<RouteCost> changed, long tic, long timeOut) {

		// initialize heavy count
		int heavyCount = 0;

		// get current work set
		List<RouteCost> workSet = getWorkSet(changed,false);

		// initialize local lists
		List<RouteCost> modified = new ArrayList<RouteCost>(workSet.size());
		List<RouteCost> heavySet = new ArrayList<RouteCost>(workSet.size());
		Map<IAssignmentIf,Object[]> estimates = new HashMap<IAssignmentIf, Object[]>(workSet.size());

		// loop over costs in work set
		for(RouteCost cost : workSet) {

			// ensure that the MAX_WORK_TIME is only exceeded once
			if(System.currentTimeMillis()-tic>timeOut)
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
					cost.calculate();

					// add object to list
					modified.add(cost);

					// update assignment?
					IAssignmentIf assignment = getAssignment(cost);
					if(assignment!=null) {
						// initialize
						Calendar eta = null;
						// is executing?
						if(AssignmentStatus.EXECUTING.equals(assignment.getStatus())) {
							// get estimates
							eta = cost.eta();
						}
						// get current
						Calendar current = assignment.getTimeEstimatedFinished();
						// update estimate?
						if(!equal(eta,current)) {

							// add to updates
							estimates.put(assignment, new Object[]{eta});

						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// forward
		schedule(estimates);

		// remove estimated from work set
		workSet.removeAll(modified);

		// set remaining as next work set
		addToResidue(workSet);

		// update heavy list
		setHeavySet(heavySet);

		// notify listeners
		fireModified(modified,0);

	}

	private void progress(List<RouteCost> changed, long tic, long timeOut) {

		// initialize lists
		List<RouteCost> modified = new ArrayList<RouteCost>(changed.size());

		// get current work set
		List<RouteCost> workSet = getWorkSet(changed,true);

		// loop over costs in work set
		for(RouteCost cost : workSet) {

			// ensure that the MAX_WORK_TIME is only exceeded once
			if(System.currentTimeMillis()-tic>timeOut)
				break;

			try {
				// forward
				cost.progress();
				// add to calculated
				modified.add(cost);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// remove estimated from work set
		workSet.removeAll(modified);

		// set remaining as next work set
		addToResidue(workSet);

		// notify
		fireModified(modified,1);

	}

	private boolean equal(Calendar t1, Calendar t2) {
		return t1 == t2 || (t1 != null && t1.equals(t2));
	}

	private List<RouteCost> getWorkSet(List<RouteCost> changed, boolean progress) {

		// create new work set
		List<RouteCost> list = new ArrayList<RouteCost>(m_dsObjs.size());

		if(progress) {

			/* ==========================================
			 * add the residue progress work set from
			 * last work cycle to prevent starvation.
			 * ========================================== */
			for(RouteCost it: m_residueSet) {
				if(it.canProgress() && include(it,list)) list.add(it);
			}

			/* ==========================================
			 * add work that only need progress
			 * calculated.
			 * ========================================== */
			for(RouteCost it: m_dsObjs.values()) {
				if(it.canProgress() && include(it,list)) {
					IAssignmentIf assignment = getAssignment(it);
					if(assignment != null) {
						IUnitIf unit = assignment.getOwningUnit();
						if(unit!=null && UnitStatus.WORKING.equals(unit.getStatus())) {
							list.add(it);
						}
					}
				}
			}

		}
		else {

			/* ==========================================
			 * add heavy work set from last work cycle
			 * to prevent starvation
			 * ========================================== */
			for(RouteCost it: m_heavySet) {
				if(it.isDirty() && include(it,list)) list.add(it);
			}

			/* ==========================================
			 * add the residue work set from last work
			 * cycle to prevent starvation
			 * ========================================== */
			for(RouteCost it: m_residueSet) {
				if(it.isDirty() && include(it,list)) list.add(it);
			}

			// add the missing, and remove all archived costs
			for(RouteCost it : changed) {
				if(it.isDirty() && include(it,list)) list.add(it);
			}

		}

		// remove from residue
		m_residueSet.removeAll(list);

		// finished
		return list;

	}

}
