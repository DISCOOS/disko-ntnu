package org.redcross.sar.map.tool;

import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IMsoTool {

	public IMsoObjectIf getMsoObject();
	public void setMsoObject(IMsoObjectIf msoObj);
		
	public IMsoObjectIf getMsoOwner();
	public void setMsoOwner(IMsoObjectIf msoOwn);
	
	public MsoClassCode getMsoCode();

	public void setMsoData(IMsoTool tool);	
	public void setMsoData(IMsoObjectIf msoOwn, IMsoObjectIf msoObj, MsoClassCode msoCode);	
	
}
