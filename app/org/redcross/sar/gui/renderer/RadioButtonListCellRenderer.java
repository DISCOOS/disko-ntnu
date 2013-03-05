package org.redcross.sar.gui.renderer;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;

import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.gui.factory.UIFactory;

public class RadioButtonListCellRenderer implements ListCellRenderer {

	private static final long serialVersionUID = 1L;
	private final JRadioButton button = UIFactory.createRadioButtonRenderer();

	public RadioButtonListCellRenderer() {
		button.setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		button.setText(DiskoStringFactory.translate(value));
		button.setSelected(isSelected);
		button.setBackground(list.getBackground());
		button.setForeground(list.getForeground());
		return button;
	}

}
