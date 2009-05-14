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

	/**
	 * Get changeable state. This returns <code>true</code> as long as 
	 * the internal counter is greater than zero.
	 * 
	 * @return boolean
	 * @see setChangeable, resetChangeable
	 */	
	public boolean isChangeable();

	/**
	 * Set changeable state. This state remembers each time the 
	 * changeable state is set or reset using an internal counter. The 
	 * changeable state is only reset when this internal counter is zero.
	 * 
	 * @return boolean
	 */	
	public void setChangeable(boolean isChangable);

	/**
	 * Reset state to changeable by resetting the internal counter to zero. 
	 * 
	 * @return the internal counter value  
	 */
	public int resetChangeable();

	public void reset();
	public boolean finish();
	public boolean cancel();

	public void addWorkFlowListener(IWorkFlowListener listener);
	public void removeWorkFlowListener(IWorkFlowListener listener);
}
