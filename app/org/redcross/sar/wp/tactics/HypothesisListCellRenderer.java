package org.redcross.sar.wp.tactics;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IHypothesisIf;

public class HypothesisListCellRenderer extends JLabel implements
		ListCellRenderer {

	private static final long serialVersionUID = 1L;

	public HypothesisListCellRenderer() {
		super.setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		if (value instanceof IHypothesisIf) {
			IHypothesisIf h = (IHypothesisIf)value;
			setText(DiskoEnumFactory.getText(MsoClassCode.CLASSCODE_HYPOTHESIS)+(h.getNumber()));
			setIcon(DiskoIconFactory.getIcon("GENERAL.HYPOTHESIS", "24x24"));
		}
		else {
			setText(value.toString());
			setIcon(null);
		}

        // check if this cell is selected
        if (isSelected) {
        	setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
        	setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
	}
}
