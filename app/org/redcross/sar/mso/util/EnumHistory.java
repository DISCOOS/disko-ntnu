/**
 *
 */
package org.redcross.sar.mso.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

/**
 * @author kennetgu
 *
 */
public class EnumHistory<E extends Enum<E>>
{

	/**
	 * Initial enum value
	 */
	private Sample m_head;

    /**
     * List of status changes since creation
     */
    private final List<Sample> m_history = new ArrayList<Sample>(1);

    /* ==================================================================
     * Public methods
     * ================================================================== */

    public E getHeadValue() {
    	return m_head.m_enum;
    }

    public Calendar getHeadTime() {
    	return m_head.m_t;
    }

    /**
     * set time stamp for initial status
     */
    public void setHead(E value, Calendar t) {
    	if(m_head!=null) {
    		m_history.remove(m_head);
    	}
    	m_history.add(0,new Sample(value,t));
    }

    public boolean add(E value, Calendar t)
    {
    	return m_history.add(new Sample(value,t));
    }

    public List<Calendar> getHistory(E value) {
    	List<Calendar> list = new ArrayList<Calendar>(m_history.size()/2);
    	for(Sample it : m_history) {
    		if(it.m_enum.equals(value))
    			list.add(it.m_t);
    	}
    	return list;
    }

    public Calendar getFirstTime(E aStatus)
    {
    	List<Calendar> list = getHistory(aStatus);
    	return list.size()>0 ? list.get(0) : null;
    }


    public Calendar getLastTime(E aStatus)
    {
    	List<Calendar> list = getHistory(aStatus);
    	return list.size()>0 ? list.get(list.size()-1) : null;
    }

    /**
     * Get duration of given unit status. </p>
     *
     * @param aStatus - The status to get duration for
     * @param total - If <code>true</code> the sum of all durations for a given status
     * is returned, the duration of the last occurrence otherwise.
     *
     * @return Duration (second)
     */
	public double getDuration(EnumSet<E> aList, boolean total) {
		double t = 0.0;
		for(E it : aList) {
			t += getDuration(it, total);
		}
		return t;
	}

    /**
     * Get duration of given unit status. </p>
     *
     * @param aStatus - The status to get duration for
     * @param total - If <code>true</code> the sum of all durations for a given status
     * is returned, the duration of the last occurrence otherwise.
     *
     * @return Duration (second)
     */
	public double getDuration(E aStatus, boolean total) {

		// initialize
		long tic = 0;
		int size = m_history.size();

		if(total) {
	    	for(int i=0;i<size;i++) {
	    		// get start status
	    		Sample s0 = m_history.get(i);
	    		// found?
	    		if(s0.m_enum.equals(aStatus)) {
		    		// get end status time
		    		Calendar t1 = (i<size-1 ? m_history.get(i+1).m_t : Calendar.getInstance());
		    		// calculate duration
	    			tic += getDuration(s0.m_t,t1);
	    		}
	    	}
		}
		else {
			Calendar t0 = null;
			Calendar t1 = null;
	    	for(int i=0;i<size;i++) {
	    		// get start status
	    		Sample s0 = m_history.get(i);
	    		// found?
	    		if(s0.m_enum.equals(aStatus)) {
	    			// get start status time
	    			t0 = s0.m_t;
		    		// get end status time
		    		t1 = (i<size-1 ? m_history.get(i).m_t : Calendar.getInstance());
	    		}
	    	}
	    	// calculate
	    	tic = getDuration(t0, t1);
		}

		// finished
		return tic/1000;

	}

    /* ==================================================================
     * Helper methods
     * ================================================================== */

	private long getDuration(Calendar t0, Calendar t1) {
		if(t0!=null) {
			t1 = (t1==null ? Calendar.getInstance() : t1);
			return t1.getTimeInMillis() - t0.getTimeInMillis();
		}
		return 0;
	}


    /* ==================================================================
     * Inner classes
     * ================================================================== */

    class Sample
    {

    	final E m_enum;
    	final Calendar m_t;

    	Sample(E value, Calendar t) {
    		m_enum = value;
    		m_t = t;

    	}
    }

}
