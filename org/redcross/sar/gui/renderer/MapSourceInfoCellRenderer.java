package org.redcross.sar.gui.renderer;

import java.awt.Component;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class MapSourceInfoCellRenderer extends JLabel implements
		TableCellRenderer {
	
	private static final long serialVersionUID = 1L;

	public MapSourceInfoCellRenderer() {
		super.setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		switch(column) {
		case 1:
			File file = new File(value.toString());
			setText(file.getName());
			break;
		case 2:
			int i = Integer.valueOf(value.toString());
			switch(i) {
			case 0: setText("Ukjent"); break;
			case 1: setText("Ingen"); break;
			case 2: setText("Delvis"); break;
			case 3: setText("Full"); break;
			}
			break;
		default:
			setText(value.toString());
			break;
		}
		setBackground(table.getBackground());
		setForeground(table.getForeground());		
		return this;
	}
}
