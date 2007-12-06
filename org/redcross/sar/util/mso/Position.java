package org.redcross.sar.util.mso;

import java.awt.geom.Point2D;

/**
 *
 */
public class Position implements IGeodataIf
{
    private final String m_id;
    private String m_layout;
    private GeoPos m_position;

    /**
     * Constructor, no point defined.
     *
     * @param anId Object Id
     */

    public Position(String anId)
    {
        m_id = anId;
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
        m_id = anId;
        m_position = new GeoPos(aPosition);

    }

    /**
     * Create a position at a given long/lat
     *
     * @param anId  Object Id
     * @param aLong The point's longitude
     * @param aLat  The point's latitude
     */
    public Position(String anId, double aLong, double aLat)
    {
        m_id = anId;
        m_position = new GeoPos(aLong, aLat);
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

    /**
     * Set position at a point
     *
     * @param aPosition The point's coordinates
     */
    public void setPosition(Point2D.Double aPosition)
    {
        m_position.setPosition(aPosition);
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
    public int bearing(Position aPos)
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
    public static int bearing(Position aPos1, Position aPos2)
    {
        return GeoPos.bearing(aPos1.m_position, aPos2.m_position);
    }

   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Position position = (Position) o;

      if (m_id != null ? !m_id.equals(position.m_id) : position.m_id != null) return false;
      if (m_layout != null ? !m_layout.equals(position.m_layout) : position.m_layout != null) return false;
      if (m_position != null ? !m_position.equals(position.m_position) : position.m_position != null) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (m_id != null ? m_id.hashCode() : 0);
      result = 31 * result + (m_layout != null ? m_layout.hashCode() : 0);
      result = 31 * result + (m_position != null ? m_position.hashCode() : 0);
      return result;
   }
}
