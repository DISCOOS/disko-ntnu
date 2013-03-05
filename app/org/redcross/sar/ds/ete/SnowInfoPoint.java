package org.redcross.sar.ds.ete;

import java.util.Calendar;

import org.redcross.sar.util.mso.TimePos;

/**
 *  SnowInfo data point class
 *  
 * @author kennetgu
 *
 */
public class SnowInfoPoint {

/*	public enum SnowStateType {
		SST_NO,
		SST_LITTLE,
		SST_WET,
		SST_DRY
	}
	
	public enum NewSnowDepthType {
		NSD_0,
		NSD_LT_5,
		NSD_5_TO_10,
		NSD_10_TO_25,
		NSD_25_TO_50,
		NSD_50_TO_75,
		NSD_GT_75
		
	}*/
	
	TimePos m_i;
	int m_sst;
	int m_nsd;
	boolean m_history;

	
	/**
	 *  Constructor
	 *  
	 *  @param t Time at position
	 *  @param lon SnowInfo at longetude
	 *  @param lat SnowInfo at latetude
	 *  @param sst Snow state type
	 *  @param nsd new snow depth
	 */		
	public SnowInfoPoint(Calendar t, double lon, double lat, int sst, int nsd, boolean history){
		// save
		m_i = new TimePos(lon,lat,t);
		m_sst = sst;
		m_nsd = nsd;
		m_history = history;
		
	}
}
