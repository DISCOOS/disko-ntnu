package org.redcross.sar.work.event;

import java.util.ArrayList;
import java.util.List;

public class FlowEventRepeater implements IFlowListener {
	
	private final List<FlowEvent> stack = new ArrayList<FlowEvent>();	
	private final List<IFlowListener> listeners = new ArrayList<IFlowListener>();
	
	private boolean isRepeaterMode;
	
	public FlowEventRepeater() {
		isRepeaterMode = true;
	}
	
	public FlowEventRepeater(boolean isRepeaterMode) {
		this.isRepeaterMode = isRepeaterMode;
	}
	
	public boolean isRepeaterMode() {
		return isRepeaterMode;
	}
	
	public void setRepeaterMode(boolean isRepeaterMode) {
		this.isRepeaterMode = isRepeaterMode;
	}
	
	public void onFlowPerformed(FlowEvent e) {
		// repeat?
		if(isRepeaterMode) fireOnFlowPerformed(e);
	}
	
	public boolean fireWorkFinish(Object source) {
		return fireOnFlowPerformed(new FlowEvent(source,null,FlowEvent.EVENT_FINISH));
	}
	
	public boolean fireWorkCancel(Object source) {
		return fireOnFlowPerformed(new FlowEvent(source,null,FlowEvent.EVENT_CANCEL));
	}
	
	public boolean fireWorkCommit(Object source) {
		return fireOnFlowPerformed(new FlowEvent(source,null,FlowEvent.EVENT_COMMIT));
	}

	public boolean fireWorkRollback(Object source) {
		return fireOnFlowPerformed(new FlowEvent(source,null,FlowEvent.EVENT_ROLLBACK));
	}

	public boolean fireWorkChange(Object source, Object data) {
		return fireOnFlowPerformed(new FlowEvent(source,data,FlowEvent.EVENT_CHANGE));
	}
	
	public boolean fireFlowPerformed(Object source, Object data, int type) {
		return fireOnFlowPerformed(new FlowEvent(source,data,type));
	}
	
	public boolean fireOnFlowPerformed(FlowEvent e) {
		// allowed?
		if(!isReentry(e)) {
			stack.add(e);
			for(IFlowListener it : listeners) {
				it.onFlowPerformed(e);
			}
			stack.remove(e);
			return true;
		}
		return false;
	}
	
	public boolean isReentry(FlowEvent e) {
		return stack.contains(e);
	}
	
	public boolean addFlowListener(IFlowListener listener) {
		if(this!=listener) {
			if(!listeners.contains(listener)) {
				return listeners.add(listener);
			}
		}
		return false;			
	}
	
	public boolean removeFlowListener(IFlowListener listener) {
		if(listeners.contains(listener)) {
			return listeners.remove(listener);
		}
		return false;			
	}
	
}
