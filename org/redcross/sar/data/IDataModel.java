package org.redcross.sar.data;

import java.util.Collection;
import java.util.Comparator;

import org.redcross.sar.data.event.IDataListener;

public interface IDataModel<S,T extends IData> {

	public Class<T> getDataClass();

	public boolean isSupported(Class<?> dataClass);

	public Collection<IDataBinder<S,? extends IData,?>> getBinders();

	public IDataBinder<S,? extends IData,?> getBinder(IDataSource<?> source);

	public boolean connect(IDataBinder<S,? extends IData,?> binder);
	public boolean disconnect(IDataBinder<S,? extends IData,?> binder);
	public boolean disconnectAll();

	public int getAddOnCoUpdate();
	public void setAddOnCoUpdate(int flag);

	public int getRemoveOnCoUpdate();
	public void setRemoveOnCoUpdate(int flag);

	public void load();
	public void load(Collection<T> objects);
	public void addAll(Collection<T> objects);

	public int add(S id, T obj);
	public int update(S id, T obj);
	public int remove(S id);
	public void clear();

	public int findRowFromId(S id);
	public int findRowFromObject(T obj);

	public S getId(int row);
	public T getObject(int row);

	public Collection<S> getIds();
	public Collection<S> getIds(Selector<S> selector, Comparator<S> comparator);

	public Collection<T> getObjects();
	public Collection<T> getObjects(Selector<T> selector, Comparator<T> comparator);

	public Object[] getData(int row);

	public int getRowCount();

	public Object getValueAt(int row, int col);
	public void setValueAt(Object value, int iRow, int iCol);

	public void addDataListener(IDataListener listener);
	public void removeDataListener(IDataListener listener);

	public ITranslator<S, T> getTranslator();
	public void setTranslator(ITranslator<S, T> translator);

}
