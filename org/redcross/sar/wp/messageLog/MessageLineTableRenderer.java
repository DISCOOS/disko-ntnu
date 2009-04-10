package org.redcross.sar.wp.messageLog;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.mso.data.IMessageLineIf;

public class MessageLineTableRenderer extends JLabel implements TableCellRenderer {
	
	private static final long serialVersionUID = 1L;

	public MessageLineTableRenderer() {
		super.setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		// get message line
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
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else
        {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }		
		// finished
		return this;
	}
}
