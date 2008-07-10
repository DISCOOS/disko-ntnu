package org.redcross.sar.ds.ete;

import java.io.IOException;
import java.lang.Math;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;

import java.awt.geom.Point2D;

import org.redcross.sar.util.mso.Route;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.util.mso.TimePos;
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
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.interop.AutomationException;


/**
 *  Calculates route time cost along a specified route
 *  
 * @author kennetgu
 *
 */
public class RouteCost {

	private static final long SPATIAL_TIME_SHIFT_TOLERANCE = 1*60*60; /* is time shift is large then 1 hour, a
																       * new estimate must be created */
	
	 		
	 
	
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

	// objects	
	private final SnowInfo m_info_si;
	private final LightInfo m_info_li;
	private final WeatherInfo m_info_wi;
		
	private IDiskoMap m_map;
	private ISpatialReference m_srs;
	private IEnvelope m_search;
	private Raster m_altitude;
	private IIdentify m_roads;
	private IIdentify m_surface;
	private int m_roadsIndex;
	private int m_surfaceIndex;
	
	// states
    private double m_propulsion = 0;							// current propulsion method along route
	private double m_h1 = 0;									// height at last visited position
	private Calendar m_startTime = null;						// start time used when no 
	private Track m_mt = null;									// measured track  - from first to last known position
	private Track m_et = null;									// estimated track - from last known position to route destination
	private Calendar m_eta = null;    							// estimated time of arrival at route destination
	private ArrayList<Double> m_legs = null;					// leg distance between two consecutive points on route
	private ArrayList<Double> m_slopes = null;					// leg slope between two consecutive points on route 
	private ArrayList<Double> m_terrainLegCosts = null;			// terrain component cost of leg between two consecutive points on route
	private ArrayList<Double> m_weatherLegCosts = null;			// weather component cost of leg between two consecutive points on route
	private ArrayList<Double> m_lightLegCosts = null;			// light component cost of leg between two consecutive points on route
	private ArrayList<Double> m_ete = null;						// estimated time cost for each at position index
	private ArrayList<Double> m_eda = null;						// estimated moved distance on arrival at position index
	private ArrayList<Double> m_mda = null;						// measured moved distance on arrival at position index
	private LegData m_current = null;							// current leg
	
	// route data
	private Route m_route = null;								// current route
	private ArrayList<GeoPos> m_positions = null;				// list of densified positions from route
	private Polyline m_polyline = null;							// a polyline created from m_positions
	
	// flags
	private boolean m_isSpatialChanged = false;					// if true, create() is invoked calculating a new spatial and temporal estimate
	private boolean m_isTemporalChanged = false;				// if true, update() is invoked only updating the temporal part of the estimate
	private boolean m_isArchived = false;						// if true, estimate() returns current ete() value 
	private boolean m_isSuspended = true;						// if true, estimate() returns current ete() value
    
	// properties
	private final RouteCostProp m_p;		// propulsion type
	private final RouteCostProp m_sst;	// snow state type
	private final RouteCostProp m_nsd;	// new snow depth type
	private final RouteCostProp m_ss;		// snow state
	private final RouteCostProp m_su;		// surface type
	private final RouteCostProp m_us;		// upward slope 
	private final RouteCostProp m_eds;	// easy downward slope
	private final RouteCostProp m_sds;	// steep downward slope
	private final RouteCostProp m_pre;	// preciptiation type
	private final RouteCostProp m_win;	// wind type
	private final RouteCostProp m_tem;	// temperature type
	private final RouteCostProp m_w;		// weather
	private final RouteCostProp m_li;		// light 
	
	// feature maps
	private RouteCostFeatureMap m_slopeMap;				// slope feature layers (GIS data)
	private RouteCostFeatureMap m_resistanceMap;		// resistance feature layers (GIS data)
	
	// arguments
	private final int[] m_arg_p = {0,0}; 	// {propulsion}
	private final int[] m_arg_ps = {0,0}; // {propulsion,snow state} 
	
	/* =====================================================================
	 * Constructors
	 * ===================================================================== */
	
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
		
		// create tracks
		m_mt = new Track("mt");
		m_et = new Track("et");
		
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
			// get spatial reference
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

	/* =====================================================================
	 * Public methods
	 * ===================================================================== */
	
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
		m_isSpatialChanged = true;
					
	}
	
	/**
	 *  Get the position count in the internal route
	 *  
	 *  @return Returns the position count of the densified route
	 */		
	public int getPositionCount() {
		if(!m_isSpatialChanged)
			// get count
			return m_positions!=null ? m_positions.size() : 0;
		else
			return -1;		
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
		m_isSpatialChanged = true;
		// return index
		return m_arg_p[0];
	}
	
	/**
	 * Gets a copy of the measured track
	 * 
	 * @return Track of known positions and arrival times
	 */	
	public Track getMeasuredTrack() {
		try {
			return m_mt.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets a copy of the estimated track
	 * 
	 * @return Track of estimated positions and arrival times
	 */	
	public Track getEstimatedTrack() {
		try {
			return m_et.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Set last known position (lpk)
	 * @param TimePos p - the position and arrival time 
	 */
	public boolean setLastKnownPosition(TimePos p) {
		if(!equal(p,m_mt.getStopPoint())) {
			m_mt.add(new TimePos(p.getPosition(),p.getTime()));
			m_isTemporalChanged = true;
			return true;
		}
		return false;
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
	public boolean setStartTime(Calendar t0) {
		if(!equal(m_startTime,t0)) {
			if(getMillis(m_startTime,t0)>SPATIAL_TIME_SHIFT_TOLERANCE)
				m_isSpatialChanged = true;
			m_startTime = t0;
			m_isTemporalChanged = true;
			return true;
		}
		return false;
	}		
	
	/**
	 *  Calculated estimated time enroute to route destination.
	 *  
	 *  @return Estimated time enroute (ete)
	 */		
	public int estimate() throws Exception{		
		
		// return current value?
		if(m_isSuspended || m_isArchived) return ete();
		
		// initialize
		double cost = ete(); 
		
		// force initial time?
		if(m_startTime == null) setStartTime(Calendar.getInstance());
		
		// create or update?
		if (m_isSpatialChanged)
			cost = create(); // is slow
		else if (m_isTemporalChanged)
			cost = update(); // is fast
		
		// forward
		travel();

		// return estimated cost
		return (int)cost;
	}			
	
	/**
	 *  Get the nearest position in route
	 *  
	 *  @param Point2D.Double match - position to match
	 *  @param double max - maximum distance in meters
	 *  
	 *  @return Returns the index of the closest position in route. If the shortest distance
	 *  found is longer then max, <code>-1</code> is returned.
	 */		
	public int findNearest(Point2D.Double match, double max) throws Exception {
		
		// initialize variables
		double min=-1;
		int found = -1; 
		double x = match.x;
		double y = match.y;
		List<GeoPos> c = new ArrayList<GeoPos>(m_route.getItems());
				
		// search?
		if(c.size()>0) {		
			// initialize search
			GeoPos it = c.get(0);
			min = MapUtil.greatCircleDistance(y, x, it.getPosition().y, it.getPosition().x);
			found = 0;
			// search for shortest distance
			for(int i=1;i<m_route.getItems().size();i++) {
				it = c.get(i);
				double d = MapUtil.greatCircleDistance(y, x, it.getPosition().y, it.getPosition().x);
				if(d<min) {
					min = d;
					found = i;
				}						
			}
		}
		
		// limit to maximum?
		if(max!=-1 && min>max) found = -1;
		
		// finished
		return found;
	}
	
	/**
	 * If true, estimate is not up to date
	 */
	public boolean isDirty() {
		return m_isTemporalChanged || m_isSpatialChanged;
	}
	
	/**
	 * If true, offset or start time is changed
	 */
	public boolean isTemporalChanged() {
		return m_isTemporalChanged;
	}
	
	/**
	 * If true, full estimation will performed
	 */
	public boolean isSpatialChanged() {
		return m_isSpatialChanged;
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
	public void archive() {
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
		
	/* =====================================================================
	 * Estimate results
	 * ===================================================================== */
	
	/**
	 *  Gets estimated time enroute at route destination (ete)
	 *  
	 *  @return Estimated time enroute in seconds
	 */		
	public int ete() {
		if (!(m_eta==null || m_isSpatialChanged))
			return (int)(m_eta.getTimeInMillis() - System.currentTimeMillis())/1000;
		else
			return 0;
	}	
	
	/**
	 *  Gets estimated time of arrival at route destination (eta).
	 *  
	 */		
	public Calendar eta() {
		if (!(m_eta==null || m_isSpatialChanged))
			return (Calendar)m_eta.clone();
		else
			return null;
	}
	
	/**
	 * Gets estimated distance enroute at route destination (ede)
	 * 
	 */	
	public double ede() {
		// get estimated current position
		GeoPos ecp = ecp();
		// get to point on leg
		GeoPos tp = m_positions.get(m_current.index);
		// get rest distance of leg
		double ede = MapUtil.greatCircleDistance(
				ecp.getPosition().y, ecp.getPosition().x, 
				tp.getPosition().y, tp.getPosition().x);
		// get rest of estimated distance
		ede += m_eda.get(m_eda.size()-1) - m_eda.get(m_current.index);
		// finished
		return ede;
	}
	
	/**
	 *  Gets the estimated moved distance at arrival (eda)
	 *  
	 */		
	public double eda() {
		if (!m_isSpatialChanged)
			return m_eda.get(m_eda.size() - 1);
		else
			return 0;
	}	
	
	/**
	 * Gets estimated current position (ecp)
	 * 
	 */	
	public GeoPos ecp() {
		return new GeoPos(m_current.pd[0]);
	}
		
	/**
	 * Get estimated average speed to destination (eas)
	 * 
	 * @return Speed in m/s
	 */	
	public double eas() {
		double ete = ete();
		return ete>0 ? ede()/ete() : 0;
	}
		
	/* =====================================================================
	 * Measured results
	 * ===================================================================== */
	
	/**
	 * Gets a copy the first known position (fkp)
	 * 
	 */	
	public TimePos fkp() {
		return m_mt.getStartPoint();
	}
		
	/**
	 * Gets a copy the last known position (lkp)
	 * 
	 */	
	public TimePos lkp() {
		TimePos tp = m_mt.getStopPoint();
		if(tp!=null) {
			return new TimePos(tp.getPosition(),tp.getTime());
		}
		return null;
	}	
	
	/**
	 * Get measured moved time to arrival at last known position (mta)
	 * 
	 * @return Time moved in seconds
	 */	
	public int mta() {
		TimePos fkp = fkp();
		TimePos lkp = lkp();
		return fkp!=null ? (int)(lkp.getTime().getTimeInMillis() - fkp.getTime().getTimeInMillis())/1000 : 0;
	}
	
	/**
	 * Get measured distance to arrival at last known position (mda)
	 * 
	 */	
	public double mda() {
		return m_mda.get(m_mda.size()-1);
	}
	
	/**
	 * Get measured average speed to arrival at last known position (mas)
	 * 
	 * @return Speed in m/s
	 */	
	public double mas() {
		double mta = mta();
		return mta>0 ? mda()/mta() : 0;
	}	
		
	/* =====================================================================
	 * Private methods
	 * ===================================================================== */

	/**
	 * Calculate initial step of algorithm
	 */
	private LegData init(boolean create) {
		
		/* ================================================
		 * Initialize time step
		 * 
		 * Estimated time of arrival at destination is
		 * dependent on the start time and route legs. 
		 * If first known position is given, then this is
		 * used instead of the start time. The estimation
		 * starts from this point if it is given.
		 * ================================================ */
		
		// initialize
		double d = 0;					// distance between previous and current position
		int size = 10;					// default size
		int offset = 0;					// position offset index
		IPoint pe = null;				// A leg position (ESRI)
		Point2D.Double pd = null;		// A leg position (Java)
		boolean bCalculate = false;		// if true, calculate first step
		Calendar t = null;				// the start time at first position
		
		// create new estimate?
		if(create) {

			// get route data
			getRouteData();	
			
			// get maximum size
			size = Math.max(size,m_positions.size());
			
			// initialize intermediate lists
			m_slopes = new ArrayList<Double>(size);
			m_terrainLegCosts = new ArrayList<Double>(size);
			m_weatherLegCosts = new ArrayList<Double>(size);
			m_lightLegCosts = new ArrayList<Double>(size);
			
		}
		
		// create arguments object
		LegData args = new LegData();
		
		// catch all errors
		try {
			
			/* ===============================================
			 * Index - prepare
			 * 
			 * No points should be added to ET 
			 * (estimated track) before current leg is 
			 * greater then prepare index. 
			 * 
			 * This behavior is only needed when the
			 * route is spatial changed and the offset
			 * is other then leg 0. In this case, we have to
			 * calculate the spatial datasets from the 
			 * beginning, not only from the offset. If not, 
			 * no spatial cost data will exist before the 
			 * offset index and thus no cost can be 
			 * calculated for offsets before the offset
			 * supplied in this run. Hence, when a route is
			 * changed spatially, a full estimation must be 
			 * performed. However, the estimated track 
			 * should only be generated from the offset.
			 *  
			 * =============================================== */
			int prepare=-1;		// default - no prepare
			
			// get last known position
			TimePos lkp = lkp();			
			
			// start to estimate from last known position?
			if(lkp!=null) {
				// get start point
				pd = lkp.getPosition();
				// get a copy of start time
				t = (Calendar)lkp.getTime().clone();
				// get offset
				offset = findNearest(pd, -1);
				// found?
				if(offset!=-1) {
					// prepare?
					if(create) {
						// only prepare from start of route to offset
						prepare = offset;
					}
					// get next position
					Point2D.Double next = m_positions.get(offset).getPosition();				
					// get offset position
					pe = MapUtil.getEsriPoint( pd.x,pd.y, m_srs);
					// get first height
					m_h1 = getHeight(pe);
					// get first leg distance
					d = MapUtil.greatCircleDistance( pd.y,pd.x, next.y,next.x );
					// initialize arguments
					args.init(offset,t,pd,pe,prepare);
					// calculate
					bCalculate = true;
				}
			}
			// get offset position?
			if(pe==null) {
				// get start time?
				if(t==null) {
					t = Calendar.getInstance();
					t.setTime(m_startTime.getTime());
				}
				// get valid offset
				offset = (offset>0 ? offset : 0);
				// get start point
				pd = m_positions.get(offset).getPosition();
				// get start position
				pe = m_polyline.getPoint(offset);
				// get first height
				m_h1 = getHeight(pe);
				// get leg distance
				d = m_legs.get(offset);
				// move to stop point on leg
				offset++;
				// initialize arguments
				args.init(offset,t,pd,pe,prepare);			
			}
			
			// get size of results
			size = Math.max(10,m_positions.size() - args.offset - 1);
			
			// initialize results
			m_ete = new ArrayList<Double>(size);
			
			// initialize estimated track
			m_et = new Track("et","et",size);			
			
			// add as start leg point
			m_et.add(new TimePos(pd,t));				
			
			// calculate?
			if(bCalculate) {
				// prepare to calculate time cost of this initial leg
				args.next(offset,d,m_positions.get(offset).getPosition(), m_polyline.getPoint(offset));
				// calculate first step and add to estimate
				calculate(args,create,false,true);
				// increment offset index to start point on 
				args.offset++;
			}
			
			// set current leg
			m_current = args.clone();
			
			// finished
			return args;
			
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return null;
		
	}
	
	/**
	 *  Create estimate information
	 */		
	private double create() throws Exception  {

		/* ================================================
		 * Initialize leg arguments
		 * ================================================ */
		LegData args = init(true);
		
		// prepare estimate before offset?
		if(args.prepare!=-1) {		
		
			// get copy of arguments
			LegData copy = args.clone();
			
			// initialize at first position on route
			args.init(0, args.t, m_positions.get(0).getPosition(), m_polyline.getPoint(0), args.prepare);
			
			// prepare leg costs
			for(int i = 1; i <= args.prepare; i++) {
				
				// get next distance
				double d = m_legs.get(i-1);
				
				// update arguments
				args.next(i,d, m_positions.get(i).getPosition(), m_polyline.getPoint(i));
	
				// calculate leg component cost only
				calculate(args,true,true,false);
				
			}
			
			// replace with copy
			args = copy;
			
		}
		
		// get position count
		int count = m_positions.size();
		
		// loop over positions from prepare to enroute
		for(int i = args.offset; i < count; i++){
			
			// get next distance
			double d = m_legs.get(i-1);
			
			// update arguments
			args.next(i,d, m_positions.get(i).getPosition(), m_polyline.getPoint(i));

			// calculate leg time cost
			calculate(args,true,true,true);
			
		}						
		
		// update eta
		m_eta = args.t;
		
		// reset flags
		m_isSpatialChanged = false;
		m_isTemporalChanged = false;

		// return estimated cost
		return ete();
		
	}	
	
	/**
	 *  Update estimate information
	 */		
	private double update() throws Exception  {
	
		/* ================================================
		 * Initialize leg arguments
		 * ================================================ */
		LegData args = init(false); 		

		// get point count
		int count = m_positions.size();		
		
		// loop over positions from offset
		for(int i = args.offset; i < count; i++){
			
			// get next distance
			double d = m_legs.get(i-1);
			
			// update arguments
			args.next(i,d, m_positions.get(i).getPosition(), m_polyline.getPoint(i));

			// calculate segment cost
			calculate(args,false,false,true);
			
		}
		
		// update eta
		m_eta = args.t;
		
		// reset flag
		m_isTemporalChanged = false;
		
		// return calculated cost
		return ete();
	}	
	
	/**
	 *  Update estimate information
	 */		
	private boolean travel() throws Exception  {
		// can travel?
		if(m_current!=null) {
			// get current time
			Calendar t = Calendar.getInstance();
			// get ete in seconds
			long ete = (m_eta.getTimeInMillis() - t.getTimeInMillis())/1000;
			// get index of current leg 
			int index = getLegIndex(ete);		
			// get leg cost
			double lc = getLegCost(index);
			// get leg distance
			double ld = getLegDistance(index);	
			// calculate leg speed
			double lv = lc>0 ? ld/lc : 0;
			// calculate residue distance on leg
			double lr = lv*getResidueCost(index, ete);
			// calculate leg bearing 
			double lb = getLegBearing(index);
			// get leg from point
			GeoPos from = getLegFromPoint(index);
			// get new position on the straight line representing this leg
			Point2D.Double p = MapUtil.getCoordinate(
					from.getPosition().y, from.getPosition().x, lr, lb);
			// update current position
			m_current.next(m_current.offset + index,t,ld,p,null);
			// finished
			return true;
		}
		// failed
		return false;
	}

	/**
	 * Calculate leg time cost.
	 * 
	 * @param args - arguments object
	 * @param estimate - if <code>true</code> leg component costs will be calculated, else it will use the
	 * current leg component costs already estimated for the given leg index (args.index-1).
	 * @param create - if <code>true</code> leg component costs will be saved for later use. It is only active when estimate is <code>true</code>. 
	 * @param add - if <code>true</code> leg stop position (args.index) is added to estimated track (ET) together with estimated time enroute 
	 * (ETE) at this point.  
	 */
	private void calculate(LegData args, boolean estimate, boolean create, boolean add) {
		
		// create?
		if(estimate) {
		
			// get slope angle
			args.s = getSlope(args);
			
			// estimate unit costs
			args.tlc = getTerrainCost(args);
			args.wlc = getWeatherCost(args);
			args.llc = getLightCost(args);
		}
		else {
			// get saved leg component costs
			args.tlc = m_terrainLegCosts.get(args.index-1);
			args.wlc = m_weatherLegCosts.get(args.index-1);
			args.llc = m_lightLegCosts.get(args.index-1);						
		}			
		
		// save component costs?			
		if(estimate && create) {
			
			// add slope
			m_slopes.add(args.s);
			
			// add leg component costs
			m_terrainLegCosts.add(args.tlc);
			m_weatherLegCosts.add(args.wlc);
			m_lightLegCosts.add(args.llc);
			
		}
		
		// calculate leg cost
		args.lc = args.d*(args.tlc + args.wlc + args.llc);
		
		// get to current time
		args.t.add(Calendar.SECOND, (int)args.lc);

		// add to total cost
		args.ete += args.lc;
		
		// add estimate?
		if(add) {
			m_et.add(args.pd[1], args.t);
			m_ete.add(args.ete);			
		}
		
	}
	
	private int getLegIndex(long ete) {
		int i = -1;
		// still on first leg?
		if(m_current!=null) {
			if(ete<=m_current.ete) return -1;
		}
		// search for it
		for(i=0;i<m_ete.size();i++) {
			if(m_ete.get(i)>ete)				
				return i;
		}
		// finished
		return i-1;
	}
	
	private double getLegCost(int index) {
		// first leg?
		if(index==-1)
			return m_current!=null ? m_current.ete : 0.0;
		else if(index==0) 			
			return m_ete.get(index);
		else
			return m_ete.get(index) - m_ete.get(index-1);
	}				
	
	private double getResidueCost(int index, double ete) {
		// first leg?
		if(index==-1)
			return m_current!=null ? m_current.ete - ete : ete;
		else
			return m_ete.get(index) - ete;
	}				
	
	private double getLegDistance(int index) {
		// first leg?
		if(index==-1)
			return m_current.d;
		else
			return m_legs.get(m_current.offset+index);
	}				
	
	private TimePos getLegFromPoint(int index) {
		return m_et.get(index);
	}				
	
	private TimePos getLegToPoint(int index) {
		return m_et.get(index+1);
	}
	
	private double getLegBearing(int index) {
		return getLegFromPoint(index).bearing(getLegToPoint(index));
	}	
	
	/**
	 * Get time difference in milliseconds
	 * @param t1
	 * @param t2
	 * @return
	 */
	private long getMillis(Calendar t1, Calendar t2) {
		if(t1!=null && t2!=null)
			return t2.getTimeInMillis() - t1.getTimeInMillis();
		else
			return -1;
	}
	
	/**
	 * Compare two objects
	 * @param o1
	 * @param o2
	 * @return
	 */
    private boolean equal(Object o1, Object o2)
    {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }
	
	/**
	 * Get Route Data from Route
	 */
	private void getRouteData() {
		
		// allowed?
		if(m_isSpatialChanged) {
		
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
			
		}		
		
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
				newPositions.add(p.clone());		
				// create arrays
				m_legs = new ArrayList<Double>(count - 2);
				m_eda = new ArrayList<Double>(count - 2);
				// loop over all
				for(int i=1;i<count;i++) {
					// get next point
					Point2D.Double p2 = m_positions.get(i).getPosition();
					// calculate distance
					d += MapUtil.greatCircleDistance(p1.y, p1.x, p2.y, p2.x);
					// large or equal minimum length?
					if(d > min || i == count-1) {						
						// add segment distance 
						m_legs.add(d);						
						// add to cumulative distance
						sum += d;						
						// save cumulative distance
						m_eda.add(sum);						
						// add to densified polyline
						newPolyline.addPoint(m_polyline.getPoint(i), null, null);						
						// add to densified positions array
						newPositions.add(m_positions.get(i).clone());								
						// reset distance
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
	 *  @return Returns slope between two positions in degrees
	 */		
	private double getSlope(LegData args) {
		// initialize
		double h2 = 0;
		double s = 0;
		// get point
		IPoint p = args.pe[1];
		// get height 2
		h2 = getHeight(p);
		// Calculate slope?
		if (args.d > 0) {
			// calculate height difference
			double h = m_h1 - h2;
			// calculate
			s = Math.signum(h)*Math.toDegrees(Math.atan(Math.abs(h/args.d)));
		}
		
		// save current height
		m_h1 = h2;
		
		// finished
		return s;
	}
	
	private double getHeight(IPoint p) {
		// initialize
		int[] col = {0};
		int[] row = {0};
		try {
			// get pixel cell for heigth 1
			m_altitude.mapToPixel(p.getX(), p.getY(), col, row);
			// get height 1
			return Double.valueOf(m_altitude.getPixelValue(0, col[0], row[0]).toString());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return m_h1;
	}
	
	/**
	 *  Get terrain unit cost
	 *  
	 *  @param s Slope angle between position pair (pi,i)
	 *  @param t Time at current position
	 *  @param i Current position
	 *  @return Returns terrain unit cost for position pair (pi,i)
	 */		
	private double getTerrainCost(LegData args) {
		int ss = 0;				// snow state
		int rt = 0;				// resistance state type
		double cost = 0;
		
		// get types indexes
		ss = getSnowState(args.t,args.pd[0]);				
		rt = getResistanceType(args.pe[1]);		
		
		// get unit costs
		cost = getSurfaceCost(ss,rt);		
		cost += getSlopeCost(ss,args.s);		
		
		return cost;
	}

	/**
	 *  Get weather unit cost at position i
	 *  
	 *  @param t Time at position
	 *  @param i Current position  
	 *  @return Returns weather unit cost at position i
	 */		
	private double getWeatherCost(LegData args) {
		// get weather type
		int wt = getWeatherType(args.t,args.pd[0]);		
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
	private int getWeatherType(Calendar t, Point2D.Double p) {
		// initialize
		double wt = 0;
		try {
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
	private double getLightCost(LegData args) {
		// get day or night at position and time
		int lt = getLightType(args.t,args.pd[0]);				
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
	private int getResistanceType(IPoint p) {
		// initialize
		int rt = 0;
		try {
			
			// initialize
			IFeatureProxy pFeature = null;			
			IRowIdentifyObjectProxy pRowObj = null;   
			
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
	private int getSnowState(Calendar t, Point2D.Double p) {
		// initialize
		double st = 0;
		try {
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
	private int getLightType(Calendar t, Point2D.Double p) {
		// initialize
		double lt = 0;
		try {
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
	
	/* =================================================================
	 * Inner classes 
	 * ================================================================= */
	
	private class LegData implements Cloneable {
		
		public int offset = 0;		// leg - offset index
		public int index = 0;		// leg - current index
		public double d = 0.0;		// leg - distance in meters
		public double s = 0.0;		// leg - slope in degrees
		double lc = 0.0;			// leg - cost
		double tlc = 0.0;			// leg - terrain component cost in seconds
		double wlc = 0.0;			// leg - weather component cost in seconds
		double llc = 0.0;			// leg - light component cost in seconds
		double ete = 0.0;			// route - estimated time enroute
		public Calendar t;			// eta - from offset index to current index
		public Point2D.Double[] pd; // leg points - decimal degrees 
		public IPoint[] pe;			// leg points - native esri
		public int prepare;			// leg - while prepare < offset, no leg stop points should be added to estimated track  
		
		public void init(int offset, Calendar t, Point2D.Double pd, IPoint pe, int prepare) {
			// prepare
			this.offset = offset;
			this.index = offset;
			this.prepare = prepare;
			this.t = t;
			this.pd = new Point2D.Double[] {pd,pd};
			this.pe = new IPoint[]{pe,pe};
			// reset
			this.s = 0.0;
			this.lc = 0.0;			// leg - cost
			this.tlc = 0.0;			// leg - terrain component cost in seconds
			this.wlc = 0.0;			// leg - weather component cost in seconds
			this.llc = 0.0;			// leg - light component cost in seconds
			this.ete = 0.0;			// route - estimated time enroute			
		}
		
		public void next(int index, double d, Point2D.Double pd2, IPoint pe2) {
			next(index,t,d,pd2,pe2);
		}
		
		public void next(int index, Calendar t, double d, Point2D.Double pd2, IPoint pe2) {
			this.index = index;
			this.d = d;
			this.t = t;
			this.pd[0] = pd[1];
			this.pd[1] = pd2;
			this.pe[0] = pe[1];
			this.pe[1] = pe2;
		}
		
		public LegData clone() {
			// create a clone
			LegData args = new LegData();
			args.offset = this.offset;
			args.index = this.index;
			args.d = this.d;
			args.s = this.s;
			args.lc = this.lc;
			args.tlc = this.tlc;
			args.wlc = this.wlc;
			args.llc = this.llc;
			args.ete = this.ete;
			args.t = (Calendar)this.t.clone();
			args.pd = this.pd; 
			args.pe = this.pe;
			args.prepare = this.prepare;  
			return args;
		}
		
	}
	
	
}
