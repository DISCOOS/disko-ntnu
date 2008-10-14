package org.redcross.sar.data;

import java.util.Collection;
import java.util.Comparator;

import org.redcross.sar.data.event.IDataListenerIf;

public interface IDataBinderIf<S,T extends IDataIf,I> {

	public Class<T> getDataClass();

	public boolean connect(IDataSourceIf<I> source);
	public void disconnect();

	public boolean isSupported(Class<?> dataClass);

	public boolean isCoClass(Class<?> c);
	public void addCoClass(Class<?> c,Selector<?> selector);
	public void removeCoClass(Class<?> c);

	public Selector<T> getSelector();
	public void setSelector(Selector<T> filter);

	public Comparator<T> getComparator();
	public void setComparator(Comparator<T> filter);

	public void addDataListener(IDataListenerIf<S> listener);
	public void removeDataListener(IDataListenerIf<S> listener);

	public IDataSourceIf<?> getSource();
	public boolean load();
	public boolean load(Collection<T> objects);

}
