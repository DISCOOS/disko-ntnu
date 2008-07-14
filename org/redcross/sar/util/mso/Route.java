package org.redcross.sar.util.mso;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Vector;

import org.redcross.sar.map.MapUtil;

/**
 * Class for holding route information
 */
public class Route extends AbstractGeodata
{
    private final Vector<GeoPos> m_route;
    private final Vector<Double> m_distance;
    
    private int m_created = 0;

    /**
     * Constructor, default collection size
     *
     * @param anId Object Id
     */
    public Route(String anId)
    {
        this(anId, "");
    }

    /**
     * Constructor, default collection size
     *
     * @param anId  Object Id
     * @param aName Name of route
     */
    public Route(String anId, String aName)
    {
        super(anId,aName);
        m_route = new Vector<GeoPos>();
        m_distance = new Vector<Double>();
    }

    /**
     * Constructor, parameter for collection size
     *
     * @param anId  Object Id
     * @param aName Name of route
     * @param aSize The collection size
     */
    public Route(String anId, String aName, int aSize)
    {
        super(anId,aName);
        m_route = new Vector<GeoPos>(aSize);
        m_distance = new Vector<Double>();
    }

    /**
     * Add a new point to the route.
     *
     * @param aPosition The point to add.
     */
    public void add(GeoPos aPosition)
    {
        m_route.add(aPosition);
        incrementChangeCount();
    }

    /**
     * Add a new point to the route.
     *
     * @param aLongPosition The point's longitude
     * @param aLatPosition  The point's latitude
     */
    public void add(double aLongPosition, double aLatPosition)
    {
        add(new GeoPos(aLongPosition, aLatPosition));
    }

    /**
     * Remove a point from track 
     * 
     * @param aTimePos
     * @return
     */
    public boolean remove(GeoPos aGeoPos) {
    	boolean bFlag = m_route.remove(aGeoPos);
    	if(bFlag) {
    		incrementChangeCount();
    	}
    	return bFlag;
    }
    
    /**
     * Finds the index of equal position in collection
     * 
     * @param aGeoPos
     * @return index of found position, else -1
     */
    public int find(GeoPos aGeoPos) {
    	//System.out.println("find::start");
    	for(int i=0;i<m_route.size();i++) {
    		if(m_route.get(i).equals(aGeoPos)) {
    	    	//System.out.println("find::stop("+i+")");
    			return i;
    		}
    	}
    	//System.out.println("find::stop(-1)");
    	// found
    	return -1;
    }
    
    /**
     * Get position from index
     * 
     * @param index
     * @return
     */
    public GeoPos get(int index) {
    	return m_route.get(index);
    }

    /**
     * Set position at index
     * 
     * @param index
     * @param Point2D.Double aPosition - the new position
     * @return
     */    
    public boolean set(int index,Point2D.Double aPosition) {
    	if(index>0 && index<m_route.size()) {
	    	GeoPos p = m_route.get(index);
	    	p.setPosition(aPosition);
	    	incrementChangeCount();
	    	return true;
    	}
    	return false;
    }
    
    /**
     * 
     * @return number of GeoPos
     */
    public int getCount() {
    	return m_route.size();
    }
    
    /**
     * Get the collection of points in the route
     *
     * @return The GeoPos collection.
     */
    public Collection<GeoPos> getItems()
    {
        return m_route;
    }

   public boolean equals(Object o)
   {
	      if (!super.equals(o)) return false;

      Route route = (Route) o;

      if (m_route != null )
      {
         if(route.m_route==null || m_route.size()!=route.m_route.size() ) return false;
         for(int i=0;i<m_route.size();i++)
         {
            if(!m_route.get(i).equals(route.m_route.get(i)))return false;
         }

      }
      else if(route.m_route!=null) return false;


      return true;
   }

   public int hashCode()
   {
	  int result = super.hashCode();
      result = 31 * result + (m_route != null ? m_route.hashCode() : 0);
      return result;
   }

    @Override
    public Route clone() throws CloneNotSupportedException
    {  
       Route retVal = new Route(m_id,m_name);
       retVal.setLayout(m_layout);
       retVal.m_route.addAll(m_route);
       return retVal;
    }
    
    public void addAll(Route r) {
    	m_route.addAll(r.m_route);
        incrementChangeCount();
    }
    
    public void removeAll(Route r) {
    	m_route.removeAll(r.m_route);
    	incrementChangeCount();
    }
    
	public GeoPos getStartPoint() {
		return m_route.get(0);
	}

	public GeoPos getStopPoint() {
		return m_route.get(m_route.size()-1);
	}
	
	public double getDistance() {
		return getDistance(0,m_route.size()-1,false);
	}
	
	public double getDistance(int index) {
		return getDistance(0,index,false);
	}
	
	public double getDistance(int from, int to, boolean direct) {
		if(m_created!=m_changeCount) create();
		int uBound = m_route.size()-1;
		if(m_route.size()==0 || from>uBound || to>uBound)
			return 0.0;
		else if(direct){
			return m_route.get(to).distance(m_route.get(from));			
		}
		else {
			return m_distance.get(to) - m_distance.get(from);
		}
	}
	
	public double getBearing() {
		return getBearing(0,m_route.size()-1);
	}
	
	public double getBearing(int index) {
		return getBearing(0,index);
	}
	
	/**
	 * Bearing between two points 
	 * @param from
	 * @param to
	 * @return - bearing (in degrees)
	 */
	public double getBearing(int from, int to) {
		int uBound = m_route.size()-1;
		if(m_route.size()==0 || from>uBound || to>uBound)
			return 0.0;
		else 
			return m_route.get(to).bearing(m_route.get(from));
		
	}	
	
	private void create() {
		m_distance.clear();
		m_distance.add(0.0);
		if(m_route.size()>1) {
			double d = 0.0;
			Point2D.Double p1 = m_route.get(0).getPosition();
			Point2D.Double p2 = null;
			int count = m_route.size();
			for(int i=1;i<count;i++) {
				p2 = m_route.get(i).getPosition();
				d += MapUtil.greatCircleDistance(p1.y, p1.x, p2.y, p2.x);
				m_distance.add(d);
				p1 = p2;
			}
		}
		m_created = m_changeCount;
	}

}
