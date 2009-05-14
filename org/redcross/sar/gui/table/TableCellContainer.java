package org.redcross.sar.gui.table;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.JTable;

public class TableCellContainer extends AbstractTableCell {

	private JPanel m_viewPanel;
	private JPanel m_editPanel;

	public final Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
	{
		super.getTableCellEditorComponent(table, value, isSelected, row, column);
		updateCell(row);
		return getEditorComponent();
	}

	public final Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column)
	{
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		updateCell(row);
		return getComponent();
	}

	protected void updateCell(int row) { /* Override this */ }
    
	@Override
	public final int getCellWidth(Graphics g, JTable table, int row, int col) {
		int w = getColumnFixedWidth(table,col);
		if(w<0) {
			if(isEditing()) {
				w = getEditorComponent().getPreferredSize().width+2;
			} else {
				w = getComponent().getPreferredSize().width+2;
			}
		}
		return w;
	}

	@Override
	protected final JPanel getComponent() {
		if(m_viewPanel==null) {
			m_viewPanel = new JPanel();
		}
		return m_viewPanel;
	}

	@Override
	protected final JPanel getEditorComponent() {
		if(m_editPanel==null) {
			m_editPanel = new JPanel();
		}
		return m_editPanel;
	}	
	
}
