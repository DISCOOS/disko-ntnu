package org.redcross.sar.gui;

import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.model.AssignmentTableModel;
import org.redcross.sar.gui.renderer.AssignmentCellRenderer;
import org.redcross.sar.gui.renderer.AssignmentStringConverter;
import org.redcross.sar.gui.renderer.DiskoHeaderCellRenderer;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.AssignmentImpl;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;

public class AssignmentTable extends JTable {

	private static final long serialVersionUID = 1L;
	private TableRowSorter<AssignmentTableModel> tableRowSorter = null;
	private Hashtable<AssignmentStatus, RowFilter<?, ?>> rowFilters = null;
	
	@SuppressWarnings("unchecked")
	public AssignmentTable(IMsoModelIf msoModel) {
		
		// create the model
		AssignmentTableModel model = new AssignmentTableModel(msoModel);
		
		// assign the model
		setModel(model);
		
		// add row sorter
		tableRowSorter = new TableRowSorter<AssignmentTableModel>(model);
		tableRowSorter.setStringConverter(new AssignmentStringConverter());
		setRowSorter(tableRowSorter);
		
		// set default boolean renderer
		((JCheckBox)getDefaultRenderer(Boolean.class)).setFocusable(false);
		setDefaultRenderer(AssignmentImpl.class, new AssignmentCellRenderer());
		
		// set header
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setDefaultRenderer(new DiskoHeaderCellRenderer(tableHeader.getDefaultRenderer()));
        
        // misc.
		setRowHeight(34);
		setColumnSelectionAllowed(false);
		setIntercellSpacing(new Dimension(10, 1));
		setColumnWidths();
		
	}
	
	private void setColumnWidths() {
		for (int i = 0; i < 3; i++) {
			TableColumn column = getColumnModel().getColumn(i);
			column.setResizable(false);
			switch(i) {
				case 0: column.setPreferredWidth(45); break;
				case 1: column.setPreferredWidth(175); break;
				case 2: column.setPreferredWidth(400); break;
				case 3: column.setPreferredWidth(225); break;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void showOnly(Object obj) {
		if (rowFilters == null) {
			rowFilters = new Hashtable<AssignmentStatus, RowFilter<?, ?>>();
			AssignmentStatus[] values = AssignmentStatus.values();
			for (int i = 0; i < values.length; i++) {
				rowFilters.put(values[i], RowFilter.regexFilter(".*"+DiskoEnumFactory.getText(values[i])+".*"));
			}
		}
		tableRowSorter.setRowFilter((RowFilter)rowFilters.get(obj));
	}
}
