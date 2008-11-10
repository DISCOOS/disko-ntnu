package org.redcross.sar.util.mso;

import java.awt.geom.Point2D;

import org.redcross.sar.map.MapUtil;

/**
 *
 */
public class GeoPos implements Cloneable
{

    /* ===================================================================
     * POSITION ACCURACY ISSUE
     * ===================================================================
     *
     * When converting between MSO and GEO represented positions, the
     * coordinate precision is reduced. For each MSO->GEO->MSO, or
     * GEO->MSO->GEO conversion, the distance between the initial
     * coordinates and the final coordinates will increase. The average
     * increase in error distance per conversion loop is 8.5E-6 meters
     * (0.0084 millimeter).
     *
     * For safety reasons it is defined to be 1.0E-5 meters,
     * or 0.1 millimeter.
     *
     * Consequently, after X conversion loops, the position will be
     * moved about X*1.0E-5 meters from the original position. For
     * example, after 100 conversion loops, the position will be off
     * by 1 centimeter. Hence, the number of conversion loops should
     * be minimized when possible. In most cases, accuracy degradation
     * will not create a problem because the potential number of conversion
     * loops is relative low for the average time a position is liable
     * to be edited.
     *
     * Another consequence of this is that two equal positions can not be
     * assumed to have numerical equal coordinates. Equivalence is therefore
     * not found by comparing each coordinate directly. Instead,
     * two positions are defined equal if the distance between these two
     * is less than a given distance. This distance is denoted the maximum
     * equality distance and defined to be 1 centimeter. Hence, positions
     * closer than 1 centimeter is defined to equal. Furthermore, this
     * ensures that more than 100 conversion loops must be executed
     * before the initial and final coordinates represents two different
     * positions. This should be more than sufficient for the current
     * applications of GeoPos representation. Any accuracy greater than
     * one 1 meter is well beyond the accuracy of coordinates given by
     * user-input (numeric or by clicking on a map), automatic positioning
     * and logging (tracking), or any requirements of position and distance
     * calculations.
     *
     * =================================================================== */

	private final static double AVERAGE_ERROR_DISTANCE = 1E-5;								// 0.1 millimeter

    private final static double MAX_EQUALITY_DISTANCE = AVERAGE_ERROR_DISTANCE * 100;		// 1.0 centimeter


    private double m_altitude = 0.0;
	private Point2D.Double m_position = null;


    /**
     * Create a position with no point
     */
    public GeoPos()
    {
        setPosition(null);
    }

    /**
     * Create a position at a point
     *
     * @param aPosition The point's coordinates
     */
    public GeoPos(Point2D.Double aPosition)
    {
        setPosition(aPosition);
    }

    /**
     * Create a position at a point with given altitude
     *
     * @param aPosition The point's coordinates
     * @param anAltitude The point's altitude
     */
    public GeoPos(Point2D.Double aPosition, double anAltitude)
    {
        setPosition(aPosition);
        setAltitude(anAltitude);
    }

    /**
     * Create a position at a given long/lat
     *
     * @param aLong The point's longitude
     * @param aLat  The point's latitude
     */
    public GeoPos(double aLong, double aLat)
    {
        setPosition(aLong, aLat);
    }

    /**
     * Create a position at a given long/lat and altitude
     *
     * @param aLong The point's longitude
     * @param aLat  The point's latitude
     * @param aAlt The point's altitude
     */
    public GeoPos(double aLong, double aLat, double aAlt)
    {
        setPosition(aLong, aLat);
        setAltitude(aAlt);
    }

    /**
     * Get position as a point
     */
    public Point2D.Double getPosition()
    {
        return m_position;
    }

    /**
     * Set position at a point
     *
     * @param aPosition The point's coordinates
     */
    public void setPosition(Point2D.Double aPosition)
    {
    	if(aPosition!=null)
    		m_position = (Point2D.Double) aPosition.clone();
    	else
    		m_position = null;
    }

    /**
     * Set position at a given long/lat
     *
     * @param aLong The point's longitude
     * @param aLat  The point's latitude
     */
    public void setPosition(double aLong, double aLat)
    {
        m_position = new Point2D.Double(aLong, aLat);
    }

    /**
     * Get altitude at point (in meters)
     */
    public double getAltitude()
    {
        return m_altitude;
    }

    /**
     * Set altitude at point (in meters)
     */
    public void setAltitude(double anAltitude)
    {
        m_altitude = anAltitude;
    }


    /**
     * Calculate distance to another position.
     *
     * @param aPos The other position.
     * @return The distance (in meters)
     */
    public double distance(GeoPos aPos)
    {
        return distance(this, aPos);
    }

    /**
     * Calculate distance between two positions.
     *
     * @param aPos1 The first position.
     * @param aPos2 The other position.
     * @return The distance (in kilometers)
     */
    public static double distance(GeoPos aPos1, GeoPos aPos2)
    {
        return MapUtil.greatCircleDistance(
        		aPos1.m_position.y, aPos1.m_position.x,
        		aPos2.m_position.y, aPos2.m_position.x);
    }

    /**
     * Calculate bearing (in degrees) to another position.
     *
     * @param aPos The other position.
     * @return The bearing (in degrees)
     */
    public double bearing(GeoPos aPos)
    {
        return bearing(this, aPos);
    }

    /**
     * Calculate bearing between two positions.
     *
     * @param aPos1 The first position.
     * @param aPos2 The other position.
     * @return The bearing (in degrees)
     */
    public static double bearing(GeoPos aPos1, GeoPos aPos2)
    {
        return MapUtil.sphericalAzimuth(
        		aPos1.m_position.y, aPos1.m_position.x,
        		aPos2.m_position.y, aPos2.m_position.x);
    }

    /**
     * Two positions are considered to be equal if their distance (lat/long) is less than {@link #MAX_EQUALITY_DISTANCE}
     */
    @Override
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

        GeoPos in = (GeoPos) obj;

        return distance(in)<MAX_EQUALITY_DISTANCE;
    }

    /*
    protected boolean floatEquals(Point2D.Double aPoint)
    {
        return MapUtil.isFloatEqual(m_position, aPoint);

        (m_position != null ?
                (aPoint != null && (float)m_position.x == (float)aPoint.x && (float)m_position.y == (float)aPoint.y) :
                aPoint != null);
    }
	*/

    public int hashCode()
    {
        int result = 0;
        if (m_position != null)
        {
            result = 51 * result + Float.floatToIntBits((float) m_position.x);
            result = 51 * result + Float.floatToIntBits((float) m_position.y);
        }
        return result;
    }

    public GeoPos clone() {
    	return new GeoPos(getPosition());
    }

    @Override
    public String toString() {
    	return m_position.x + "E " + m_position.y +"N";
    }

}
