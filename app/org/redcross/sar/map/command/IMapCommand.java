package org.redcross.sar.map.command;

import javax.swing.AbstractButton;
import javax.swing.JPanel;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.work.event.IFlowListener;

import com.esri.arcgis.systemUI.ICommand;

public interface IMapCommand extends ICommand {

	public enum MapCommandType {
		ZOOM_IN_FIXED_COMMAND,
		ZOOM_OUT_FIXED_COMMAND,
		ZOOM_FULL_EXTENT_COMMAND,
		ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND,
		ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND,
		MAP_TOGGLE_COMMAND,
		TOC_COMMAND,
		SCALE_COMMAND,
		GOTO_COMMAND,
		ELEMENT_COMMAND
    }	
	
	public String getName();
	
	public MapCommandType getType();
	
	public void onCreate(Object obj);
	
	public void onClick();	
			
	public boolean isHosted();
	
	public IHostDiskoCommand getHostCommand();
	
	public void setMsoDrawData(IMapCommand command);
	
	public void setMsoDrawData(IMsoObjectIf msoOwner, 
			IMsoObjectIf msoObject, IMsoManagerIf.MsoClassCode msoClassCode);
	
	public void setMsoObject(IMsoObjectIf msoObject);
	
	public IMsoObjectIf getMsoObject();
	
	public void setMsoOwner(IMsoObjectIf msoOwner);
	
	public IMsoObjectIf getMsoOwner();
	
	public IMsoManagerIf.MsoClassCode getMsoClassCode();

	public DefaultDialog getDialog();
	
	public AbstractButton getButton();
	
	public void addWorkEventListener(IFlowListener listener);
	
	public void removeWorkEventListener(IFlowListener listener);
	
	public Object getAttribute(String attribute);
	
	public void setAttribute(Object value, String attribute);
	
	public boolean isShowDialog();
		
	public void setShowDialog(boolean isShowDialog);
	
	public boolean setPropertyPanel(JPanel panel);
	
	public JPanel getPropertyPanel();
	
	public JPanel addPropertyPanel();
	
	public boolean removePropertyPanel(JPanel panel);
	
	public IDiskoCommandState save();
	
	public boolean load(IDiskoCommandState state);
	
	public interface IDiskoCommandState {};
	
}
