package org.redcross.sar.util.mso;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

/**
 * Class for holding track information
 */
public class Track extends AbstractGeodata
{

    private final ArrayList<TimePos> m_track;

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
        add(new TimePos(aPosition.x, aPosition.y, aCalendar));
    }
    
    /**
     * Add a new point to the track.
     * Calls {@link #add(TimePos)} .
     *
     * @param aLongPosition
     * @param aLatPosition
     * @param aCalendar
     */
    public void add(double aLongPosition, double aLatPosition, Calendar aCalendar)
    {
        add(new TimePos(aLongPosition, aLatPosition, aCalendar));
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
    public int getCount() {
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
        
}
