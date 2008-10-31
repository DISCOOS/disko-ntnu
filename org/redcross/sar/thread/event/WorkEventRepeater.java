package org.redcross.sar.thread.event;

import java.util.ArrayList;
import java.util.List;

public class WorkEventRepeater implements IWorkListener {
	
	private final List<WorkEvent> stack = new ArrayList<WorkEvent>();	
	private final List<IWorkListener> listeners = new ArrayList<IWorkListener>();
	
	private boolean isRepeaterMode;
	
	public WorkEventRepeater() {
		isRepeaterMode = true;
	}
	
	public WorkEventRepeater(boolean isRepeaterMode) {
		this.isRepeaterMode = isRepeaterMode;
	}
	
	public boolean isRepeaterMode() {
		return isRepeaterMode;
	}
	
	public void setRepeaterMode(boolean isRepeaterMode) {
		this.isRepeaterMode = isRepeaterMode;
	}
	
	public void onWorkPerformed(WorkEvent e) {
		// repeat?
		if(isRepeaterMode) fireOnWorkPerformed(e);
	}
	
	public boolean fireWorkFinish(Object source) {
		return fireOnWorkPerformed(new WorkEvent(source,null,WorkEvent.EVENT_FINISH));
	}
	
	public boolean fireWorkCancel(Object source) {
		return fireOnWorkPerformed(new WorkEvent(source,null,WorkEvent.EVENT_CANCEL));
	}
	
	public boolean fireWorkCommit(Object source) {
		return fireOnWorkPerformed(new WorkEvent(source,null,WorkEvent.EVENT_COMMIT));
	}

	public boolean fireWorkRollback(Object source) {
		return fireOnWorkPerformed(new WorkEvent(source,null,WorkEvent.EVENT_ROLLBACK));
	}

	public boolean fireWorkChange(Object source, Object data) {
		return fireOnWorkPerformed(new WorkEvent(source,data,WorkEvent.EVENT_CHANGE));
	}
	
	public boolean fireWorkPerformed(Object source, Object data, int type) {
		return fireOnWorkPerformed(new WorkEvent(source,data,type));
	}
	
	public boolean fireOnWorkPerformed(WorkEvent e) {
		// allowed?
		if(!isReentry(e)) {
			stack.add(e);
			for(IWorkListener it : listeners) {
				it.onWorkPerformed(e);
			}
			stack.remove(e);
			return true;
		}
		return false;
	}
	
	public boolean isReentry(WorkEvent e) {
		return stack.contains(e);
	}
	
	public boolean addWorkListener(IWorkListener listener) {
		if(this!=listener) {
			if(!listeners.contains(listener)) {
				return listeners.add(listener);
			}
		}
		return false;			
	}
	
	public boolean removeWorkListener(IWorkListener listener) {
		if(listeners.contains(listener)) {
			return listeners.remove(listener);
		}
		return false;			
	}
	
}
