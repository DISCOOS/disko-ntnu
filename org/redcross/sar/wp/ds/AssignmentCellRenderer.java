package org.redcross.sar.wp.ds;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.redcross.sar.gui.renderer.DiskoTableCellRenderer;

public class AssignmentCellRenderer extends DiskoTableCellRenderer {

	private static final long serialVersionUID = 1L;

	private static final AssignmentStringConverter converter = new AssignmentStringConverter(true);

	/* ==================================================
	 * TableCellRenderer implementation
	 * ================================================== */

	public JLabel getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {

		// initialize
		String text = "";

		// get model info
		super.initialize(table, row, col);

		// convert value to text?
		if(m_rowInModel!=-1 && m_colInModel!=-1) {

			text = converter.toString(m_model,m_rowInModel,m_colInModel);

		}

		// forward
		JLabel renderer = super.prepare(table, text, isSelected, hasFocus, row, col, false, false);

		// update alignment and borders
		update(renderer,renderer.getIcon());


		// finished
		return renderer;
	}

}
