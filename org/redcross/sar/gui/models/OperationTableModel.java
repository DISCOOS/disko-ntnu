package org.redcross.sar.gui.models;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.app.Utils;

public class OperationTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private Object[] rows = null;

	public void update() {
		// get active operations locally / on sara server
		List<String[]> data = Utils.getApp().getMsoModel().getModelDriver().getActiveOperations();
		// has active operations?
		if(data!=null) {
			// allocate memory
			rows = new Object[data.size()];
			// loop over all units
			for (int i = 0; i < data.size(); i++) {
				// allocate memory
				Object[] row = new Object[1];
				// update row
				row[0] = data.get(i)[0];
				// save row
				rows[i] = row;
			}
		}
		else {
			rows = null;
		}
		super.fireTableDataChanged();
	}
    
	public int getColumnCount() {
		return 1;
	}

	public int getRowCount() {
		return rows!=null ? rows.length : 0;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
    	// invalid index?
    	if(!(rowIndex<rows.length)) return null;
    	// get row
		Object[] row = (Object[]) rows[rowIndex];
		if(row != null)
			return row[columnIndex];
		else
			return null;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		default:
			return Object.class;
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Aksjonsnummer";
		default:
			return null;
		}
	}	
}
