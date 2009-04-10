package org.redcross.sar.gui.renderer;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class MsoIconListCellRenderer extends MsoIconRenderer implements ListCellRenderer {
	
	private static final long serialVersionUID = 1L;

	public MsoIconListCellRenderer(int options, boolean complete, String catalog) {
		super(options,complete,catalog);
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus)
	{

		// update selection state
		if (isSelected){
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} 
		else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		
		return getRenderer(value);
		

	}
	
}
