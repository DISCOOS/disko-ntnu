package org.redcross.sar.mso.data;

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

    private final MsoReferenceImpl<IHypothesisIf> m_searchAreaHypothesis = new MsoReferenceImpl<IHypothesisIf>(this, "SearchAreaHypothesis", 1, false);

    public SearchAreaImpl(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(anObjectId);
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

    protected void defineReferences()
    {
        addReference(m_searchAreaHypothesis);
    }

    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return true;
    }

    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return true;
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

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(SearchAreaStatus aStatus)
    {
        m_status.setValue(aStatus);
    }

    public void setStatus(String aStatus)
    {
        m_status.setValue(aStatus);
    }

    public SearchAreaStatus getStatus()
    {
        return m_status.getValue();
    }

    public String getStatusText()
    {
        return m_status.getInternationalName();
    }

    public IMsoModelIf.ModificationState getStatusState()
    {
        return m_status.getState();
    }

    public IAttributeIf.IMsoEnumIf<SearchAreaStatus> getStatusAttribute()
    {
        return m_status;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setGeodata(Polygon aGeodata)
    {
        m_geodata.setValue(aGeodata);
    }

    public Polygon getGeodata()
    {
        return m_geodata.getPolygon();
    }

    public IMsoModelIf.ModificationState getGeodataState()
    {
        return m_geodata.getState();
    }

    public IAttributeIf.IMsoPolygonIf getGeodataAttribute()
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

    public IMsoModelIf.ModificationState getPriorityState()
    {
        return m_priority.getState();
    }

    public IAttributeIf.IMsoIntegerIf getPriorityAttribute()
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

    public IMsoModelIf.ModificationState getRemarksState()
    {
        return m_remarks.getState();
    }

    public IAttributeIf.IMsoStringIf getRemarksAttribute()
    {
        return m_remarks;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setSearchAreaHypothesis(IHypothesisIf aHypothesis)
    {
        m_searchAreaHypothesis.setReference(aHypothesis);
    }

    public IHypothesisIf getSearchAreaHypothesis()
    {
        return m_searchAreaHypothesis.getReference();
    }

    public IMsoModelIf.ModificationState getSearchAreaHypothesisState()
    {
        return m_searchAreaHypothesis.getState();
    }

    public IMsoReferenceIf<IHypothesisIf> getSearchAreaHypothesisAttribute()
    {
        return m_searchAreaHypothesis;
    }
}