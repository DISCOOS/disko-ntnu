package org.redcross.sar.ds.ete;

import java.lang.Math;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import java.awt.geom.Point2D;

import org.redcross.sar.util.mso.Route;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.util.mso.Track;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;

import com.esri.arcgis.system.IArray;
import com.esri.arcgis.carto.IRasterLayer;
import com.esri.arcgis.carto.IIdentify;
import com.esri.arcgis.carto.IRowIdentifyObjectProxy;
import com.esri.arcgis.geodatabase.IFeatureProxy;
import com.esri.arcgis.datasourcesraster.Raster;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.esriSegmentExtension;


/**
 *  Calculates route time cost along a spesified route
 *  
 * @author kennetgu
 *
 */
public class RouteCost {

    public enum SurfaceType
    {
        DEFAULT,
    	PATH,
        OPEN,
        SCATTERED,
        DENSE,
        VERY_DENSE,
        INFINITE
    }
    
	private RouteCostProps m_params = null;

    private double m_propulsion = 0;
	private ArrayList<Double> m_distances = null;
	private ArrayList<Double> m_slopes = null;
	private ArrayList<Double> m_terrainUnitCosts = null;
	private ArrayList<Double> m_weatherUnitCosts = null;
	private ArrayList<Double> m_lightUnitCosts = null;
	private ArrayList<Double> m_cumCost = null;
	private ArrayList<Double> m_cumDistance = null;
	private ArrayList<Integer> m_themes = null;
	private ArrayList<GeoPos> m_positions = null;
	private boolean m_isShifted = false;
	private boolean m_isReplaced = false;
	private boolean m_isArchived = false;
	private boolean m_isSuspended = true;
    
	private Route m_route = null;
	private SnowInfo m_info_si = null;
	private LightInfo m_info_li = null;
	private WeatherInfo m_info_wi = null;
	
	private Polyline m_polyline = null;
	
	private IDiskoMap m_map = null;
	private IEnvelope m_extent = null;
	private IEnvelope m_search = null;
	private ISpatialReference m_srs = null;
	private Raster m_altitude = null;
	private IIdentify m_roads = null;
	private IIdentify m_surface = null;
	private int m_roadsIndex = 0;
	private int m_surfaceIndex = 0;
	
	// start position
	private double m_h1 = 0;
	private int m_startIndex = 0;
	private Calendar m_startTime = null;
	
	// properties
	private RouteCostProp m_p;		// propulsion type
	private RouteCostProp m_sst;	// snow state type
	private RouteCostProp m_nsd;	// new snow depth type
	private RouteCostProp m_ss;		// snow state
	private RouteCostProp m_su;		// surface type
	private RouteCostProp m_us;		// upward slope 
	private RouteCostProp m_eds;	// easy downward slope
	private RouteCostProp m_sds;	// steep downward slope
	private RouteCostProp m_pre;	// preciptiation type
	private RouteCostProp m_win;	// wind type
	private RouteCostProp m_tem;	// temperature type
	private RouteCostProp m_w;		// weather
	private RouteCostProp m_li;		// light 
	
	// feature maps
	private RouteCostFeatureMap m_slopeMap;
	private RouteCostFeatureMap m_resistanceMap;
	
	// arguments
	private int[] m_arg_p = {0,0}; // {propulsion}
	private int[] m_arg_ps = {0,0}; // {propulsion,snow state} 
	
	/**
	 *  Constructor
	 *  
	 *  @param route The Route to calculate time cost for
	 */		
	public RouteCost(Route route, int propulsion, IDiskoMap map) {
		
		// save by reference
		m_map = map;
		
		// create objects
		m_info_wi = new WeatherInfo();
		m_info_si = new SnowInfo();
		m_info_li = new LightInfo();
		
		// create parameters
		m_params = RouteCostProps.getInstance();
		m_params.create(m_map);
		
		// get properties
		m_p = m_params.getProp("P");
		m_sst = m_params.getProp("P");
		m_nsd = m_params.getProp("NSD");
		m_ss = m_params.getProp("SS");
		m_su = m_params.getProp("SU");
		m_us = m_params.getProp("US");
		m_eds = m_params.getProp("EDS");
		m_sds = m_params.getProp("SDS");
		m_pre = m_params.getProp("PRE");
		m_win = m_params.getProp("WIN");
		m_tem = m_params.getProp("TEM");
		m_w = m_params.getProp("W");
		m_li = m_params.getProp("LI");
		
		try {
  			
			// get map spatial reference 
  			m_srs = map.getSpatialReference();
			// get feature maps
  			m_resistanceMap = m_params.getMap("RESISTANCE");
  			m_slopeMap = m_params.getMap("SLOPE");
			// create altitude information
			m_altitude = (Raster)((IRasterLayer)m_slopeMap.getLayer("ALTITUDE")).getRaster();
			// get surface information
			m_roads = (IIdentify)m_resistanceMap.getLayer("ROADS");
			m_surface = (IIdentify)m_resistanceMap.getLayer("SURFACE");
			m_roadsIndex = m_resistanceMap.getFieldIndex("ROADS");
			m_surfaceIndex = m_resistanceMap.getFieldIndex("SURFACE");
			// create search envelope
			m_search = new Envelope();
			m_search.putCoords(0, 0, 0, 0);
			m_search.setHeight(10);
			m_search.setWidth(10);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// save members
		setPropulsion(propulsion);
		
		// forward
		setRoute(route);
		
	}

	/**
	 *  Gets the spesified route
	 *  
	 *  @return The route to estimate time cost for
	 */		
	public Route getRoute() {
		// return specified route
		return m_route;
	}

	/**
	 *  Sets the spesified route
	 *  
	 *  @param The route to estimate time cost for
	 */		
	public void setRoute(Route route) {
		
		// save route
		m_route = route;
		
		// set dirty flag
		m_isReplaced = true;
					
		// get data
		getRouteData();
		
	}

	private void getRouteData() {
		
		// allowed?
		if(m_isReplaced) {
		
			try {
				// get polyline
				m_polyline = MapUtil.getEsriPolyline(m_route,m_map.getSpatialReference());	
				// get positions as array list
				m_positions = new ArrayList<GeoPos>(m_route.getItems());
				/* ========================================================================
				 * Segment length (length between two points on route) must be set to a 
				 * minimum value that ensures each point is inside a different height
				 * (altitude) cell. If a AxA meter resolution (x,y) raster grid is used 
				 * to represent the height at any point (x,y), then the minimum length
				 * becomes A*<square root of 2>+B, where B is an sensitivity factor. If B=0, 
				 * then in theory, two point may lay on the diagonal outer points on the
				 * same AxA cell.
				 * 
				 * A is assumed to be 25 meter, and B is heuristically set to 5 meter.
				 * 
				 */
				// get minimum segment length
				double min = 25*Math.sqrt(2)+5;	
				// densify the polyline
				densify(min);
			}
			catch (Exception e) {
				e.printStackTrace();
			}		
			// get least bounding square around route
			m_extent = getLeastRectangle();			
			
		}		
		
	}
	
	/**
	 *  Gets the specified propulsion method along the route
	 *  
	 *  @return Returns propulsion method along the route
	 */		
	public double getPropulsion() {
		return m_propulsion;
	}

	/**
	 *  Sets the specified propulsion method along the route
	 *  
	 *  @param The propulsion method along the route
	 *  @return Returns the propulsion index
	 */		
	public int setPropulsion(double propulsion) {
		// limit propulsion and save
		m_propulsion = m_p.limitVariable(propulsion);
		// update arguments
		m_arg_p[0] = m_p.getIndex(m_propulsion);
		m_arg_ps[0] = m_arg_p[0];
		// force deep estimation
		m_isReplaced = true;
		// return index
		return m_arg_p[0];
	}
	
	/**
	 *  Gets the route length
	 *  
	 *  @return Returns length of route
	 */		
	public double getDistance() {
		// return great circle distance?
		if (!m_isReplaced)
			return m_cumDistance.get(m_positions.size() - 2);
		else
			return 0;
	}	
	
	/**
	 *  Gets the route length at position index
	 *  
	 *  @return Returns route length at position index
	 */		
	public double getDistance(int index) {
		// return great circle distance?
		if (!m_isReplaced)
			if (index == 0)
				return 0;
			else
				return m_cumDistance.get(index - 1);
		else
			return 0;
	}	
	
	/**
	 * Shift start time and start position
	 * @param Calendar t0 - Start time
	 * @param int offset - Start index (position)
	 */
	public void shift(Calendar t0, int offset) {
		m_startTime = t0;
		m_startIndex = offset;
		m_isShifted = true;		
	}
	
	/**
	 * Get start time
	 */
	public Calendar getStartTime() {
		return m_startTime;
	}
	
	/**
	 * Set start time
	 */
	public void setStartTime(Calendar t0) {
		if(!equal(m_startTime,t0)) {
			m_startTime = t0;
			m_isShifted = true;
			Date time = t0.getTime();
		}
	}
	
    private boolean equal(Object o1, Object o2)
    {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }
	
	
	/**
	 * Get start position
	 */
	public int getStartPosition() {
		return m_startIndex;
	}
	
	/**
	 * Set position shift (start position) 
	 */
	public void setStartPosition(int index) {
		if(m_startIndex != index) {
			m_startIndex = index;
			m_isShifted = true;
		}
	}
	
	/**
	 *  Calculated estimated time enroute for a specified route.
	 *  
	 *  @return Estimated time enroute for a specified route
	 */		
	public int estimate() throws Exception{		
		return estimateRouteCost(m_startIndex,m_startTime);			
	}
	
	/**
	 *  Calculated estimated time enroute for a specified route.
	 *  
	 *  @param offset Start estimation from position index
	 *  @return Estimated time enroute for a specified route
	 */		
	public int estimate(int offset, Calendar t0) throws Exception{
		return estimateRouteCost(offset,t0);			
	}
	
	/**
	 *  Calculates estimated time enroute from a given position.
	 *  
	 *  @param GeoPos pos - Calculation is started from a given position
	 *  @param double max - Maximum starting distance from route
	 *  @param Calendar t0 - Start time
	 *  @return Estimated time enroute (ETE) from a given position and point in time
	 */		
	public int estimate(GeoPos pos, double max, Calendar t0) throws Exception {
		return estimateRouteCost(findNearest(pos,max),t0);
	}

	/**
	 *  Calculates estimated time enroute to a given position
	 *  
	 *  @param index Enroute position
	 *  @return Estimated time enroute to a given position
	 */		
	public int ete() {
		// return great circle distance?
		if (!m_isReplaced)
			return m_cumCost.get(m_cumCost.size() - 1).intValue();
		else
			return 0;
	}	
	
	/**
	 *  Calculates estimated time enroute to a given position
	 *  
	 *  @param index Enroute position
	 *  @return Estimated time enroute to a given position
	 */		
	public int ete(int index) {
		// return great circle distance?
		if (!m_isReplaced)
			if (index == 0)
				return 0;
			else
				return m_cumCost.get(index - 1).intValue();
		else
			return 0;
	}
	
	/**
	 *  Calculates estimated time of arrival.
	 *  
	 *  @return Estimated time of arrival at last position
	 */		
	public Calendar eta() {
		if (!m_isReplaced) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(m_startTime.getTimeInMillis()+ete()*1000);
			return c;
		}
		else
			return null;
	}
		
	/**
	 *  Calculates estimated time of arrival at position.
	 *  
	 *  @param index Position index
	 *  @return Estimated time of arrival at position
	 */		
	public Calendar eta(int index) {
		if (!m_isReplaced) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(m_startTime.getTimeInMillis()+ete(index)*1000);
			return c;
		}
		else
			return null;
	}
	
	/**
	 *  Get the nearest position in route
	 *  
	 *  @param GeoPos match - position to match
	 *  @param double max - maximum distance in meters
	 *  
	 *  @return Returns the index of the closest position in route. If the shortest distance
	 *  found is longer then max, <code>-1</code> is returned.
	 */		
	public int findNearest(GeoPos match, double max) throws Exception {
		
		// initialize variables
		int found = -1; 
		double min = max+1;
		double x = match.getPosition().x;
		double y = match.getPosition().y;
		List<GeoPos> c = new ArrayList<GeoPos>(m_route.getItems());
				
		// search for shortest distance
		for(int i=0;i<m_route.getItems().size();i++) {
			GeoPos it = c.get(i);
			double d = MapUtil.greatCircleDistance(x, y, it.getPosition().x, it.getPosition().y);
			if(d<min) {
				min = d;
				found = i;
			}						
		}
		
		// finished
		return found;
	}
	
	/*
	public int findNearest(GeoPos match) throws Exception {
		
		// initialize variables
		GeoPos gp = null;
		
		// create position
		IPoint p = new Point(); 			
		p.setX(match.getPosition().x);
		p.setY(match.getPosition().y);
		p.setSpatialReferenceByRef(m_map.getSpatialReference());	
		
		// find closest
		p = m_polyline.returnNearestPoint(p, esriSegmentExtension.esriNoExtension);
		
		// found?
		if(p!=null && !p.isEmpty()) {
			
			// get geo position
			gp = new GeoPos(p.getX(),p.getY());
	
			// get iterator
			ArrayList<GeoPos> it = new ArrayList<GeoPos>(m_route.getItems());
			
			// get count
			int count = it.size();
			
			// search for object 
			for(int i = 0; i < count; i++){
				// found?
				if (it.get(i).equals(gp))
					return i;
			}
		}
		// return not found
		return -1;
	}
	*/
	
	/**
	 * If true, estimate is not up to date
	 */
	public boolean isDirty() {
		return m_isShifted || m_isReplaced;
	}
	
	/**
	 * If true, offset or start time is changed
	 */
	public boolean isShifted() {
		return m_isShifted;
	}
	
	/**
	 * If true, full estimation will performed
	 */
	public boolean isReplaced() {
		return m_isReplaced;
	}
	
	/**
	 * If true, estimation is finished
	 */
	public boolean isArchived() {
		return m_isArchived;
	}
	
	/**
	 * Archive result
	 */
	public void archive(Track track) {
		// TODO: Calculate statistics based on track 
		m_isArchived = true;
	}
		
	/**
	 * If true, estimation is suspended
	 */
	public boolean isSuspended() {
		return m_isSuspended;
	}
	
	/**
	 * sets isSuspended() true. Only possible if not archived
	 */
	public void suspend() {
		m_isSuspended = !m_isArchived && true;
	}

	/**
	 * sets isSuspended() false
	 */
	public void resume() {
		m_isSuspended = false;
	}
		
	/**
	 *  Get the position count in the internal route
	 *  
	 *  @return Returns the position count of the densified route
	 */		
	private int getCount() {
		// get count
		return m_positions!=null ? m_positions.size() : 0;
	}	
	
	/**
	 *  Get the least bounding rectangle around route
	 *  
	 *  @return Returns the least bounding rectangle around route
	 */		
	private Envelope getLeastRectangle() {
		// initialize
		Envelope extent = null;			
		try {
			// get extent
			extent = (Envelope)m_polyline.getEnvelope();			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// return extent		
		return extent;
	}
	
	/**
	 *  Densify route
	 *  
	 *  @param int min Minimum distance between two points
	 */		
	private void densify(double min) {
		try {
			// initialize
			double d = 0;
			double sum = 0;
			GeoPos p = null;
			int count = m_polyline.getPointCount();
			// has points?
			if(count > 0) {				
				// get new polyline
				Polyline newPolyline = new Polyline();				
				newPolyline.setSpatialReferenceByRef(m_polyline.getSpatialReference());
				// get new position array
				ArrayList<GeoPos> newPositions = new ArrayList<GeoPos>();
				// get first position
				p = m_positions.get(0);
				// get first position
				Point2D.Double p1 = p.getPosition();
				// add first point to densified polyline
				newPolyline.addPoint(m_polyline.getPoint(0), null, null);
				// add first position to densified positions array
				newPositions.add(p);		
				// create arrays
				m_distances = new ArrayList<Double>(count - 2);
				m_cumDistance = new ArrayList<Double>(count - 2);
				// loop over all
				for(int i=1;i<count;i++) {
					// get next point
					Point2D.Double p2 = m_positions.get(i).getPosition();
					// calculate distance
					d += MapUtil.greatCircleDistance(p1.y, p1.x, p2.y, p2.x);
					// large or equal minimum length?
					if(d > min || i == count-1) {						
						// add segment distance 
						m_distances.add(d);						
						// add to cumulative distance
						sum += d;						
						// save cumulative cost
						m_cumDistance.add(sum);						
						// add to densified polyline
						newPolyline.addPoint(m_polyline.getPoint(i), null, null);						
						// add to densified positions array
						newPositions.add(p);								
						// reset distanse
						d = 0;						
					} 
					// save current point
					p1 = p2;
				}
				// replace
				m_polyline = newPolyline;
				m_positions = newPositions;
				// update route
				m_route = MapUtil.getMsoRoute(newPolyline);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return;
	}

	/**
	 *  Get slope between two positions.
	 *  
	 *  If returned slope is positive: uphill.
	 *  If returned slope is negative: downhill.
	 *  
	 * 	@param i Stop position 
	 * 	@param d horizontal distance from start position
	 *  @return Returns slope between two positions
	 */		
	private double getSlope(int i, double d) {
		// initialize
		double h2 = 0;
		double s = 0;
		int[] col = {0};
		int[] row = {0};
		try {
			
			// update current
			IPoint p = m_polyline.getPoint(i);
			// get pixel cell for heigth 2
			m_altitude.mapToPixel(p.getX(), p.getY(), col, row);
			// get height 2
			h2 = Double.valueOf(m_altitude.getPixelValue(0, col[0], row[0]).toString());
			// get height 1?
			if (i == 1) {
				// update current
				p = m_polyline.getPoint(i);
				// get pixel cell for heigth 1
				m_altitude.mapToPixel(p.getX(), p.getY(), col, row);
				// get height 1
				m_h1 = Double.valueOf(m_altitude.getPixelValue(0, col[0], row[0]).toString());
			}				
			// Calculate slope?
			if (i > 0 && d > 0) {
				// calculate hight diffence
				double h = m_h1 - h2;
				// calculate
				s = Math.signum(h)*Math.toDegrees(Math.atan(Math.abs(h/d)));
			}
			// save current height
			m_h1 = h2;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// set to - from*/
		return s;
	}
	
	/**
	 *  Get terrain unit cost
	 *  
	 *  @param s Slope angle between position pair (pi,i)
	 *  @param t Time at current position
	 *  @param i Current position
	 *  @return Returns terrain unit cost for position pair (pi,i)
	 */		
	private double getTerrainCost(double s, Calendar t, int i) {
		int ss = 0;				// snow state
		int rt = 0;				// resistance state type
		double cost = 0;
		
		// get types indexes
		ss = getSnowState(t,i-1);				
		rt = getResistanceType(i);		
		
		// get unit costs
		cost = getSurfaceCost(ss,rt);		
		cost += getSlopeCost(ss,s);		
		
		return cost;
	}

	/**
	 *  Get weather unit cost at position i
	 *  
	 *  @param t Time at position
	 *  @param i Current position  
	 *  @return Returns weather unit cost at position i
	 */		
	private double getWeatherCost(Calendar t, int i) {
		// get weather type
		int wt = getWeatherType(t,i);		
		// return lookup value
		return m_w.getValue(wt, m_arg_p, false);
	}
	
	/**
	 *  Get weather type
	 *  
	 *  @param pre Precipitation type
	 *  @param win Wind type
	 *  @param tem Temperature type
	 *  @return Returns weather type
	 */		
	private int getWeatherType(Calendar t, int i) {
		// initialize
		double wt = 0;
		try {
			// get point
			Point2D.Double p = m_positions.get(i).getPosition();
			// get forecasted weather at position
			WeatherInfoPoint wf = m_info_wi.getForecast(t,p.x,p.y);
			// found?
			if(wf != null) {
				// get weather type
				wt = m_pre.getWeightedVariable(wf.m_pre) 
						  	+ m_win.getWeightedVariable(wf.m_win) 
			  				+ m_tem.getWeightedVariable(wf.m_tem); 
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// return weather type
		return (int)wt;			
	}
	
	/**
	 *  Get light unit cost at position i
	 *  
	 *  @param t Time at position
	 *  @param i Current position  
	 *  @return Returns light unit cost at position i
	 */		
	private double getLightCost(Calendar t, int i) {
		// get day or night at position and time
		int lt = getLightType(t,i);				
		// return lookup value
		return m_li.getValue(lt, m_arg_p, false);
	}

	/**
	 *  Get surface unit cost
	 *  
	 *  @param ss Snow state
	 *  @param rt Resistance type
	 *  @return Returns surface unit cost
	 */		
	private double getSurfaceCost(int ss, int su) {
		// update snow state
		m_arg_ps[1] = ss;
		// return lookup value
		return m_su.getValue(su, m_arg_ps, false);
	}

	/**
	 *  Get slope unit cost
	 *  
	 *  @param ss Snow State at previous position pi
	 *  @param s Slope angle between position pair (pi,i)
	 *  @param pi Previous position
	 *  @param i Current position
	 *  @return Returns surface unit cost between position pair (pi,i)
	 */		
	private double getSlopeCost(int ss, double s) {
		
		// update snow state
		m_arg_ps[1] = ss;

		if(s>=0) {
			// limit variable and return slope type index
			int st = (int)m_us.getIndex(s);			
			// return lookup
			return m_us.getValue(st, m_arg_ps, false);			
		}
		if(s <= 15) {
			// limit variable and return slope type index
			int st = (int)m_eds.getIndex(s);			
			// return lookup
			return m_eds.getValue(st, m_arg_ps, false);			
			
		}
		else {
			// limit variable and return slope type index
			int st = (int)m_sds.getIndex(s);			
			// return lookup
			return m_sds.getValue(st, m_arg_ps, false);							
		}
	}

	/**
	 *  Get resistance type
	 *  
	 *  @param s Slope angle between position pair (pi,i)
	 *  @param t Time at current position
	 *  @param i Current position
	 *  @return Returns resistance type between position pair (pi,i)
	 */		
	private int getResistanceType(int i) {
		// initialize
		int rt = 0;
		try {
			
			// initialize
			IFeatureProxy pFeature = null;			
			IRowIdentifyObjectProxy pRowObj = null;   
			
			// update current
			IPoint p = m_polyline.getPoint(i);
						
			// prepare search envelope
			m_search.centerAt(p);
			
			// identify height below point
			IArray arr = m_roads.identify(m_search);
						
			// found road?
			if (arr != null) {
			
				// Get the feature that was identified by casting to IRowIdentifyObject   
				pRowObj = new IRowIdentifyObjectProxy(arr.getElement(0));   
				pFeature = new IFeatureProxy(pRowObj.getRow());
				
				// get map value
				int map = Integer.valueOf(pFeature.getValue(m_roadsIndex).toString());
				
				// get variable index
				rt = m_resistanceMap.getIndexFromMap(map);
				
				// get feature value
				m_themes.add(rt);
			
			} 
			else {
			
				// identify surface below point
				arr = (IArray)m_surface.identify(m_search);
				
				// found anything?
				if (arr != null) {
				
					// Get the feature that was identified by casting to IRowIdentifyObject   
					pRowObj = new IRowIdentifyObjectProxy(arr.getElement(0));   
					pFeature = new IFeatureProxy(pRowObj.getRow());
					
					// get map value
					int map = Integer.valueOf(pFeature.getValue(m_surfaceIndex).toString());
					
					// get variable index
					rt = m_resistanceMap.getIndexFromMap(map);
					
					// get feature value
					m_themes.add(rt);
				
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// return default
		return rt;
	}
	
	/**
	 *  Get snow state index at position i
	 *  
	 *  @param t Time at current position
	 *  @param i Current position
	 *  @return Returns snow type at position i
	 */		
	private int getSnowState(Calendar t, int i) {
		// initialize
		double st = 0;
		try {
			// get position
			Point2D.Double p = m_positions.get(i).getPosition();
			// get forecasted snow state at position
			SnowInfoPoint sf = m_info_si.getForecast(t,p.x,p.y);
			// found?
			if(sf != null) {
				// get snow type
				st = m_sst.getWeightedVariable(sf.m_sst) 
						  	+ m_nsd.getWeightedVariable(sf.m_nsd); 
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// return snow state index
		return (int)m_ss.getIndex(st);						
	}
		
	/**
	 *  Get light type at position and time
	 *  
	 *  @param t Time at position
	 *  @param i Current position  
	 *  @return Returns light type at position and time
	 */		
	private int getLightType(Calendar t, int i) {
		// initialize
		double lt = 0;
		try {
			// get point
			Point2D.Double p = m_positions.get(i).getPosition();
			// get forecasted light at position
			LightInfoPoint lf = m_info_li.getForecast(t,p.x,p.y);
			// found?
			if(lf != null ){
				// translate to type
				lt = m_li.getWeightedVariable(lf.m_li);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return (int)lt;
	}
	
	/**
	 *  Internal algorithm that calculates time cost along a spesified route 
	 *  from a given position.
	 *  
	 *  @param offset Calculation is started from given position
	 *  @param t0 Initial point in time
	 *  @return Estimated elapsed time from given position
	 */		
	private int estimateRouteCost(int offset, Calendar t0) throws Exception  {

		// initialize
		double cost = 0;			// cumulative time cost in seconds
	
		// force initial time?
		if(t0 == null)
			t0 = Calendar.getInstance();
		
		// valid parameters?
		if(offset >= 0 && t0 != null){
			
			// create or update?
			if (m_isReplaced)
				cost = createEstimate(offset,t0); // is slow
			else
				cost = updateEstimate(offset,t0); // is fast
			
		}

		// return estimated cost
		return (int)cost;
		
	}
	
	/**
	 *  Create estimate information
	 */		
	private double createEstimate(int offset, Calendar t0) throws Exception  {

		// initialize
		double d = 0;				// distance between previous and current position
		double s = 0;				// slope between previous and current position
		double cost = 0;			// cumulative time cost in seconds
		double utc = 0;				// unit terrain cost in seconds
		double uwc = 0;				// unit weather cost in seconds
		double ulc = 0;				// unit light cost in seconds
		double cd = 0;				// segment time cost in seconds
		Calendar t = null;			// set current time
	
		// get route data
		getRouteData();
		
		// get point count
		int count = getCount();
		
		// update start time, point offset, and start height
		m_h1 = 0;
		m_startTime = t0;
		m_startIndex = offset;
		
		// create objects
		m_slopes = new ArrayList<Double>(count - 2);
		m_cumCost = new ArrayList<Double>(count - 2);
		m_terrainUnitCosts = new ArrayList<Double>(count - 2);
		m_weatherUnitCosts = new ArrayList<Double>(count - 2);
		m_lightUnitCosts = new ArrayList<Double>(count - 2);
		m_themes = new ArrayList<Integer>(count);
						
		// initialize time step
		t = Calendar.getInstance();
		t.setTime(t0.getTime());
		
		// loop over all positions
		for(int i = offset; i < count; i++){
			
			// valid parameter?
			if(i > 0) {

				// get distance
				d = m_distances.get(i-1);
				
				// get slope angle
				s = getSlope(i,d);
				
				// add slope
				m_slopes.add(s);
				
				// get current time
				t.add(Calendar.SECOND, (int)cost);
				
				// estimate unit costs
				utc = getTerrainCost(s,t,i);
				uwc = getWeatherCost(t,i-1);
				ulc = getLightCost(t,i-1);
				
				// save unit costs
				m_terrainUnitCosts.add(utc);
				m_weatherUnitCosts.add(uwc);
				m_lightUnitCosts.add(ulc);
				
				// calculate segment cost
				cd = d*(utc + uwc + ulc);
				
				// add segment cost
				cost += cd;
				
				// save cumulative cost
				m_cumCost.add(cost);
				
			}
		}						

		// reset flag
		m_isReplaced = false;			

		// return estimated cost
		return cost;
		
	}	

	
	/**
	 *  Update estimate information
	 */		
	private double updateEstimate(int offset, Calendar t0) throws Exception  {

		// initialize
		GeoPos i  = null;			// current slope
		GeoPos pi = null;			// previous position
		double d = 0;				// distance between previous and current position
		Calendar t = null;			// set current time
		double cost = 0;			// cumulative time cost in seconds
		double utc = 0;				// unit terrain cost in seconds
		double uwc = 0;				// unit weather cost in seconds
		double ulc = 0;				// unit light cost in seconds
		double cd = 0;				// segment time cost in seconds
	
		// get point count
		int count = getCount();
		
		// update offset and start time
		m_startTime = t0;
		m_startIndex = offset;
		
		// initialize time step
		t = Calendar.getInstance();
		t.setTime(t0.getTime());
		
		// loop over positions from offset
		for(int j = offset; j < count; j++){
			
			// get position
			i = m_positions.get(j);
			
			// valid parameter?
			if(pi != null) {

				// get current time
				t.add(Calendar.SECOND, (int)cost);
				
				// get distance
				d = m_distances.get(j - 1);

				// get save unit costs
				utc = m_terrainUnitCosts.get(j - 1);
				uwc = m_weatherUnitCosts.get(j - 1);
				ulc = m_lightUnitCosts.get(j - 1);
				
				// calculate segment cost
				cd = d *(utc + uwc + ulc);
				
				// add segment cost
				cost += cd;
				
				// save cumulative cost
				m_cumCost.set(j - 1,cost);				
				
			}
			// update last position
			pi = i;
		}
		
		// return calculated cost
		return cost;
	}	
	
}
