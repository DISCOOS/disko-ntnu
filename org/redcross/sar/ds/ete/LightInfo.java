package org.redcross.sar.ds.ete;

import java.util.Calendar;


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
	public LightInfoPoint getForecast(Calendar t, double lon, double lat) {
		LightInfoPoint lp = null;
		Calendar now = Calendar.getInstance();
		
		// valid range?
		if (now.before(t)) {
		
			// TODO: Get light forecast at point						
			
			// create light point
			lp = new LightInfoPoint(t,lon,lat,0,false);
			
		}

		// return succes
		return lp;
		
	}
	
	/**
	 *  Get light history
	 *  
	 *  @param t0 Start history at time
	 *  @param hours Get history number of hours forward (+) or backwards (-)
	 *  
	 *  @return Get closest history to given time span
	 */		
	public LightInfoPoint getHistory(Calendar t, double lon, double lat, int hours) {
		LightInfoPoint lp = null;
		Calendar now = Calendar.getInstance();
		
		// valid range?
		if (now.after(t)) {
		
			// TODO: Get light history at point						
			
			// create light point
			lp = new LightInfoPoint(t,lon,lat,0,true);
			
		}

		// return succes
		return lp;
		
	}
	
}