package org.redcross.sar.gui.table;

import java.io.File;
import java.util.Comparator;

import javax.swing.filechooser.FileSystemView;

public class DefaultFileComperator implements Comparator<File> {
	
	final FileSystemView view = FileSystemView.getFileSystemView();
	
	@Override
	public int compare(File f1, File f2) {
		// forward
		int c = evaluate(view.isComputerNode(f1),view.isComputerNode(f2));
		// finished?
		if(c!=-2) return evaluate(c,f1,f2);
		// forward
		c = evaluate(!view.isFileSystem(f1),!view.isFileSystem(f2));
		// finished?
		if(c!=-2) return evaluate(c,f1,f2);
		// forward
		c = evaluate(view.isDrive(f1),view.isDrive(f2));
		// finished?
		if(c!=-2) return evaluate(c,f1,f2);
		// forward
		c = evaluate(view.isFloppyDrive(f1),view.isFloppyDrive(f2));
		// finished?
		if(c!=-2) return evaluate(c,f1,f2);
		// forward
		c = evaluate(view.isFileSystemRoot(f1),view.isFileSystemRoot(f2));
		// finished?
		if(c!=-2) return evaluate(c,f1,f2);
		// forward
		c = evaluate(f1.isDirectory(),f2.isDirectory());
		// finished?
		if(c!=-2) return evaluate(c,f1,f2);
		// compare on name
		return f1.getName().compareTo(f2.getName());
	}
	
	private int evaluate(boolean b1, boolean b2) {
		if(b1==b2) {
			return b1 ? 0 : -2;
		}
		else if(b1 && !b2) return -1;
		return 1;
	}
	
	private int evaluate(int c, File f1, File f2) {
		if(c==0) {
			return f1.getName().compareTo(f2.getName());
		}
		return c;
	}
	
	
}
