package org.redcross.sar.gui.event;

import java.util.EventListener;

import org.redcross.sar.gui.panel.ITogglePanel;

public interface IToggleListener extends EventListener {

	public void toggleChanged(ITogglePanel panel, boolean isExpanded, int dx, int dy);

}
