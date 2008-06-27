package org.redcross.sar.wp.ds;

import java.util.Comparator;

import org.redcross.sar.app.Utils;
import org.redcross.sar.ds.ete.RouteCost;
import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.gui.model.MsoObjectTableModel;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.mso.Selector;

public class AssignmentTableModel extends MsoObjectTableModel<IAssignmentIf> {

	private static final long serialVersionUID = 1L;
	private static final String NAME = "Name";
	private static final String DISTANCE = "Distance";
	private static final String ETE = "ETE";	
	private static final String AVG_SPEED = "Avg_Speed";	
	private static final String[] ATTRIBUTES = new String[]{NAME, "status", 
		"timeestimatedfinished",ETE,DISTANCE,AVG_SPEED};
	private static final String[] CAPTIONS = new String[]{"Oppdrag",
		"Status","ETA","ETE","Avstand","Gj.hast"};
	
	private RouteCostEstimator estimator = null;
	
	
	private static final Selector<IAssignmentIf> m_assignmentSelector = new Selector<IAssignmentIf>()
	{
		public boolean select(IAssignmentIf msoObj)
		{
			return true;
		}
	};

	private static final Comparator<IMsoObjectIf> m_assignmentComparator = new Comparator<IMsoObjectIf>()
	{
		public int compare(IMsoObjectIf o1, IMsoObjectIf o2)
		{
			IAssignmentIf a1 = (IAssignmentIf)o1;
			IAssignmentIf a2 = (IAssignmentIf)o2;
			if(a1.getType() == a2.getType())
			{
				return a1.getNumber() - a2.getNumber();
			}
			else
			{
				return a1.getType().ordinal() - a2.getType().ordinal();
			}
		}
	};
	
	public AssignmentTableModel(IMsoModelIf model) {
		// forward
		super(model, MsoClassCode.CLASSCODE_ASSIGNMENT, ATTRIBUTES, CAPTIONS);
		
		if(model.getMsoManager().operationExists()) {
			// get command post
			ICmdPostIf cmdPost = model.getMsoManager().getCmdPost();
			// load data?
			if(cmdPost!=null) {
				load(cmdPost.getAssignmentList());
			}
		}
	}
	
	public void setEstimator(RouteCostEstimator estimator) {
		this.estimator = estimator;
	}

	protected Object getMsoValue(IMsoObjectIf msoObj, String name) {
		if(NAME.equals(name)) {
			return (IAssignmentIf)msoObj;
		}
		else if(ETE.equals(name)) {
			IAssignmentIf assignment = (IAssignmentIf)msoObj;
			RouteCost cost = estimator!=null ? estimator.getCost(assignment) : null;
			return cost!=null ? Utils.getTime(cost.ete()) : "00:00:00";
		}
		else if(DISTANCE.equals(name)) {
			IAssignmentIf assignment = (IAssignmentIf)msoObj;
			RouteCost cost = estimator!=null ? estimator.getCost(assignment) : null;
			return cost!=null ? Math.round(cost.getDistance()) + " m" : "0 m";
		}
		else if(AVG_SPEED.equals(name)) {
			IAssignmentIf assignment = (IAssignmentIf)msoObj;
			RouteCost cost = estimator!=null ? estimator.getCost(assignment) : null;
			String speed = "0 m/s";
			if(cost!=null) {
				speed = String.valueOf(Math.round(
						(double)cost.getDistance() / (double)cost.ete())) + " m/s"; 
			}
			return speed;
		}
		// failed
		return null;
	}
	
	@Override
	public boolean select(IAssignmentIf msoObj) {
		return m_assignmentSelector.select(msoObj);	
	}

	@Override
	public void sort() {
		sort(m_assignmentComparator);
	}


}
