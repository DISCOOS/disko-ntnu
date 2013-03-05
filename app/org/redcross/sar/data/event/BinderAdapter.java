package org.redcross.sar.data.event;

public class BinderAdapter<S> implements IBinderListener<S> {

	public void onDataCreated(BinderEvent<S> e) {}
	public void onDataChanged(BinderEvent<S> e) {}
	public void onDataDeleted(BinderEvent<S> e) {}
	public void onDataUnselected(BinderEvent<S> e) {}
	public void onDataClearAll(BinderEvent<S> e) {}

	public void onCoDataChanged(BinderEvent<S> e) {}
	public void onCoDataUnselected(BinderEvent<S> e) {}


}
