package org.redcross.sar.gui.panel;

public interface IPanelManager {

	public boolean requestShow();
	public boolean requestHide();

	public boolean requestMoveTo(int x, int y, boolean isRelative);
	public boolean requestResize(int w, int h, boolean isRelative);
	public boolean requestFitToContent();

}
