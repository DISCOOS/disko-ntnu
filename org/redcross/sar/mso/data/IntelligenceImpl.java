package org.redcross.sar.mso.data;

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
    private final AttributeImpl.MsoEnum<IntelligenceStatus> m_status = new AttributeImpl.MsoEnum<IntelligenceStatus>(this, "Status", IntelligenceStatus.UNCONFIRMED);

    private final MsoReferenceImpl<IPOIIf> m_intelligencePOI = new MsoReferenceImpl<IPOIIf>(this, "IntelligencePOI", true);
    private final MsoReferenceImpl<IRouteIf> m_intelligenceRoute = new MsoReferenceImpl<IRouteIf>(this, "IntelligenceRoute", true);
    private final MsoReferenceImpl<ISubjectIf> m_intelligenceSubject = new MsoReferenceImpl<ISubjectIf>(this, "IntelligenceSubject", true);
    private final MsoReferenceImpl<ITrackIf> m_intelligenceTrack = new MsoReferenceImpl<ITrackIf>(this, "IntelligenceTrack", true);

    public IntelligenceImpl(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(anObjectId);
    }

    public IntelligenceImpl(IMsoObjectIf.IObjectIdIf anObjectId, IPOIIf aPoi, ISubjectIf aSubject, IRouteIf aRoute, ITrackIf aTrack) throws MsoCastException
    {
        super(anObjectId);
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

    protected void defineReferences()
    {
        addReference(m_intelligencePOI);
        addReference(m_intelligenceRoute);
        addReference(m_intelligenceSubject);
        addReference(m_intelligenceTrack);
    }

    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return true;
    }

    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return true;
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

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_INTELLIGENCE;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(IntelligenceStatus aStatus)
    {
        m_status.setValue(aStatus);
    }

    public void setStatus(String aStatus)
    {
        m_status.setValue(aStatus);
    }

    public IntelligenceStatus getStatus()
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

    public IAttributeIf.IMsoEnumIf<IntelligenceStatus> getStatusAttribute()
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

    public IMsoModelIf.ModificationState getDescriptionState()
    {
        return m_description.getState();
    }

    public IAttributeIf.IMsoStringIf getDescriptionAttribute()
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

    public IMsoModelIf.ModificationState getPriorityState()
    {
        return m_priority.getState();
    }

    public IAttributeIf.IMsoIntegerIf getPriorityAttribute()
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

    public IMsoModelIf.ModificationState getSourceState()
    {
        return m_source.getState();
    }

    public IAttributeIf.IMsoStringIf getSourceAttribute()
    {
        return m_source;
    }

    public void setTime(Calendar aTime)
    {
        m_time.setValue(aTime);
    }

    public Calendar getTime()
    {
        return m_time.getCalendar();
    }

    public IMsoModelIf.ModificationState getTimeState()
    {
        return m_time.getState();
    }

    public IAttributeIf.IMsoCalendarIf getTimeAttribute()
    {
        return m_time;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/
    public void setIntelligencePOI(IPOIIf aPOI)
    {
        m_intelligencePOI.setReference(aPOI);
    }

    public IPOIIf getIntelligencePOI()
    {
        return m_intelligencePOI.getReference();
    }

    public IMsoModelIf.ModificationState getIntelligencePOIState()
    {
        return m_intelligencePOI.getState();
    }

    public IMsoReferenceIf<IPOIIf> getIntelligencePOIAttribute()
    {
        return m_intelligencePOI;
    }

    public void setIntelligenceRoute(IRouteIf aRoute)
    {
        m_intelligenceRoute.setReference(aRoute);
    }

    public IRouteIf getIntelligenceRoute()
    {
        return m_intelligenceRoute.getReference();
    }

    public IMsoModelIf.ModificationState getIntelligenceRouteState()
    {
        return m_intelligenceRoute.getState();
    }

    public IMsoReferenceIf<IRouteIf> getIntelligenceRouteAttribute()
    {
        return m_intelligenceRoute;
    }

    public void setIntelligenceSubject(ISubjectIf aSubject)
    {
        m_intelligenceSubject.setReference(aSubject);
    }

    public ISubjectIf getIntelligenceSubject()
    {
        return m_intelligenceSubject.getReference();
    }

    public IMsoModelIf.ModificationState getIntelligenceSubjectState()
    {
        return m_intelligenceSubject.getState();
    }

    public IMsoReferenceIf<ISubjectIf> getIntelligenceSubjectAttribute()
    {
        return m_intelligenceSubject;
    }

    public void setIntelligenceTrack(ITrackIf aTrack)
    {
        m_intelligenceTrack.setReference(aTrack);
    }

    public ITrackIf getIntelligenceTrack()
    {
        return m_intelligenceTrack.getReference();
    }

    public IMsoModelIf.ModificationState getIntelligenceTrackState()
    {
        return m_intelligenceTrack.getState();
    }

    public IMsoReferenceIf<ITrackIf> getIntelligenceTrackAttribute()
    {
        return m_intelligenceTrack;
    }

}