package org.redcross.sar.gui.event;

import java.util.EventObject;

import org.redcross.sar.gui.field.IFieldModel;

public class FieldModelEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public static final int EVENT_ORIGIN_CHANGED = 1;
	public static final int EVENT_STATE_CHANGED = 2;
	public static final int EVENT_VALUE_CHANGED = 4;
	
	private int m_flags;
	
	public FieldModelEvent(IFieldModel<?> source, int flags) {
		// forward
		super(source);
		// prepare
		m_flags = flags;
	}
	
	public IFieldModel<?> getSource() {
		return (IFieldModel<?>)super.getSource();
	}
	
	public int getFlags() {
		return m_flags;
	}
	
	public boolean isValueChanged() {
		return isType(EVENT_VALUE_CHANGED);
	}
	
	public boolean isOriginChanged() {
		return isType(EVENT_ORIGIN_CHANGED);
	}

	public boolean isStateChanged() {
		return isType(EVENT_STATE_CHANGED);
	}
	
	public boolean isType(int type) {
		return (m_flags & type) == type;
	}
	
	
}
