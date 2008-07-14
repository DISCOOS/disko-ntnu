package org.redcross.sar.ds;

import org.redcross.sar.ds.event.IDsUpdateListener;
import org.redcross.sar.thread.IDiskoWork;

public interface IDsIf<T> extends IDiskoWork<T> {

	public String getOprID();
	
	public boolean addUpdateListener(IDsUpdateListener listener);
	public boolean removeUpdateListener(IDsUpdateListener listener);

}
