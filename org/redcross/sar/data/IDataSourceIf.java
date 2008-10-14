package org.redcross.sar.data;

import java.util.Collection;

import org.redcross.sar.data.event.ISourceListenerIf;

public interface IDataSourceIf<I> {

	public void addSourceListener(ISourceListenerIf<I> listener);
	public void removeSourceListener(ISourceListenerIf<I> listener);

	public Collection<?> getItems(Class<?> c);

	public boolean isSupported(Class<?> dataClass);

}
