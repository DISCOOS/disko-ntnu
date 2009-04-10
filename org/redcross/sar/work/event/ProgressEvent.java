/**
 *
 */
package org.redcross.sar.work.event;

import java.util.EventObject;

/**
 * @author kennetgu
 *
 */
public class ProgressEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public enum ProgressEventType {
		EVENT_START,
		EVENT_SHOW,
		EVENT_HIDE,
		EVENT_CHANGE,
		EVENT_CANCEL,
		EVENT_FINISH
	};

	private ProgressEventType m_type = ProgressEventType.EVENT_START;


	public ProgressEvent(Object source, ProgressEventType type) {
		super(source);
		m_type = type;
	}

	public ProgressEventType getType() {
		return m_type;
	}

	public boolean isType(ProgressEventType type) {
		return m_type.equals(type);
	}

}
