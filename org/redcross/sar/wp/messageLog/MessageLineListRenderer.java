package org.redcross.sar.wp.messageLog;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.mso.data.IMessageLineIf;

public class MessageLineListRenderer extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;

	public MessageLineListRenderer() {
		setOpaque(true);
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean hasFocus) {
		
		// cast to message line
		IMessageLineIf line = (IMessageLineIf)value; 

		// get icon and text
		String text = line.toString();
		Icon icon = DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(line.getLineType()), "32x32");
		
		// update label
		setIcon(icon);
		setText(text);
		
		// update selection state
		if (isSelected)
        {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else
        {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
		
		return this;
	}

}
