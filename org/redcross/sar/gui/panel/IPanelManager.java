package org.redcross.sar.gui.panel;

public interface IPanelManager {
	
	public boolean requestShow();
	public boolean requestHide();
	
	public boolean requestMoveTo(int dx, int dy);
	public boolean requestResize(int w, int h);
	
}
