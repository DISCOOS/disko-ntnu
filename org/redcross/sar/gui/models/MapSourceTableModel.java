package org.redcross.sar.gui.models;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.map.MapSourceInfo;

public class MapSourceTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private ArrayList<Object[]> rows = null;
	
	public MapSourceTableModel(List<MapSourceInfo> list){		
		rows = new ArrayList<Object[]>();
		load(list);
	}
	
	public void load(List<MapSourceInfo> list) {
		rows.clear();
		for (int i = 0; i< list.size(); i++) {
			add(list.get(i));
		}			
		super.fireTableDataChanged();

	}
	
	private void add(MapSourceInfo info){
		Object[] row = new Object[5];
		row[0] = info.isCurrent();
		row[1] = info.getMxdDoc();
		row[2] = info.getCoverage();
		row[3] = info.getType();
		row[4] = info.getStatus();
		rows.add(row);
	}
	
	public int getColumnCount() {
		return 5;
	}

	public int getRowCount() {
		return rows.size();
	}

	public Object getValueAt(int row, int col) {
		if (row >= 0 && row < rows.size() && 
				col >= 0 && col < getColumnCount()) {
			Object[] data = (Object[])rows.get(row);
			return data[col];
		}
		return null;
	}

	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
			case 0: return Boolean.class;
			default: return Object.class;
		}
	}

	@Override
	public String getColumnName(int column) {
		switch(column) {
			case 0: return "Valgt";
			case 1: return "Kartdokument";
			case 2: return "Dekning";
			case 3: return "Type";
			case 4: return "Status";
			default: return null;
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		switch(col) {
		case 0: return true;
		default: return false;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		if (row >= 0 && row < rows.size() && 
				col >= 0 && col < getColumnCount()) {
			if(col==0) {
				// update all rows
				for(int i=0;i<getRowCount();i++) {
					Object[] data = (Object[])rows.get(i);
					data[col] = (i==row);
				}				
				fireTableDataChanged();
			}
			else {
				Object[] data = (Object[])rows.get(row);
				data[col] = value;
			}
		}
	}
	
}
