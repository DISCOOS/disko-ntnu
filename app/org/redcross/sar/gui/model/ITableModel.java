package org.redcross.sar.gui.model;

import javax.swing.table.TableModel;

import org.redcross.sar.data.IData.DataOrigin;

public interface ITableModel extends TableModel {

	public String getHeaderTooltipText(int column);
	public void setHeaderTooltipText(int column, String text);

	public boolean isHeaderEditable(int column);
	public void setHeaderEditable(int column, boolean isEditable);

	public String getHeaderEditor(int column);
	public void setHeaderEditor(int column, String name);

	public int getColumnAlignment(int column);
	public void setColumnAlignment(int column, int alignment);

	public boolean isColumnWidthFixed(int column);	
	public int getColumnFixedWidth(int column);
	public void setColumnFixedWidth(int column, int fixedwidth);
	
	public IState getState(int row);
	
	public interface IState {
		public DataOrigin getOrigin();
		public boolean isLoopbackMode();
		public boolean isRollbackMode();
	}
	
}
