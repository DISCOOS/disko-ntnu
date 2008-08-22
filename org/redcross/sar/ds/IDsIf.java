package org.redcross.sar.ds;

import java.util.List;

import org.redcross.sar.ds.event.IDsUpdateListenerIf;
import org.redcross.sar.thread.IDiskoWork;

public interface IDsIf<T extends IDsObjectIf> extends IDiskoWork<Boolean> {

	public String getOprID();
	
	public boolean isWorking();
	public boolean isSuspended();
	
	public List<T> getItems();
	
	public boolean addUpdateListener(IDsUpdateListenerIf listener);
	public boolean removeUpdateListener(IDsUpdateListenerIf listener);

}
