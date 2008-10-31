package org.redcross.sar.gui;

import org.redcross.sar.thread.event.IWorkListener;

public interface IChangeable {
	
	/* ================================================
	 * IWork interface
	 * ================================================ */
	
	public boolean isDirty();	
	public void setDirty(boolean isDirty);
	
	public boolean isChangeable();
	public void setChangeable(boolean isChangable);
	
	public void reset();
	public boolean finish();
	public boolean cancel();
	
	public void addWorkListener(IWorkListener listener);
	public void removeWorkListener(IWorkListener listener);
}
