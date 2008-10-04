package org.redcross.sar.wp.ds;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class AssignmentCellRenderer extends JLabel implements TableCellRenderer {
	
	private static final long serialVersionUID = 1L;
	
	private static final AssignmentStringConverter converter = new AssignmentStringConverter(); 

	public AssignmentCellRenderer() {
		super.setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int col) {

		// convert indexes to model
		row = table.convertRowIndexToModel(row);
		col = table.convertColumnIndexToModel(col);
		
		// initialize
		Border b = null;
		
		// has row and col?
		if(row!=-1 && col!=-1) {
		
			// set text
			setText(converter.toString(table.getModel(),row,col));
			
			// set alignment
			switch(col) {
			case AssignmentTableModel.NAME_INDEX:				
			case AssignmentTableModel.UNIT_INDEX:
			case AssignmentTableModel.STATUS_INDEX:
				setHorizontalAlignment(SwingConstants.LEFT);
				b = BorderFactory.createEmptyBorder(0, 5, 0, 0);
				break;
			default:
				setHorizontalAlignment(SwingConstants.RIGHT);
				b = BorderFactory.createEmptyBorder(0, 0, 0, 2);
			}
			
		}
		
		// set border
		if(getIcon()==null) 
			setBorder(b);
		else
			setBorder(BorderFactory.createEmptyBorder());
			
		// update selection state
		if (isSelected){
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} 
		else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		
		// finished
		return this;
	}
}
