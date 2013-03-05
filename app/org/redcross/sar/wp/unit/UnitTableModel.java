package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.gui.model.AbstractMsoTableModel;

public class UnitTableModel extends AbstractMsoTableModel<IUnitIf>
{
	private static final long serialVersionUID = 1L;

	public static final String NAME = "name";
	public static final String VIEW = "view";
	public static final String EDIT = "edit";

	public static final String[] NAMES = new String[] { NAME, VIEW, EDIT };
	public static final String[] CAPTIONS = new String[] { "Enhet", "Vis", "Endre status" };

	/* ================================================================
	 *  Constructors
	 * ================================================================ */

	public UnitTableModel(IMsoModelIf model)
	{
		// forward
		super(IUnitIf.class,NAMES,CAPTIONS,false);

		// install model
		connect(model,IUnitIf.ALL_SELECTOR,IUnitIf.TYPE_AND_NUMBER_COMPARATOR);

		// load units from model
		load(model.getMsoManager().getCmdPost().getUnitList());

	}

	/* ================================================================
	 *  MsoTableModel implementation
	 * ================================================================ */

	protected Object getCellValue(int row, String column) {

		// translate
		if(NAME.equals(column))
		{
			IUnitIf unit = getId(row);
			String name = MsoUtils.getUnitName(getId(row), true);			
			return unit.isChanged()?name.concat("*"):name;
		}
		else if(VIEW.equals(column))
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
