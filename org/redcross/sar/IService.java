package org.redcross.sar;

import org.redcross.sar.data.IDataSource;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.IWorkLoop.LoopState;

public interface IService {

	/**<b>Get service id</b></p> If service is data source bound, the data source id is used. */
	public Object getID();
	
	/** Get data source bound state. */
	public boolean isDataSourceBound();

	public boolean connect(IDataSource<?> source);
	public boolean disconnect();

	public boolean init();
	public boolean load();
	public boolean start();
	public boolean resume();
	public boolean suspend();
	public boolean stop();

	public LoopState getLoopState();
	public boolean isLoopState(LoopState state);

	public IWorkLoop getWorkLoop();


}
