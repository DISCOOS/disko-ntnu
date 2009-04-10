package org.redcross.sar.gui.table;

import javax.swing.Icon;
import javax.swing.table.TableModel;

public interface TableIconConverter {
	
	public Icon toIcon(TableModel model, int row, int column);
	
}
