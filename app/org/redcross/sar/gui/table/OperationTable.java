package org.redcross.sar.gui.table;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableStringConverter;

import no.cmr.hrs.sar.tools.IDHelper;

import org.redcross.sar.gui.model.OperationTableModel;
import org.redcross.sar.gui.renderer.DiskoTableCellRenderer;

public class OperationTable extends DiskoTable {

	private static final long serialVersionUID = 1L;
	
	private TableRowSorter<OperationTableModel> tableRowSorter = null;
	private TableStringConverter converter = new TableStringConverter() {

		@Override
		public String toString(TableModel model, int row, int col) {
			return IDHelper.formatOperationID((String)model.getValueAt(row, col));
		}
		
	};
	
	private DiskoTableCellRenderer renderer = new DiskoTableCellRenderer() {

		private static final long serialVersionUID = 1L;
		
		/* ==================================================
		 * TableCellRenderer implementation
		 * ================================================== */

		public JLabel getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int col) {

			// initialize
			String text = "";

			// get model info
			super.initialize(table, row, col);

			// convert value to text?
			if(m_rowInModel!=-1 && m_colInModel!=-1) {

				text = converter.toString(getModel(),m_rowInModel,m_colInModel);

			}

			// forward
			JLabel renderer = super.prepare(table, text, isSelected, hasFocus, row, col, false, false);

			// update alignment and borders
			update(renderer,renderer.getIcon());


			// finished
			return renderer;
		}
	};	
	
	public OperationTable() {
		
		// forward
		super(new OperationTableModel());
		
		// create table model
		OperationTableModel model = (OperationTableModel)getModel();
		
		// add row sorter
		tableRowSorter = new TableRowSorter<OperationTableModel>(model);
		setRowSorter(tableRowSorter);		
		setStringConverter(converter);
		setDefaultRenderer(Object.class, renderer);
		
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
		// finished
		return select;		
	}
}
