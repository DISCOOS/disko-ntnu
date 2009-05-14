package org.redcross.sar.gui.renderer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class MsoListCellRenderer extends MsoLabelRenderer implements ListCellRenderer {
	
	private static final long serialVersionUID = 1L;

	public MsoListCellRenderer(int optionsName, boolean completeName) {
		super(optionsName, completeName);
	}
	
	public MsoListCellRenderer(int optionsName, boolean completeName, boolean showIcon, String iconCatalog, int mapIconTo) {
		super(optionsName, completeName, showIcon, iconCatalog, mapIconTo);
	}
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus)
	{
		// forward
		JLabel label = super.getRenderer(value);
		
		// update selection state
		if (isSelected){
			label.setBackground(list.getSelectionBackground());
			label.setForeground(list.getSelectionForeground());
		} 
		else {
			label.setBackground(list.getBackground());
			label.setForeground(list.getForeground());
		}
		
		return label;
		
	}
	
}
