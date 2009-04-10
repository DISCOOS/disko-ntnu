package org.redcross.sar.map.command;

import javax.swing.AbstractButton;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.map.command.IMapCommand.IDiskoCommandState;

public interface IHostDiskoCommand {

	public IMapCommand getCommand();
	
	public void setCommand(IMapCommand command);
	
	public void onClick();
	
	public DefaultDialog getDialog();
	
	public AbstractButton getButton();
	
	public IDiskoCommandState save();
	
	public boolean load(IDiskoCommandState state);
		
	
}
