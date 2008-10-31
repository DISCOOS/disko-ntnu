package org.redcross.sar.gui.mso.model;

import java.util.EnumSet;
import java.util.List;

import org.redcross.sar.data.Selector;
import org.redcross.sar.gui.model.AbstractMsoTableModel;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.AbstractUnit;
import org.redcross.sar.mso.data.AssignmentImpl;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.util.MsoUtils;

public class UnitTableModel extends AbstractMsoTableModel<IUnitIf> {

	private static final long serialVersionUID = 1L;

	public static final String UNIT = "unit";
	public static final String PERSONNEL = "personnel";
	public static final String ASSIGNMENTS = "assignments";
	public static final String REMARKS = "remarks";
	public static final String STATUS = "status";

	public static final String[] NAMES = new String[]{ UNIT,PERSONNEL,ASSIGNMENTS,REMARKS,STATUS };
	public static final String[] CAPTIONS = new String[]{	"Enhet","Antall mnsk.","Oppdrag i kø","Merknader","Status" };

	/* =============================================================
	 * Constructors
	 * ============================================================= */

	public UnitTableModel(IMsoModelIf msoModel) {
		this(msoModel,IUnitIf.ACTIVE_RANGE);
	}

	public UnitTableModel(IMsoModelIf msoModel, EnumSet<UnitStatus> status) {
		// forward
		super(IUnitIf.class,NAMES,CAPTIONS,false);
		// install model
		connect(msoModel,createDefaultSelector(status),IUnitIf.UNIT_TYPE_AND_NUMBER_COMPARATOR);
		load(msoModel.getMsoManager().getCmdPost().getUnitList());
	}

	/* =============================================================
	 * Public methods
	 * ============================================================= */

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return AbstractUnit.class;
		case 1:
			return AssignmentImpl.class;
		case 2:
			return AssignmentImpl.class;
		case 3:
			return AssignmentImpl.class;
		case 4:
			return UnitStatus.class;
		default:
			return Object.class;
		}
	}



	/* =============================================================
	 * MsoTableModel implementation
	 * ============================================================= */

	@Override
	protected Object getCellValue(int row, String column) {
		if(UNIT.equals(column)) {
			return getId(row);
		}
		else if(PERSONNEL.equals(column)) {
			return getId(row).getUnitPersonnelItems().size();
		}
		else if(ASSIGNMENTS.equals(column)) {
			return getAllocated(getId(row));
		}
		else if(REMARKS.equals(column)) {
			return getId(row).getRemarks();
		}
		else if(STATUS.equals(column)) {
			return getId(row).getStatus();
		}
		return null;
	}

	/* =============================================================
	 * Helper methods
	 * ============================================================= */

	private String getAllocated(IUnitIf unit) {
		// initialize
		String text = "Ingen";
		// get assignment
		List<IAssignmentIf> data = unit.getEnqueuedAssignments();
		// build string
		for(int i=0;i<data.size();i++) {
			// get name
			String name = MsoUtils.getAssignmentName(data.get(i),1).toLowerCase();
			text = (i==0 ? name : text +  "; " + name);
		}
		return text;
	}

	private static Selector<IUnitIf> createDefaultSelector(final EnumSet<UnitStatus> status) {
		Selector<IUnitIf> selector = new Selector<IUnitIf>() {
	        public boolean select(IUnitIf aUnit)
	        {
	            return (status.contains(aUnit.getStatus()));
	        }
	    };
		return selector;
	}

}
