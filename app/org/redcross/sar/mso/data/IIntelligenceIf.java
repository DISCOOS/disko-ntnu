package org.redcross.sar.mso.data;


import java.util.Calendar;

import org.redcross.sar.data.IData;

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

    public IData.DataOrigin getStatusState();

    public IMsoAttributeIf.IMsoEnumIf<IntelligenceStatus> getStatusAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setDescription(String aDescription);

    public String getDescription();

    public IData.DataOrigin getDescriptionState();

    public IMsoAttributeIf.IMsoStringIf getDescriptionAttribute();

    public void setPriority(int aPriority);

    public int getPriority();

    public IData.DataOrigin getPriorityState();

    public IMsoAttributeIf.IMsoIntegerIf getPriorityAttribute();

    public void setSource(String aSource);

    public String getSource();

    public IData.DataOrigin getSourceState();

    public IMsoAttributeIf.IMsoStringIf getSourceAttribute();

    public void setTime(Calendar aTime);

    public Calendar getTime();

    public IData.DataOrigin getTimeState();

    public IMsoAttributeIf.IMsoCalendarIf getTimeAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setIntelligencePOI(IPOIIf aPOI);

    public IPOIIf getIntelligencePOI();

    public IData.DataOrigin getIntelligencePOIState();

    public IMsoRelationIf<IPOIIf> getIntelligencePOIAttribute();

    public void setIntelligenceRoute(IRouteIf aRoute);

    public IRouteIf getIntelligenceRoute();

    public IData.DataOrigin getIntelligenceRouteState();

    public IMsoRelationIf<IRouteIf> getIntelligenceRouteAttribute();

    public void setIntelligenceSubject(ISubjectIf aSubject);

    public ISubjectIf getIntelligenceSubject();

    public IData.DataOrigin getIntelligenceSubjectState();

    public IMsoRelationIf<ISubjectIf> getIntelligenceSubjectAttribute();

    public void setIntelligenceTrack(ITrackIf aTrack);

    public ITrackIf getIntelligenceTrack();

    public IData.DataOrigin getIntelligenceTrackState();

    public IMsoRelationIf<ITrackIf> getIntelligenceTrackAttribute();
}
