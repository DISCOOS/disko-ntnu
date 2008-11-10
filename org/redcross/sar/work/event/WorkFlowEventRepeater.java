package org.redcross.sar.work.event;

import java.util.ArrayList;
import java.util.List;

public class WorkFlowEventRepeater implements IWorkFlowListener {
	
	private final List<WorkFlowEvent> stack = new ArrayList<WorkFlowEvent>();	
	private final List<IWorkFlowListener> listeners = new ArrayList<IWorkFlowListener>();
	
	private boolean isRepeaterMode;
	
	public WorkFlowEventRepeater() {
		isRepeaterMode = true;
	}
	
	public WorkFlowEventRepeater(boolean isRepeaterMode) {
		this.isRepeaterMode = isRepeaterMode;
	}
	
	public boolean isRepeaterMode() {
		return isRepeaterMode;
	}
	
	public void setRepeaterMode(boolean isRepeaterMode) {
		this.isRepeaterMode = isRepeaterMode;
	}
	
	public void onFlowPerformed(WorkFlowEvent e) {
		// repeat?
		if(isRepeaterMode) fireOnWorkPerformed(e);
	}
	
	public boolean fireWorkFinish(Object source) {
		return fireOnWorkPerformed(new WorkFlowEvent(source,null,WorkFlowEvent.EVENT_FINISH));
	}
	
	public boolean fireWorkCancel(Object source) {
		return fireOnWorkPerformed(new WorkFlowEvent(source,null,WorkFlowEvent.EVENT_CANCEL));
	}
	
	public boolean fireWorkCommit(Object source) {
		return fireOnWorkPerformed(new WorkFlowEvent(source,null,WorkFlowEvent.EVENT_COMMIT));
	}

	public boolean fireWorkRollback(Object source) {
		return fireOnWorkPerformed(new WorkFlowEvent(source,null,WorkFlowEvent.EVENT_ROLLBACK));
	}

	public boolean fireWorkChange(Object source, Object data) {
		return fireOnWorkPerformed(new WorkFlowEvent(source,data,WorkFlowEvent.EVENT_CHANGE));
	}
	
	public boolean fireWorkPerformed(Object source, Object data, int type) {
		return fireOnWorkPerformed(new WorkFlowEvent(source,data,type));
	}
	
	public boolean fireOnWorkPerformed(WorkFlowEvent e) {
		// allowed?
		if(!isReentry(e)) {
			stack.add(e);
			for(IWorkFlowListener it : listeners) {
				it.onFlowPerformed(e);
			}
			stack.remove(e);
			return true;
		}
		return false;
	}
	
	public boolean isReentry(WorkFlowEvent e) {
		return stack.contains(e);
	}
	
	public boolean addWorkFlowListener(IWorkFlowListener listener) {
		if(this!=listener) {
			if(!listeners.contains(listener)) {
				return listeners.add(listener);
			}
		}
		return false;			
	}
	
	public boolean removeWorkFlowListener(IWorkFlowListener listener) {
		if(listeners.contains(listener)) {
			return listeners.remove(listener);
		}
		return false;			
	}
	
}
