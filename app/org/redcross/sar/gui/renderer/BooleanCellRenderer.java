package org.redcross.sar.gui.renderer;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.redcross.sar.gui.factory.UIFactory;

public class BooleanCellRenderer implements TableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private JCheckBox m_checkbox;

	public BooleanCellRenderer() {
		m_checkbox = UIFactory.createCheckBoxRenderer();
		m_checkbox.setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if(value!=null)
			m_checkbox.setSelected(Boolean.valueOf(value.toString()));
		else
			m_checkbox.setSelected(false);

		m_checkbox.setBackground(table.getBackground());
		m_checkbox.setForeground(table.getForeground());
		return m_checkbox;
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

}
