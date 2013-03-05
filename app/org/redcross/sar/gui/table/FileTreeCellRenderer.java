package org.redcross.sar.gui.table;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;

public class FileTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;
	
	protected final static FileSystemView view = FileSystemView.getFileSystemView();
	
	/* =====================================================
	 * TreeCellRenderer implementation
	 * ===================================================== */
	
	public Component getTreeCellRendererComponent(
			JTree tree,Object value, 
			boolean selected, boolean expanded, 
			boolean leaf, int row, boolean hasFocus)
    	{
		
		// forward
    	super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);

    	// get file
    	File file = (File)value;
    	
    	// try to get default name
    	String text = view.getSystemDisplayName(file);
    	
    	// update text
    	if(text!=null && !text.isEmpty()) {
        	setText(text);    		
    	}
    	else {
    		if(view.isDrive(file)) {
    			setText("Drive (" + file.toString().replace(System.getProperty("file.separator"), "") + ")");
    		}
    		else if(view.isFloppyDrive(file)) {
    			setText("Floppy (" + file.toString().replace(System.getProperty("file.separator"), "") + ")");    			
    		}
    		else if ((file.getName()+"").compareTo("") == 0)
	    		setText(file.getPath()+""); // prints label of node
	    	else 
	    		setText(file.getName()+"");
    	}
    	
    	// is leaf?
    	if(leaf) {
    		Icon icon = view.getSystemIcon(file);
    		setIcon(icon);
    		setLeafIcon(icon!=null ? icon : getDefaultLeafIcon());	
    	}
    	else if(view.isFileSystemRoot(file) && view.isFileSystem(file)) {
			Icon icon = view.getSystemIcon(file);
			setIcon(icon);
    		setClosedIcon(icon!=null ? icon : getDefaultClosedIcon());
    		setOpenIcon(icon!=null ? icon : getDefaultOpenIcon());
    	}
    	else {
    		setIcon(getDefaultClosedIcon());
    		setClosedIcon(getDefaultClosedIcon());
    		setOpenIcon(getDefaultOpenIcon());    
    	}
	
		// finished
    	return this;   
  	}
	

}
