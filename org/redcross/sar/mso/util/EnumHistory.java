/**
 * 
 */
package org.redcross.sar.mso.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author kennetgu
 *
 */
public class EnumHistory<T extends Enum<?>> 
{
	
    /**
     * List of status changes since creation
     */        
    private final List<Sample> m_history = new ArrayList<Sample>(1);

    /* ==================================================================
     * Public methods
     * ================================================================== */
	
    public boolean add(T value, Calendar t) 
    {
    	return m_history.add(new Sample(value,t));
    }
    
    public List<Calendar> getHistory(T value) {
    	List<Calendar> list = new ArrayList<Calendar>(m_history.size()/2);
    	for(Sample it : m_history) {
    		if(it.m_enum.equals(value))
    			list.add(it.m_t);
    	}
    	return list;
    }
    

    /* ==================================================================
     * Inner classes
     * ================================================================== */
    
    class Sample 
    {
    	
    	final T m_enum;
    	final Calendar m_t;
    	
    	Sample(T value, Calendar t) {
    		m_enum = value;
    		m_t = t;
    		
    	}
    }
    
}
