package org.redcross.sar.map.command;

import javax.swing.AbstractButton;

import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.map.command.IDiskoTool.IDiskoToolState;

public interface IDiskoHostTool {

	public IDiskoTool getTool();
	
	public void setTool(IDiskoTool tool);
	
	public void onClick();
	
	public boolean activate(boolean allow);
	
	public boolean deactivate();	
	
	public DiskoDialog getDialog();
	
	public AbstractButton getButton();
	
	public IDiskoToolState save();
	
	public boolean load(IDiskoToolState state);
		
	
}
