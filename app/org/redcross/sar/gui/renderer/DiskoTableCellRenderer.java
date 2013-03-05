package org.redcross.sar.gui.renderer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import org.redcross.sar.gui.model.ITableModel;

public class DiskoTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	protected ITableModel m_model;
	protected int m_rowInModel;
	protected int m_colInModel;

	/* ==================================================
	 * Constructors
	 * ================================================== */

	public DiskoTableCellRenderer() {
		super.setOpaque(true);
	}

	/* ==================================================
	 * TableCellRenderer implementation
	 * ================================================== */

	@Override
	public JLabel getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {

		// forward
		initialize(table,row,col);

		// forward
		return prepare(table, value, isSelected, hasFocus, row, col,true,true);
	}

	/* ==================================================
	 * Protected methods
	 * ================================================== */

	protected void initialize(JTable table, int row, int col) {
		// convert to model
		m_rowInModel = table.convertRowIndexToModel(row);
		m_colInModel = table.convertColumnIndexToModel(col);
		// get supported table model
		m_model = table.getModel() instanceof ITableModel ? (ITableModel)table.getModel() : null;

	}

	protected JLabel prepare(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col, boolean initialize, boolean update) {

		// initialize?
		if(initialize) initialize(table,row,col);

		// forward
		JLabel renderer = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

		// update?
		if(update) update(renderer, renderer.getIcon());

		// update selection state?
		if (isSelected){
			renderer.setBackground(table.getSelectionBackground());
			renderer.setForeground(table.getSelectionForeground());
		}
		else {
			renderer.setBackground(table.getBackground());
			renderer.setForeground(table.getForeground());
		}

		// finished
		return renderer;

	}

	protected void update(JLabel renderer, Icon icon) {

		// initialize
		Border b = null;
		int horzAlign = SwingConstants.LEFT;

		// get alignment from model?
		if(m_model!=null && m_colInModel!=-1) {
			horzAlign = m_model.getColumnAlignment(m_colInModel);
		}
		else {
			horzAlign = renderer.getHorizontalAlignment();
		}

		// update
		renderer.setIcon(icon);
		renderer.setHorizontalAlignment(horzAlign);

		// set border
		if(icon==null) {
			if(horzAlign==SwingConstants.LEFT) {
				b = BorderFactory.createEmptyBorder(0, 5, 0, 0);
			}
			else if(horzAlign==SwingConstants.RIGHT) {
				b = BorderFactory.createEmptyBorder(0, 0, 0, 2);
			}
			else {
				b = BorderFactory.createEmptyBorder();
			}
		}
		else {
			b = BorderFactory.createEmptyBorder();
		}

		// update
		renderer.setBorder(b!=null ? b : BorderFactory.createEmptyBorder());

	}
}
