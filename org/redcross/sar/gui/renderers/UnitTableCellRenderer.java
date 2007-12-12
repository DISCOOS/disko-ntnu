package org.redcross.sar.gui.renderers;

import java.awt.Component;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import org.redcross.sar.app.Utils;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;

public class UnitTableCellRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = 1L;
	private Hashtable<Enum<?>, ImageIcon> icons = null;

	public UnitTableCellRenderer() {
		setOpaque(true); //MUST do this for background to show up.
		icons = new Hashtable<Enum<?>, ImageIcon>();
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value != null && value instanceof IUnitIf) {
			IUnitIf unit = (IUnitIf)value;
			setIcon(getIcon(unit.getType()));
			setText(MsoUtils.getUnitName(unit, true));
			setToolTipText(MsoUtils.getUnitName(unit, true));
			//check if this cell is selected. Change border
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			}
			else {
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}
			return this;
		}
		return null;
	}
	
	private ImageIcon getIcon(Enum key) {
		ImageIcon icon = (ImageIcon)icons.get(key);
		if (icon == null) {
			icon = Utils.getIcon(key);
			icons.put(key, icon);
		}
		return icon;
	}
}
