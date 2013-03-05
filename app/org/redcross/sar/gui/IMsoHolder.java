package org.redcross.sar.gui;

import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IMsoHolder {

	public IMsoObjectIf getMsoObject();
	public void setMsoObject(IMsoObjectIf msoObj);
	
}
