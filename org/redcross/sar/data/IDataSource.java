package org.redcross.sar.data;

import java.util.Collection;

import org.redcross.sar.data.event.ISourceListener;

/**
 * 
 * @author Administrator
 *
 * @param <I> - the class or interface that implements the source event information
 */

public interface IDataSource<I> {

	public Object getID();
	
	public void addSourceListener(ISourceListener<I> listener);
	public void removeSourceListener(ISourceListener<I> listener);

	public Collection<?> getItems(Class<?> c);
	public Collection<?> getItems(Enum<?> e);

	public boolean isSupported(Class<?> dataClass);
	
	public boolean isAvailable();

}
