package org.redcross.sar.wp.simulator;

import org.redcross.sar.data.Selector;
import org.redcross.sar.gui.model.MsoTableModel;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;

public class AssignmentTableModel extends MsoTableModel<IAssignmentIf> {

	private static final long serialVersionUID = 1L;

	private static final String NAME = "name";
	private static final String STATUS = "status";
	private static final String UNIT = "unit";
	private static final String ETA = "timeestimatedfinished";
	private static final String CHANGED = "changed";

	private static final String[] NAMES = new String[]{NAME, UNIT, ETA, CHANGED, STATUS};
	private static final String[] CAPTIONS = new String[]{"Oppdrag","Ansvarlig","ETA","Endret","Status"};

	private boolean m_archived;

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public AssignmentTableModel(IMsoModelIf model, boolean archived) {
		// forward
		super(IAssignmentIf.class,NAMES,CAPTIONS,true);
		// prepare
		m_archived = archived;
		// get command post
		ICmdPostIf cmdPost = model.getMsoManager().getCmdPost();
		// install
		connect(model,m_selector,IAssignmentIf.ASSIGNMENT_TYPE_NUMBER_COMPERATOR);
		// load data
		load(cmdPost.getAssignmentList());
	}

	/* ============================================================
	 * MsoTableModel implementation
	 * ============================================================ */

	@Override
	protected Object getCellValue(int row, String column) {
		if(NAME.equals(column)) {
			return getId(row);
		}
		else if(UNIT.equals(column)) {
			return getId(row).getOwningUnit();
		}
		else if(CHANGED.equals(column)) {
			return getId(row);
		}
		// failed
		return null;
	}

	/* ============================================================
	 * Anonymous classes
	 * ============================================================ */

	private final Selector<IAssignmentIf> m_selector = new Selector<IAssignmentIf>()
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

}
