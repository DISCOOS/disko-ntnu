package org.redcross.sar.gui.renderers;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.util.MsoUtils;

public class AssignmentTableCellRenderer extends JLabel implements
		TableCellRenderer {
	
	private static final long serialVersionUID = 1L;

	public AssignmentTableCellRenderer() {
		super.setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		IAssignmentIf assignment = (IAssignmentIf)value;
		if (column == 1) {
			setText(MsoUtils.getAssignmentName(assignment,1));
		}
		else if (column == 2) {
			setText(DiskoEnumFactory.getText(assignment.getStatus()));
		}
		else {
			setText(null);
		}
		setBackground(table.getBackground());
		setForeground(table.getForeground());
		return this;
	}
}
