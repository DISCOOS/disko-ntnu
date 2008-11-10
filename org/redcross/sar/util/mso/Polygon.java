package org.redcross.sar.util.mso;

import java.util.Collection;
import java.util.Vector;

/**
 * Class for holding polygon information
 */
public class Polygon extends AbstractGeodata
{
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
		super(anId,aName);
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
		super(anId,aName);
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
	 * Add a new vertex to the route.
	 *
	 * @param aLongPosition The vertex' longitude
	 * @param aLatPosition  The vertex' latitude
	 * @param aAltitude The vertex' altitude
	 */
	public void add(double aLongPosition, double aLatPosition, double aAltitude)
	{
		add(new GeoPos(aLongPosition, aLatPosition, aAltitude));
	}

	/**
	 * Get the collection of vertices of the polygon
	 *
	 * @return The vertices collection.
	 */
	public Collection<GeoPos> getVertices()
	{
		return m_polygon;
	}


	public boolean equals(Object o)
	{
		if (!super.equals(o)) return false;

		Polygon polygon = (Polygon) o;

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
		int result = super.hashCode();
		result = 31 * result + (m_polygon != null ? m_polygon.hashCode() : 0);
		return result;
	}

	@Override
	public Polygon clone() throws CloneNotSupportedException
	{
		Polygon retVal = new Polygon(m_id,m_name);
		retVal.setLayout(m_layout);
		retVal.m_polygon.addAll(m_polygon);
		return retVal;
	}

}
