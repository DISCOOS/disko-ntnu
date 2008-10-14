package org.redcross.sar.wp.tactics;

import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.renderer.DiskoTableCellRenderer;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.util.MsoUtils;

public class UnitCellRenderer extends DiskoTableCellRenderer {

	private static final long serialVersionUID = 1L;

	private Hashtable<Enum<?>, ImageIcon> icons = null;
	private String catalog = "48x48";

	public UnitCellRenderer(String catalog) {
		// prepare
		this.catalog = catalog;
		this.icons = new Hashtable<Enum<?>, ImageIcon>();
		// MUST do this for background to show up.
		setOpaque(true);
	}

	@SuppressWarnings("unchecked")
	public JLabel getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {

		// initialize
		String text = "";
		String tooltip = "";
		Icon icon = null;

		// get model info
		super.initialize(table, row, col);

		// convert value to icon and text?
		if(value!=null && m_colInModel!=-1) {
			// translate
			if (m_colInModel == 0) {
				IUnitIf unit = (IUnitIf)value;
				icon = getIcon(unit.getType());
				text = MsoUtils.getUnitName(unit, false);
				tooltip = MsoUtils.getUnitName(unit, true);
			}
			else if (m_colInModel > 0 && m_colInModel < 3) {
				text = String.valueOf(value);
				tooltip = text;
			}
			else if (m_colInModel == 4) {
				text = DiskoEnumFactory.getText((Enum<UnitStatus>)value,"text");
				tooltip = DiskoEnumFactory.getText((Enum<UnitStatus>)value,"tooltip");
			}
		}

		// get renderer
		JLabel renderer = super.prepare(table, text, isSelected, hasFocus, row, col, false, false);

		// set tooltip text
		renderer.setToolTipText(tooltip);

		// update
		update(renderer,icon);

		// finished
		return renderer;

	}

	private ImageIcon getIcon(Enum<?> e) {
		ImageIcon icon = (ImageIcon)icons.get(e);
		if (icon == null) {
			icon = DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),catalog);
			icons.put(e, icon);
		}
		return icon;
	}

}
