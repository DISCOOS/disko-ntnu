package org.redcross.sar.wp.ds;

import java.awt.Dimension;

import org.redcross.sar.ds.sc.Advisor;
import org.redcross.sar.gui.panel.BasePanel;

public class LevelPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private LevelTable m_table;
	private LevelTableModel m_data;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public LevelPanel() {

		// forward
		super();

		// initialize GUI
		initialize();

	}

	/* =============================================================================
	 * Public methods
	 * ============================================================================= */

	public void connect(Advisor source) {
		m_data.connect(LevelTableModel.createBinder(source));
		m_data.load();

	}

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	private void initialize() {
		// prepare base panel
		setHeaderVisible(false);
		Dimension d = new Dimension(getTable().getMinimumColumnTotalWidth(),100);
		setPreferredSize(d);
		setMinimumSize(d);
		// set body component
		setBodyComponent(getTable());
	}

	private LevelTable getTable() {
		if(m_table==null) {

			// create table
			m_table = new LevelTable();
			m_table.setAllColumnsVisible();

			// get table model
			m_data = (LevelTableModel)m_table.getModel();

			// set minimum size
			int width = m_table.getMinimumColumnTotalWidth();
			m_table.setMinimumSize(new Dimension(width,35));

		}
		return m_table;
	}

}
