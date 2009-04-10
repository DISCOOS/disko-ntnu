package org.redcross.sar.wp.tactics;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.renderer.DiskoTableCellRenderer;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.util.MsoUtils;

public class AssignmentCellRenderer extends DiskoTableCellRenderer {

	private static final long serialVersionUID = 1L;

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

		// convert value to icon and text?
		if(value!=null && m_colInModel!=-1) {
			// cast to IAssignmentIf
			IAssignmentIf assignment = (IAssignmentIf)value;
			// translate
			if (m_colInModel == 1) {
				icon = DiskoIconFactory.getIcon(
						DiskoEnumFactory.getIcon(
						MsoUtils.getType(assignment,false)),"32x32");
				text = MsoUtils.getAssignmentName(assignment,1);
			}
			else if (m_colInModel == 2) {
				text = DiskoEnumFactory.getText(assignment.getStatus());
			}
		}

		// get renderer
		JLabel renderer = super.prepare(table, text, isSelected, hasFocus, row, col, false, false);

		// update
		update(renderer,icon);

		// finished
		return renderer;

	}
}
