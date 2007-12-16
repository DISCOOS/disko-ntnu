package org.redcross.sar.gui.map;

import javax.swing.JPanel;

import org.redcross.sar.map.command.IDiskoTool;
import org.redcross.sar.map.command.IDiskoHostTool;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IHostToolDialog {
	
	public IDiskoHostTool getHostTool();
	
	public void setHostTool(IDiskoHostTool tool);
	
	public IDiskoTool  getActiveTool();
	
	public void setActiveTool(IDiskoTool tool);
	
	public void register(IDiskoTool tool, JPanel panel);

	public void setup();
	
	public IDiskoTool getTool(DiskoToolType type);
	
	public boolean getEnabled(DiskoToolType type);
	
	public void setEnabled(DiskoToolType type, boolean isEnabled);
	
	public void setVisible(DiskoToolType type, boolean isVisible);
	
	public boolean getVisible(DiskoToolType type);
	
	public void setMsoDrawData(IDiskoTool tool);
	
	public void setMsoDrawData(IMsoObjectIf msoOwner, 
			IMsoObjectIf msoObject, IMsoManagerIf.MsoClassCode msoClassCode);
	
	public void setAttribute(Object value, String attribute);
	
	public Object getAttribute(DiskoToolType type, String attribute);
	
	public void setAttribute(DiskoToolType type, Object value, String attribute);

}