package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public interface ICmdPostIf extends IMsoObjectIf
{
    public static final String bundleName = "org.redcross.sar.mso.data.properties.CmdPost";

    /**
     * Command post status enum
     */
    public enum CmdPostStatus
    {
        IDLE,
        OPERATING,
        PAUSED,
        RELEASED
    }
    
    public final EnumSet<CmdPostStatus> ACTIVE_CMDPOST_SET = 
		EnumSet.of(CmdPostStatus.IDLE, CmdPostStatus.OPERATING, CmdPostStatus.PAUSED);    

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(CmdPostStatus aStatus);

    public void setStatus(String aStatus);

    public CmdPostStatus getStatus();

    public String getStatusText();

    public IMsoModelIf.ModificationState getStatusState();

    public IAttributeIf.IMsoEnumIf<CmdPostStatus> getStatusAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setEstablished(Calendar anEstablished);

    public Calendar getEstablished();

    public IMsoModelIf.ModificationState getEstablishedState();

    public IAttributeIf.IMsoCalendarIf getEstablishedAttribute();

    public void setCallSign(String aCallSign);

    public String getCallSign();

    public IMsoModelIf.ModificationState getCallSignState();

    public IAttributeIf.IMsoStringIf getCallSignAttribute();

    public void setReleased(Calendar aReleased);

    public Calendar getReleased();

    public IMsoModelIf.ModificationState getReleasedState();

    public IAttributeIf.IMsoCalendarIf getReleasedAttribute();

    public void setShift(int aShift);

    public int getShift();

    public IMsoModelIf.ModificationState getShiftState();

    public IAttributeIf.IMsoIntegerIf getShiftAttribute();

    public void setTelephone1(String aTelephone1);

    public String getTelephone1();

    public IMsoModelIf.ModificationState getTelephone1State();

    public IAttributeIf.IMsoStringIf getTelephone1Attribute();

    public void setTelephone2(String aTelephone2);

    public String getTelephone2();

    public IMsoModelIf.ModificationState getTelephone2State();

    public IAttributeIf.IMsoStringIf getTelephone2Attribute();

    public void setTelephone3(String aTelephone3);

    public String getTelephone3();

    public IMsoModelIf.ModificationState getTelephone3State();

    public IAttributeIf.IMsoStringIf getTelephone3Attribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public IAreaListIf getAreaList();

    public IMsoModelIf.ModificationState getAreaListState(IAreaIf anIAreaIf);

    public Collection<IAreaIf> getAreaListItems();

    public IAssignmentListIf getAssignmentList();

    public IMsoModelIf.ModificationState getAssignmentListState(IAssignmentIf anIAssignmentIf);

    public Collection<IAssignmentIf> getAssignmentListItems();

    public IPersonnelListIf getAttendanceList();

    public IMsoModelIf.ModificationState getAttendanceListState(IPersonnelIf anIPersonnelIf);

    public Collection<IPersonnelIf> getAttendanceListItems();

    public IBriefingListIf getBriefingList();

    public IMsoModelIf.ModificationState getBriefingListState(IBriefingIf anIBriefingIf);

    public Collection<IBriefingIf> getBriefingListItems();

    public ICalloutListIf getCalloutList();

    public IMsoModelIf.ModificationState getCalloutListState(ICalloutIf anICalloutIf);

    public Collection<ICalloutIf> getCalloutListItems();

    public ICheckpointListIf getCheckpointList();

    public IMsoModelIf.ModificationState getCheckpointListState(ICheckpointIf anICheckpointIf);

    public Collection<ICheckpointIf> getCheckpointListItems();

    public IEnvironmentListIf getEnvironmentList();

    public IMsoModelIf.ModificationState getEnvironmentListState(IEnvironmentIf anIEnvironmentIf);

    public Collection<IEnvironmentIf> getEnvironmentListItems();

    public IEquipmentListIf getEquipmentList();

    public IMsoModelIf.ModificationState getEquipmentListState(IEquipmentIf anIEquipmentIf);

    public Collection<IEquipmentIf> getEquipmentListItems();

    public IEventLogIf getEventLog();

    public IMsoModelIf.ModificationState getEventLogState(IEventIf anIEventIf);

    public Collection<IEventIf> getEventLogItems();

    public IForecastListIf getForecastList();

    public IMsoModelIf.ModificationState getForecastListState(IForecastIf anIForecastIf);

    public Collection<IForecastIf> getForecastListItems();

    public IHypothesisListIf getHypothesisList();

    public IMsoModelIf.ModificationState getHypothesisListState(IHypothesisIf anIHypothesisIf);

    public Collection<IHypothesisIf> getHypothesisListItems();

    public IIntelligenceListIf getIntelligenceList();

    public IMsoModelIf.ModificationState getIntelligenceListState(IIntelligenceIf anIIntelligenceIf);

    public Collection<IIntelligenceIf> getIntelligenceListItems();

    public IMessageLogIf getMessageLog();

    public IMsoModelIf.ModificationState getMessageLogState(IMessageIf anIMessageIf);

    public Collection<IMessageIf> getMessageLogItems();

    public IMessageLineListIf getMessageLines();

    public IMsoModelIf.ModificationState getMessageLineState(IMessageLineIf anIMessageLineIf);

    public Collection<IMessageLineIf> getMessageLineItems();

    public IOperationAreaListIf getOperationAreaList();

    public IMsoModelIf.ModificationState getOperationAreaListState(IOperationAreaIf anIOperationAreaIf);

    public Collection<IOperationAreaIf> getOperationAreaListItems();

    public IPOIListIf getPOIList();

    public IMsoModelIf.ModificationState getPOIListState(IPOIIf anIPOIIf);

    public Collection<IPOIIf> getPOIListItems();

    public IRouteListIf getRouteList();

    public IMsoModelIf.ModificationState getRouteListState(IRouteIf anIRouteIf);

    public Collection<IRouteIf> getRouteListItems();

    public ISearchAreaListIf getSearchAreaList();

    public IMsoModelIf.ModificationState getSearchAreaListState(ISearchAreaIf anISearchAreaIf);

    public Collection<ISearchAreaIf> getSearchAreaListItems();

    public ISketchListIf getSketchList();

    public IMsoModelIf.ModificationState getSketchListState(ISketchIf anISketchIf);

    public Collection<ISketchIf> getSketchListItems();

    public ISubjectListIf getSubjectList();

    public IMsoModelIf.ModificationState getSubjectListState(ISubjectIf anISubjectIf);

    public Collection<ISubjectIf> getSubjectListItems();

    public ITaskListIf getTaskList();

    public IMsoModelIf.ModificationState getTaskListState(ITaskIf anITaskIf);

    public Collection<ITaskIf> getTaskListItems();

    public ITrackListIf getTrackList();

    public IMsoModelIf.ModificationState getTrackListState(ITrackIf anITrackIf);

    public Collection<ITrackIf> getTrackListItems();

    public IUnitListIf getUnitList();

    public IMsoModelIf.ModificationState getUnitListState(IUnitIf anIUnitIf);

    public Collection<IUnitIf> getUnitListItems();

    /*-------------------------------------------------------------------------------------------
    * Other List accessor methods
    *-------------------------------------------------------------------------------------------*/
    
    public ICommunicatorIf getCommunicator();
    
    public AbstractDerivedList<ICommunicatorIf> getCommunicatorList();

    public List<ICommunicatorIf> getActiveCommunicators();

    public ITimeLineIf getTimeLine();
}