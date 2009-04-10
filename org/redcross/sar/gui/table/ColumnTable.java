package org.redcross.sar.gui.table;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.model.ColumnTableModel;
import org.redcross.sar.gui.model.OperationTableModel;
import org.redcross.sar.gui.renderer.BooleanCellRenderer;

public class ColumnTable extends DiskoTable {

	private static final long serialVersionUID = 1L;
	
	private TableRowSorter<ColumnTableModel> tableRowSorter = null;
	
	public ColumnTable() {
		
		// forward
		super(new ColumnTableModel());
		
		// create table model
		ColumnTableModel model = (ColumnTableModel)getModel();
		
		// set model
		model.setHeaderEditable(0, true);
		model.setHeaderEditor(0, "checkbox");
		
		// add row sorter
		tableRowSorter = new TableRowSorter<ColumnTableModel>(model);
		setRowSorter(tableRowSorter);		
		
		// set boolean renderer
		setDefaultRenderer(Boolean.class, new BooleanCellRenderer());
		
		// prepare header
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        
		// prepare table
		setRowHeight(25);
		setShowHorizontalLines(true);
		setShowVerticalLines(false);
		setRowSelectionAllowed(true);
		setCellSelectionEnabled(true);
		setColumnSelectionAllowed(false);
		setAutoFitWidths(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					
	}	
	
	public int update() {
		// forward
		int select = ((OperationTableModel)getModel()).update();
		// select row if possible
		if(select!=-1) {
			// convert to model index
			select = tableRowSorter.convertRowIndexToModel(select);
			// set selected
			getSelectionModel().setSelectionInterval(select, 0);
		}
		else if(getRowCount()>0) {
			getSelectionModel().setSelectionInterval(0, 0);
		}
		// update
		validate();
		// finshed
		return select;		
	}
}
