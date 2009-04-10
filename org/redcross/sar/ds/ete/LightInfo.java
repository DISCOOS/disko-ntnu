package org.redcross.sar.ds.ete;

import java.util.Calendar;
import java.util.TimeZone;


/**
 *  Singleton Light information class
 *  
 * @author kennetgu
 *
 */
public class LightInfo {

  	private static LightInfo m_this;
  	
  	/**
	 *  Constructor
	 */		
	public LightInfo() {}

	/**
	 * Get singleton instance of class
	 * 
	 * @return Returns singleton instance of class
	 */
  	public static synchronized LightInfo getInstance()
  	{
  		if (m_this == null)
  			// it's ok, we can call this constructor
  			m_this = new LightInfo();		
  		return m_this;
  	}

	/**
	 * Method overridden to protect singleton
	 * 
	 * @throws CloneNotSupportedException
	 * @return Returns nothing. Method overridden to protect singleton 
	 */
  	public Object clone() throws CloneNotSupportedException
  	{
  		throw new CloneNotSupportedException(); 
  		// that'll teach 'em
  	}
  	
  	
	/**
	 *  Get light at point
	 *  
	 *  @param t Time at position
	 *  @return Closest forcast to given time
	 */		
	public LightInfoPoint get(Calendar t, double lon, double lat) {

		// finished
		return new LightInfoPoint(t,lon,lat,getTimeZone(t));				
		
	}
	
	private static int getTimeZone(Calendar t) {
		return TimeZone.getDefault().getOffset(t.getTimeInMillis());		
	}
	
}