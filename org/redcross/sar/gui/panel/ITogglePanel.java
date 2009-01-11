package org.redcross.sar.gui.panel;

import org.redcross.sar.gui.event.IToggleListener;

public interface ITogglePanel extends IPanel {

	public boolean toggle();

	public void expand();
	public void collapse();

	public boolean isExpanded();
	public void setExpanded(boolean isExpanded);

	public int getMinimumCollapsedHeight();

	public int getPreferredExpandedHeight();
	public void setPreferredExpandedHeight(int height);

	public void addToggleListener(IToggleListener listener);
	public void removeToggleListener(IToggleListener listener);

}
