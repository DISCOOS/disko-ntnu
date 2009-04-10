package org.redcross.sar.gui.panel;

import org.redcross.sar.map.tool.IMapTool;

public interface IToolPanel extends IPanel {
	
	public IMapTool getTool();
	
}
