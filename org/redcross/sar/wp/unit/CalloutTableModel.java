package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.ICalloutListIf;
import org.redcross.sar.gui.model.MsoTableModel;

/**
 * Provides contents for call-out table in overview mode
 *
 * @author thomasl
 */
public class CalloutTableModel extends MsoTableModel<ICalloutIf>
{
	private static final long serialVersionUID = 1L;

	private static final String DTG = "dtg";
	private static final String TITLE = "title";

	/* ===============================================================
	 * Constructors
	 * =============================================================== */

	public CalloutTableModel(IMsoModelIf model)
	{
		// forward
		super(ICalloutIf.class,false);
		// create table
		create(getNames(),getCaptions());
		// get list
		ICalloutListIf list = model.getMsoManager().getCmdPost().getCalloutList();
		// forward
		connect(model, list, ICalloutIf.CALLOUT_COMPARATOR);
		load(list);
	}

	/* ===============================================================
	 * MsoTableModel implementation
	 * =============================================================== */

	protected Object getCellValue(int row, String column) {
		// get personnel
		ICalloutIf callout = getId(row);
		// translate
		if(DTG.equals(column))
            return callout.getCreatedTime();
		else if(TITLE.equals(column))
			return callout.getTitle();
		// not found
		return null;
	}

	/* ===============================================================
	 * Public methods
	 * =============================================================== */

	/**
	 * @param index Index of call-out
	 *
	 * @return Call-out at given row index
	 */
	public ICalloutIf getCallout(int row)
	{
		return getId(row);
	}

	/* ===============================================================
	 * Helper methods
	 * =============================================================== */

	public String[] getNames() {
		return new String[] {TITLE, DTG};
	}

	public String[] getCaptions() {
		return new String[] {"DTG", "Tittel"};
	}


}
