package org.redcross.sar.gui.event;

import java.util.EventListener;

public interface IMsoFieldListener extends EventListener {
	public void onMsoFieldChanged(MsoFieldEvent e);
}
