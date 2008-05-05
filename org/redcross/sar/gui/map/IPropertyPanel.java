package org.redcross.sar.gui.map;

import java.awt.event.ActionListener;

public interface IPropertyPanel {
	
	public void update();
	
	public void addActionListener(ActionListener listener);	
	public void removeActionListener(ActionListener listener);
	
}
