package org.redcross.sar.gui.tree;

import javax.swing.event.EventListenerList;

import org.redcross.sar.gui.event.TreeFilterEvent;
import org.redcross.sar.gui.event.TreeFilterListener;

public abstract class AbstractTreeFilter implements TreeFilter {

	private final EventListenerList listeners = new EventListenerList();
	
	private boolean isEnabled = true;

	/* =================================================================
	 * TreeFilter implementation
	 * ================================================================= */
	
	public abstract boolean isShown(Object obj);

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		if(this.isEnabled != isEnabled) {
			this.isEnabled = isEnabled;
			fireFilterChanged(new TreeFilterEvent(this));
		}
	}
	
	public void addTreeFilterListener(TreeFilterListener listener) 
	{
		listeners.add(TreeFilterListener.class, listener);
	}

	public void removeTreeFilterListener(TreeFilterListener listener) 
	{
		listeners.remove(TreeFilterListener.class, listener);		
	}
	
	/* =================================================================
	 * Helper methods
	 * ================================================================= */
	
	protected void fireFilterChanged(TreeFilterEvent e) {
		TreeFilterListener[] list = listeners.getListeners(TreeFilterListener.class);
		for(int i=0;i<list.length;i++)
			list[i].filterChanged(e);
	}
	

}
