/**
 * 
 */
package org.redcross.sar.thread.event;

import java.util.EventObject;

/**
 * @author kennetgu
 *
 */
public class ProgressEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public enum DiskoProgressEventType {
		EVENT_START,
		EVENT_SHOW,
		EVENT_HIDE,
		EVENT_UPDATE,
		EVENT_CANCEL,
		EVENT_FINISH
	};
	
	private DiskoProgressEventType m_type = DiskoProgressEventType.EVENT_START;
	
	
	public ProgressEvent(Object arg0, DiskoProgressEventType type) {
		super(arg0);
		m_type = type;
	}
	
	public DiskoProgressEventType getType() {
		return m_type;
	}
	
	public boolean isCanceled() {
		return m_type == DiskoProgressEventType.EVENT_CANCEL; 
	}
	
}
