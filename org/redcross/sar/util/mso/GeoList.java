package org.redcross.sar.util.mso;

import java.util.List;
import java.util.Vector;

/**
 * Class for holding a set of {@link IGeodataIf} objects
 */
public class GeoList
{
    private final String m_id;

    private final String m_name;
    private final Vector<IGeodataIf> m_geodata;

    /**
     * Constructor, default collection size
     *
     * @param anId Object Id
     */
    public GeoList(String anId)
    {
        this(anId, "");
    }

    /**
     * Constructor, default collection size
     *
     * @param anId  Object Id
     * @param aName Name of collection
     */
    public GeoList(String anId, String aName)
    {
        m_id = anId;
        m_name = aName;
        m_geodata = new Vector<IGeodataIf>();
    }

    /**
     * Constructor, parameter for collection size
     *
     * @param anId  Object Id
     * @param aName Name of collection
     * @param aSize The collection size
     */
    public GeoList(String anId, String aName, int aSize)
    {
        m_id = anId;
        m_name = aName;
        m_geodata = new Vector<IGeodataIf>(aSize);
    }

    /**
     * Add a new object to the list.
     *
     * @param aGeodata The object to add.
     */
    public void add(IGeodataIf aGeodata)
    {
        m_geodata.add(aGeodata);
    }


    /**
     * Get the collection
     *
     * @return The IGeodataIf list.
     */
    public List<IGeodataIf> getPositions()
    {
        return m_geodata;
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

   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      GeoList that = (GeoList) o;

      if (m_geodata != null ? !m_geodata.equals(that.m_geodata) : that.m_geodata != null) return false;
      if (m_id != null ? !m_id.equals(that.m_id) : that.m_id != null) return false;
      if (m_name != null ? !m_name.equals(that.m_name) : that.m_name != null) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (m_id != null ? m_id.hashCode() : 0);
      result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
      result = 31 * result + (m_geodata != null ? m_geodata.hashCode() : 0);
      return result;
   }
}
