package org.redcross.sar.util.mso;

import java.util.Collection;
import java.util.Vector;

/**
 * Class for holding route information
 */
public class Route implements IGeodataIf, Cloneable
{
    private final String m_id;
    private String m_layout;

    private final String m_name;
    private final Vector<GeoPos> m_route;

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
        m_id = anId;
        m_name = aName;
        m_route = new Vector<GeoPos>();
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
        m_id = anId;
        m_name = aName;
        m_route = new Vector<GeoPos>(aSize);
    }

    /**
     * Add a new point to the route.
     *
     * @param aPosition The point to add.
     */
    public void add(GeoPos aPosition)
    {
        m_route.add(aPosition);
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
     * Get the collection of points in the route
     *
     * @return The GeoPos collection.
     */
    public Collection<GeoPos> getPositions()
    {
        return m_route;
    }

    /**
     * Get name of route
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

      Route route = (Route) o;

      if (m_id != null ? !m_id.equals(route.m_id) : route.m_id != null) return false;
      if (m_layout != null ? !m_layout.equals(route.m_layout) : route.m_layout != null) return false;
      if (m_name != null ? !m_name.equals(route.m_name) : route.m_name != null) return false;
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
      int result;
      result = (m_id != null ? m_id.hashCode() : 0);
      result = 31 * result + (m_layout != null ? m_layout.hashCode() : 0);
      result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
      result = 31 * result + (m_route != null ? m_route.hashCode() : 0);
      return result;
   }

    @Override
    public Object clone() throws CloneNotSupportedException
    { // todo test!!!!!
        super.clone();
        Route retVal = new Route(m_id,m_name);
        retVal.setLayout(m_layout);
        retVal.m_route.addAll(m_route);
        return retVal;
    }

}
