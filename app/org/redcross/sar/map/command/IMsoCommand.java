package org.redcross.sar.map.command;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IMsoCommand {

	public void setMsoData(IMsoCommand command);	
	public void setMsoData(IMsoObjectIf msoOwner, 
			IMsoObjectIf msoObject, IMsoManagerIf.MsoClassCode msoClassCode);
	
	public IMsoObjectIf getMsoObject();
	public void setMsoObject(IMsoObjectIf msoObject);
		
	public IMsoObjectIf getMsoOwner();	
	public void setMsoOwner(IMsoObjectIf msoOwner);
	
	public IMsoManagerIf.MsoClassCode getMsoClassCode();
	
}
