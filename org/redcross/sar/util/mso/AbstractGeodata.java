package org.redcross.sar.util.mso;

/**
 * Class for holding polygon information
 */
public abstract class AbstractGeodata implements IGeodataIf, Cloneable
{
    protected final String m_id;

    protected String m_layout;

    protected int m_changeCount;

    protected final String m_name;

    /**
     * Constructor, default collection size
     *
     * @param anId Object Id
     */
    public AbstractGeodata(String anId)
    {
        this(anId, "");
    }

    /**
     * Constructor, default collection size
     *
     * @param anId  Object Id
     * @param aName Name of object
     */
    public AbstractGeodata(String anId, String aName)
    {
        m_id = anId;
        m_name = aName;
        m_layout = "";
    }

    public int getChangeCount()
    {
        return m_changeCount;
    }

    protected void incrementChangeCount() {
    	m_changeCount++;
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

      AbstractGeodata data = (AbstractGeodata) o;

      if (m_id != null ? !m_id.equals(data.m_id) : data.m_id != null) return false;
      if (m_layout != null ? !m_layout.equals(data.m_layout) : data.m_layout != null) return false;
      if (m_name != null ? !m_name.equals(data.m_name) : data.m_name != null) return false;

      // is equal
      return true;
   }

   public int hashCode()
   {
      int result;
      result = (m_id != null ? m_id.hashCode() : 0);
      result = 31 * result + (m_layout != null ? m_layout.hashCode() : 0);
      result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
      return result;
   }

    @Override
    public abstract AbstractGeodata clone() throws CloneNotSupportedException;

}
