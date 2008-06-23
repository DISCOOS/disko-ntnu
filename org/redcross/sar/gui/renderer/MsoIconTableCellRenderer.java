package org.redcross.sar.gui.renderer;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class MsoIconTableCellRenderer extends MsoIconRenderer implements TableCellRenderer {
	
	public MsoIconTableCellRenderer(int options, String catalog) {
		super(options,catalog);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean cellHasFocus, int row, int col) {

		// update selection state
		if (isSelected){
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} 
		else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		// forward
		return getRenderer(value);
	}
	
}

