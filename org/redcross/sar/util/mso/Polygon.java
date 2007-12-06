package org.redcross.sar.util.mso;

import java.util.Collection;
import java.util.Vector;

/**
 * Class for holding polygon information
 */
public class Polygon implements IGeodataIf, Cloneable
{
    private final String m_id;
    private String m_layout;

    private final String m_name;
    private final Vector<GeoPos> m_polygon;

    /**
     * Constructor, default collection size
     *
     * @param anId Object Id
     */
    public Polygon(String anId)
    {
        this(anId, "");
    }

    /**
     * Constructor, default collection size
     *
     * @param anId  Object Id
     * @param aName Name of route
     */
    public Polygon(String anId, String aName)
    {
        m_id = anId;
        m_name = aName;
        m_polygon = new Vector<GeoPos>();
    }

    /**
     * Constructor, parameter for collection size
     *
     * @param anId  Object Id
     * @param aName Name of route
     * @param aSize The collection size
     */
    public Polygon(String anId, String aName, int aSize)
    {
        m_id = anId;
        m_name = aName;
        m_polygon = new Vector<GeoPos>(aSize);
    }

    /**
     * Add a new vertex to the polygon.
     *
     * @param aVertex The vertex to add.
     */
    public void add(GeoPos aVertex)
    {
        m_polygon.add(aVertex);
    }

    /**
     * Add a new vertex to the route.
     *
     * @param aLongPosition The vertex' longitude
     * @param aLatPosition  The vertex' latitude
     */
    public void add(double aLongPosition, double aLatPosition)
    {
        add(new GeoPos(aLongPosition, aLatPosition));
    }

    /**
     * Get the collection of vertices of the polygon
     *
     * @return The vertice collection.
     */
    public Collection<GeoPos> getVertices()
    {
        return m_polygon;
    }

    /**
     * Get name of polygon
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

      Polygon polygon = (Polygon) o;

      if (m_id != null ? !m_id.equals(polygon.m_id) : polygon.m_id != null) return false;
      if (m_layout != null ? !m_layout.equals(polygon.m_layout) : polygon.m_layout != null) return false;
      if (m_name != null ? !m_name.equals(polygon.m_name) : polygon.m_name != null) return false;
      if (m_polygon != null ? !m_polygon.equals(polygon.m_polygon) : polygon.m_polygon != null) return false;
       if (m_polygon != null )
       {
          if(polygon.m_polygon==null || m_polygon.size()!=polygon.m_polygon.size() ) return false;
          for(int i=0;i<m_polygon.size();i++)
          {
             if(!m_polygon.get(i).equals(polygon.m_polygon.get(i)))return false;
          }

       }
       else if(polygon.m_polygon!=null) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (m_id != null ? m_id.hashCode() : 0);
      result = 31 * result + (m_layout != null ? m_layout.hashCode() : 0);
      result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
      result = 31 * result + (m_polygon != null ? m_polygon.hashCode() : 0);
      return result;
   }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        super.clone();
        Polygon retVal = new Polygon(m_id,m_name);
        retVal.setLayout(m_layout);
        retVal.m_polygon.addAll(m_polygon);
        return retVal;
    }

}
