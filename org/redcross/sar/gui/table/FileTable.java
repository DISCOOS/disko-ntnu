package org.redcross.sar.gui.table;

import java.io.File;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.model.FileTableModel;
import org.redcross.sar.gui.renderer.FileTableCellRenderer;
import org.redcross.sar.gui.tree.AbstractTreeFilter;
import org.redcross.sar.gui.tree.TreeFilter;
import org.redcross.sar.util.Utils;

public class FileTable extends DiskoTable {

	private static final long serialVersionUID = 1L;

	private TableRowSorter<FileTableModel> tableRowSorter;

	/* =================================================
	 * Constructors
	 * ================================================= */

	public FileTable() {

		// forward
		super(new FileTableModel());

		// create table model
		FileTableModel model = (FileTableModel)getModel();

		// Enable correct auto width of name column 
		setStringConverter(new FileTableStringConverter());

		// add row sorter
		tableRowSorter = new TableRowSorter<FileTableModel>(model);
		tableRowSorter.setStringConverter(getStringConverter());
		tableRowSorter.setComparator(0, new DefaultFileComperator());
		tableRowSorter.setComparator(1, new Comparator<Long>(){

			@Override
			public int compare(Long o1, Long o2) {
				return o1.compareTo(o2);
			}

		});
		tableRowSorter.setMaxSortKeys(1);
		tableRowSorter.setSortsOnUpdates(true);
		tableRowSorter.setRowFilter(createDefaultFilter());
		setRowSorter(tableRowSorter);		

		// prepare table
		setRowHeight(18);
		setShowHorizontalLines(false);
		setShowVerticalLines(false);
		setRowSelectionAllowed(true);
		setCellSelectionEnabled(true);
		setColumnSelectionAllowed(false);
		setAutoFitWidths(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// set default cell renderers
		FileTableCellRenderer renderer = new FileTableCellRenderer();
		setDefaultRenderer(File.class, renderer);
		setDefaultRenderer(Long.class, renderer);
		setDefaultRenderer(Object.class, renderer);

		// prepare header
		JTableHeader tableHeader = getTableHeader();
		tableHeader.setResizingAllowed(true);
		tableHeader.setReorderingAllowed(false);

	}	

	/* =================================================
	 * Public methods
	 * ================================================= */

	public int findRow(String file) {
		return findRow(new File(file));
	}

	public int findRow(File file) {
		FileTableModel model = (FileTableModel)getModel();
		for(int i=0;i<getRowCount();i++) {
			if(file.equals(model.getFile(i)))
				return i;
		}
		return -1;
	}

	/* =====================================================
	 * Static methods
	 * ===================================================== */

	
	public static RowFilter<FileTableModel, Integer> createDefaultFilter() {

		RowFilter<FileTableModel, Integer> filter = new RowFilter<FileTableModel, Integer>() {
			
			public boolean include(Entry<? extends FileTableModel, ? extends Integer> entry) {
				
				// is file hidden?
				File file = (File)entry.getValue(0);
				
				// filter?
				return file!=null ? file.isHidden() : true;
				
			}
		};

		return filter;

	}
	
	public static RowFilter<FileTableModel, Integer> createFileFilter() {

		RowFilter<FileTableModel, Integer> filter = new RowFilter<FileTableModel, Integer>() {
			
			public boolean include(Entry<? extends FileTableModel, ? extends Integer> entry) {
				
				// is file hidden?
				File file = (File)entry.getValue(0);
				
				// filter?
				return file!=null ? file.isHidden() || file.isFile() : true;
				
			}
		};

		return filter;

	}	
	
	public static RowFilter<FileTableModel, Integer> createDirectoryFilter() {

		RowFilter<FileTableModel, Integer> filter = new RowFilter<FileTableModel, Integer>() {
			
			public boolean include(Entry<? extends FileTableModel, ? extends Integer> entry) {
				
				// is file hidden?
				File file = (File)entry.getValue(0);
				
				// filter?
				return file!=null ? file.isHidden() || file.isDirectory() : true;
				
			}
		};

		return filter;

	}	

	public static RowFilter<FileTableModel, Integer> createExtensionFilter(final String ext) {

		RowFilter<FileTableModel, Integer> filter = new RowFilter<FileTableModel, Integer>() {
			
			public boolean include(Entry<? extends FileTableModel, ? extends Integer> entry) {
				
				// is file hidden?
				File file = (File)entry.getValue(0);
				// get extension of found file
				String found = Utils.getExtension(file);
				
				// filter?
				return file!=null ? file.isHidden() 
						|| !found.equalsIgnoreCase(ext) 
						|| file.isDirectory(): true;
				
			}
		};

		return filter;

	}	
	
}
