package org.redcross.sar.wp.ds;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.util.MsoUtils;

public class AssignmentCellRenderer extends JLabel implements TableCellRenderer {
	
	private static final long serialVersionUID = 1L;
	
	private static final AssignmentStringConverter converter = new AssignmentStringConverter(); 

	public AssignmentCellRenderer() {
		super.setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {

		// set icon
		if (column == 0) {
			IAssignmentIf assignment = (IAssignmentIf)value;
			Enum<?> type = MsoUtils.getType(assignment,true);
			setIcon(DiskoIconFactory.getIcon(
						DiskoEnumFactory.getIcon(type),"32x32"));
		}
		else {
			setIcon(null);
		}
		
		// set text
		setText(converter.toString(table.getModel(),row,column));
		
		// update selection state
		if (isSelected){
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} 
		else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		if(hasFocus)  requestFocusInWindow();
		
		// finished
		return this;
	}
}
