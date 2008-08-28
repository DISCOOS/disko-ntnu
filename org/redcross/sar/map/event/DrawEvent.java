package org.redcross.sar.map.event;

public class DrawEvent extends java.util.EventObject {

	private static final long serialVersionUID = 1L;

	public enum EventType
    {
    	BEGIN_EVENT,
    	CHANGE_EVENT,
    	FINISH_EVENT,
    	CANCEL_EVENT
    }
	
    private int m_flags;
    private EventType m_eventType;
    
    public DrawEvent(Object source, EventType type, int flags)
    {
        super(source);
        m_flags = flags;
        m_eventType = type;
    }
    
    public int getFlags() {
    	return m_flags;
    }
    
    public EventType getType() {
    	return m_eventType;
    }        
	
}
