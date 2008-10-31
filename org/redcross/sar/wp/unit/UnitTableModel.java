package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitListIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.gui.model.AbstractMsoTableModel;

public class UnitTableModel extends AbstractMsoTableModel<IUnitIf>
{
	private static final long serialVersionUID = 1L;

	public static final String NAME = "name";
	public static final String UNIT = "unit";
	public static final String EDIT = "edit";

	public static final String[] NAMES = new String[] { NAME, UNIT, EDIT };
	public static final String[] CAPTIONS = new String[] { "Enhet", "Status", "Oppløs" };

	/* ================================================================
	 *  Constructors
	 * ================================================================ */

	public UnitTableModel(IMsoModelIf model)
	{
		// forward
		super(IUnitIf.class,NAMES,CAPTIONS,false);

		// install model
		IUnitListIf list = model.getMsoManager().getCmdPost().getUnitList();
		connect(model,list,IUnitIf.UNIT_TYPE_AND_NUMBER_COMPARATOR);
		load(list);

	}

	/* ================================================================
	 *  MsoTableModel implementation
	 * ================================================================ */

	protected Object getCellValue(int row, String column) {

		// translate
		if(NAME.equals(column))
			return MsoUtils.getUnitName(getId(row), true);
		else if(UNIT.equals(column))
			return getId(row);

		// not supported
		return null;

	}

	/* ================================================================
	 *  AbstractTableModel implementation
	 * ================================================================ */

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex == 1 || columnIndex == 2;
	}

	/* ================================================================
	 *  Public methods
	 * ================================================================ */

	/**
	 * Return unit at given row in table model
	 */
	public IUnitIf getUnit(int row)
	{
		return getId(row);
	}


}
