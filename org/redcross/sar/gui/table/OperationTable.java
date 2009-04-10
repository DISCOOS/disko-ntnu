package org.redcross.sar.gui.table;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.model.OperationTableModel;

public class OperationTable extends DiskoTable {

	private static final long serialVersionUID = 1L;
	
	private TableRowSorter<OperationTableModel> tableRowSorter = null;
	
	public OperationTable() {
		
		// forward
		super(new OperationTableModel());
		
		// create table model
		OperationTableModel model = (OperationTableModel)getModel();
		
		// add row sorter
		tableRowSorter = new TableRowSorter<OperationTableModel>(model);
		setRowSorter(tableRowSorter);		
		
		// prepare table
		setRowHeight(25);
		setBorder(null);
		setShowHorizontalLines(true);
		setShowVerticalLines(false);
		setRowSelectionAllowed(true);
		setCellSelectionEnabled(true);
		setColumnSelectionAllowed(false);
		setAutoFitWidths(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					
		// prepare header
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(true);
        tableHeader.setReorderingAllowed(false);
        
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
