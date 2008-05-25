package org.redcross.sar.gui.map;

import java.util.EnumSet;

import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.map.command.IDrawTool.FeatureType;

public interface IDrawToolCollection extends IToolCollection {
	
	public IDrawTool getSelectedTool();
	
	public void register(IDrawTool tool);
	
	public void enableToolTypes(EnumSet<FeatureType> types);
	
}
