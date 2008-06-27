package org.redcross.sar.wp.ds;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.gui.renderer.DiskoHeaderCellRenderer;
import org.redcross.sar.mso.IMsoModelIf;

public class AssignmentTable extends JTable {

	private static final long serialVersionUID = 1L;
	
	private TableRowSorter<AssignmentTableModel> tableRowSorter = null;
	
	@SuppressWarnings("unchecked")
	public AssignmentTable(IMsoModelIf msoModel) {
		
		// create the model
		AssignmentTableModel model = new AssignmentTableModel(msoModel);
		
		// assign the model
		setModel(model);
		
		// add row sorter
		tableRowSorter = new TableRowSorter<AssignmentTableModel>(model);
		tableRowSorter.setStringConverter(new AssignmentStringConverter());
		tableRowSorter.setSortsOnUpdates(true);
		setRowSorter(tableRowSorter);
		
		// set header
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setDefaultRenderer(new DiskoHeaderCellRenderer(tableHeader.getDefaultRenderer()));
        
        // set default renderer
        setDefaultRenderer(Object.class, new AssignmentCellRenderer());
        
        // misc.
		setRowHeight(34);
		setColumnSelectionAllowed(false);
		setColumnWidths();
		setShowVerticalLines(false);
		setUpdateSelectionOnSort(true);
				
	}
	
	private void setColumnWidths() {
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setResizable(false);
			switch(i) {
				case 0: column.setPreferredWidth(125); break;
				case 1: column.setPreferredWidth(40); break;
				case 2: column.setPreferredWidth(30); break;
				case 3: column.setPreferredWidth(30); break;
				case 4: column.setPreferredWidth(30); break;
			}
		}
	}
	
	public void setEstimator(RouteCostEstimator estimator) {
		((AssignmentTableModel)getModel()).setEstimator(estimator);
	}
	

}
