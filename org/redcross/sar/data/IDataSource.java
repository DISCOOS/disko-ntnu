package org.redcross.sar.data;

import java.util.Collection;

import org.redcross.sar.data.event.ISourceListener;

public interface IDataSource<I> {

	public void addSourceListener(ISourceListener<I> listener);
	public void removeSourceListener(ISourceListener<I> listener);

	public Collection<?> getItems(Class<?> c);

	public boolean isSupported(Class<?> dataClass);

}
