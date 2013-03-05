package org.redcross.sar.gui.model;

import java.io.File;

import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;

public class FileTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	public static final String COMPUTER = "Computer";
	
	protected final static FileSystemView view = FileSystemView.getFileSystemView();
	
	protected File root;
	protected String[] children;
	
	/* =====================================================
	 * Constructors
	 * ===================================================== */
	
	public FileTableModel() {
		this(COMPUTER);
	}

	public FileTableModel(String root) {
		this(new File(root));
	}
	
	public FileTableModel(File dir) {
		setRoot(dir);
	}
	
	/* =====================================================
	 * Public methods
	 * ===================================================== */
	
	public void setRoot( File dir ) {
		if ( dir != null ) {
			root = dir;
			if(COMPUTER.equals(dir.getName())) {
				File[] roots = File.listRoots();
				if(roots!=null) {
					children = new String[roots.length];
					for(int i=0;i<roots.length;i++) {
						children[i] = roots[i].getName();
					}
				}
			}
			else {
				children = dir.list();
			}
		}
		else {
			root = null;
			children = null;
		}
		fireTableDataChanged();
	}
	
	public File getFile(int index) {
		return children!=null && index>=0 && index<children.length ? 
				new File(root,children[index]) : null;
	}

	/* =====================================================
	 * TableModel implementation
	 * ===================================================== */
	
	@Override
	public int getRowCount() {
		return children != null ? children.length : 0;
	}

	@Override
	public int getColumnCount() {
		return 3; //children != null ? 3 : 0;
	}

	@Override
	public Object getValueAt(int row, int column){
		
		if ( root == null || children == null ) {
			return null;
		}
		
		File file = new File( root, children[row] );
		
		switch ( column ) {
			case 0: 
				return file; 				
			case 1: 
				if ( file.isDirectory() ) {
					return new Long( -1 );
				}
				else {
					return new Long( file.length() );
				}
			case 2: return file;
			default: return "";
		}
		
	}
	
	@Override
	public String getColumnName( int column ) {
		switch ( column ) {
			case 0: return "Navn"; 
			case 1: return "Størrelse";
			case 2: return "Beskrivelse";
			default: return "unknown";
		}
	}

	@Override
	public Class<?> getColumnClass( int column ) {
		switch ( column ) {
		case 0: return File.class; 
		case 1: return Long.class;
		case 2: return File.class;
		default: return Object.class;
	}
	}
	
	
	
}
