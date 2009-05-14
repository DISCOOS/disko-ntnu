package org.redcross.sar.gui.renderer;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public interface ITableHeaderRenderer extends TableCellRenderer {

	public int getWidth(Graphics g, JTable table, int row, int col);
	
	public void doClick();
	public void doClick(String name);
	
	public JComponent getComponent();
	
	public String getEditor();		
	public void setEditor(String name);
	
	public JComponent getEditorComponent(String name);
	
	public boolean isEditorVisible();
	public void setEditorVisible(boolean isVisible);
	
	public void addActionListener(ActionListener listener);
	public void removeActionListener(ActionListener listener);
	
	public void setBounds(Rectangle bounds);
	
	public int getDefaultHeight();
	
}
