package org.redcross.sar.gui.tree;

import org.redcross.sar.gui.event.TreeFilterListener;

public interface TreeFilter {
	
	public boolean isShown(Object obj);
	
	public boolean isEnabled();
	public void setEnabled(boolean isEnabled);

	public void addTreeFilterListener(TreeFilterListener listener);
	public void removeTreeFilterListener(TreeFilterListener listener); 
	
}
