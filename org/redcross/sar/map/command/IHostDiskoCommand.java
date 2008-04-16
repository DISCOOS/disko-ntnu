package org.redcross.sar.map.command;

import javax.swing.AbstractButton;

import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.map.command.IDiskoCommand.IDiskoCommandState;

public interface IHostDiskoCommand {

	public IDiskoCommand getCommand();
	
	public void setCommand(IDiskoCommand command);
	
	public void onClick();
	
	public DiskoDialog getDialog();
	
	public AbstractButton getButton();
	
	public IDiskoCommandState save();
	
	public boolean load(IDiskoCommandState state);
		
	
}
