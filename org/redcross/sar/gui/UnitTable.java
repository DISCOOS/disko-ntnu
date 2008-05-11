package org.redcross.sar.gui;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.models.UnitTableModel;
import org.redcross.sar.gui.renderers.DiskoTableHeaderCellRenderer;
import org.redcross.sar.gui.renderers.UnitTableCellRenderer;
import org.redcross.sar.gui.renderers.UnitTableStringConverter;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.AbstractUnit;

public class UnitTable extends JTable {

	private static final long serialVersionUID = 1L;
	
	private TableRowSorter<UnitTableModel> tableRowSorter = null;
	
	public UnitTable(IMsoModelIf msoModel, String catalog) {
		
		// set default unit cell table renderer
		setDefaultRenderer(AbstractUnit.class, new UnitTableCellRenderer(catalog));
		
		// prepare data model
		UnitTableModel model = new UnitTableModel(msoModel); 
		setModel(model);
		
		// add row sorter
		tableRowSorter = new TableRowSorter<UnitTableModel>(model);
		tableRowSorter.setStringConverter(new UnitTableStringConverter());
		setRowSorter(tableRowSorter);		
		
		// prepare table
		setRowHeight(getRowHeight(catalog));
		setShowHorizontalLines(false);
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
        tableHeader.setDefaultRenderer(new DiskoTableHeaderCellRenderer(tableHeader.getDefaultRenderer()));
        
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
	
	private int getRowHeight(String catalog) {
		Icon icon = DiskoIconFactory.getIcon("GENERAL.EMPTY", catalog);
		if(icon!=null) {
			return icon.getIconHeight()+2;
		}
		else return 20;
	}
	
}
