package org.redcross.sar.gui;

import org.redcross.sar.work.event.IFlowListener;

public interface IChangeable {

	/* ================================================
	 * IChangeable interface
	 * ================================================ */

	/**
	 * Check if changes exists.
	 * 
	 * @return Returns <code>true</code> if changes exists.
	 */
	public boolean isDirty();
	
	/**
	 * Force a given dirty state. 
	 * 
	 * @param isDirty - the new dirty state
	 */	
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
	 * Clear state to changeable by resetting the internal counter to zero. 
	 * 
	 * @return the internal counter value  
	 */
	public int clearChangeableCount();

	/**
	 * Reset to default values.  
	 */
	public void reset();
	
	/**
	 * Finish change flow. Changes should be collected and handled 
	 * before <code>isDirty()</code> is reset.
	 *  
	 * @return boolean
	 */
	public boolean finish();
	
	/**
	 * Cancel any changes to previous values.
	 * 
	 * @return boolean
	 */
	public boolean cancel();

	public void addFlowListener(IFlowListener listener);
	public void removeFlowListener(IFlowListener listener);
}
