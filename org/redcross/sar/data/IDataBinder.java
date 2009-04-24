package org.redcross.sar.data;

import java.util.Collection;
import java.util.Comparator;

import org.redcross.sar.data.event.IBinderListener;

/**
 * 
 * @author Administrator
 *
 * @param <S> - the class or interface that implements the id object
 * @param <T> - the class or interface that implements the data object 
 * @param <I> - the class or interface that implements the source event information
 */
public interface IDataBinder<S,T extends IData,I> {

	public Class<T> getDataClass();

	public boolean connect(IDataSource<I> source);
	public boolean disconnect();

	public boolean isSupported(Class<?> dataClass);

	public boolean isCoClass(Class<?> c);
	public void addCoClass(Class<?> c,Selector<?> selector);
	public void removeCoClass(Class<?> c);

	public Selector<T> getSelector();
	public void setSelector(Selector<T> filter);

	public Comparator<T> getComparator();
	public void setComparator(Comparator<T> filter);

	public void addBinderListener(IBinderListener<S> listener);
	public void removeBinderListener(IBinderListener<S> listener);

	public IDataSource<?> getSource();
	public boolean load();
	public boolean load(Collection<T> objects);
	public boolean load(Collection<T> objects, boolean append);
	public boolean clear();

}
