/**
 * 
 */
package org.redcross.sar.ds;

import java.util.HashMap;
//import java.util.logging.Logger;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import org.redcross.sar.map.DiskoMap;

/**
 * Singleton class with route cost parameters
 * 
 * @author kennetgu
 *
 */
public class RouteCostProps {

	// Logger for this object
	//private static final Logger LOGGER = Logger.getLogger(
	//		"org.redcross.sar.ds.RouteCostProps");
		
	private DiskoMap m_map;
	private ResourceBundle m_res;
	private HashMap<String,RouteCostProp> m_props;
	private HashMap<String,RouteCostFeatureMap> m_fmaps;

  	private static RouteCostProps m_this;	  	
  	
	/**
 	* Constructor
 	*/
  	private RouteCostProps() {
		// try to get resource bundle
		try
		{
			// get resource
			m_res = ResourceBundle.getBundle("org.redcross.sar.ds.properties.RouteCostParams");
			// populate
		}
		catch(MissingResourceException e)
		{
			System.err.println("Unable to load resource bundle RouteCostParams in RouteCostParams");
		}		
  	}

	/**
	 * Get singleton instance of class
	 * 
	 * @return Returns singleton instance of class
	 */
  	public static synchronized RouteCostProps getInstance()
  	{
  		if (m_this == null) {
  			// it's ok, we can call this constructor
  			m_this = new RouteCostProps();
  		}
  		// return reference
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
	 * Gets Route Cost property object
	 * 
	 */
  	public RouteCostProp getProp(String key)
  	{
  		// get and return
  		return m_props.get(key);
  	}  	
  	
	/**
	 * Gets Route Cost feature map object
	 * 
	 */
  	public RouteCostFeatureMap getMap(String key)
  	{
  		// get and return
  		return m_fmaps.get(key);
  	}  	
  	
  	/**
	 * Get resource bundle
	 * 
	 */
	public ResourceBundle getResource() 
  	{
  		return m_res;
  	}

  	/**
	 * Get map 
	 * 
	 */
	public DiskoMap getMap() 
  	{
  		return m_map;
  	}
	
	/**
	 * Get properties and load to memory
	 * 
	 */
  	@SuppressWarnings("unchecked")
	private void getProps() 
  	{

  		// create hash map
  		m_props = new HashMap<String,RouteCostProp>(11);
  		
  		// get propulsion
  		m_props.put("P", new RouteCostProp(m_res,"P"));
  		
  		// get Snow State Type
  		m_props.put("SST", new RouteCostProp(m_res,"SST"));
  		
  		// get New Snow Depth
  		m_props.put("NSD", new RouteCostProp(m_res,"NSD"));
  		
  		// get Snow State
  		m_props.put("SS", new RouteCostProp(m_res,"SS"));
  		
  		// get Surface
  		m_props.put("SU", new RouteCostProp(m_res,"SU"));
  		
  		// get Upward Slope
  		m_props.put("US", new RouteCostProp(m_res,"US"));
  		
  		// get Easy Downward Slope
  		m_props.put("EDS", new RouteCostProp(m_res,"EDS"));
  		
  		// get Steep Downward Slope
  		m_props.put("SDS", new RouteCostProp(m_res,"SDS"));
  		
  		// get Precipitation
  		m_props.put("PRE", new RouteCostProp(m_res,"PRE"));
  		
  		// get Wind
  		m_props.put("WIN", new RouteCostProp(m_res,"WIN"));
  		
  		// get Temperature
  		m_props.put("TEM", new RouteCostProp(m_res,"TEM"));
  		
  		// get Temperature
  		m_props.put("W", new RouteCostProp(m_res,"W"));
  		
  		// get Light
  		m_props.put("LI", new RouteCostProp(m_res,"LI"));
  		
		// create array lists
  		m_fmaps = new HashMap<String,RouteCostFeatureMap>();
		
		try {
			// get feature class count
			int icount = Integer.valueOf(m_res.getString("FEATURECLASS.COUNT")).intValue();
			// loop over all feature classes
			for(int i=0;i<icount;i++) {
				// get key
				String key = "FEATURECLASS." + i;
				// get featur class name
				m_fmaps.put(m_res.getString(key),new RouteCostFeatureMap(this,key));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}  		
  		
  	}  	

	/**
	 *  create the properties
	 *  
	 */		
	public void create(DiskoMap map) {

		// save map
		m_map = map;
		
		// update parameters
		getProps();
		
	}
}



	