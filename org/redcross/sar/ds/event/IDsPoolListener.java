package org.redcross.sar.ds.event;

import java.util.EventListener;

public interface IDsPoolListener extends EventListener {

	public void handleInstallEvent(DsEvent.Install e);

}
