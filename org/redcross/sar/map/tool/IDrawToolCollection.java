package org.redcross.sar.map.tool;

import java.util.EnumSet;

import org.redcross.sar.map.tool.IDrawTool.FeatureType;


public interface IDrawToolCollection extends IToolCollection {
	
	public IDrawTool getSelectedTool();
	
	public void register(IDrawTool tool);
	
	public void enableToolTypes(EnumSet<FeatureType> types);
	
}
