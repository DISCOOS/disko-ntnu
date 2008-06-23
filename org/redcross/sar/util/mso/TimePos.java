package org.redcross.sar.util.mso;

import java.awt.geom.Point2D;
import java.util.Calendar;

import org.redcross.sar.map.MapUtil;

/**
 * Class for handling a position object with time
 */
public class TimePos extends GeoPos implements Comparable<TimePos>
{
    private final Calendar m_time;

    public TimePos()
    {
        super();
        m_time = null;
    }

    public TimePos(Calendar aCalendar)
    {
        super();
        m_time = aCalendar;
    }

    public TimePos(Position aPosition, Calendar aCalendar)
    {
        super();
        // set position?
        if(aPosition!=null)
        	setPosition(aPosition.getPosition());
        m_time = aCalendar;
    }
    
    public TimePos(Point2D.Double aPosition, Calendar aCalendar)
    {
        super(aPosition);
        m_time = aCalendar;
    }

    public TimePos(double aLongPosition, double aLatPosition, Calendar aCalendar)
    {
        super(aLongPosition, aLatPosition);
        m_time = aCalendar;
    }

    public String getDTG()
    {
        return DTG.CalToDTG(m_time);
    }

    /**
     * Calculate difference to another TimePos object
     *
     * @param aTimePos The other value.
     * @return Difference in hours. Is negative if aTimePos is after this value
     */
    public double timeSince(TimePos aTimePos)
    {
        return (m_time.getTimeInMillis() - aTimePos.m_time.getTimeInMillis()) / (1000 * 3600);
    }

    /**
     * Calculate average speed in km/h along a line to another TimePos
     *
     * @param aTimePos The other point
     * @return Average speed, set to 0 if time difference is 0.
     */
    public double speed(TimePos aTimePos)
    {
        double time = Math.abs(timeSince(aTimePos));
        double distance = distance(aTimePos);
        if (time > 0)
        {
            return distance / time;
        }
        return 0;
    }

    public int compareTo(TimePos aTimePos)
    {
        return m_time.compareTo(aTimePos.m_time);
    }

    public Calendar getTime()
    {
        return m_time;
    }

    public boolean equals(Object obj)
    {
    	
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }

        TimePos in = (TimePos) obj;

        if (m_time != null ? !m_time.equals(in.m_time) : in.m_time != null)
        {
            return false;
        }

        boolean bFlag = floatEquals(in.getPosition());        

        return bFlag; 
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (m_time != null ? m_time.hashCode() : 0);
        return result;
    }
}
