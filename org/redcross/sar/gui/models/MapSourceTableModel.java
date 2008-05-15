package org.redcross.sar.gui.models;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.map.MapSourceInfo;

public class MapSourceTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private ArrayList<Object[]> rows = null;
	
	public MapSourceTableModel(ArrayList<MapSourceInfo> list){		
		rows = new ArrayList<Object[]>();
		for (int i = 0; i< list.size(); i++) {
			add(list.get(i));
		}			
		super.fireTableDataChanged();
	}
	
	private void add(MapSourceInfo mapinfo){
		Object[] row = new Object[4];
		row[0] = new Boolean(mapinfo.isCurrent());
		row[1] = mapinfo.getMxdPath();
		row[2] = mapinfo.getType();
		row[3] = mapinfo.getStatus();
		rows.add(row);
		//System.out.println("test: " +row[0] + ", " + row[1]+", " +row[2]+", " +row[3]);
	}
	
	public int getColumnCount() {
		return 4;
	}

	public int getRowCount() {
		return rows.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= 0 && rowIndex < rows.size() && 
				columnIndex >= 0 && columnIndex < rows.size()+1) {
			Object[] row = (Object[])rows.get(rowIndex);
			return row[columnIndex];
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
			case 2: return "Kartdokument";
			case 3: return "Type";
			case 4: return "Status";
			default: return null;
		}
	}
	
}
