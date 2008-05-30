package org.redcross.sar.gui;

import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IChangeable {
	
	/* ================================================
	 * IWork interface
	 * ================================================ */
	
	public boolean isDirty();	
	public void setDirty(boolean isDirty);
	
	public boolean isChangeable();
	public void setChangeable(boolean isChangable);
	
	public IMsoObjectIf getMsoObject();
	public void setMsoObject(IMsoObjectIf msoObj);
	
	public void reset();
	public boolean finish();
	public boolean cancel();
	
	public void addDiskoWorkListener(IDiskoWorkListener listener);
	public void removeDiskoWorkListener(IDiskoWorkListener listener);
}
