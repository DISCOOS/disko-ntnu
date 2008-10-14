package org.redcross.sar.data.event;

import java.util.EventListener;

public interface IDataListenerIf<S> extends EventListener {

	public void onDataCreated(DataEvent<S> e);
	public void onDataChanged(DataEvent<S> e);
	public void onDataDeleted(DataEvent<S> e);
	public void onDataClearAll(DataEvent<S> e);
	public void onDataCoClassChanged(DataEvent<S> e);

}
