package org.redcross.sar.ds;

import java.util.List;

import org.redcross.sar.data.IDataSource;
import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.ds.event.IDsChangeListener;
import org.redcross.sar.thread.IWorkLoop;
import org.redcross.sar.thread.IWorkLoop.LoopState;

public interface IDs<T extends IDsObject> extends IDataSource<DsEvent.Update> {

	public String getOprID();

	public Class<T> getDataClass();
	public boolean isSupported(Class<?> dataClass);

	public boolean start();
	public boolean resume();
	public boolean suspend();
	public boolean stop();

	public LoopState getLoopState();
	public boolean isLoopState(LoopState state);

	public IWorkLoop getWorkLoop();

	public List<T> getItems();

	public void addChangeListener(IDsChangeListener listener);
	public void removeChangeListener(IDsChangeListener listener);

}
