package org.redcross.sar.gui.renderers;

import java.awt.Component;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.redcross.sar.app.Utils;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;

public class UnitTableCellRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	private Hashtable<Enum<?>, ImageIcon> icons = null;
	private String catalog = "48x48";

	public UnitTableCellRenderer(String catalog) {
		// prepare
		this.catalog = catalog;
		this.icons = new Hashtable<Enum<?>, ImageIcon>();
		// MUST do this for background to show up.
		setOpaque(true); 
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value != null) {
			if (column == 0) {
				IUnitIf unit = (IUnitIf)value;
				setIcon(getIcon(unit.getType()));
				setText(MsoUtils.getUnitName(unit, true));
				setToolTipText(MsoUtils.getUnitName(unit, true));
			}
			else if (column > 0 && column < 3) {
				setIcon(null);
				setText(String.valueOf(value));
				setToolTipText(String.valueOf(value));
			}
			else {
				setText(null);
			}
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
	
	private ImageIcon getIcon(Enum e) {
		ImageIcon icon = (ImageIcon)icons.get(e);
		if (icon == null) {
			icon = Utils.getIcon(e,catalog);
			icons.put(e, icon);
		}
		return icon;
	}
	
}
