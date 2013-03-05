package org.redcross.sar.gui.event;

import java.util.EventObject;

import org.redcross.sar.gui.field.IField;

public class FieldEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public static final int EVENT_MODEL_SET = 1; 
	public static final int EVENT_MODEL_RESET = 2; 
	public static final int EVENT_FIELD_CHANGED = 3; 
	
	private int m_type;
	
	public FieldEvent(IField<?> source, int type) {
		// forward
		super(source);
		// prepare
		m_type = type;
	}
	
	public IField<?> getSource() {
		return (IField<?>)super.getSource();
	}
	
	public int getType() {
		return m_type;
	}
	
}
