package org.redcross.sar.thread.event;

import java.util.ArrayList;
import java.util.List;

public class DiskoWorkRepeater implements IDiskoWorkListener {
	
	private final List<DiskoWorkEvent> stack = new ArrayList<DiskoWorkEvent>();	
	private final List<IDiskoWorkListener> listeners = new ArrayList<IDiskoWorkListener>();
	
	private boolean isRepeaterMode;
	
	public DiskoWorkRepeater() {
		isRepeaterMode = true;
	}
	
	public DiskoWorkRepeater(boolean isRepeaterMode) {
		this.isRepeaterMode = isRepeaterMode;
	}
	
	public boolean isRepeaterMode() {
		return isRepeaterMode;
	}
	
	public void setRepeaterMode(boolean isRepeaterMode) {
		this.isRepeaterMode = isRepeaterMode;
	}
	
	public void onWorkPerformed(DiskoWorkEvent e) {
		// repeat?
		if(isRepeaterMode) fireOnWorkPerformed(e);
	}
	
	public boolean fireWorkFinish(Object source) {
		return fireOnWorkPerformed(new DiskoWorkEvent(source,null,DiskoWorkEvent.EVENT_FINISH));
	}
	
	public boolean fireWorkCancel(Object source) {
		return fireOnWorkPerformed(new DiskoWorkEvent(source,null,DiskoWorkEvent.EVENT_CANCEL));
	}
	
	public boolean fireWorkCommit(Object source) {
		return fireOnWorkPerformed(new DiskoWorkEvent(source,null,DiskoWorkEvent.EVENT_COMMIT));
	}

	public boolean fireWorkRollback(Object source) {
		return fireOnWorkPerformed(new DiskoWorkEvent(source,null,DiskoWorkEvent.EVENT_ROLLBACK));
	}

	public boolean fireWorkChange(Object source, Object data) {
		return fireOnWorkPerformed(new DiskoWorkEvent(source,data,DiskoWorkEvent.EVENT_CHANGE));
	}
	
	public boolean fireWorkPerformed(Object source, Object data, int type) {
		return fireOnWorkPerformed(new DiskoWorkEvent(source,data,type));
	}
	
	public boolean fireOnWorkPerformed(DiskoWorkEvent e) {
		// allowed?
		if(!isReentry(e)) {
			stack.add(e);
			for(IDiskoWorkListener it : listeners) {
				it.onWorkPerformed(e);
			}
			stack.remove(e);
			return true;
		}
		return false;
	}
	
	public boolean isReentry(DiskoWorkEvent e) {
		return stack.contains(e);
	}
	
	public boolean addDiskoWorkListener(IDiskoWorkListener listener) {
		if(this!=listener) {
			if(!listeners.contains(listener)) {
				return listeners.add(listener);
			}
		}
		return false;			
	}
	
	public boolean removeDiskoWorkListener(IDiskoWorkListener listener) {
		if(listeners.contains(listener)) {
			return listeners.remove(listener);
		}
		return false;			
	}
	
}
