package org.redcross.sar.data.event;

import java.util.EventListener;

public interface IBinderListener<S> extends EventListener {

	public void onDataCreated(BinderEvent<S> e);
	public void onDataChanged(BinderEvent<S> e);
	public void onDataDeleted(BinderEvent<S> e);
	public void onDataUnselected(BinderEvent<S> e);
	public void onDataClearAll(BinderEvent<S> e);
	public void onCoDataChanged(BinderEvent<S> e);
	public void onCoDataUnselected(BinderEvent<S> e);

}
