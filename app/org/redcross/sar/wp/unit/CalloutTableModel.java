package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.ICalloutListIf;
import org.redcross.sar.gui.model.AbstractMsoTableModel;

/**
 * Provides contents for call-out table in overview mode
 *
 * @author thomasl
 */
public class CalloutTableModel extends AbstractMsoTableModel<ICalloutIf>
{
	private static final long serialVersionUID = 1L;

	public static final String DTG = "dtg";
	public static final String TITLE = "title";
	public static final String VIEW = "view";

	public static final String[] NAMES = new String[] { DTG, VIEW, VIEW };
	public static final String[] CAPTIONS = new String[] { "DTG", "Tittel", "Vis" };

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
		connect(model, ICalloutIf.ALL_SELECTOR, ICalloutIf.CALLOUT_COMPARATOR);
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
		return NAMES;
	}

	public String[] getCaptions() {
		return CAPTIONS;
	}


}
