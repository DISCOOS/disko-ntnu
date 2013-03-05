package org.redcross.sar.gui.table;

import java.io.File;
import java.text.DecimalFormat;

import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

public class FileTableStringConverter extends TableStringConverter {

	private final static FileSystemView view = FileSystemView.getFileSystemView();
	
	@Override
	public String toString(TableModel model, int row, int column) {
		
		// initialize
		String text = "";
		
		// get value
		Object value = model.getValueAt(row, column);
		
		// convert
		switch ( column ) {
		case 0: 
			File file = (File)value;
			text = view.getSystemDisplayName(file);
			text = text==null || text.isEmpty() ? file.getName() : text;
			break;
		case 1:
			Long size = (Long)value;
			if (size==-1) {
				text = "--";
			}
			else {
				DecimalFormat f = new DecimalFormat("#,###,###");
				text = f.format(Math.ceil(size/1024)) + " KB";
			}		
			break;
		case 2: 
			text = view.getSystemTypeDescription((File)value);
			break;
		default: 
			text = value!=null ? value.toString() : "";
		}
		
		// finished
		return text;
		
	}

}
