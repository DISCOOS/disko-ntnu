package org.redcross.sar.event;

import java.util.EventListener;

public interface ICatalogListener extends EventListener {

	public void handleCatalogEvent(CatalogEvent.Instance e);

}
