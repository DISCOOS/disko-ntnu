package org.redcross.sar.gui.panel;

public interface IPanelManager {

	public boolean isRootManager();

	public IPanelManager setParentManager(IPanelManager parent);
	public IPanelManager getParentManager();

	public boolean requestShow();
	public boolean requestHide();

	public boolean requestMoveTo(int x, int y, boolean isRelative);
	public boolean requestResize(int w, int h, boolean isRelative);

	public boolean requestFitToMinimumContentSize(boolean pack);
	public boolean requestFitToMaximumContentSize(boolean pack);
	public boolean requestFitToPreferredContentSize(boolean pack);

}
