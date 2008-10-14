package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;

import org.redcross.sar.gui.model.MsoTableModel;

/**
 * Table model for the personnel overview panel
 *
 * @author thomasl
 */
public class PersonnelTableModel extends MsoTableModel<IPersonnelIf>
{
	private static final long serialVersionUID = 1L;

	private static final String NAME = "name";
	private static final String UNIT = "status";
	private static final String SHOW = "status";
	private static final String EDIT = "edit";

	/* ===============================================================
	 * Constructors
	 * =============================================================== */

	public PersonnelTableModel(IMsoModelIf model)
	{
		// forward
		super(IPersonnelIf.class,false);
		// create table
		create(getNames(),getCaptions());
		// get list
		IPersonnelListIf list = model.getMsoManager().getCmdPost().getAttendanceList();
		// forward
		connect(model, list, IPersonnelIf.PERSONNEL_NAME_COMPARATOR);
		load(list.getItems());
	}

	/* ===============================================================
	 * MsoTableModel implementation
	 * =============================================================== */

	protected Object getCellValue(int row, String column) {
		// get personnel
		IPersonnelIf personnel = getId(row);
		// translate
		if(NAME.equals(column))
			return MsoUtils.getPersonnelName(personnel, false);
		else if(UNIT.equals(column)) {
			// get current unit
            IUnitIf unit = personnel.getOwningUnit();
            return unit == null ? "" : MsoUtils.getUnitName(unit,false);
		}
		else if(SHOW.equals(column))
			return personnel;
		else if(EDIT.equals(column))
			return personnel;
		// not found
		return null;
	}

	/* ===============================================================
	 *  AbstractTableModel implementation
	 * =============================================================== */

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex == 2 || columnIndex == 3;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		default:
			return Object.class;
		}
	}


	/* ===============================================================
	 * Public methods
	 * =============================================================== */

	/**
	 * @param clickedRow
	 * @return Personnel at given row in table
	 */
	public IPersonnelIf getPersonnel(int row)
	{
		return getId(row);
	}

	/* ===============================================================
	 * Helper methods
	 * =============================================================== */

	public String[] getNames() {
		return new String[] {NAME, UNIT, SHOW, EDIT};
	}

	public String[] getCaptions() {
		return new String[] {"Navn", "Underordnet", "Vis", "Endre status"};
	}

}
