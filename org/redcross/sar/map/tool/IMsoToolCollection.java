package org.redcross.sar.map.tool;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IMsoToolCollection {
	
	public void setMsoData(IMsoTool tool);	
	public void setMsoData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, IMsoManagerIf.MsoClassCode msoClassCode);	

}
