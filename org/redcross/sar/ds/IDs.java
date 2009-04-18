package org.redcross.sar.ds;

import java.util.List;

import org.redcross.sar.IService;
import org.redcross.sar.data.IDataSource;
import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.ds.event.IDsChangeListener;

public interface IDs<T extends IDsObject> extends IDataSource<DsEvent.Update>, IService {

	public Object getID();

	public Class<T> getDataClass();
	public boolean isSupported(Class<?> dataClass);

	public List<T> getItems();

	public void addChangeListener(IDsChangeListener listener);
	public void removeChangeListener(IDsChangeListener listener);

}
