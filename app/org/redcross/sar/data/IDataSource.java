package org.redcross.sar.data;

import java.util.Collection;

import org.redcross.sar.data.event.ISourceListener;

/**
 * 
 * @author Administrator
 *
 * @param <D> - the class or interface that implements the source event data (information)
 */

public interface IDataSource<D> {

	public Object getID();
	
	public void addSourceListener(ISourceListener<D> listener);
	public void removeSourceListener(ISourceListener<D> listener);

	public Collection<?> getItems(Class<?> c);
	public Collection<?> getItems(Enum<?> e);

	public boolean isSupported(Class<?> dataClass);
	
	public boolean isAvailable();

}
