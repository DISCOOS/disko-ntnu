package org.redcross.sar.wp.unit;

import org.redcross.sar.gui.model.AbstractMsoTableModel;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;

/**
 * Personnel data for current call-out in details panel
 *
 * @author thomasl, kennetgu
 */
public class CalloutPersonnelTableModel extends AbstractMsoTableModel<IPersonnelIf>
{
	private static final long serialVersionUID = 1L;

	private static final String NAME = "name";
	private static final String STATUS = "status";
	private static final String EDIT = "edit";

	private IPersonnelListIf m_list;

	/* ===============================================================
	 * Constructors
	 * =============================================================== */

	public CalloutPersonnelTableModel()
	{
		// forward
		super(IPersonnelIf.class,false);
		// create table
		create(getNames(),getCaptions());
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
		else if(STATUS.equals(column))
			return personnel.getImportStatusText();
		else if(EDIT.equals(column))
			return personnel;
		// not found
		return null;
	}

	protected void cleanup(IUnitIf id, boolean finalize) {
		if(finalize) m_list = null;
	}

	/* ===============================================================
	 * AbstractTableModel implementation
	 * =============================================================== */

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return column == 2;
	}

	/* ===============================================================
	 * Public methods
	 * =============================================================== */

	public IPersonnelListIf getPersonnelList() {
		return m_list;
	}

	public void setPersonnelList(IMsoModelIf model, IPersonnelListIf list)
	{

		// prepare
		m_list = list;

		// install model?
		if(list!=null) {
			connect(model,list,IPersonnelIf.PERSONNEL_NAME_COMPARATOR);
			load(list);
		}
		else {
			disconnectAll();
			clear();
		}
	}

	public IPersonnelIf getPersonnel(int row)
	{
		return getId(row);
	}

	/* ===============================================================
	 * Helper methods
	 * =============================================================== */

	public String[] getNames() {
		return new String[] {NAME, STATUS, EDIT};
	}

	public String[] getCaptions() {
		return new String[] {"Navn", "Status", "Endre"};
	}

}
