package org.redcross.sar.gui;

import java.awt.event.ActionListener;

import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;

public interface IDiskoPanel extends IMsoUpdateListenerIf {
	
	public void update();
	
	public boolean isDirty();	
	public void setDirty(boolean isDirty);
	
	public boolean isChangeable();
	public void setChangeable(boolean isChangable);
	
	public IMsoObjectIf getMsoObject();
	public void setMsoObject(IMsoObjectIf msoObj);
	
	public void reset();
	public boolean finish();
	public boolean cancel();
	
	public void addActionListener(ActionListener listener);	
	public void removeActionListener(ActionListener listener);
	
	
	
}
