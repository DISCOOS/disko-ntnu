package org.redcross.sar.map;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.ListSelectionModel;

import org.redcross.sar.gui.models.MapSourceTableModel;
import org.redcross.sar.gui.renderers.BooleanTableCellRenderer;

public class MapSourceTable extends JTable {

	private static final long serialVersionUID = 1L;

	public MapSourceTable(ArrayList<MapSourceInfo> list){
		MapSourceTableModel model = new MapSourceTableModel(list);
		setModel(model);
		BooleanTableCellRenderer btcr = new BooleanTableCellRenderer();
		
		setDefaultRenderer(Boolean.class, btcr);
		
		btcr.setSelected(this, true, 0, 0);
		
		setColumnWidths();
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
	}
	
	private void setColumnWidths() {
		for (int i = 0; i < 5; i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setResizable(false);
			switch(i) {
				case 0: 
					column.setPreferredWidth(15);
					break;
				case 1:
					column.setPreferredWidth(15);
					break;
				case 2: 
					column.setPreferredWidth(200);
					break;
				case 3: 
					column.setPreferredWidth(75);
					break;
				case 4: 
					column.setPreferredWidth(50);
					break;	
			}
		}
	}	
}
