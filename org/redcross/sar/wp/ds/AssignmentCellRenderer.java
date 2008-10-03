package org.redcross.sar.wp.ds;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
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
		
		// has row and col?
		if(row!=-1 && col!=-1) {
		
			// set text
			setText(converter.toString(table.getModel(),row,col));
			
		}
		
		// set border
		if(getIcon()==null) 
			setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
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
