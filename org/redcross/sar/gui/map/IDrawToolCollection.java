package org.redcross.sar.gui.map;

import java.io.IOException;
import java.util.EnumSet;

import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.map.command.IDrawTool.FeatureType;

public interface IDrawToolCollection extends IToolCollection {
	
	public void register(IDiskoMap map) throws IOException;
	
	public void register(IDrawTool tool);
	
	public void enableToolTypes(EnumSet<FeatureType> types);
	
}
