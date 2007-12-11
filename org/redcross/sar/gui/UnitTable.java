package org.redcross.sar.gui;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.models.UnitTableModel;
import org.redcross.sar.gui.renderers.DiskoTableHeaderCellRenderer;
import org.redcross.sar.gui.renderers.UnitTableCellRenderer;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.AbstractUnit;

public class UnitTable extends JTable {

	private static final long serialVersionUID = 1L;
	
	private static final int ROW_HEIGHT = 50;
	
	public UnitTable(IMsoModelIf msoModel) {
		
		// set default unit cell table renderer
		setDefaultRenderer(AbstractUnit.class, new UnitTableCellRenderer());
		
		// prepare data model
		UnitTableModel model = new UnitTableModel(msoModel); 
		setModel(model);
		
		// prepare table
		setRowHeight(ROW_HEIGHT);
		setShowHorizontalLines(false);
		setShowVerticalLines(false);
		setRowSelectionAllowed(true);
		setCellSelectionEnabled(true);
		setColumnSelectionAllowed(false);
		setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		/*
		// prepare row sorter
		TableRowSorter<UnitTableModel> sorter = new TableRowSorter<UnitTableModel>(model);
		setRowSorter(sorter);
		*/
		
		// prepare header
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(true);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setDefaultRenderer(new DiskoTableHeaderCellRenderer(tableHeader.getDefaultRenderer()));
        
        // add model lister to ensure data fit
        getModel().addTableModelListener(new TableModelListener() {

			public void tableChanged(TableModelEvent arg0) {
				// size to fit
				for(int i=0;i<getModel().getColumnCount();i++)
					getColumnModel().getColumn(i).sizeWidthToFit();
			}
        	
        });
        
	}
	
}
