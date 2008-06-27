package org.redcross.sar.wp.simulator;

import java.awt.Component;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.DTG;

public class AssignmentCellRenderer extends JLabel implements TableCellRenderer {
	
	private static final long serialVersionUID = 1L;

	public AssignmentCellRenderer() {
		super.setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		switch(column) {
		case 0:
			IAssignmentIf assignment = (IAssignmentIf)value;
			Enum<?> type = MsoUtils.getType(assignment,true);
			setIcon(DiskoIconFactory.getIcon(
					DiskoEnumFactory.getIcon(type),"32x32"));
			setText(MsoUtils.getAssignmentName(assignment,1));
			break;
		case 1:
			setText(DiskoEnumFactory.getText((AssignmentStatus)value));
			setIcon(null);
			break;
		case 2:
			setText(DTG.CalToDTG((Calendar)value));
			setIcon(null);
			break;
		default:
			setText(value!=null ? value.toString() : null);
			setIcon(null);
		}
		// update selection state
		if (isSelected){
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} 
		else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		if(hasFocus) 
			requestFocusInWindow();
		return this;
	}
}
