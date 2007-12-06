package org.redcross.sar.ds;

import java.util.Calendar;

/**
 *  Singleton Weather information class
 *  
 * @author kennetgu
 *
 */
public class WeatherInfo {

  	private static WeatherInfo m_this;	
  	
  	/**
	 *  Constructor
	 */		
	public WeatherInfo() {}

	/**
	 * Get singleton instance of class
	 * 
	 * @return Returns singleton instance of class
	 */
  	public static synchronized WeatherInfo getInstance()
  	{
  		if (m_this == null)
  			// it's ok, we can call this constructor
  			m_this = new WeatherInfo();		
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
	 *  Get forecasted weather at point
	 *  
	 *  @param t Time at position
	 *  @return Closest forcast to given time 
	 */		
	public WeatherInfoPoint getForecast(Calendar t, double lon, double lat) {
		WeatherInfoPoint wp = null;
		Calendar now = Calendar.getInstance();
		
		// valid range?
		if (now.before(t)) {
		
			// TODO: Get forecast at point						
			
			// create weather point
			wp = new WeatherInfoPoint(t,lon,lat,0,0,0,0,0,false);
			
		}

		// return succes
		return wp;
		
	}
	
	/**
	 *  Get weather history
	 *  
	 *  @param t0 Start history at time
	 *  @param hours Get history number of hours forward (+) or backwards (-)
	 *  
	 *  @return Get closest history to given time span
	 */		
	public WeatherInfoPoint getHistory(Calendar t, double lon, double lat, int hours) {
		WeatherInfoPoint wp = null;
		Calendar now = Calendar.getInstance();
		
		// valid range?
		if (now.after(t)) {
		
			// TODO: Get weather history at point						
			
			// create weather point
			wp = new WeatherInfoPoint(t,lon,lat,0,0,0,0,0,true);
			
		}

		// return succes
		return wp;
		
	}
	
}