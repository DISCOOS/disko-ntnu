/**
 *
 */
package org.redcross.sar.work.event;

import java.util.EventObject;

import org.redcross.sar.work.IWork;

/**
 * @author kennetgu
 *
 */
public class WorkEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public final static int STATE_EVENT = 0;
	public final static int PRIORITY_EVENT = 1;

	private int type;
	private Object data;

	public WorkEvent(IWork source, Object data, int type) {
		super(source);
		this.data = data;
		this.type = type;
	}

	@Override
	public IWork getSource() {
		return (IWork)super.getSource();
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

	public boolean isPriorityEvent() {
		return (type == PRIORITY_EVENT);
	}

}
