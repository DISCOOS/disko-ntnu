package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;
import org.redcross.sar.util.mso.Polygon;

public class SearchAreaImpl extends AbstractMsoObject implements ISearchAreaIf
{
    private final AttributeImpl.MsoPolygon m_geodata = new AttributeImpl.MsoPolygon(this, "Geodata");
    private final AttributeImpl.MsoInteger m_priority = new AttributeImpl.MsoInteger(this, "Priority");
    private final AttributeImpl.MsoString m_remarks = new AttributeImpl.MsoString(this, "Remarks");

    private final AttributeImpl.MsoEnum<SearchAreaStatus> m_status = new AttributeImpl.MsoEnum<SearchAreaStatus>(this, "Status", 1, SearchAreaStatus.PROCESSING);

    private final MsoRelationImpl<IHypothesisIf> m_searchAreaHypothesis = new MsoRelationImpl<IHypothesisIf>(this, "SearchAreaHypothesis", 1, false, null);

    public SearchAreaImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    protected void defineAttributes()
    {
        addAttribute(m_geodata);
        addAttribute(m_priority);
        addAttribute(m_remarks);
        addAttribute(m_status);
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
        addObject(m_searchAreaHypothesis);
    }

    public static SearchAreaImpl implementationOf(ISearchAreaIf anInterface) throws MsoCastException
    {
        try
        {
            return (SearchAreaImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to SearchAreaImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(SearchAreaStatus aStatus)
    {
        m_status.set(aStatus);
    }

    public void setStatus(String aStatus)
    {
        m_status.set(aStatus);
    }

    public SearchAreaStatus getStatus()
    {
        return m_status.get();
    }

    public String getStatusText()
    {
        return m_status.getInternationalName();
    }

    public IData.DataOrigin getStatusState()
    {
        return m_status.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<SearchAreaStatus> getStatusAttribute()
    {
        return m_status;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setGeodata(Polygon aGeodata)
    {
        m_geodata.set(aGeodata);
    }

    public Polygon getGeodata()
    {
        return m_geodata.get();
    }

    public IData.DataOrigin getGeodataState()
    {
        return m_geodata.getOrigin();
    }

    public IMsoAttributeIf.IMsoPolygonIf getGeodataAttribute()
    {
        return m_geodata;
    }

    public void setPriority(int aPriority)
    {
        m_priority.setValue(aPriority);
    }

    public int getPriority()
    {
        return m_priority.intValue();
    }

    public IData.DataOrigin getPriorityState()
    {
        return m_priority.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getPriorityAttribute()
    {
        return m_priority;
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

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setSearchAreaHypothesis(IHypothesisIf aHypothesis)
    {
        m_searchAreaHypothesis.set(aHypothesis);
    }

    public IHypothesisIf getSearchAreaHypothesis()
    {
        return m_searchAreaHypothesis.get();
    }

    public IData.DataOrigin getSearchAreaHypothesisState()
    {
        return m_searchAreaHypothesis.getOrigin();
    }

    public IMsoRelationIf<IHypothesisIf> getSearchAreaHypothesisAttribute()
    {
        return m_searchAreaHypothesis;
    }
}