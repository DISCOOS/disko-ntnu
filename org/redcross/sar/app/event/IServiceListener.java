package org.redcross.sar.app.event;

import java.util.EventListener;

public interface IServiceListener extends EventListener {

	public void handleExecuteEvent(ServiceEvent.Execute e);

}
