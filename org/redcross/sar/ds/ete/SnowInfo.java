package org.redcross.sar.ds.ete;

import java.util.Calendar;


/**
 *  Singleton Snow information class
 *  
 * @author kennetgu
 *
 */
public class SnowInfo {

  	private static SnowInfo m_this;
  	
  	/**
	 *  Constructor
	 */		
	public SnowInfo() {}

	/**
	 * Get singleton instance of class
	 * 
	 * @return Returns singleton instance of class
	 */
  	public static synchronized SnowInfo getInstance()
  	{
  		if (m_this == null)
  			// it's ok, we can call this constructor
  			m_this = new SnowInfo();		
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
	 *  Get forecasted snow state at point
	 *  
	 *  @param t Time at position
	 *  @return Closest forcast to given time 
	 */		
	public SnowInfoPoint getForecast(Calendar t, double lon, double lat) {
		SnowInfoPoint sp = null;
		Calendar now = Calendar.getInstance();
		
		// valid range?
		if (now.before(t)) {
		
			// TODO: Get forecast at point						
			
			// create snow info point
			sp = new SnowInfoPoint(t,lon,lat,0,0,false);
			
		}

		// return succes
		return sp;
		
	}
	
	/**
	 *  Get snow history
	 *  
	 *  @param t0 Start history at time
	 *  @param hours Get history number of hours forward (+) or backwards (-)
	 *  
	 *  @return Get closest history to given time span
	 */		
	public SnowInfoPoint getHistory(Calendar t, double lon, double lat, int hours) {
		SnowInfoPoint sp = null;
		Calendar now = Calendar.getInstance();
		
		// valid range?
		if (now.after(t)) {
		
			// TODO: Get snow history at point						
			
			// create snow info point
			sp = new SnowInfoPoint(t,lon,lat,0,0,true);
			
		}

		// return succes
		return sp;
		
	}
	
}