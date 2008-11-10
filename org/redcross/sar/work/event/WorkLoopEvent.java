/**
 *
 */
package org.redcross.sar.work.event;

import java.util.EventObject;

/**
 * @author kennetgu
 *
 */
public class WorkLoopEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public final static int STATE_EVENT = 0;
	public final static int WORK_EVENT = 1;

	private int type;
	private Object data;

	public WorkLoopEvent(Object source, Object data, int type) {
		super(source);
		this.data = data;
		this.type = type;
	}

	public Object getData() {
		return data;
	}

	public int getType() {
		return type;
	}

	public boolean isStateEvent() {
		return (type == STATE_EVENT);
	}

	public boolean isWorkEvent() {
		return (type == WORK_EVENT);
	}

}
