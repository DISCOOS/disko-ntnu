package org.redcross.sar.map.event;

public class ToolEvent extends java.util.EventObject {

	private static final long serialVersionUID = 1L;

	public enum ToolEventType
    {
    	BEGIN_EVENT,
    	CHANGE_EVENT,
    	FINISH_EVENT,
    	CANCEL_EVENT,
    	FOCUS_EVENT
    }
	
    private int m_flags;
    private boolean isConsumed;
    private ToolEventType m_eventType;
    
    public ToolEvent(Object source, ToolEventType type, int flags)
    {
        super(source);
        m_flags = flags;
        m_eventType = type;
    }
    
    public int getFlags() {
    	return m_flags;
    }
    
    public ToolEventType getType() {
    	return m_eventType;
    }        
    
    public boolean isType(ToolEventType type) {
    	return m_eventType.equals(type);
    }        
    
    public void consume() {
    	isConsumed=true;
    }
    
    public boolean isConsumed() {
    	return isConsumed;
    }
    
}
