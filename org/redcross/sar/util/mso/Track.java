package org.redcross.sar.util.mso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

/**
 * Class for holding track information
 */
public class Track implements IGeodataIf, Cloneable
{
    private final String m_id;
    private String m_layout;

    private final String m_name;
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
        m_id = anId;
        m_name = aName;
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
        m_id = anId;
        m_name = aName;
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
    	if(bFlag)
    		Collections.sort(m_track);
    	return bFlag;
    }
    
    /**
     * Finds the index of equal time and position in collection
     * 
     * @param aTimePos
     * @return index of found position, else -1
     */
    public int find(TimePos aTimePos) {
    	System.out.println("find::start");
    	for(int i=0;i<m_track.size();i++) {
    		if(m_track.get(i).equals(aTimePos)) {
    	    	System.out.println("find::stop("+i+")");
    			return i;
    		}
    	}
    	System.out.println("find::stop(-1)");
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
    public Collection<TimePos> getTrackTimePos()
    {
        return m_track;
    }

    /**
     * Get name of track
     *
     * @return The name
     */
    public String getName()
    {
        return m_name;
    }

    public String getId()
    {
        return m_id;
    }

    public void setLayout(String aLayout)
    {
        m_layout = aLayout;
    }

    public String getLayout()
    {
        return m_layout;
    }

   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Track track = (Track) o;

      if (m_id != null ? !m_id.equals(track.m_id) : track.m_id != null) return false;
      if (m_layout != null ? !m_layout.equals(track.m_layout) : track.m_layout != null) return false;
      if (m_name != null ? !m_name.equals(track.m_name) : track.m_name != null) return false;
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
      int result;
      result = (m_id != null ? m_id.hashCode() : 0);
      result = 31 * result + (m_layout != null ? m_layout.hashCode() : 0);
      result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
      result = 31 * result + (m_track != null ? m_track.hashCode() : 0);
      return result;
   }

    @Override
    public Object clone() throws CloneNotSupportedException
    { // todo test!!!!!
        super.clone();
        Track retVal = new Track(m_id,m_name);
        retVal.setLayout(m_layout);
        retVal.m_track.addAll(m_track);
        return retVal;
    }
}
