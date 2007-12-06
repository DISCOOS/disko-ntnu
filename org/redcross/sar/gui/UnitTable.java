package org.redcross.sar.gui;

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.redcross.sar.gui.models.UnitTableModel;
import org.redcross.sar.gui.renderers.UnitTableCellRenderer;
import org.redcross.sar.mso.IMsoModelIf;

public class UnitTable extends JTable {

	private static final long serialVersionUID = 1L;
	
	public UnitTable(IMsoModelIf msoModel, int numColumns) {
		UnitTableModel model = new UnitTableModel(msoModel, numColumns);
		setModel(model);
		Dimension size = new Dimension(60, 60);
		UnitTableCellRenderer renderer = new UnitTableCellRenderer();
		setDefaultRenderer(Object.class, renderer);
		setTableHeader(null);
		setRowHeight(size.height);
		for(int i=0;i<numColumns;i++) {
			TableColumn col = getColumnModel().getColumn(i);
			col.setMinWidth(size.width*3);
			col.setMaxWidth(size.width*3);
		}
		setShowHorizontalLines(false);
		setShowVerticalLines(false);
		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(true);
		setIntercellSpacing(new Dimension(5, 5));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
}
