package org.redcross.sar.ds.ete;

import java.util.Calendar;

import org.redcross.sar.util.mso.TimePos;

/**
 *  LightInfo data point class
 *  
 * @author kennetgu
 *
 */
public class LightInfoPoint {

	TimePos m_i;
	int m_li;
	boolean m_history;

	
	/**
	 *  Constructor
	 *  
	 *  @param t Time at position
	 *  @param lon LightInfo at longetude
	 *  @param lat LightInfo at latetude
	 *  @param li Light condition type
	 */		
	public LightInfoPoint(Calendar t, double lon, double lat, int li,boolean history){
		// save
		m_i = new TimePos(lon,lat,t);
		m_li = li;
		m_history = history;
		
	}
}
