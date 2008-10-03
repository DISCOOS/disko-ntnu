package org.redcross.sar.wp.simulator;

import java.util.Comparator;

import org.redcross.sar.gui.model.MsoObjectTableModel;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.mso.Selector;

public class AssignmentTableModel extends MsoObjectTableModel<IAssignmentIf> {

	private static final long serialVersionUID = 1L;
	private static final String NAME = "name";
	private static final String STATUS = "status";
	private static final String UNIT = "unit";
	private static final String ETA = "timeestimatedfinished";
	private static final String CHANGED = "changed";
	private static final String[] ATTRIBUTES = new String[]{NAME, UNIT, ETA, CHANGED, STATUS};
	private static final String[] CAPTIONS = new String[]{"Oppdrag","Enhet","ETA", "Endret","Status"};	

	private final Selector<IAssignmentIf> m_assignmentSelector = new Selector<IAssignmentIf>()
	{
		
		public boolean select(IAssignmentIf msoObj)
		{
			if(msoObj!=null ) {
				// get history flag
				boolean isHistory = (msoObj.hasBeenAborted() || msoObj.hasBeenFinished());
				// finished
				return m_archived ? isHistory : !isHistory;	
			}
			return false;
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
	
	private boolean m_archived;
	
	public AssignmentTableModel(IMsoModelIf model, boolean archived) {
		// forward
		super(model, MsoClassCode.CLASSCODE_ASSIGNMENT, ATTRIBUTES, CAPTIONS);
		// prepare
		m_archived = archived;
		// get command post
		ICmdPostIf cmdPost = model.getMsoManager().getCmdPost();
		// load data?
		if(cmdPost!=null) {
			load(cmdPost.getAssignmentList());
		}
	}

	protected Object getMsoValue(IMsoObjectIf msoObj, String name) {
		if(NAME.equals(name)) {
			return (IAssignmentIf)msoObj;
		}
		else if(UNIT.equals(name)) {
			return ((IAssignmentIf)msoObj).getOwningUnit();
		}
		else if(CHANGED.equals(name)) {
			return ((IAssignmentIf)msoObj);
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
