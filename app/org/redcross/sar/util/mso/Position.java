package org.redcross.sar.util.mso;

import java.awt.geom.Point2D;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.data.IMsoDataIf.MsoDataType;

/**
 *
 */
public class Position extends AbstractGeodata
{

    private int m_type;
    private GeoPos m_position;

    /**
     * Constructor, no point defined.
     *
     * @param anId Object Id
     */

    public Position(String anId)
    {
    	super(anId);
        m_position = new GeoPos();
    }

    /**
     * Create a position at a point.
     *
     * @param anId      Object Id
     * @param aPosition The point's coordinates
     */
    public Position(String anId, Point2D.Double aPosition)
    {
    	super(anId);
    	m_type = 1;
        m_position = new GeoPos(aPosition);

    }

    /**
     * Create a position at a point.
     *
     * @param anId      Object Id
     * @param aPosition The point's coordinates
     * @param aType The position type
     */
    public Position(String anId, Point2D.Double aPosition, int aType)
    {
    	super(anId);
    	m_type = aType;
        m_position = new GeoPos(aPosition);

    }

    /**
     * Create a position at a given long/lat
     *
     * @param anId  Object Id
     * @param aLong The point's longitude
     * @param aLat  The point's latitude
     * @param aType The position type
     */
    public Position(String anId, double aLong, double aLat)
    {
    	super(anId);
    	m_type = 1;
        m_position = new GeoPos(aLong, aLat);
    }

    /**
     * Create a position at a given long/lat
     *
     * @param anId  Object Id
     * @param aLong The point's longitude
     * @param aLat  The point's latitude
     * @param aType The position type
     */
    public Position(String anId, double aLong, double aLat, int aType)
    {
    	super(anId);
    	m_type = aType;
        m_position = new GeoPos(aLong, aLat);
    }
    
    @Override
	public MsoDataType getDataType() {
		return MsoDataType.POSITION;
	}

	@Override
	public GeoClassCode getClassCode() {
		return GeoClassCode.CLASSCODE_POSITION;
	}
	
    public int getType() {
    	return m_type;
    }

    public void setType(int type) {
    	m_type = type;
        incrementChangeCount();
    }

    /**
     * Set position at a point
     *
     * @param aPosition The point's coordinates
     */
    public void setPosition(Point2D.Double aPosition)
    {
        m_position.setPosition(aPosition);
        incrementChangeCount();
    }

    /**
     * Set position at a given long/lat
     *
     * @param aLong The point's longitude
     * @param aLat  The point's latitude
     */
    public void setPosition(double aLong, double aLat)
    {
        m_position.setPosition(aLong, aLat);
        incrementChangeCount();
    }

    /**
     * Get position as a point
     */
    public Point2D.Double getPosition()
    {
        return m_position.getPosition();
    }

    /**
     * Calculate distance to another position.
     *
     * @param aPos The other position.
     * @return The distance (in kilometers)
     */
    public double distance(Position aPos)
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
    public static double distance(Position aPos1, Position aPos2)
    {
        return GeoPos.distance(aPos1.m_position, aPos2.m_position);
    }

    /**
     * Calculate bearing (in degrees) to another position.
     *
     * @param aPos The other position.
     * @return The bearing (in degrees)
     */
    public double bearing(Position aPos)
    {
        return bearing(this, aPos);
    }

    /**
     * Return position as GeoPos
     *
     */
    public GeoPos getGeoPos() {
    	return m_position;
    }

    /**
     * Calculate bearing between two positions.
     *
     * @param aPos1 The first position.
     * @param aPos2 The other position.
     * @return The bearing (in degrees)
     */
    public static double bearing(Position aPos1, Position aPos2)
    {
        return GeoPos.bearing(aPos1.m_position, aPos2.m_position);
    }

   public boolean equals(Object o)
   {
	  if (!super.equals(o)) return false;

      Position position = (Position) o;

      if (m_position != null ? !m_position.equals(position.m_position) : position.m_position != null) return false;

      return true;
   }

   public int hashCode()
   {
	  int result = super.hashCode();
      result = 31 * result + (m_position != null ? m_position.hashCode() : 0);
      return result;
   }

   	@Override
	public Position clone() throws CloneNotSupportedException {
		return new Position(m_id,m_position.getPosition(),m_type);
	}

	@Override
	public int compareTo(IData data) {
		if(data instanceof Position) {
			if(equals(data)) return 0;
			else return (int)distance((Position)data);
		}
		return -1;
	}	

}
