package org.redcross.sar.util.mso;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.redcross.sar.map.MapUtil;

/**
 * Class for holding track information
 */
public class Track extends AbstractGeodata
{

    private final List<TimePos> m_track;
    private final Vector<Double> m_distance;

    private int m_created = 0;

    /**
     * Constructor, default collection size
     *
     * @param anId Object Id
     */
    public Track(String anId)
    {
        this(anId, "");
    }

    /**
     * Constructor, default collection size
     *
     * @param anId  Object Id
     * @param aName Name of track
     */
    public Track(String anId, String aName)
    {
        super(anId, aName);
        m_track = new ArrayList<TimePos>();
        m_distance = new Vector<Double>();
    }

    /**
     * Constructor, parameter for collection size
     *
     * @param anId  Object Id
     * @param aName Name of track
     * @param aSize The collection size
     */
    public Track(String anId, String aName, int aSize)
    {
        super(anId, aName);
        m_track = new ArrayList<TimePos>(aSize);
        m_distance = new Vector<Double>();
    }

    /**
     * Add a new point to the track.
     * After adding, the collection is sorted according to time.
     *
     * @param aTimePos The point to add.
     */
    public void add(TimePos aTimePos)
    {
        m_track.add(aTimePos);
        Collections.sort(m_track);
        incrementChangeCount();
    }

    /**
     * Add a new point to the track.
     * Calls {@link #add(TimePos)} .
     *
     * @param aPosition
     * @param aCalendar
     */
    public void add(Point2D.Double aPosition, Calendar aCalendar)
    {
        add(new TimePos(aPosition, aCalendar));
    }

    /**
     * Add a new point to the track.
     * Calls {@link #add(TimePos)} .
     *
     * @param aPosition
     * @param anAltitude
     * @param aCalendar
     */
    public void add(Point2D.Double aPosition, double anAltitude, Calendar aCalendar)
    {
        add(new TimePos(aPosition, anAltitude, aCalendar));
    }

    /**
     * Add a new point to the track.
     * Calls {@link #add(TimePos)} .
     *
     * @param aLon
     * @param aLat
     * @param aCalendar
     */
    public void add(double aLon, double aLat, Calendar aCalendar)
    {
        add(new TimePos(aLon, aLat, aCalendar));
    }

    /**
     * Remove a point from track
     *
     * @param aTimePos
     * @return
     */
    public boolean remove(TimePos aTimePos) {
    	boolean bFlag = m_track.remove(aTimePos);
    	if(bFlag) {
    		incrementChangeCount();
    	}
    	return bFlag;
    }

    /**
     * Finds the index of equal time and position in collection
     *
     * @param aTimePos
     * @return index of found position, else -1
     */
    public int find(TimePos aTimePos) {
    	//System.out.println("find::start");
    	for(int i=0;i<m_track.size();i++) {
    		if(m_track.get(i).equals(aTimePos)) {
    	    	//System.out.println("find::stop("+i+")");
    			return i;
    		}
    	}
    	//System.out.println("find::stop(-1)");
    	// found
    	return -1;
    }

    /**
     * Finds the index of the GeoPos nearest in distance to passed GeoPos. Time is not considered.
     *
     * @param aGeoPos - the position to match
     * @param max - the maximum distance allowed from track. Pass <code>-1</code> if no limit
     *
     * @return The index of nearest position. If no position is located closer than
     * <code>max</code> from <code>aGeoPos</code>, <code>-1</code> will be returned.
     */
    public int nearest(GeoPos aGeoPos, double max) {
    	int index = -1;
    	double min = Double.MAX_VALUE;
    	//System.out.println("find::start");
    	for(int i=0;i<m_track.size();i++) {
    		// get distance
    		double d = m_track.get(i).distance(aGeoPos);
    		// limit?
    		if(max==-1 || max>d) {
    			// is nearer than last found?
    			if(d<min) {
    				index = i;
    				min = d;
    			}
    		}
    	}
    	//System.out.println("nearest::stop(-1)");
    	// finished
    	return index;
    }

    /**
     * Get position from index
     *
     * @param index
     * @return
     */
    public TimePos get(int index) {
    	return m_track.get(index);
    }

    /**
     * Set position at index
     *
     * @param index
     * @param Point2D.Double aPosition - the new position
     * @return
     */
    public boolean set(int index,Point2D.Double aPosition) {
    	if(index>0 && index<m_track.size()) {
	    	TimePos p = m_track.get(index);
	    	p.setPosition(aPosition);
	    	incrementChangeCount();
	    	return true;
    	}
    	return false;
    }

    /**
     *
     * @return number of TimePos
     */
    public int size() {
    	return m_track.size();
    }

    /**
     * Get the collection of points in the track
     *
     * @return The TimePos collection.
     */
    public Collection<TimePos> getItems()
    {
        return m_track;
    }

   public boolean equals(Object o)
   {
	  if (!super.equals(o)) return false;

      Track track = (Track) o;

      if (m_track != null )
      {
         if(track.m_track==null || m_track.size()!=track.m_track.size() ) return false;
         for(int i=0;i<m_track.size();i++)
         {
            if(!m_track.get(i).equals(track.m_track.get(i)))return false;
         }

      }
      else if(track.m_track!=null) return false;

      return true;
   }

   public int hashCode()
   {
	  int result = super.hashCode();
      result = 31 * result + (m_track != null ? m_track.hashCode() : 0);
      return result;
   }

    @Override
    public Track clone() throws CloneNotSupportedException
    {
        Track retVal = new Track(m_id,m_name);
        retVal.setLayout(m_layout);
        retVal.m_track.addAll(m_track);
        return retVal;
    }

    public void addAll(Track t) {
    	m_track.addAll(t.m_track);
    	incrementChangeCount();
    }

    public void removeAll(Track t) {
    	m_track.removeAll(t.m_track);
    	incrementChangeCount();
    }

    public void removeRange(int from, int to) {
    	List<TimePos> range = m_track.subList(from, to);
    	m_track.removeAll(range);
    	incrementChangeCount();
    }

	public TimePos getStartPoint() {
		if(m_track.size()>0)
			return m_track.get(0);
		else
			return null;
	}

	public TimePos getStopPoint() {
		if(m_track.size()>0)
			return m_track.get(m_track.size()-1);
		else
			return null;
	}

	public double getDistance() {
		return getDistance(0,m_track.size()-1,false);
	}

	public double getDistance(int index) {
		return getDistance(0,index,false);
	}

	public double getDistance(int from, int to, boolean direct) {
		int uBound = m_track.size()-1;
		if(m_track.size()==0 || from>uBound || to>uBound || from==to)
			return 0.0;
		else if(direct){
			return m_track.get(to).distance(m_track.get(from));
		}
		else {
			if(m_created!=m_changeCount || m_distance.size()!=uBound) create();
			return m_distance.get(to) - m_distance.get(from);
		}
	}

	/**
	 * Get duration from start to stop
	 *
	 * @return Duration (seconds)
	 */
	public double getDuration() {
		return getDuration(0,m_track.size()-1);
	}

	/**
	 * Get duration from start to given point
	 *
	 * @return Duration (seconds)
	 */
	public double getDuration(int index) {
		return getDuration(0,index);
	}

	/**
	 * Get duration between two given points
	 *
	 * @return Duration (seconds)
	 */
	public double getDuration(int from, int to) {
		int uBound = m_track.size()-1;
		if(m_track.size()==0 || from>uBound || to>uBound || from==to)
			return 0.0;
		else
			return m_track.get(to).timeSince(m_track.get(from));
	}

	/**
	 * Get time between given point and stop point
	 *
	 * @return Reminder (seconds)
	 */
	public double getReminder(int index) {
		return getDuration(index,m_track.size()-1);
	}

	/**
	 * Get average speed between start and stop points
	 *
	 * @return Speed (m/s)
	 */
	public double getSpeed() {
		return getSpeed(0,m_track.size()-1,false);
	}

	/**
	 * Get average speed between start and given point
	 *
	 * @return Speed (m/s)
	 */
	public double getSpeed(int index) {
		return getSpeed(0,index,false);
	}

	/**
	 * Average speed between two points
	 * @param from - from point
	 * @param to - to point
	 * @param direct - if <code>true</code>, the direct line between the points are used,
	 * else the distance along the track is used.
	 * @return - speed (m/s)
	 */
	public double getMaximumSpeed(int from, int to, boolean direct) {
		int uBound = m_track.size()-1;
		if(m_track.size()==0 || from>uBound || to>uBound || from==to)
			return 0.0;
		else {
			double max = 0;
			for(int i=from ; i<to; i++) {
				double d = getDistance(i,i+1,direct);
				double t = getDuration(i,i+1);
				max = Math.max(t>0 ? d/t:0.0,max);
			}
			return max;
		}
	}

	/**
	 * Get minimum leg speed between start and stop points
	 *
	 * @return Speed (m/s)
	 */
	public double getMinimumSpeed() {
		return getMinimumSpeed(0,m_track.size()-1,false);
	}

	/**
	 * Get minimum leg speed between start and given point
	 *
	 * @return Speed (m/s)
	 */
	public double getMinimumSpeed(int index) {
		return getMinimumSpeed(0,index,false);
	}

	/**
	 * Minimum leg speed between two points
	 * @param from - from point
	 * @param to - to point
	 * @param direct - if <code>true</code>, the direct line between the points are used,
	 * else the distance along the track is used.
	 * @return - speed (m/s)
	 */
	public double getMinimumSpeed(int from, int to, boolean direct) {
		int uBound = m_track.size()-1;
		if(m_track.size()==0 || from>uBound || to>uBound  || from==to)
			return 0.0;
		else {
			double min = Integer.MAX_VALUE;
			for(int i=from ; i<to; i++) {
				double d = getDistance(i,i+1,direct);
				double t = getDuration(i,i+1);
				min = Math.min(t>0 ? d/t:0.0,min);
			}
			return min;
		}
	}

	/**
	 * Get maximum leg speed between start and stop points
	 *
	 * @return Speed (m/s)
	 */
	public double getMaximumSpeed() {
		return getMaximumSpeed(0,m_track.size()-1,false);
	}

	/**
	 * Get maximum leg speed between start and given point
	 *
	 * @return Speed (m/s)
	 */
	public double getMaximumSpeed(int index) {
		return getMaximumSpeed(0,index,false);
	}

	/**
	 * Maximum leg speed between two points
	 * @param from - from point
	 * @param to - to point
	 * @param direct - if <code>true</code>, the direct line between the points are used,
	 * else the distance along the track is used.
	 * @return - speed (m/s)
	 */
	public double getSpeed(int from, int to, boolean direct) {
		int uBound = m_track.size()-1;
		if(m_track.size()==0 || from>uBound || to>uBound || from==to)
			return 0.0;
		else {
			double d = getDistance(from,to,direct);
			double t = getDuration(from,to);
			return (t>0 ? d/t:0.0);
		}
	}


	/**
	 * Get bearing from start to stop point
	 *
	 * @return Bearing (degrees)
	 */
	public double getBearing() {
		return getBearing(0,m_track.size()-1);
	}

	/**
	 * Get bearing from start to given point
	 *
	 * @return Bearing (degrees)
	 */
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
		int uBound = m_track.size()-1;
		if(m_track.size()==0 || from>uBound || to>uBound || from==to)
			return 0.0;
		else
			return m_track.get(to).bearing(m_track.get(from));

	}

	/**
	 * Find the maximum track point index at which duration from start is less or equal to requested reminder
	 *
	 * @param time - duration from start to found track point, or reminder from found
	 * track point to end of track (in seconds)
	 * @param duration - if <code>true</code> search for the duration, else search for the reminder
	 *
	 * @return - index of found track point
	 */
	public int find(double time, boolean duration) {
		int i = 0;
		int count = m_track.size();
		// search for duration?
		if(duration) {
			if(time==0) return 0;
			TimePos p0 = m_track.get(0);
			for(i=1;i<count;i++) {
				if(m_track.get(i).timeSince(p0)>time)
					return i-1;
			}
			// finished
			return i-1;
		}
		// search for reminder
		int uBound = count-1;
		if(time==0) return uBound;
		TimePos pN = m_track.get(uBound);
		for(i=uBound;i>=0;i--) {
			if(pN.timeSince(m_track.get(i))>time)
				return i;
		}
		// finished
		return i;
	}

	private void create() {
		m_distance.clear();
		m_distance.add(0.0);
		if(m_track.size()>1) {
			double d = 0.0;
			Point2D.Double p1 = m_track.get(0).getPosition();
			Point2D.Double p2 = null;
			int count = m_track.size();
			for(int i=1;i<count;i++) {
				p2 = m_track.get(i).getPosition();
				d += MapUtil.greatCircleDistance(p1.y, p1.x, p2.y, p2.x);
				m_distance.add(d);
				p1 = p2;
			}
		}
		m_created = m_changeCount;
	}

	public void clear() {
		m_track.clear();
		m_distance.clear();
	}

}
