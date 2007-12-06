package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

import java.util.Calendar;

public interface IIntelligenceIf extends IMsoObjectIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.Intelligence";

    /**
     * Status enum is
     */
    public enum IntelligenceStatus
    {
        UNCONFIRMED,
        REJECTED,
        CONFIRMED
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(IntelligenceStatus aStatus);

    public void setStatus(String aStatus);

    public IntelligenceStatus getStatus();

    public String getStatusText();

    public IMsoModelIf.ModificationState getStatusState();

    public IAttributeIf.IMsoEnumIf<IntelligenceStatus> getStatusAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setDescription(String aDescription);

    public String getDescription();

    public IMsoModelIf.ModificationState getDescriptionState();

    public IAttributeIf.IMsoStringIf getDescriptionAttribute();

    public void setPriority(int aPriority);

    public int getPriority();

    public IMsoModelIf.ModificationState getPriorityState();

    public IAttributeIf.IMsoIntegerIf getPriorityAttribute();

    public void setSource(String aSource);

    public String getSource();

    public IMsoModelIf.ModificationState getSourceState();

    public IAttributeIf.IMsoStringIf getSourceAttribute();

    public void setTime(Calendar aTime);

    public Calendar getTime();

    public IMsoModelIf.ModificationState getTimeState();

    public IAttributeIf.IMsoCalendarIf getTimeAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setIntelligencePOI(IPOIIf aPOI);

    public IPOIIf getIntelligencePOI();

    public IMsoModelIf.ModificationState getIntelligencePOIState();

    public IMsoReferenceIf<IPOIIf> getIntelligencePOIAttribute();

    public void setIntelligenceRoute(IRouteIf aRoute);

    public IRouteIf getIntelligenceRoute();

    public IMsoModelIf.ModificationState getIntelligenceRouteState();

    public IMsoReferenceIf<IRouteIf> getIntelligenceRouteAttribute();

    public void setIntelligenceSubject(ISubjectIf aSubject);

    public ISubjectIf getIntelligenceSubject();

    public IMsoModelIf.ModificationState getIntelligenceSubjectState();

    public IMsoReferenceIf<ISubjectIf> getIntelligenceSubjectAttribute();

    public void setIntelligenceTrack(ITrackIf aTrack);

    public ITrackIf getIntelligenceTrack();

    public IMsoModelIf.ModificationState getIntelligenceTrackState();

    public IMsoReferenceIf<ITrackIf> getIntelligenceTrackAttribute();
}
