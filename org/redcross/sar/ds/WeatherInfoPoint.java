package org.redcross.sar.ds;

import java.util.Calendar;

import org.redcross.sar.util.mso.TimePos;


/**
 *  WeatherInfo data point class
 *  
 * @author kennetgu
 *
 */
public class WeatherInfoPoint {

	TimePos m_i;
	int m_pre;
	int m_win;
	int m_bea;
	int m_tem;
	int m_hPa;
	boolean m_history;
	
	/**
	 *  Constructor
	 *  
	 *  @param t Time at position
	 *  @param lon WeatherInfo at longetude
	 *  @param lat WeatherInfo at latetude
	 *  @param pre Preciptiation in mm/h 
	 *  @param win Wind in m/s
	 *  @param bea Bearing in 360 degrees
	 *  @param tem Temperature in degrees
	 *  @param tem Pressure in hecto pascal
	 */		
	public WeatherInfoPoint(Calendar t, double lon, double lat, int pre, int win, int bea, int tem, int hPa, boolean history){
		// save
		m_i = new TimePos(lon,lat,t);
		m_pre = pre;
		m_win = win;
		m_bea = bea;
		m_tem = tem;
		m_tem = hPa;
		m_history = history;
		
	}
}
