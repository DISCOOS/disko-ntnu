package org.redcross.sar.ds.ete;

import java.io.IOException;
import java.lang.Math;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;

import java.awt.geom.Point2D;

import org.redcross.sar.util.mso.Route;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;
import org.redcross.sar.ds.AbstractDsObject;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IAssignmentIf;

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
public class RouteCost extends AbstractDsObject {

	private static final long ESTIMATE_TIME_TOLERANCE = 1*60*60; /* if progress is larger then 1 hour, a
																  * new estimate must be created */

	private static final String[] ATTRIBUTES = {"ete","eta","ede","eda","ecp",
												"eas","fkp","lkp","mta","mda","mas"};
    
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
	
	// calculation
	private int m_method = 0;
	
	// states
    private double m_propulsion = 0;							// current propulsion method along route
	//private double m_h1 = 0;									// height at last visited position
	private Calendar m_startTime = null;						// start time used when no 
	private Track m_mt = null;									// measured track  - from first to last known position
	private Track m_et = null;									// estimated track - from last known position to route destination
	private Calendar m_eta = null;    							// estimated time of arrival at route destination
	private List<Double> m_legs = null;							// leg distance between two consecutive points on route
	private List<Double> m_slopes = null;						// leg slope between two consecutive points on route 
	private List<Double> m_terrainLegCosts = null;				// terrain component cost of leg between two consecutive points on route
	private List<Double> m_weatherLegCosts = null;				// weather component cost of leg between two consecutive points on route
	private List<Double> m_lightLegCosts = null;				// light component cost of leg between two consecutive points on route
	private List<Double> m_altitudes = null;					// altitude at each point
	private LegData m_current = null;							// current leg
	
	// route data
	private Route m_route = null;								// current route
	private List<GeoPos> m_positions = null;					// list of densified positions from route
	private Polyline m_polyline = null;							// a polyline created from m_positions
	
	// flags
	private boolean m_isSpatialChanged = false;					// if true, create() is invoked calculating a new spatial and temporal estimate
	private boolean m_isTemporalChanged = false;				// if true, update() is invoked only updating the temporal part of the estimate
	private boolean m_isArchived = false;						// if true, estimate() returns previous calculated ete() value
	private boolean m_isSuspended = true;						// if true, estimate() returns previous calculated ete() value
    
	// properties
	private final RouteCostProp m_p;		// propulsion type
	private final RouteCostProp m_sst;		// snow state type
	private final RouteCostProp m_nsd;		// new snow depth type
	private final RouteCostProp m_ss;		// snow state type
	private final RouteCostProp m_su;		// surface type type
	private final RouteCostProp m_us;		// upward slope type
	private final RouteCostProp m_eds;		// easy downward slope type
	private final RouteCostProp m_sds;		// steep downward slope type
	private final RouteCostProp m_pre;		// precipitation type
	private final RouteCostProp m_win;		// wind type
	private final RouteCostProp m_tem;		// temperature type
	private final RouteCostProp m_w;		// weather condition type
	private final RouteCostProp m_li;		// light condition type
	
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
	public RouteCost(IAssignmentIf id, Route route, int propulsion, IDiskoMap map) {
		
		// forward
		super(id);
		
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
	 * IDsObjectIf implementation
	 * ===================================================================== */

	@Override
	public IAssignmentIf getId() {
		return (IAssignmentIf)super.getId();
	}
	
	public String getAttrName(int index) {
		return ATTRIBUTES[index];
	}
	
	public int getAttrIndex(String name) {
		int count = ATTRIBUTES.length;
		for(int i=0;i<count;i++) {
			if(ATTRIBUTES[i].equals(name))
				return i;
		}
		return -1;
	}
	
	public int getAttrCount() {
		return ATTRIBUTES.length;
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
			if(getMillis(m_startTime,t0)>ESTIMATE_TIME_TOLERANCE)
				m_isSpatialChanged = true;
			m_startTime = t0;
			m_isTemporalChanged = true;
			return true;
		}
		return false;
	}		
	
	public boolean hasRoute() {
		return m_route!=null;
	}
	
	public boolean canEstimate() {
		return hasRoute() && !(m_isSuspended || m_isArchived);
	}
	
	/**
	 *  Calculates estimated track (<code>et</code>) from current route (see <code>setRoute</code>). </p>
	 *  Progress is updated if possible. (see <code>canProgress</code>)</p>
	 *  If no route is supplied or if the estimate is suspended or archived, last calculated <code>ete</code> will be returned.</p> 
	 *  Use <code>hasRoute()</code>, <code>canEstimate()</code>, <code>isArchived()</code> or <code>isSuspended()</code> to check if a new 
	 *  calculation of <code>et</code> will occur. 
	 *  
	 *  @return Estimated time enroute at destination (<code>ete</code>)
	 *  @throws Exception
	 */		
	public int estimate() throws Exception {		
		
		// return current value?
		if(m_isSuspended || m_isArchived) return ete();
		
		// initialize
		double cost = ete(); 
		
		// can estimate?
		if(hasRoute()) {
			// force initial time?
			if(m_startTime == null) setStartTime(Calendar.getInstance());
			
			// create or update?
			if (m_isSpatialChanged)
				cost = create(); // is slow
			else if (m_isTemporalChanged)
				cost = update(); // is fast
		}
		
		// forward
		progress();

		// return estimated cost
		return (int)cost;
	}			
	
	/**
	 * Indicates if progress is possible to estimate
	 * 
	 * @return <code>true</code> if at least on leg exists in estimated track 
	 * (<code>et</code>) and at least one position exits in measured track (<code>mt</code>).</p> 
	 * <code>estimate()</code> will create <code>et</code>.</p>
	 * <code>setLastKnownPosition(*)</code> will add a point to <code>mt</code>.
	 */
	public boolean canProgress() {
		return !(m_et.getCount()==0 || m_mt.getCount()==0);
	}
	
	/**
	 * Update current time and estimated current position (<code>ecp</code>).
	 * 
	 * @return <code>true</code> if estimated current position was updated (<code>ecp</code>), 
	 * <code>false</code> otherwise. </p>
	 * This method will only succeed if at least on leg exists in estimated track 
	 * (<code>et</code>) and at least one position exits in measured track (<code>mt</code>).</p> 
	 * <code>estimate()</code> will create <code>et</code>.</p>
	 * <code>setLastKnownPosition(*)</code> will add a point to <code>mt</code>.
	 * 
	 *  @throws Exception
	 */
	public boolean progress() throws Exception  {
		// can't progress?
		if(!canProgress()) return false;
		// initialize current leg?
		if(m_current==null) {
			m_current = init(false);
		}
		// get current time
		Calendar t = Calendar.getInstance();
		// get ete in seconds
		long ete = Math.max(0,(m_eta.getTimeInMillis() - t.getTimeInMillis())/1000);
		// get index of current leg (from point index)
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
		// initialize estimated current position
		Point2D.Double ecp = null;
		// reached to-point?
		if(ld==0) {			
			ecp = getLegFromPoint(index).getPosition();
		}
		else {
			// get leg from point
			GeoPos from = getLegFromPoint(index);
			// get new position on the straight line representing this leg
			ecp = MapUtil.getCoordinate(
					from.getPosition().y, from.getPosition().x, lr, lb);
		}
		// update current position
		m_current.next(m_current.offset + index,t,ld,ecp,null);
		// finished
		return true;
	}
	
	
	/**
	 *  Get the nearest position on route
	 *  
	 *  @param Point2D.Double match - position to match
	 *  @param double max - maximum distance in meters, use <code>-1</code> to disable
	 *  @param boolean track - search in tracks instead route (measured and/or estimated) 
	 *  @param boolean all - search in both measured and estimated tracks. Only used if <code>track</code> is <code>true</code>
	 *  
	 *  @return Returns the index of the closest position in route. If the shortest distance
	 *  found is longer then max, <code>-1</code> is returned.
	 */		
	public int findNearest(Point2D.Double match, double max, boolean track, boolean all) throws Exception {
		
		// use measured or estimated track instead of route?
		if(!hasRoute() || track) {
			if(all) {
				// use union of measured and estimated track
				Track tr = m_mt.clone();
				tr.addAll(m_et);
				return findNearest(new ArrayList<GeoPos>(tr.getItems()), match, max);  
			}
			else {
				// use estimated track
				return findNearest(new ArrayList<GeoPos>(m_et.getItems()), match, max);
			}
				
		}
		// use route points
		return findNearest(new ArrayList<GeoPos>(m_route.getItems()), match, max);

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
		if (!(m_eta==null || m_isSpatialChanged)) {
			if(m_current!=null)
				return (int)(m_eta.getTimeInMillis() - m_current.t.getTimeInMillis())/1000;
			else
				return (int)(m_eta.getTimeInMillis() - m_startTime.getTimeInMillis())/1000;
		}
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
		// has estimated point?
		if(ecp!=null) {
			// get index of to-point on current leg 
			int index = Math.min(m_current.index - m_current.offset,m_et.getCount()-2);
			// valid?
			if(index>=0) {
				// get to point on leg
				GeoPos to = getLegToPoint(index);
				// get rest distance of leg
				double ede = MapUtil.greatCircleDistance(
						ecp.getPosition().y, ecp.getPosition().x, 
						to.getPosition().y, to.getPosition().x);
				// get rest of estimated distance
				ede += m_et.getDistance(index+1,m_et.getCount()-1,false);
				// finished
				return ede;
			}
		}
		return 0.0;
	}
	
	/**
	 *  Gets the estimated moved distance at arrival (eda)
	 *  
	 */		
	public double eda() {
		if (!m_isSpatialChanged)
			return m_mt.getDistance() + m_et.getDistance();
		else
			return 0.0;
	}	
	
	/**
	 * Gets estimated current position (ecp)
	 * 
	 */	
	public GeoPos ecp() {
		if(m_current!=null)
			return new GeoPos(m_current.pd[1]);
		else
			return null;
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
		return m_mt!=null ? (int)m_mt.getDuration(): 0;
	}
	
	/**
	 * Get measured distance to arrival at last known position (mda)
	 * 
	 */	
	public double mda() {
		return m_mt!=null ?  m_mt.getDistance() : 0.0;
	}
	
	/**
	 * Get measured average speed to arrival at last known position (mas)
	 * 
	 * @return Speed in m/s
	 */	
	public double mas() {
		double mta = mta();
		return mta>0 ? mda()/mta : 0.0;
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
		double h = 0;					// height at current position
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
			m_altitudes = new ArrayList<Double>(size);
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
				offset = findNearest(pd,-1,false,false);
				// found?
				if(offset!=-1) {
					// prepare?
					if(create) {
						// only prepare from start of route to offset
						prepare = offset;
					}
					// get next position
					Point2D.Double next = getPoint(offset,false,false);
					// get offset position
					pe = MapUtil.getEsriPoint(pd, m_srs);
					// forward
					h = initAltitude(offset,pe,create);
					// get first leg distance
					d = MapUtil.greatCircleDistance( pd.y,pd.x, next.y,next.x );
					// initialize arguments
					args.init(offset,t,h,pd,pe,prepare);
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
				pd = getPoint(offset,false,false);
				// get start position
				pe = getEsriPoint(offset,false,false);
				// forward
				h = initAltitude(offset,pe,create);					
				// get leg distance
				d = m_legs.get(offset);
				// move to stop point on leg
				offset++;
				// initialize arguments
				args.init(offset,t,h,pd,pe,prepare);			
			}
			
			// get size of results
			size = Math.max(10,m_positions.size() - args.offset - 1);
			
			// initialize estimated track
			m_et = new Track("et","et",size);			
			
			// add first point in track 
			m_et.add(new TimePos(pd,h,t));
			
			// calculate?
			if(bCalculate) {
				// prepare to calculate time cost of this initial leg
				args.next(offset,d,getPoint(offset,false,false), getEsriPoint(offset,false,false));
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
	
	private double initAltitude(int offset, IPoint pe, boolean create) {
		double h = 0.0;
		// get first height
		if(create) {
			h = queryAltitude(pe);
			m_altitudes.add(h);
		}
		else {
			h = m_altitudes.get(offset);
		}
		return h;
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
			IPoint pe = getEsriPoint(0,false,false);
			Point2D.Double pd = getPoint(0,false,false);
			args.init(0, args.t, queryAltitude(pe), pd, pe, args.prepare);
			
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
			// get saved altitude
			args.h = m_altitudes.get(args.index);
			// get saved leg component costs
			args.tlc = m_terrainLegCosts.get(args.index-1);
			args.wlc = m_weatherLegCosts.get(args.index-1);
			args.llc = m_lightLegCosts.get(args.index-1);
		}			
		
		// save component costs?			
		if(estimate && create) {
			
			// add height
			m_altitudes.add(args.h);
			
			// add slope
			m_slopes.add(args.s);
			
			// add leg component costs
			m_terrainLegCosts.add(args.tlc);
			m_weatherLegCosts.add(args.wlc);
			m_lightLegCosts.add(args.llc);
			
		}
		
		// calculate leg cost
		switch(m_method) {
		case 0: args.lc = args.d*(args.tlc + args.wlc + args.llc); break;
		case 1: args.lc = args.d*(args.tlc + args.wlc + args.llc); break;
		}
				
		// get to current time
		args.t.add(Calendar.SECOND, (int)args.lc);

		// add to total cost
		args.ete += args.lc;
		
		// add estimated point arrival time at point on route?
		if(add) {
			SimpleDateFormat f = new SimpleDateFormat();
			System.out.println(getId().toString() + "#" + f.format(args.t.getTime()));
			m_et.add(args.pd[1], args.h, args.t);
		}
		
	}
	
	private int findNearest(List<GeoPos> items, Point2D.Double match, double max) throws Exception {
		
		// initialize variables
		double min=-1;
		int found = -1; 
		double x = match.x;
		double y = match.y;
		int count = items.size();
				
		// search?
		if(count>0) {		
			// initialize search
			GeoPos it = items.get(0);
			min = MapUtil.greatCircleDistance(y, x, it.getPosition().y, it.getPosition().x);
			found = 0;			
			// search for shortest distance
			for(int i=1;i<count;i++) {
				it = items.get(i);
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
	
	private Point2D.Double getPoint(int index, boolean track, boolean all) {
		// use measured or estimated track instead of route?
		if(!hasRoute() || track) {
			if(all) {
				// get measured point?
				if(index<m_mt.getCount())
					return m_mt.get(index).getPosition();  
				// remove measured offset
				index -= m_mt.getCount()-1;
				// return estimated
				return m_et.get(index).getPosition();  
			}
			else {
				// return estimated
				return m_et.get(index).getPosition();  				
			}
				
		}
		// use route point
		return m_positions.get(index).getPosition();			
	}
	
	private IPoint getEsriPoint(int index, boolean track, boolean all) {
		try {
			// use measured or estimated track instead of route?
			if(!hasRoute() || track) {
				if(all) {
					// get measured point?
					if(index<m_mt.getCount())
						return MapUtil.getEsriPoint(m_mt.get(index).getPosition(),m_srs);  
					// remove measured offset
					index -= m_mt.getCount()-1;
					// return estimated
					return MapUtil.getEsriPoint(m_et.get(index).getPosition(),m_srs);  
				}
				else {
					// return estimated
					return MapUtil.getEsriPoint(m_et.get(index).getPosition(),m_srs); 				
				}
					
			}
			// use route point
			return m_polyline.getPoint(index);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return null;
	}	
	
	private int getLegIndex(long ete) {
		return m_et.find(ete,false);
	}
	
	private double getLegCost(int index) {
		return m_et.getDuration(index,index+1);
	}				
	
	private double getResidueCost(int index, double ete) {
		return ete - m_et.getReminder(index);
	}				
	
	private double getLegDistance(int index) {
		return m_et.getDistance(index,index+1,false);
	}				
	
	private TimePos getLegFromPoint(int index) {
		return m_et.get(index);
	}				
	
	private TimePos getLegToPoint(int index) {
		return m_et.get(index+1);
	}
	
	private double getLegBearing(int index) {
		return m_et.getBearing(index, index+1);
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
		h2 = queryAltitude(p);
		// Calculate slope?
		if (args.d > 0) {
			// calculate height difference
			double h = args.h - h2;
			// calculate
			s = Math.signum(h)*Math.toDegrees(Math.atan(Math.abs(h/args.d)));
		}
		
		// save current height
		args.h = h2;
		
		// finished
		return s;
	}
	
	private double queryAltitude(IPoint p) {
		// initialize
		int[] col = {0};
		int[] row = {0};
		try {
			// get pixel cell indexes
			m_altitude.mapToPixel(p.getX(), p.getY(), col, row);
			// get altitude
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
		return 0.0;
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
	 *  Get surface unit cost
	 *  
	 *  @param ss Snow state
	 *  @param rt Resistance type
	 *  @return Returns surface unit cost
	 */		
	private double getSurfaceCost(int ss, int su) {
		// update snow state
		m_arg_ps[1] = ss;
		// finished
		return m_su.getValue(su, m_arg_ps, 0, m_method);
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
			
			/* =========================================
			 * Upward Slope = [0,50] degrees
			 * ========================================= */
			
			// limit variable and return slope type index
			int st = (int)m_us.getIndex(s);		
			
			// finished
			return m_us.getValue(st, m_arg_ps, 0, m_method);
			
		}
		if(s <0 && s>=-15) {
			
			/* =========================================
			 * Easy Downward Slope = [-15,0> degrees
			 * ========================================= */
			
			// limit variable and return slope type index
			int st = (int)m_eds.getIndex(s);
			
			// finished
			return m_eds.getValue(st, m_arg_ps, 0, m_method);
			
		}
		else {
			
			/* =========================================
			 * Steep Downward Slope = [-50,-15> degrees
			 * ========================================= */
			
			// limit variable and return slope type index
			int st = (int)m_sds.getIndex(s);			
			
			// finished
			return m_sds.getValue(st, m_arg_ps, 0, m_method);
			
			
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
	 *  Get weather unit cost at position i
	 *  
	 *  @param t Time at position
	 *  @param i Current position  
	 *  @return Returns weather unit cost at position i
	 */		
	private double getWeatherCost(LegData args) {
		// get weather type
		int wt = getWeatherType(args.t,args.pd[0]);
		// finished
		return m_w.getValue(wt, m_arg_p, 0, m_method);
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
		// finished
		return m_li.getValue(lt, m_arg_p, 0, m_method);
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
			LightInfoPoint lf = m_info_li.get(t,p.x,p.y);
			// found?
			if(lf != null ){
				// translate to type
				lt = m_li.getWeightedVariable(lf.isCivilTwilight() ? 1 : 0);
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
		public double h;			// leg - altitude 
		public int prepare;			// leg - while prepare < offset, no leg stop points should be added to estimated track  
		
		public void init(int offset, Calendar t, double h, Point2D.Double pd, IPoint pe, int prepare) {
			// prepare
			this.offset = offset;
			this.index = offset;
			this.prepare = prepare;
			this.h = h;
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
			this.pd[0] = this.pd[1];
			this.pd[1] = pd2;
			this.pe[0] = this.pe[1];
			this.pe[1] = pe2;
		}
		
		public LegData clone() {
			// create a clone
			LegData args = new LegData();
			args.offset = this.offset;
			args.index = this.index;
			args.d = this.d;
			args.s = this.s;
			args.h = this.h;
			args.lc = this.lc;
			args.tlc = this.tlc;
			args.wlc = this.wlc;
			args.llc = this.llc;
			args.ete = this.ete;
			args.t = (Calendar)this.t.clone();
			args.pd = this.pd.clone(); 
			args.pe = this.pe.clone();
			args.prepare = this.prepare;  
			return args;
		}
		
	}

}