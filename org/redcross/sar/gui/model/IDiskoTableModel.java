package org.redcross.sar.gui.model;

import javax.swing.table.TableModel;

public interface IDiskoTableModel extends TableModel {

	public String getHeaderTooltipText(int column);	
	public void setHeaderTooltipText(int column, String text);
	
	public boolean isHeaderEditable(int column);
	public void setHeaderEditable(int column, boolean isEditable);
	
	public String getHeaderEditor(int column);
	public void setHeaderEditor(int column, String name);
	
}
