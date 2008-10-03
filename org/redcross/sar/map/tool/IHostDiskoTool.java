package org.redcross.sar.map.tool;

import javax.swing.AbstractButton;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.map.tool.IMapTool.IDiskoToolState;

public interface IHostDiskoTool {

	public IMapTool getTool();
	
	public void setTool(IMapTool tool, boolean activate);
	
	public void onClick();
	
	public boolean activate(boolean allow);
	
	public boolean deactivate();	
	
	public DefaultDialog getDialog();
	
	public AbstractButton getButton();
	
	public IDiskoToolState save();
	
	public boolean load(IDiskoToolState state);
	
}
