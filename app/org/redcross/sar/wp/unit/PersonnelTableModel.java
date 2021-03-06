package org.redcross.sar.wp.unit;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.ITranslator;
import org.redcross.sar.gui.model.AbstractMsoTableModel;

/**
 * Table model for the personnel overview panel
 *
 * @author thomasl, kenneth
 */
@SuppressWarnings("unchecked")
public class PersonnelTableModel extends AbstractMsoTableModel<IPersonnelIf>
{
	private static final long serialVersionUID = 1L;

	private static final String NAME = "name";
	private static final String UNIT = "unit";
	private static final String VIEW = "view";
	private static final String STATUS = "status";
	private static final String RELEASED = "released";
	
	/* ===============================================================
	 * Constructors
	 * =============================================================== */

	public PersonnelTableModel(IMsoModelIf model)
	{

		// forward
		super(IPersonnelIf.class,getNames(),getCaptions(),false);

		// install model
		connect(model, IPersonnelIf.REINSTATE_SELECTOR, IPersonnelIf.PERSONNEL_NAME_COMPARATOR);

		// add co-data classes
		getMsoBinder().addCoClass(IUnitIf.class, IUnitIf.ALL_SELECTOR);

		// set co-data translator
		setTranslator(m_translator);

		// load personnel from attendance list
		load(model.getMsoManager().getCmdPost().getAttendanceList());

	}

	/* ===============================================================
	 * MsoTableModel implementation
	 * =============================================================== */

	protected Object getCellValue(int row, String column) {
		// get personnel
		IPersonnelIf personnel = getId(row);
		// translate
		if(NAME.equals(column)) {
			String name = MsoUtils.getPersonnelName(personnel, false);
			return personnel.isChanged()?name.concat("*"):name;
		}
		else if(UNIT.equals(column)) {
			// get current unit
            IUnitIf unit = personnel.getOwningUnit();
            return unit == null ? "" : MsoUtils.getUnitName(unit,false);
		}
		else if(VIEW.equals(column))
			return personnel;
		else if(RELEASED.equals(column)) {
			List<IPersonnelIf> list = personnel.getPreviousOccurrences();
			return String.valueOf(list!=null?list.size():0);
		}
		else if(STATUS.equals(column))
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
		return columnIndex > 2;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		default:
			return Object.class;
		}
	}


	/* ===============================================================
	 * Public methods
	 * =============================================================== */

	/**
	 * @param row
	 * @return Personnel at given row in table
	 */
	public IPersonnelIf getPersonnel(int row)
	{
		return getId(row);
	}

	/**
	 * @param row
	 * @return Personnel effort history at given row in table
	 */
	public List<IPersonnelIf> getHistory(int row)
	{
		return (List<IPersonnelIf>)getValueAt(row, 4);
	}
	
	/* ===============================================================
	 * Helper methods
	 * =============================================================== */

	public static String[] getNames() {
		return new String[] {NAME, UNIT, RELEASED, VIEW, STATUS};
	}

	public static String[] getCaptions() {
		return new String[] {"Navn", "Underordnet", "Dimittert", "Vis", "Endre status"};
	}

	/* ===============================================================
	 * Inner classes
	 * =============================================================== */

	private ITranslator<IPersonnelIf, IData> m_translator = new ITranslator<IPersonnelIf, IData>() {

		@Override
		public IPersonnelIf[] translate(IData[] data) {
			Collection<IPersonnelIf> list = new Vector<IPersonnelIf>();
			for(IData item : data) {
				if(item instanceof IUnitIf) {
					// add all
					list.addAll(impl.getIds());
				}
			}
			// get found personnel
			data = new IPersonnelIf[list.size()];
			list.toArray(data);
			// finished
			return (IPersonnelIf[])data;
		}

	};

}
