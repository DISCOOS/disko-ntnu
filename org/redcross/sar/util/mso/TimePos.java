package org.redcross.sar.util.mso;

import java.awt.geom.Point2D;
import java.util.Calendar;

import org.redcross.sar.util.Utils;

/**
 * Class for handling a position object with time
 */
public class TimePos extends GeoPos implements Comparable<TimePos>, Cloneable
{
    private final Calendar m_time;

    public TimePos()
    {
        super();
        m_time = Calendar.getInstance();
    }

    public TimePos(Calendar aCalendar)
    {
        super();
        m_time = createTime(aCalendar);
    }

    public TimePos(Position aPosition, Calendar aCalendar)
    {
        super();
        // set position?
        if(aPosition!=null)
        	setPosition(aPosition.getPosition());
        m_time = createTime(aCalendar);
    }

    public TimePos(Point2D.Double aPosition, Calendar aCalendar)
    {
        super(aPosition);
        m_time = createTime(aCalendar);
    }

    public TimePos(Point2D.Double aPosition, double anAltitude, Calendar aCalendar)
    {
        super(aPosition,anAltitude);
        m_time = createTime(aCalendar);
    }

    public TimePos(double aLongPosition, double aLatPosition, Calendar aCalendar)
    {
        super(aLongPosition, aLatPosition);
        m_time = createTime(aCalendar);
    }

    public TimePos(double aLongPosition, double aLatPosition, double anAltitude, Calendar aCalendar)
    {
        super(aLongPosition, aLatPosition, anAltitude);
        m_time = createTime(aCalendar);
    }

    private Calendar createTime(Calendar aCalendar) {
    	return aCalendar!=null ? (Calendar)aCalendar.clone() : Calendar.getInstance();
    }

    public String getDTG()
    {
        return DTG.CalToDTG(m_time);
    }

    /**
     * Calculate difference to another TimePos object
     *
     * @param TimePos aTimePos - The other position.
     * @return Difference in seconds. Is negative if aTimePos is after this value
     */
    public double timeSince(TimePos aTimePos)
    {
        return (m_time.getTimeInMillis() - aTimePos.m_time.getTimeInMillis()) / 1000;
    }

    /**
     * Calculate average speed in m/s along a line to another TimePos
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

    public GeoPos getGeoPos()
    {
        return new GeoPos(getPosition());
    }

    public boolean equals(Object obj)
    {

    	if(!super.equals(obj)) return false;

    	TimePos in = (TimePos) obj;

        if (m_time != null ? !m_time.equals(in.m_time) : in.m_time != null)
        {
            return false;
        }

        return true;

    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (m_time != null ? m_time.hashCode() : 0);
        return result;
    }

    public TimePos clone() {
    	return new TimePos(getPosition(),getTime());
    }

    @Override
    public String toString() {
    	return super.toString() + " " + Utils.toString(getTime());
    }
}
