package org.redcross.sar.data.event;

public class DataAdapter<S> implements IDataListenerIf<S> {

	public void onDataCreated(DataEvent<S> e) {}
	public void onDataChanged(DataEvent<S> e) {}
	public void onDataDeleted(DataEvent<S> e) {}
	public void onDataClearAll(DataEvent<S> e) {}
	public void onDataCoClassChanged(DataEvent<S> e) {}

}
