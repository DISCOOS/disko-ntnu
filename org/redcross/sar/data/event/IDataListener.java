package org.redcross.sar.data.event;

import java.util.EventListener;

public interface IDataListener extends EventListener {

	public void onDataChanged(DataEvent e);

}
