package org.redcross.sar.ds.event;

import java.util.EventListener;

public interface IDsChangeListener extends EventListener {

	public void handleUpdateEvent(DsEvent.Update e);

}
