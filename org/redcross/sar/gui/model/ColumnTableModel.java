package org.redcross.sar.gui.model;

import javax.swing.table.TableColumn;

public class ColumnTableModel extends DiskoTableModel {

	private static final long serialVersionUID = 1L;

	private Boolean[] m_rows;
	private DiskoTableColumnModel m_model;
	
	public static final String CHECK = "check";
	public static final String NAME = "name";
	
	public static final String[] NAMES = new String[] { CHECK, NAME };
	public static final String[] CAPTIONS = new String[] { "", "Kolonner" };
	
	/* =====================================================
	 * Constructors
	 * ===================================================== */
	
	public ColumnTableModel() {
		// forward
		super(NAMES,CAPTIONS);
	}

	/* =====================================================
	 * AbstractTableModel implementations
	 * ===================================================== */
	
	@Override
	public int getRowCount() {
		return m_model!=null ? m_model.getColumnCount() : 0;
	}

	@Override
	public Object getValueAt(int row, int col) {
    	// invalid index?
    	if(!(row<getRowCount())) return null;
		switch (col) {
		case 0: 
			return m_rows[row];
		case 1: 
	    	// get column    	
			TableColumn column = m_model.getColumn(row, false);
			if(column != null)
				return column.getHeaderValue();
			else
				return null;
		default: 
			return null;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
    	// invalid index?
    	if(!(row<getRowCount())) return;
		switch (col) {
		case 0: 
			m_rows[row] = (Boolean)value;
		}
	}

	@Override
	public String getColumnName(int col) {
		switch (col) {
		case 0: return "";
		case 1: return "Kolonne";
		default: return null;
		}
	}
			
	@Override
	public Class<?> getColumnClass(int col) {
		switch (col) {
		case 0: return Boolean.class;
		case 1: return String.class;
		default: return Object.class;
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col==0;
	}

	/* =====================================================
	 * Public methods
	 * ===================================================== */
	
	public void load(DiskoTableColumnModel model) {
		m_model = model;
		m_rows = (model!=null ? new Boolean[model.getColumnCount()] : null);
		if(m_rows!=null) {
			for(int i=0;i<getRowCount();i++) {
				m_rows[i] = m_model.isColumnVisible(m_model.getColumn(i, false));
			}			
		}
		fireTableDataChanged();
	}
	
	public void apply() {
		for(int i=0;i<getRowCount();i++) {
			m_model.setColumnVisible(m_model.getColumn(i, false), m_rows[i]);
		}
	}

}
