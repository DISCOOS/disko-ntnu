package org.redcross.sar.mso.data;

import no.cmr.tools.Log;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.util.mso.Route;

public class RouteImpl extends AbstractMsoObject implements IRouteIf
{
    private final AttributeImpl.MsoRoute m_geodata = new AttributeImpl.MsoRoute(this, "Geodata");
    private final AttributeImpl.MsoString m_remarks = new AttributeImpl.MsoString(this, "Remarks");
    private final AttributeImpl.MsoInteger m_areaSequenceNumber = new AttributeImpl.MsoInteger(this, "AreaSequenceNumber");

    public RouteImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    public RouteImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, Route aRoute)
    {
        super(theMsoModel, anObjectId);
        setGeodata(aRoute);
    }

    protected void defineAttributes()
    {
        addAttribute(m_geodata);
        addAttribute(m_remarks);
        addAttribute(m_areaSequenceNumber);
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
    }

    public static RouteImpl implementationOf(IRouteIf anInterface) throws MsoCastException
    {
        try
        {
            return (RouteImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to RouteImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_ROUTE;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/
    public void setGeodata(Route aGeodata)
    {
        m_geodata.set(aGeodata);
    }

    public Route getGeodata()
    {
        return m_geodata.get();
    }

    public IData.DataOrigin getGeodataState()
    {
        return m_geodata.getOrigin();
    }

    public IMsoAttributeIf.IMsoRouteIf getGeodataAttribute()
    {
        return m_geodata;
    }

    public void setRemarks(String aRemarks)
    {
        m_remarks.setValue(aRemarks);
    }

    public String getRemarks()
    {
        return m_remarks.getString();
    }

    public IData.DataOrigin getRemarksState()
    {
        return m_remarks.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getRemarksAttribute()
    {
        return m_remarks;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        RouteImpl route = (RouteImpl) o;

        if (m_geodata != null ? !m_geodata.equals(route.m_geodata) : route.m_geodata != null)
        {
            return false;
        }
        if (m_remarks != null ? !m_remarks.equals(route.m_remarks) : route.m_remarks != null)
        {
            return false;
        }
        if (m_areaSequenceNumber != null ? !m_areaSequenceNumber.equals(route.m_areaSequenceNumber) : route.m_areaSequenceNumber != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (m_geodata != null ? m_geodata.hashCode() : 0);
        result = 31 * result + (m_remarks != null ? m_remarks.hashCode() : 0);
        result = 31 * result + (m_areaSequenceNumber != null ? m_areaSequenceNumber.hashCode() : 0);
        return result;
    }

    public void setAreaSequenceNumber(int aNumber)
    {
        m_areaSequenceNumber.setValue(aNumber);
    }

    public int getAreaSequenceNumber()
    {
        return m_areaSequenceNumber.intValue();
    }

    public IData.DataOrigin getAreaSequenceNumberState()
    {
        return m_areaSequenceNumber.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getAreaSequenceNumberAttribute()
    {
        return m_areaSequenceNumber;
    }

    /*-------------------------------------------------------------------------------------------
     * Public methods
     *-------------------------------------------------------------------------------------------*/

    public void addRoutePoint(GeoPos aGeoPos)
    {
        Route r = getGeodata();
        try
        {
            Route tc = (Route)r.clone();
            tc.add(aGeoPos);
            setGeodata(tc);
        }
        catch (CloneNotSupportedException e)
        {
            Log.error("CloneNotSupportedException in addRoutePoint, no point added.");
        }
    }

    public void removeRoutePoint(GeoPos aGeoPos)
    {
    	Route r = getGeodata();
        try
        {
            Route rc = (Route)r.clone();
            rc.remove(aGeoPos);
            setGeodata(rc);
        }
        catch (CloneNotSupportedException e)
        {
            Log.error("CloneNotSupportedException in removeRoutePoint, no point removed.");
        }
    }

	@Override
	public GeoPos getRoutePoint(int index) {
		Route r = getGeodata();
		if(r!=null) {
			return r.get(index);
		}
		return null;
	}

	@Override
	public int getRoutePointCount() {
		Route r = getGeodata();
		if(r!=null) {
			return r.size();
		}
		return 0;
	}

	@Override
	public GeoPos getRouteStartPoint() {
		Route r = getGeodata();
		if(r!=null) {
			return r.getStartPoint();
		}
		return null;
	}

	@Override
	public GeoPos getRouteStopPoint() {
		Route r = getGeodata();
		if(r!=null) {
			return r.getStopPoint();
		}
		return null;
	}
}