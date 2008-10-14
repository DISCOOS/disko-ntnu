package org.redcross.sar.data;

import java.util.Collection;

import javax.swing.event.ChangeListener;

public interface IDataModelIf<S,T extends IDataIf> {

	public Class<T> getDataClass();

	public boolean isSupported(Class<?> dataClass);

	public Collection<IDataBinderIf<S,? extends IDataIf,?>> getBinders();

	public IDataBinderIf<S,? extends IDataIf,?> getBinder(IDataSourceIf<?> source);

	public boolean connect(IDataBinderIf<S,? extends IDataIf,?> binder);
	public boolean disconnect(IDataBinderIf<S,? extends IDataIf,?> binder);
	public boolean disconnectAll();

	public void load();
	public void load(Collection<T> objects);
	public void addAll(Collection<T> objects);

	public void add(S id, T obj);
	public void update(S id, T obj);
	public void remove(S id);
	public void clear();

	public int findRowFromId(S id);
	public int findRowFromObject(T obj);

	public S getId(int row);
	public T getObject(int row);

	public Collection<S> getIds();
	public Collection<T> getObjects();

	public Object[] getData(int row);

	public int getRowCount();

	public Object getValueAt(int row, int col);
	public void setValueAt(Object value, int iRow, int iCol);

	public void addChangeListener(ChangeListener listener);
	public void removeChangeListener(ChangeListener listener);

}
