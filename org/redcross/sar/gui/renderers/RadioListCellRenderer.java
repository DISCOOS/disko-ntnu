package org.redcross.sar.gui.renderers;

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
}
