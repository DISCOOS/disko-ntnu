package org.redcross.sar.gui.table;

import java.io.File;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableModel;

public class FileTableIconConverter implements TableIconConverter {

	private final static FileSystemView view = FileSystemView.getFileSystemView();	
	
	private final static Icon computerIcon = (Icon)UIManager.get("FileView.computerIcon");
	private final static Icon floppyIcon = (Icon)UIManager.get("FileView.floppyDriveIcon");
	private final static Icon driveIcon = (Icon)UIManager.get("FileView.hardDriveIcon");
	private final static Icon dirIcon = (Icon)UIManager.get("FileView.directoryIcon");
	private final static Icon fileIcon = (Icon)UIManager.get("FileView.fileIcon");
	
	public Icon toIcon(TableModel model, int row, int column) {
		
		// initialize
		Icon icon = null;
		
		// get value
		Object value = model.getValueAt(row, column);
		
		// convert
		switch ( column ) {
		case 0: 
			File file = (File)value;
			icon = view.getSystemIcon(file);
			
			if(icon==null) {
	    		if(view.isDrive(file)) {
	    			icon = driveIcon;
	    		}
	    		else if(view.isFloppyDrive(file)) {
	    			icon = floppyIcon;    			
	    		}
	    		else if(view.isComputerNode(file) || !view.isFileSystem(file)) {
	    			icon = computerIcon;
	    		}
	    		else if(view.isFileSystemRoot(file) && view.isFileSystem(file)) {
	    			icon = driveIcon;
		    	}
	    		else if(file.isDirectory()) {
	    			icon = dirIcon;
	    		}
	    		else {
	    			icon = fileIcon;		    			
	    		}
			}
		}
		// finished
		return icon;
		
	}

}
