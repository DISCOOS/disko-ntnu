package org.redcross.sar.gui.event;

import java.util.EventListener;

public interface IFieldListener extends EventListener {
	public void onFieldChanged(FieldEvent e);
}
