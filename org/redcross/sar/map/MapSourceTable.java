package org.redcross.sar.map;

import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.ListSelectionModel;

import org.redcross.sar.gui.models.MapSourceTableModel;
import org.redcross.sar.gui.renderers.BooleanCellRenderer;
import org.redcross.sar.gui.renderers.MapSourceInfoCellRenderer;

public class MapSourceTable extends JTable {

	private static final long serialVersionUID = 1L;

	public MapSourceTable(List<MapSourceInfo> list){
		this.setDefaultRenderer(Boolean.class, new BooleanCellRenderer());
		this.setDefaultRenderer(Object.class,new MapSourceInfoCellRenderer());
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setRowHeight(25);
		this.setFillsViewportHeight(true);
		this.setBorder(null);
		this.setShowVerticalLines(false);
		this.setModel(new MapSourceTableModel(list));
		this.setColumnWidths();
	}
	
	public void load(List<MapSourceInfo> list) {
		((MapSourceTableModel)getModel()).load(list);
	}
	
	private void setColumnWidths() {
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			switch(i) {
				case 0: 
					column.setPreferredWidth(15);
					break;
				case 1: 
					column.setPreferredWidth(150);
					break;
				case 2: 
					column.setPreferredWidth(75);
					break;
				case 3: 
					column.setPreferredWidth(100);
					break;
				case 4: 
					column.setPreferredWidth(50);
					break;	
			}
		}
	}	
}
