package org.redcross.sar.gui.renderer;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;

import org.redcross.sar.gui.factory.DiskoStringFactory;

public class RadioListCellRenderer extends JRadioButton implements ListCellRenderer {

	private static final long serialVersionUID = 1L;

	public RadioListCellRenderer() {
		super.setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		setText(DiskoStringFactory.translate(value));
		setSelected(isSelected);
		setBackground(list.getBackground());
		setForeground(list.getForeground());
		return this;
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
