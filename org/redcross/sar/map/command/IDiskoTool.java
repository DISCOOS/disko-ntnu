package org.redcross.sar.map.command;

import javax.swing.AbstractButton;
import javax.swing.JPanel;

import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IDiskoTool {

	public enum DiskoToolType {
		DRAW_HOST_TOOL,
		FREEHAND_TOOL,
		LINE_TOOL,
		POI_TOOL,
		POSITION_TOOL,
		FLANK_TOOL,
		SPLIT_TOOL,
		SELECT_FEATURE_TOOL,
		ZOOM_IN_TOOL,
		ZOOM_OUT_TOOL,
		PAN_TOOL,
		ERASE_COMMAND,
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
	
	public DiskoToolType getType();
	
	public boolean isHosted();
	
	public IDiskoHostTool getHostTool();
	
	public boolean activate(boolean allow);
	
	public boolean deactivate();
	
	public void setMsoDrawData(IDiskoTool tool);
	
	public void setMsoDrawData(IMsoObjectIf msoOwner, 
			IMsoObjectIf msoObject, IMsoManagerIf.MsoClassCode msoClassCode);
	
	public void setMsoObject(IMsoObjectIf msoObject);
	
	public IMsoObjectIf getMsoObject();
	
	public void setMsoOwner(IMsoObjectIf msoOwner);
	
	public IMsoObjectIf getMsoOwner();
	
	public IMsoManagerIf.MsoClassCode getMsoClassCode();

	public IDiskoMap getMap();
	
	public DiskoDialog getDialog();
	
	public AbstractButton getButton();
	
	public void addDiskoWorkEventListener(IDiskoWorkListener listener);
	
	public void removeDiskoWorkEventListener(IDiskoWorkListener listener);
	
	public Object getAttribute(String attribute);
	
	public void setAttribute(Object value, String attribute);
	
	public boolean isShowDialog();
		
	public void setShowDialog(boolean isShowDialog);
	
	public boolean setPropertyPanel(JPanel panel);
	
	public JPanel getPropertyPanel();
	
	public JPanel addPropertyPanel();
	
	public boolean removePropertyPanel(JPanel panel);
	
	public IDiskoToolState save();
	
	public boolean load(IDiskoToolState state);
	
	public interface IDiskoToolState {};
	
}
