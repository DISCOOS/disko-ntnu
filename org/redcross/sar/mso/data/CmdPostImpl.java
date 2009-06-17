package org.redcross.sar.mso.data;

import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.MsoCastException;

/**
 * Command, control and communication center (command post)
 */
@SuppressWarnings("unchecked")
public class CmdPostImpl extends AbstractMsoObject implements ICmdPostIf, IHierarchicalUnitIf, ICommunicatorIf
{
    private final AttributeImpl.MsoCalendar m_established = new AttributeImpl.MsoCalendar(this, "Established");
    private final AttributeImpl.MsoString m_callSign = new AttributeImpl.MsoString(this, "CallSign");
    private final AttributeImpl.MsoString m_toneId = new AttributeImpl.MsoString(this, "ToneID");
    private final AttributeImpl.MsoCalendar m_released = new AttributeImpl.MsoCalendar(this, "Released");
    private final AttributeImpl.MsoInteger m_shift = new AttributeImpl.MsoInteger(this, "Shift");
    private final AttributeImpl.MsoString m_telephone1 = new AttributeImpl.MsoString(this, "Telephone1");
    private final AttributeImpl.MsoString m_telephone2 = new AttributeImpl.MsoString(this, "Telephone2");
    private final AttributeImpl.MsoString m_telephone3 = new AttributeImpl.MsoString(this, "Telephone3");
    private final AttributeImpl.MsoEnum<CmdPostStatus> m_status = new AttributeImpl.MsoEnum<CmdPostStatus>(this, "Status", 1, CmdPostStatus.IDLE);

    private final AbstractCoList<ICommunicatorIf> m_communicatorList;
    private final TimeLineImpl m_timeLine = new TimeLineImpl();

    private final AreaListImpl m_areaList = new AreaListImpl(this, "AreaList", true, 100);
    private final AssignmentListImpl m_assignmentList = new AssignmentListImpl(this, "AssignmentList", true, 100);
    private final PersonnelListImpl m_attendanceList = new PersonnelListImpl(this, "AttendanceList", true, 100);
    private final BriefingListImpl m_briefingList = new BriefingListImpl(this, "BriefingList", true, 100);
    private final CalloutListImpl m_calloutList = new CalloutListImpl(this, "CalloutList", true, 100);
    private final CheckpointListImpl m_checkpointList = new CheckpointListImpl(this, "CheckpointList", true, 100);
    private final EnvironmentListImpl m_environmentList = new EnvironmentListImpl(this, "EnvironmentList", true, 100);
    private final EquipmentListImpl m_equipmentList = new EquipmentListImpl(this, "EquipmentList", true, 100);
    private final EventLogImpl m_eventLog = new EventLogImpl(this, "EventLog", true, 100);
    private final ForecastListImpl m_forecastList = new ForecastListImpl(this, "ForecastList", true, 100);
    private final HypothesisListImpl m_hypothesisList = new HypothesisListImpl(this, "HypothesisList", true, 100);
    private final IntelligenceListImpl m_intelligenceList = new IntelligenceListImpl(this, "IntelligenceList", true, 100);
    private final MessageLogImpl m_messageLog = new MessageLogImpl(this, "MessageLog", true, 100);
    private final MessageLineListImpl m_messageLineList = new MessageLineListImpl(this, "MessageLineList", true, 1000);
    private final OperationAreaListImpl m_operationAreaList = new OperationAreaListImpl(this, "OperationAreaList", true, 100);
    private final POIListImpl m_poiList = new POIListImpl(this, "POIList", true, 100);
    private final RouteListImpl m_routeList = new RouteListImpl(this, "RouteList", true, 100);
    private final SearchAreaListImpl m_searchAreaList = new SearchAreaListImpl(this, "SearchAreaList", true, 100);
    private final SketchListImpl m_sketchList = new SketchListImpl(this, "SketchList", true, 100);
    private final SubjectListImpl m_subjectList = new SubjectListImpl(this, "SubjectList", true, 100);
    private final TaskListImpl m_taskList = new TaskListImpl(this, "TaskList", true, 100);
    private final TrackListImpl m_trackList = new TrackListImpl(this, "TrackList", true, 100);
    private final UnitListImpl m_unitList = new UnitListImpl(this, "UnitList", true, 100);

    public CmdPostImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
        m_status.setValue(CmdPostStatus.IDLE);

        m_communicatorList = createCommunicatorList();
    }

    AbstractCoList<ICommunicatorIf> createCommunicatorList()
    {
        return new AbstractCoList<ICommunicatorIf>()
        {
            public boolean hasInterestIn(Object anObject)
            {
                return anObject instanceof ICommunicatorIf;
            }

            public void handleItemCreate(Object anObject)
            {
                ICommunicatorIf item = (ICommunicatorIf) anObject;
                m_items.put(item.getObjectId(), item);
            }

            public void handleItemDelete(Object anObject)
            {
                ICommunicatorIf item = (ICommunicatorIf) anObject;
                m_items.remove(item.getObjectId());
            }

            public void handleItemModify(Object anObject)
            {
            }
        };
    }

    protected void defineAttributes()
    {
        addAttribute(m_established);
        addAttribute(m_callSign);
        addAttribute(m_toneId);
        addAttribute(m_released);
        addAttribute(m_shift);
        addAttribute(m_telephone1);
        addAttribute(m_telephone2);
        addAttribute(m_telephone3);
        addAttribute(m_status);
    }

    protected void defineLists()
    {
        addList(m_areaList);
        addList(m_assignmentList);
        addList(m_attendanceList);
        addList(m_briefingList);
        addList(m_calloutList);
        addList(m_checkpointList);
        addList(m_environmentList);
        addList(m_equipmentList);
        addList(m_eventLog);
        addList(m_forecastList);
        addList(m_hypothesisList);
        addList(m_intelligenceList);
        addList(m_messageLog);
        addList(m_messageLineList);
        addList(m_operationAreaList);
        addList(m_poiList);
        addList(m_routeList);
        addList(m_searchAreaList);
        addList(m_sketchList);
        addList(m_subjectList);
        addList(m_taskList);
        addList(m_trackList);
        addList(m_unitList);
    }

    protected void defineObjects()
    {
    }

    public char getCommunicatorNumberPrefix()
    {
        try
        {
            // Get unit resources
            ResourceBundle unitResource = Internationalization.getBundle(IUnitIf.class);
            String letter = unitResource.getString("UnitType.CP.letter");
            if (letter.length() == 1)
            {
                return letter.charAt(0);
            } else
            {
                return 'C';
            }
        }
        catch (MissingResourceException e)
        {
        }

        return 'C';
    }

    public int getCommunicatorNumber()
    {
        // TODO update when multiple command posts
        return 1;
    }

    public static CmdPostImpl implementationOf(ICmdPostIf anInterface) throws MsoCastException
    {
        try
        {
            return (CmdPostImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to CmdPostImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_CMDPOST;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(CmdPostStatus aStatus)
    {
        m_status.setValue(aStatus);
    }

    public void setStatus(String aStatus)
    {
        m_status.setValue(aStatus);
    }

    public CmdPostStatus getStatus()
    {
        return m_status.getValue();
    }

    public String getStatusText()
    {
        return m_status.getInternationalName();
    }

    public IData.DataOrigin getStatusState()
    {
        return m_status.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<CmdPostStatus> getStatusAttribute()
    {
        return m_status;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setEstablished(Calendar anEstablished)
    {
        m_established.setValue(anEstablished);
    }

    public Calendar getEstablished()
    {
        return m_established.getCalendar();
    }

    public IData.DataOrigin getEstablishedState()
    {
        return m_established.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getEstablishedAttribute()
    {
        return m_established;
    }

    public void setCallSign(String aCallSign)
    {
        m_callSign.setValue(aCallSign);
    }

    public String getCallSign()
    {
        return m_callSign.getString();
    }

    public IData.DataOrigin getCallSignState()
    {
        return m_callSign.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getCallSignAttribute()
    {
        return m_callSign;
    }

    public void setToneID(String toneId)
    {
        m_toneId.setValue(toneId);
    }

    public String getToneID()
    {
        return m_toneId.getString();
    }

    public IData.DataOrigin getToneIDState()
    {
        return m_toneId.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getToneIDAttribute()
    {
        return m_toneId;
    }

    public void setReleased(Calendar aReleased)
    {
        m_released.setValue(aReleased);
    }

    public Calendar getReleased()
    {
        return m_released.getCalendar();
    }

    public IData.DataOrigin getReleasedState()
    {
        return m_released.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getReleasedAttribute()
    {
        return m_released;
    }

    public void setShift(int aShift)
    {
        m_shift.setValue(aShift);
    }

    public int getShift()
    {
        return m_shift.intValue();
    }

    public IData.DataOrigin getShiftState()
    {
        return m_shift.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getShiftAttribute()
    {
        return m_shift;
    }

    public void setTelephone1(String aTelephone1)
    {
        m_telephone1.setValue(aTelephone1);
    }

    public String getTelephone1()
    {
        return m_telephone1.getString();
    }

    public IData.DataOrigin getTelephone1State()
    {
        return m_telephone1.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getTelephone1Attribute()
    {
        return m_telephone1;
    }

    public void setTelephone2(String aTelephone2)
    {
        m_telephone2.setValue(aTelephone2);
    }

    public String getTelephone2()
    {
        return m_telephone2.getString();
    }

    public IData.DataOrigin getTelephone2State()
    {
        return m_telephone2.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getTelephone2Attribute()
    {
        return m_telephone2;
    }

    public void setTelephone3(String aTelephone3)
    {
        m_telephone3.setValue(aTelephone3);
    }

    public String getTelephone3()
    {
        return m_telephone3.getString();
    }

    public IData.DataOrigin getTelephone3State()
    {
        return m_telephone3.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getTelephone3Attribute()
    {
        return m_telephone3;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public IAreaListIf getAreaList()
    {
        return m_areaList;
    }

    public IData.DataOrigin getAreaListState(IAreaIf anIAreaIf)
    {
        return m_areaList.getOrigin(anIAreaIf);
    }

    public Collection<IAreaIf> getAreaListItems()
    {
        return m_areaList.getObjects();
    }

    public IAssignmentListIf getAssignmentList()
    {
        return m_assignmentList;
    }

    public IData.DataOrigin getAssignmentListState(IAssignmentIf anIAssignmentIf)
    {
        return m_assignmentList.getOrigin(anIAssignmentIf);
    }

    public Collection<IAssignmentIf> getAssignmentListItems()
    {
        return m_assignmentList.getObjects();
    }

    public IPersonnelListIf getAttendanceList()
    {
        return m_attendanceList;
    }

    public IData.DataOrigin getAttendanceListState(IPersonnelIf anIPersonnelIf)
    {
        return m_attendanceList.getOrigin(anIPersonnelIf);
    }

    public Collection<IPersonnelIf> getAttendanceListItems()
    {
        return m_attendanceList.getObjects();
    }

    public IBriefingListIf getBriefingList()
    {
        return m_briefingList;
    }

    public IData.DataOrigin getBriefingListState(IBriefingIf anIBriefingIf)
    {
        return m_briefingList.getOrigin(anIBriefingIf);
    }

    public Collection<IBriefingIf> getBriefingListItems()
    {
        return m_briefingList.getObjects();
    }

    public ICalloutListIf getCalloutList()
    {
        return m_calloutList;
    }

    public IData.DataOrigin getCalloutListState(ICalloutIf anICalloutIf)
    {
        return m_calloutList.getOrigin(anICalloutIf);
    }

    public Collection<ICalloutIf> getCalloutListItems()
    {
        return m_calloutList.getObjects();
    }

    public ICheckpointListIf getCheckpointList()
    {
        return m_checkpointList;
    }

    public IData.DataOrigin getCheckpointListState(ICheckpointIf anICheckpointIf)
    {
        return m_checkpointList.getOrigin(anICheckpointIf);
    }

    public Collection<ICheckpointIf> getCheckpointListItems()
    {
        return m_checkpointList.getObjects();
    }

    public IEnvironmentListIf getEnvironmentList()
    {
        return m_environmentList;
    }

    public IData.DataOrigin getEnvironmentListState(IEnvironmentIf anIEnvironmentIf)
    {
        return m_environmentList.getOrigin(anIEnvironmentIf);
    }

    public Collection<IEnvironmentIf> getEnvironmentListItems()
    {
        return m_environmentList.getObjects();
    }

    public IEquipmentListIf getEquipmentList()
    {
        return m_equipmentList;
    }

    public IData.DataOrigin getEquipmentListState(IEquipmentIf anIEquipmentIf)
    {
        return m_equipmentList.getOrigin(anIEquipmentIf);
    }

    public Collection<IEquipmentIf> getEquipmentListItems()
    {
        return m_equipmentList.getObjects();
    }

    public IEventLogIf getEventLog()
    {
        return m_eventLog;
    }

    public IData.DataOrigin getEventLogState(IEventIf anIEventIf)
    {
        return m_eventLog.getOrigin(anIEventIf);
    }

    public Collection<IEventIf> getEventLogItems()
    {
        return m_eventLog.getObjects();
    }

    public IForecastListIf getForecastList()
    {
        return m_forecastList;
    }

    public IData.DataOrigin getForecastListState(IForecastIf anIForecastIf)
    {
        return m_forecastList.getOrigin(anIForecastIf);
    }

    public Collection<IForecastIf> getForecastListItems()
    {
        return m_forecastList.getObjects();
    }

    public IHypothesisListIf getHypothesisList()
    {
        return m_hypothesisList;
    }

    public IData.DataOrigin getHypothesisListState(IHypothesisIf anIHypothesisIf)
    {
        return m_hypothesisList.getOrigin(anIHypothesisIf);
    }

    public Collection<IHypothesisIf> getHypothesisListItems()
    {
        return m_hypothesisList.getObjects();
    }

    public IIntelligenceListIf getIntelligenceList()
    {
        return m_intelligenceList;
    }

    public IData.DataOrigin getIntelligenceListState(IIntelligenceIf anIIntelligenceIf)
    {
        return m_intelligenceList.getOrigin(anIIntelligenceIf);
    }

    public Collection<IIntelligenceIf> getIntelligenceListItems()
    {
        return m_intelligenceList.getObjects();
    }

    public IMessageLogIf getMessageLog()
    {
        return m_messageLog;
    }

    public IData.DataOrigin getMessageLogState(IMessageIf anIMessageIf)
    {
        return m_messageLog.getOrigin(anIMessageIf);
    }

    public Collection<IMessageIf> getMessageLogItems()
    {
        return m_messageLog.getObjects();
    }


    public IMessageLineListIf getMessageLines()
    {
        return m_messageLineList;
    }

    public IData.DataOrigin getMessageLineState(IMessageLineIf anIMessageLineIf)
    {
        return m_messageLineList.getOrigin(anIMessageLineIf);
    }

    public Collection<IMessageLineIf> getMessageLineItems()
    {
        return m_messageLineList.getObjects();
    }

    public IOperationAreaListIf getOperationAreaList()
    {
        return m_operationAreaList;
    }

    public IData.DataOrigin getOperationAreaListState(IOperationAreaIf anIOperationAreaIf)
    {
        return m_operationAreaList.getOrigin(anIOperationAreaIf);
    }

    public Collection<IOperationAreaIf> getOperationAreaListItems()
    {
        return m_operationAreaList.getObjects();
    }

    public IPOIListIf getPOIList()
    {
        return m_poiList;
    }

    public IData.DataOrigin getPOIListState(IPOIIf anIPOIIf)
    {
        return m_poiList.getOrigin(anIPOIIf);
    }

    public Collection<IPOIIf> getPOIListItems()
    {
        return m_poiList.getObjects();
    }

    public IRouteListIf getRouteList()
    {
        return m_routeList;
    }

    public IData.DataOrigin getRouteListState(IRouteIf anIRouteIf)
    {
        return m_routeList.getOrigin(anIRouteIf);
    }

    public Collection<IRouteIf> getRouteListItems()
    {
        return m_routeList.getObjects();
    }

    public ISearchAreaListIf getSearchAreaList()
    {
        return m_searchAreaList;
    }

    public IData.DataOrigin getSearchAreaListState(ISearchAreaIf anISearchAreaIf)
    {
        return m_searchAreaList.getOrigin(anISearchAreaIf);
    }

    public Collection<ISearchAreaIf> getSearchAreaListItems()
    {
        return m_searchAreaList.getObjects();
    }

    public ISketchListIf getSketchList()
    {
        return m_sketchList;
    }

    public IData.DataOrigin getSketchListState(ISketchIf anISketchIf)
    {
        return m_sketchList.getOrigin(anISketchIf);
    }

    public Collection<ISketchIf> getSketchListItems()
    {
        return m_sketchList.getObjects();
    }

    public ISubjectListIf getSubjectList()
    {
        return m_subjectList;
    }

    public IData.DataOrigin getSubjectListState(ISubjectIf anISubjectIf)
    {
        return m_subjectList.getOrigin(anISubjectIf);
    }

    public Collection<ISubjectIf> getSubjectListItems()
    {
        return m_subjectList.getObjects();
    }

    public ITaskListIf getTaskList()
    {
        return m_taskList;
    }

    public IData.DataOrigin getTaskListState(ITaskIf anITaskIf)
    {
        return m_taskList.getOrigin(anITaskIf);
    }

    public Collection<ITaskIf> getTaskListItems()
    {
        return m_taskList.getObjects();
    }

    public ITrackListIf getTrackList()
    {
        return m_trackList;
    }

    public IData.DataOrigin getTrackListState(ITrackIf anITrackIf)
    {
        return m_trackList.getOrigin(anITrackIf);
    }

    public Collection<ITrackIf> getTrackListItems()
    {
        return m_trackList.getObjects();
    }

    public IUnitListIf getUnitList()
    {
        return m_unitList;
    }

    public IData.DataOrigin getUnitListState(IUnitIf anIUnitIf)
    {
        return m_unitList.getOrigin(anIUnitIf);
    }

    public Collection<IUnitIf> getUnitListItems()
    {
        return m_unitList.getObjects();
    }

    /*-------------------------------------------------------------------------------------------
    * Other List accessor methods
    *-------------------------------------------------------------------------------------------*/

	@Override
	public String getCommunicatorShortName() {
		return getCommunicatorNumberPrefix() + " " + getCommunicatorNumber();
	}

    public ICommunicatorIf getCommunicator() {
    	return this;
    }

    public ITimeLineIf getTimeLine()
    {
        return m_timeLine;
    }

    public AbstractCoList<ICommunicatorIf> getCommunicatorList()
    {
        return m_communicatorList;
    }


    private final EnumSet<CmdPostStatus> m_activeCmdPostStatusSet = EnumSet.of(CmdPostStatus.IDLE, CmdPostStatus.OPERATING, CmdPostStatus.PAUSED);
    private Selector<ICommunicatorIf> m_activeCommunicatorsSelector = new Selector<ICommunicatorIf>()
    {
        public boolean select(ICommunicatorIf anObject)
        {
            if (anObject instanceof ICmdPostIf)
            {
                ICmdPostIf cmdPost = (ICmdPostIf) anObject;
                if (m_activeCmdPostStatusSet.contains(cmdPost.getStatus()))
                {
                    return true;
                } else
                {
                    return false;
                }
            } else if (anObject instanceof IUnitIf)
            {
                IUnitIf unit = (IUnitIf) anObject;
                if (IUnitIf.ACTIVE_SET.contains(unit.getStatus()))
                {
                    return true;
                } else
                {
                    return false;
                }
            } else
            {
                return false;
            }
        }
    };

    public List<ICommunicatorIf> getActiveCommunicators()
    {
        return m_communicatorList.selectItems(m_activeCommunicatorsSelector, COMMUNICATOR_COMPARATOR);
    }

    /*-------------------------------------------------------------------------------------------
    *  Methods from IHierarchicalUnitIf
    *-------------------------------------------------------------------------------------------*/

    public boolean setSuperiorUnit(IHierarchicalUnitIf aUnit)
    {
        return aUnit == null;
    }

    public IUnitIf getSuperiorUnit()
    {
        return null;
    }

    public IData.DataOrigin getSuperiorUnitState()
    {
        return IData.DataOrigin.NONE;
    }

    public IMsoRelationIf<IHierarchicalUnitIf> getSuperiorUnitAttribute()
    {
        return null;
    }

    public List<IHierarchicalUnitIf> getSubOrdinates()
    {
        return AbstractUnit.getSubOrdinates(this);
    }

}