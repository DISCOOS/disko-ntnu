package org.redcross.sar.gui;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.models.OperationTableModel;
import org.redcross.sar.gui.models.UnitTableModel;
import org.redcross.sar.gui.renderers.DiskoHeaderCellRenderer;
import org.redcross.sar.gui.renderers.UnitCellRenderer;
import org.redcross.sar.gui.renderers.UnitStringConverter;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.AbstractUnit;

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
		setFillsViewportHeight(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// prepare header
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(true);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setDefaultRenderer(new DiskoHeaderCellRenderer(tableHeader.getDefaultRenderer()));
        
        // add model lister to ensure data fit
        getModel().addTableModelListener(new TableModelListener() {

			public void tableChanged(TableModelEvent arg0) {
				// size to fit
				for(int i=0;i<getModel().getColumnCount();i++)
					getColumnModel().getColumn(i).sizeWidthToFit();
				// apply sorting
				((TableRowSorter)getRowSorter()).sort();
			}
        	
        });
        
	}	
	
	public void update() {
		((OperationTableModel)getModel()).update();
	}
}
