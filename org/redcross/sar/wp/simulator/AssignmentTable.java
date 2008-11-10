package org.redcross.sar.wp.simulator;

import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.model.DiskoTableColumnModel;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.wp.simulator.AssignmentTableModel;
import org.redcross.sar.wp.simulator.AssignmentStringConverter;

public class AssignmentTable extends DiskoTable {

	private static final long serialVersionUID = 1L;

	private TableRowSorter<AssignmentTableModel> tableRowSorter = null;

	@SuppressWarnings("unchecked")
	public AssignmentTable(IMsoModelIf msoModel, boolean archived) {

		// forward
		super();

		// create the model
		AssignmentTableModel model = new AssignmentTableModel(msoModel, archived);

		// assign the model
		setModel(model);

		// add row sorter
		tableRowSorter = new TableRowSorter<AssignmentTableModel>(model);
		tableRowSorter.setStringConverter(new AssignmentStringConverter());
		tableRowSorter.setComparator(0, IAssignmentIf.ASSIGNMENT_TYPE_NUMBER_COMPERATOR);
		tableRowSorter.setMaxSortKeys(1);
		tableRowSorter.setSortsOnUpdates(true);
		setRowSorter(tableRowSorter);

        // set default renderer
        setDefaultRenderer(Object.class, new AssignmentCellRenderer());

        // set layout
		setRowHeight(35);
		setColumnWidths();
		setAutoFitWidths(true);
		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);
		setShowVerticalLines(false);

	}

	private void setColumnWidths() {
		DiskoTableColumnModel model = (DiskoTableColumnModel)getColumnModel();
		for (int i = 0; i < model.getColumnCount(false); i++) {
			TableColumn column = model.getColumn(i,false);
			switch(i) {
				case 0: setColumnWidth(column, 175, true, true, false); break;
				case 1: setColumnWidth(column, 100, true, true, false); break;
				case 2: setColumnWidth(column, 55, true, true, false); break;
				case 3: setColumnWidth(column, 55, true, true, false); break;
				case 4: setColumnWidth(column, 100, true, true, false); break;
			}
		}
	}

}
