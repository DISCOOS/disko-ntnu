package org.redcross.sar.gui.renderer;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class BooleanCellRenderer extends JCheckBox implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	public BooleanCellRenderer() {
		super.setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if(value!=null)
			setSelected(Boolean.valueOf(value.toString()));
		else
			setSelected(false);

		setBackground(table.getBackground());
		setForeground(table.getForeground());
		return this;
	}


	public void setSelected(JTable table, boolean value, int row, int column){

		boolean isSelected;
		boolean hasFocus;
		if(value){
			isSelected = true;
			hasFocus = true;
			getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}

	/* =======================================================
	 * Increased performance (See DefaultTableCellRenderer).
	 * ======================================================= */

	@Override
	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) { /* NOP */ }

	@Override
	public void firePropertyChange(String propertyName, char oldValue,
			char newValue) { /* NOP */ }

	@Override
	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) { /* NOP */ }

	@Override
	public void revalidate() { /* NOP */ }

	@Override
	public void repaint() { /* NOP */ }

	@Override
	public void repaint(int x, int y, int width, int height) { /* NOP */ }

	@Override
	public void repaint(long tm) { /* NOP */ }

	@Override
	public void validate() { /* NOP */ }

}
