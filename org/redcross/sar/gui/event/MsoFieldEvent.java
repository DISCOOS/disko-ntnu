package org.redcross.sar.gui.event;

import java.util.EventObject;

import org.redcross.sar.gui.field.IMsoField;

public class MsoFieldEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public static final int ATTRIBUTE_SET = 1; 
	public static final int ATTRIBUTE_RESET = 2; 
	
	private int m_type;
	
	public MsoFieldEvent(IMsoField source, int type) {
		// forward
		super(source);
		// prepare
		m_type = type;
	}
	
	public IMsoField getSource() {
		return (IMsoField)super.getSource();
	}
	
	public int getType() {
		return m_type;
	}
	
}
