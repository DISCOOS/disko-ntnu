package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;

import java.util.Calendar;

public class IntelligenceImpl extends AbstractMsoObject implements IIntelligenceIf
{

    private final AttributeImpl.MsoString m_description = new AttributeImpl.MsoString(this, "Description");
    private final AttributeImpl.MsoInteger m_priority = new AttributeImpl.MsoInteger(this, "Priority");
    private final AttributeImpl.MsoString m_source = new AttributeImpl.MsoString(this, "Source");
    private final AttributeImpl.MsoCalendar m_time = new AttributeImpl.MsoCalendar(this, "Time");
    private final AttributeImpl.MsoEnum<IntelligenceStatus> m_status = new AttributeImpl.MsoEnum<IntelligenceStatus>(this, "Status", 1, IntelligenceStatus.UNCONFIRMED);

    private final MsoRelationImpl<IPOIIf> m_intelligencePOI = new MsoRelationImpl<IPOIIf>(this, "IntelligencePOI", 0, true, null);
    private final MsoRelationImpl<IRouteIf> m_intelligenceRoute = new MsoRelationImpl<IRouteIf>(this, "IntelligenceRoute", 0, true, null);
    private final MsoRelationImpl<ISubjectIf> m_intelligenceSubject = new MsoRelationImpl<ISubjectIf>(this, "IntelligenceSubject", 0, true, null);
    private final MsoRelationImpl<ITrackIf> m_intelligenceTrack = new MsoRelationImpl<ITrackIf>(this, "IntelligenceTrack", 0, true, null);

    public IntelligenceImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    public IntelligenceImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, IPOIIf aPoi, ISubjectIf aSubject, IRouteIf aRoute, ITrackIf aTrack) throws MsoCastException
    {
        super(theMsoModel, anObjectId);
        setIntelligencePOI(aPoi);
        setIntelligenceSubject(aSubject);
        setIntelligenceRoute(aRoute);
        setIntelligenceTrack(aTrack);
    }

    protected void defineAttributes()
    {
        addAttribute(m_description);
        addAttribute(m_priority);
        addAttribute(m_source);
        addAttribute(m_time);
        addAttribute(m_status);
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
        addObject(m_intelligencePOI);
        addObject(m_intelligenceRoute);
        addObject(m_intelligenceSubject);
        addObject(m_intelligenceTrack);
    }

    public static IntelligenceImpl implementationOf(IIntelligenceIf anInterface) throws MsoCastException
    {
        try
        {
            return (IntelligenceImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to IntelligenceImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_INTELLIGENCE;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(IntelligenceStatus aStatus)
    {
        m_status.set(aStatus);
    }

    public void setStatus(String aStatus)
    {
        m_status.set(aStatus);
    }

    public IntelligenceStatus getStatus()
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

    public IMsoAttributeIf.IMsoEnumIf<IntelligenceStatus> getStatusAttribute()
    {
        return m_status;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setDescription(String aDescription)
    {
        m_description.setValue(aDescription);
    }

    public String getDescription()
    {
        return m_description.getString();
    }

    public IData.DataOrigin getDescriptionState()
    {
        return m_description.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getDescriptionAttribute()
    {
        return m_description;
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

    public void setSource(String aSource)
    {
        m_source.setValue(aSource);
    }

    public String getSource()
    {
        return m_source.getString();
    }

    public IData.DataOrigin getSourceState()
    {
        return m_source.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getSourceAttribute()
    {
        return m_source;
    }

    public void setTime(Calendar aTime)
    {
        m_time.set(aTime);
    }

    public Calendar getTime()
    {
        return m_time.getCalendar();
    }

    public IData.DataOrigin getTimeState()
    {
        return m_time.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getTimeAttribute()
    {
        return m_time;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/
    public void setIntelligencePOI(IPOIIf aPOI)
    {
        m_intelligencePOI.set(aPOI);
    }

    public IPOIIf getIntelligencePOI()
    {
        return m_intelligencePOI.get();
    }

    public IData.DataOrigin getIntelligencePOIState()
    {
        return m_intelligencePOI.getOrigin();
    }

    public IMsoRelationIf<IPOIIf> getIntelligencePOIAttribute()
    {
        return m_intelligencePOI;
    }

    public void setIntelligenceRoute(IRouteIf aRoute)
    {
        m_intelligenceRoute.set(aRoute);
    }

    public IRouteIf getIntelligenceRoute()
    {
        return m_intelligenceRoute.get();
    }

    public IData.DataOrigin getIntelligenceRouteState()
    {
        return m_intelligenceRoute.getOrigin();
    }

    public IMsoRelationIf<IRouteIf> getIntelligenceRouteAttribute()
    {
        return m_intelligenceRoute;
    }

    public void setIntelligenceSubject(ISubjectIf aSubject)
    {
        m_intelligenceSubject.set(aSubject);
    }

    public ISubjectIf getIntelligenceSubject()
    {
        return m_intelligenceSubject.get();
    }

    public IData.DataOrigin getIntelligenceSubjectState()
    {
        return m_intelligenceSubject.getOrigin();
    }

    public IMsoRelationIf<ISubjectIf> getIntelligenceSubjectAttribute()
    {
        return m_intelligenceSubject;
    }

    public void setIntelligenceTrack(ITrackIf aTrack)
    {
        m_intelligenceTrack.set(aTrack);
    }

    public ITrackIf getIntelligenceTrack()
    {
        return m_intelligenceTrack.get();
    }

    public IData.DataOrigin getIntelligenceTrackState()
    {
        return m_intelligenceTrack.getOrigin();
    }

    public IMsoRelationIf<ITrackIf> getIntelligenceTrackAttribute()
    {
        return m_intelligenceTrack;
    }

}