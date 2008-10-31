package org.redcross.sar.wp.simulator;

import org.redcross.sar.data.Selector;
import org.redcross.sar.gui.model.AbstractMsoTableModel;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IUnitIf;

public class UnitTableModel extends AbstractMsoTableModel<IUnitIf> {

	private static final long serialVersionUID = 1L;
	private static final String UNIT = "Unit";
	private static final String[] NAMES = new String[]{UNIT};
	private static final String[] CAPTIONS = new String[]{"Enhet"};

	private final Selector<IUnitIf> m_selector = new Selector<IUnitIf>()
	{
		public boolean select(IUnitIf anObject)
		{
			// get history flag
			boolean isHistory = anObject.isReleased();
			// finished
			return m_archived ? isHistory : !isHistory;
		}
	};

	private boolean m_archived;

	/* =========================================================
	 * Constructors
	 * ========================================================= */

	public UnitTableModel(IMsoModelIf model,boolean archived) {
		// forward
		super(IUnitIf.class, NAMES, CAPTIONS, false);
		// prepare
		m_archived = archived;
		// forward
		connect(model,m_selector,IUnitIf.UNIT_TYPE_AND_NUMBER_COMPARATOR);
		// load data
		load(model.getMsoManager().getCmdPost().getUnitList().getItems());
	}

	/* =========================================================
	 * MsoTableModel implementation
	 * ========================================================= */

	@Override
	protected Object getCellValue(int row, String column) {
		if(UNIT.equals(column)) {
			return getId(row);
		}
		return null;
	}

	/* =========================================================
	 * Public methods
	 * ========================================================= */

	public IUnitIf getUnit(int iRow) {
		return getId(iRow);
	}

	/* =========================================================
	 * Helper methods
	 * ========================================================= */


}
