package org.redcross.sar.mso.data;


import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.redcross.sar.data.IData;

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

    public IData.DataOrigin getStatusState();

    public IMsoAttributeIf.IMsoEnumIf<CmdPostStatus> getStatusAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setEstablished(Calendar anEstablished);

    public Calendar getEstablished();

    public IData.DataOrigin getEstablishedState();

    public IMsoAttributeIf.IMsoCalendarIf getEstablishedAttribute();

    public void setCallSign(String aCallSign);

    public String getCallSign();

    public IData.DataOrigin getCallSignState();

    public IMsoAttributeIf.IMsoStringIf getCallSignAttribute();

    public void setReleased(Calendar aReleased);

    public Calendar getReleased();

    public IData.DataOrigin getReleasedState();

    public IMsoAttributeIf.IMsoCalendarIf getReleasedAttribute();

    public void setShift(int aShift);

    public int getShift();

    public IData.DataOrigin getShiftState();

    public IMsoAttributeIf.IMsoIntegerIf getShiftAttribute();

    public void setTelephone1(String aTelephone1);

    public String getTelephone1();

    public IData.DataOrigin getTelephone1State();

    public IMsoAttributeIf.IMsoStringIf getTelephone1Attribute();

    public void setTelephone2(String aTelephone2);

    public String getTelephone2();

    public IData.DataOrigin getTelephone2State();

    public IMsoAttributeIf.IMsoStringIf getTelephone2Attribute();

    public void setTelephone3(String aTelephone3);

    public String getTelephone3();

    public IData.DataOrigin getTelephone3State();

    public IMsoAttributeIf.IMsoStringIf getTelephone3Attribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public IAreaListIf getAreaList();

    public IData.DataOrigin getAreaListState(IAreaIf anIAreaIf);

    public Collection<IAreaIf> getAreaListItems();

    public IAssignmentListIf getAssignmentList();

    public IData.DataOrigin getAssignmentListState(IAssignmentIf anIAssignmentIf);

    public Collection<IAssignmentIf> getAssignmentListItems();

    public IPersonnelListIf getAttendanceList();

    public IData.DataOrigin getAttendanceListState(IPersonnelIf anIPersonnelIf);

    public Collection<IPersonnelIf> getAttendanceListItems();

    public IBriefingListIf getBriefingList();

    public IData.DataOrigin getBriefingListState(IBriefingIf anIBriefingIf);

    public Collection<IBriefingIf> getBriefingListItems();

    public ICalloutListIf getCalloutList();

    public IData.DataOrigin getCalloutListState(ICalloutIf anICalloutIf);

    public Collection<ICalloutIf> getCalloutListItems();

    public ICheckpointListIf getCheckpointList();

    public IData.DataOrigin getCheckpointListState(ICheckpointIf anICheckpointIf);

    public Collection<ICheckpointIf> getCheckpointListItems();

    public IEnvironmentListIf getEnvironmentList();

    public IData.DataOrigin getEnvironmentListState(IEnvironmentIf anIEnvironmentIf);

    public Collection<IEnvironmentIf> getEnvironmentListItems();

    public IEquipmentListIf getEquipmentList();

    public IData.DataOrigin getEquipmentListState(IEquipmentIf anIEquipmentIf);

    public Collection<IEquipmentIf> getEquipmentListItems();

    public IEventLogIf getEventLog();

    public IData.DataOrigin getEventLogState(IEventIf anIEventIf);

    public Collection<IEventIf> getEventLogItems();

    public IForecastListIf getForecastList();

    public IData.DataOrigin getForecastListState(IForecastIf anIForecastIf);

    public Collection<IForecastIf> getForecastListItems();

    public IHypothesisListIf getHypothesisList();

    public IData.DataOrigin getHypothesisListState(IHypothesisIf anIHypothesisIf);

    public Collection<IHypothesisIf> getHypothesisListItems();

    public IIntelligenceListIf getIntelligenceList();

    public IData.DataOrigin getIntelligenceListState(IIntelligenceIf anIIntelligenceIf);

    public Collection<IIntelligenceIf> getIntelligenceListItems();

    public IMessageLogIf getMessageLog();

    public IData.DataOrigin getMessageLogState(IMessageIf anIMessageIf);

    public Collection<IMessageIf> getMessageLogItems();

    public IMessageLineListIf getMessageLines();

    public IData.DataOrigin getMessageLineState(IMessageLineIf anIMessageLineIf);

    public Collection<IMessageLineIf> getMessageLineItems();

    public IOperationAreaListIf getOperationAreaList();

    public IData.DataOrigin getOperationAreaListState(IOperationAreaIf anIOperationAreaIf);

    public Collection<IOperationAreaIf> getOperationAreaListItems();

    public IPOIListIf getPOIList();

    public IData.DataOrigin getPOIListState(IPOIIf anIPOIIf);

    public Collection<IPOIIf> getPOIListItems();

    public IRouteListIf getRouteList();

    public IData.DataOrigin getRouteListState(IRouteIf anIRouteIf);

    public Collection<IRouteIf> getRouteListItems();

    public ISearchAreaListIf getSearchAreaList();

    public IData.DataOrigin getSearchAreaListState(ISearchAreaIf anISearchAreaIf);

    public Collection<ISearchAreaIf> getSearchAreaListItems();

    public ISketchListIf getSketchList();

    public IData.DataOrigin getSketchListState(ISketchIf anISketchIf);

    public Collection<ISketchIf> getSketchListItems();

    public ISubjectListIf getSubjectList();

    public IData.DataOrigin getSubjectListState(ISubjectIf anISubjectIf);

    public Collection<ISubjectIf> getSubjectListItems();

    public ITaskListIf getTaskList();

    public IData.DataOrigin getTaskListState(ITaskIf anITaskIf);

    public Collection<ITaskIf> getTaskListItems();

    public ITrackListIf getTrackList();

    public IData.DataOrigin getTrackListState(ITrackIf anITrackIf);

    public Collection<ITrackIf> getTrackListItems();

    public IUnitListIf getUnitList();

    public IData.DataOrigin getUnitListState(IUnitIf anIUnitIf);

    public Collection<IUnitIf> getUnitListItems();

    /*-------------------------------------------------------------------------------------------
    * Other List accessor methods
    *-------------------------------------------------------------------------------------------*/
    
    public ICommunicatorIf getCommunicator();
    
    public AbstractCoList<ICommunicatorIf> getCommunicatorList();

    public List<ICommunicatorIf> getActiveCommunicators();

    public ITimeLineIf getTimeLine();
}