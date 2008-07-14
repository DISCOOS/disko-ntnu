package org.redcross.sar.ds.event;

public class DsEvent extends java.util.EventObject {

    public DsEvent(Object source)
    {
        super(source);
    }
    
    public static class Update extends DsEvent
    {
    	
    	private final Object[] m_data;
    	
        public Update(Object source, Object[] data)
        {
            super(source);
            m_data = data;
        }
        
        public Object[] getData() {
        	return m_data;
        }
        
    }
    
	
}
