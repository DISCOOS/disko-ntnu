package org.redcross.sar.map.command;

import javax.swing.AbstractButton;

import org.redcross.sar.gui.DiskoDialog;

public interface IHostDiskoTool {

	public IDiskoTool getTool();
	
	public void setTool(IDiskoTool tool);
	
	public void onClick();
	
	public boolean activate(boolean allow);
	
	public boolean deactivate();	
	
	public DiskoDialog getDialog();
	
	public AbstractButton getButton();
	
	
}
