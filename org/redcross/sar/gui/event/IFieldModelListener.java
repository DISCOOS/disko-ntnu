package org.redcross.sar.gui.event;

import java.util.EventListener;

public interface IFieldModelListener extends EventListener {

	public void onFieldModelChanged(FieldModelEvent e);
	
}
