package org.redcross.sar.gui.renderer;

import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MapSourceInfoCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	public MapSourceInfoCellRenderer() {
		super.setOpaque(true);
	}

	public JLabel getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		// forward
		JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		// translate
		switch(column) {
		case 1:
			File file = new File(value.toString());
			label.setText(file.getName());
			break;
		case 2:
			int i = Integer.valueOf(value.toString());
			switch(i) {
			case 0: label.setText("Ukjent"); break;
			case 1: label.setText("Ingen"); break;
			case 2: label.setText("Delvis"); break;
			case 3: label.setText("Full"); break;
			}
			break;
		default:
			label.setText(value.toString());
			break;
		}
		label.setBackground(table.getBackground());
		label.setForeground(table.getForeground());
		return label;
	}
}
