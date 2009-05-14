package org.redcross.sar.gui.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class MsoTableCellRenderer extends MsoLabelRenderer implements TableCellRenderer {
	
	private static final long serialVersionUID = 1L;

	public MsoTableCellRenderer(int optionsName, boolean completeName) {
		super(optionsName, completeName);
	}
	
	public MsoTableCellRenderer(int optionsName, boolean completeName, boolean showIcon, String iconCatalog, int mapIconTo) {
		super(optionsName, completeName, showIcon, iconCatalog, mapIconTo);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean cellHasFocus, int row, int col) {
		// forward
		JLabel label = getRenderer(value);;
		// update selection state
		if (isSelected){
			label.setBackground(table.getSelectionBackground());
			label.setForeground(table.getSelectionForeground());
		} 
		else {
			label.setBackground(table.getBackground());
			label.setForeground(table.getForeground());
		}
		// forward
		return label;
	}
	
}

