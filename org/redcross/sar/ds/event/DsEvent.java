package org.redcross.sar.ds.event;

import org.redcross.sar.ds.IDsObjectIf;

public class DsEvent extends java.util.EventObject {

    public enum EventType
    {
    	ADDED_EVENT,
    	MODIFIED_EVENT,
    	REMOVED_EVENT
    }
	
    private int m_flags;
    private EventType m_eventType;
    
    public DsEvent(Object source, EventType type, int flags)
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
    
    public static class Update extends DsEvent
    {
    	
		private static final long serialVersionUID = 1L;
		
		private final IDsObjectIf[] m_data;
    	
        public Update(Object source, EventType type, int flags, IDsObjectIf[] data)
        {
            super(source,type,flags);
            m_data = data;
        }
        
        public IDsObjectIf[] getData() {
        	return m_data;
        }
        
    }
    
	
}
