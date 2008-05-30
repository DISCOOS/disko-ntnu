package org.redcross.sar.map.tool;

import java.io.IOException;

import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IToolCollection {
	
	public IHostDiskoTool getHostTool();	
	public void setHostTool(IHostDiskoTool tool);
	
	public void setup();
	
	public IDiskoTool getTool(DiskoToolType type);
	public boolean containsToolType(DiskoToolType type);	
	
	public void register(IDiskoTool tool);
	public void register(IDiskoMap map) throws IOException;	
	
	public IDiskoTool getSelectedTool();	
	public void setSelectedTool(IDiskoTool tool, boolean activate);

	public boolean getEnabled(DiskoToolType type);
	public void setEnabled(DiskoToolType type, boolean isEnabled);
	
	public boolean getVisible(DiskoToolType type);
	public void setVisible(DiskoToolType type, boolean isVisible);
	
	
	public void setMsoDrawData(IDiskoTool tool);	
	public void setMsoDrawData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, IMsoManagerIf.MsoClassCode msoClassCode);
	
	public Object getAttribute(DiskoToolType type, String attribute);	
	public void setAttribute(Object value, String attribute);	
	public void setAttribute(DiskoToolType type, Object value, String attribute);
	
	public void setBatchUpdate(boolean isBatchUpdate);

	public void enableTools(boolean isEnabled);	
	public void enableToolType(DiskoToolType type);
	
	public void getToolCaption();
	
}
