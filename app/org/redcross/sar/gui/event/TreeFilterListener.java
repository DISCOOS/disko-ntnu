package org.redcross.sar.gui.event;

import java.util.EventListener;

public interface TreeFilterListener extends EventListener {
	
	public void filterChanged(TreeFilterEvent e);
	
}
