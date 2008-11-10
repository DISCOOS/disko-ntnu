package org.redcross.sar.gui.mso.model;

import java.util.EnumSet;

import org.redcross.sar.data.Selector;
import org.redcross.sar.gui.model.AbstractMsoTableModel;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.AssignmentImpl;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentListIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;

public class AssignmentTableModel extends AbstractMsoTableModel<IAssignmentIf> {

	private static final long serialVersionUID = 1L;

	public static final String CHECK = "check";
	public static final String NAME = "name";
	public static final String STATUS = "edit";

	public static final String[] NAMES = new String[] { CHECK, NAME, STATUS };
	public static final String[] CAPTIONS = new String[] { "Velg", "Oppdrag", "Status" };

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public AssignmentTableModel(IMsoModelIf model) {
		this(model,EnumSet.allOf(AssignmentStatus.class));
	}

	public AssignmentTableModel(IMsoModelIf model, EnumSet<AssignmentStatus> status) {
		// forward
		super(IAssignmentIf.class,NAMES,CAPTIONS,false);
		// install model
		IAssignmentListIf list = model.getMsoManager().getCmdPost().getAssignmentList();
		connect(model,createDefaultSelector(status),IAssignmentIf.ASSIGNMENT_TYPE_NUMBER_COMPERATOR);
		load(list);
	}

	/* ================================================================
	 *  MsoTableModel implementation
	 * ================================================================ */

	protected Object getCellValue(int row, String column) {

		// translate
		if(CHECK.equals(column))
			return new Boolean(false);
		else if(NAME.equals(column))
			return getId(row);
		else if(STATUS.equals(column))
			return getId(row);

		// not supported
		return null;

	}


	/* ================================================================
	 *  AbstractTableModel implementation
	 * ================================================================ */

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		case 1:
			return AssignmentImpl.class;
		case 2:
			return AssignmentImpl.class;
		default:
			return Object.class;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return true;
		}
		return false;
	}

	/* ================================================================
	 *  Helper methods
	 * ================================================================ */

	private static Selector<IAssignmentIf> createDefaultSelector(final EnumSet<AssignmentStatus> status) {
		Selector<IAssignmentIf> selector = new Selector<IAssignmentIf>() {
	        public boolean select(IAssignmentIf anAssignment)
	        {
	            return (status.contains(anAssignment.getStatus()));
	        }
	    };
		return selector;
	}


}
