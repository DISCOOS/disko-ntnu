package org.redcross.sar.map.tool;

import java.io.IOException;

import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.tool.IMapTool.MapToolType;

public interface IToolCollection {
	
	public IHostDiskoTool getHostTool();	
	public void setHostTool(IHostDiskoTool tool);
	
	public void setup();
	
	public IMapTool getTool(MapToolType type);
	public boolean containsToolType(MapToolType type);	
	
	public void register(IMapTool tool);
	public void register(IDiskoMap map) throws IOException;	
	
	public IMapTool getSelectedTool();	
	public void setSelectedTool(IMapTool tool, boolean activate);

	public boolean getEnabled(MapToolType type);
	public void setEnabled(MapToolType type, boolean isEnabled);
	
	public boolean getVisible(MapToolType type);
	public void setVisible(MapToolType type, boolean isVisible);
		
	public Object getAttribute(MapToolType type, String attribute);	
	public void setAttribute(Object value, String attribute);	
	public void setAttribute(MapToolType type, Object value, String attribute);
	
	public void setBatchUpdate(boolean isBatchUpdate);

	public void enableTools(boolean isEnabled);	
	public void enableToolType(MapToolType type);
	
	public void getToolCaption();
	
}
