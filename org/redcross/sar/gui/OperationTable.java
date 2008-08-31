package org.redcross.sar.gui;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.model.OperationTableModel;
import org.redcross.sar.gui.renderer.DiskoHeaderRenderer;

public class OperationTable extends JTable {

	private static final long serialVersionUID = 1L;
	
	private TableRowSorter<OperationTableModel> tableRowSorter = null;
	
	public OperationTable() {
		
		// create table model
		OperationTableModel model = new OperationTableModel();
		
		// prepare data model
		setModel(model);
		
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
		//setFillsViewportHeight(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		
		// prepare header
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(true);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setDefaultRenderer(new DiskoHeaderRenderer());
        
        // add model lister to ensure data fit
        getModel().addTableModelListener(new TableModelListener() {

			public void tableChanged(TableModelEvent arg0) {
				// size to fit
				for(int i=0;i<getModel().getColumnCount();i++)
					getColumnModel().getColumn(i).sizeWidthToFit();
				// apply sorting
				((TableRowSorter<?>)getRowSorter()).sort();
			}
        	
        });
        
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
		else if(getRowCount()>0)
			getSelectionModel().setSelectionInterval(0, 0);
		// update
		doLayout();
		// finshed
		return select;		
	}
}
