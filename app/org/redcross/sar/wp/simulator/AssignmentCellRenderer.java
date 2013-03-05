package org.redcross.sar.wp.simulator;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.renderer.DiskoTableCellRenderer;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.simulator.AssignmentStringConverter;

public class AssignmentCellRenderer extends DiskoTableCellRenderer {

	private static final long serialVersionUID = 1L;

	private static final AssignmentStringConverter converter = new AssignmentStringConverter();

	public AssignmentCellRenderer() {
		super.setOpaque(true);
	}

	public JLabel getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {

		// initialize
		String text = "";
		Icon icon = null;

		// get model info
		super.initialize(table, row, col);

		// convert value to text?
		if(m_rowInModel!=-1 && m_colInModel!=-1) {

			// get icon
			if (col == 0) {
				IAssignmentIf assignment = (IAssignmentIf)value;
				Enum<?> type = MsoUtils.getType(assignment,true);
				icon = DiskoIconFactory.getIcon(
							DiskoEnumFactory.getIcon(type),"32x32");
			}

			// get text
			text = converter.toString(m_model,m_rowInModel,m_colInModel);

		}

		// get renderer
		JLabel renderer = super.prepare(table, text, isSelected, hasFocus, row, col, false, false);

		// update
		update(renderer,icon);

		// finished
		return renderer;

	}
}
