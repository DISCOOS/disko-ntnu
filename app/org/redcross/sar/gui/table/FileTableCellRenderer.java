package org.redcross.sar.gui.table;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;


public class FileTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private final static FileTableIconConverter icons = new FileTableIconConverter();	
	private final static FileTableStringConverter strings = new FileTableStringConverter();	
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		// get default renderer		
		JLabel label = (JLabel)super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );

		// convert indexes
		row = table.convertRowIndexToModel(row);
		column = table.convertColumnIndexToModel(column);
				
		// set icon and text
		label.setIcon(icons.toIcon(table.getModel(), row, column));
		label.setText(strings.toString(table.getModel(), row, column));
		
		// update alignment
		label.setHorizontalAlignment(column==1 ? SwingConstants.RIGHT : SwingConstants.LEFT);
		
		// update border
		label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		// finished
		return label;
		
	}	

}
