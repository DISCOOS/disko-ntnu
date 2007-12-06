package org.redcross.sar.gui;

import java.io.IOException;
import java.util.List;

import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.IDiskoTool;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IDrawDialog {
	
	public void onLoad(IDiskoMap map) throws IOException;
	
	public void setSnapTolerance(int value);
	
	public List getSnapToLayers();
	
	public void setSnapableLayers(List layers);
	
	public void setToolSet(MsoClassCode code, Object[] attributes);
	
}
