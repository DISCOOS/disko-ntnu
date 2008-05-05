package org.redcross.sar.gui.map;

import java.io.IOException;

import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;

public interface IDrawDialog extends IHostToolDialog {
	
	public void register(IDiskoMap map) throws IOException;
	
	public void register(IDrawTool tool);

	public void setToolSet(MsoClassCode code, Object[] attributes);
	
}
