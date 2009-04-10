package org.redcross.sar.wp.ds;

import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.model.DiskoTableColumnModel;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.gui.table.DiskoTableHeader;

public class LevelTable extends DiskoTable {

	private static final long serialVersionUID = 1L;

	private TableRowSorter<LevelTableModel> tableRowSorter;

	/* ===============================================================
	 * Constructors
	 * =============================================================== */

	@SuppressWarnings("unchecked")
	public LevelTable() {

		// forward
		super();

		// create the model
		LevelTableModel model = new LevelTableModel();

		// assign the model
		setModel(model);

		// forward
		installHeader();

		// set string converter
		setStringConverter(new LevelStringConverter(true));

		// add row sorter
		tableRowSorter = new TableRowSorter<LevelTableModel>(model);
		tableRowSorter.setStringConverter(new LevelStringConverter(false));
		tableRowSorter.setMaxSortKeys(1);
		tableRowSorter.setSortsOnUpdates(true);
		setRowSorter(tableRowSorter);

        // set default renderer
        setDefaultRenderer(Object.class, createRenderer());

        // set layout
		setRowHeight(35);
		setColumnWidths();
		setAutoFitWidths(true);
		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);
		setShowVerticalLines(false);

		/*
		// set header alignments
		int count = model.getColumnCount();
		for(int i=0; i<count; i++) {
			// set alignment
			switch(i) {
			case LevelTableModel.NAME_INDEX:
				model.setColumnAlignment(i,SwingConstants.LEFT);
				break;
			default:
				model.setColumnAlignment(i,SwingConstants.RIGHT);
				break;
			}
		}
		*/
	}



	/* ===============================================================
	 * Helper methods
	 * =============================================================== */

	private void installHeader() {

		// get header
		final DiskoTableHeader header = (DiskoTableHeader)getTableHeader();

		// do not allow to reorder or resize columns
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);

	}

	private LevelCellRenderer createRenderer() {
		return new LevelCellRenderer();
	}

	private void setColumnWidths() {
		DiskoTableColumnModel model = (DiskoTableColumnModel)getColumnModel();
		for (int i = 0; i < model.getColumnCount(false); i++) {
			TableColumn column = model.getColumn(i,false);
			switch(i) {
			case LevelTableModel.NAME_INDEX:
				setColumnWidth(column, 100, true, true, false); break;
			default:
				setColumnWidth(column, 50, true, true, false); break;
			}
		}
	}
}
