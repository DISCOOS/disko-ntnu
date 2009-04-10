package org.redcross.sar.gui;

import org.redcross.sar.work.event.IWorkFlowListener;

public interface IChangeable {

	/* ================================================
	 * IWork interface
	 * ================================================ */

	public boolean isDirty();
	public void setDirty(boolean isDirty);

	public int isMarked();
	public void setMarked(int isMarked);

	public boolean isChangeable();
	public void setChangeable(boolean isChangable);

	public void reset();
	public boolean finish();
	public boolean cancel();

	public void addWorkFlowListener(IWorkFlowListener listener);
	public void removeWorkFlowListener(IWorkFlowListener listener);
}
