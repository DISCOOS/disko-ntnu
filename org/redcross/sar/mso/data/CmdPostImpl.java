package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.MsoCastException;
import org.redcross.sar.util.mso.Selector;

import java.util.*;

/**
 * Command, control and communication center (command post)
 */
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
    private final AttributeImpl.MsoEnum<CmdPostStatus> m_status = new AttributeImpl.MsoEnum<CmdPostStatus>(this, "Status", CmdPostStatus.IDLE);

    private final AbstractDerivedList<ICommunicatorIf> m_communicatorList;
    private final TimeLineImpl m_timeLine = new TimeLineImpl();

    private final AreaListImpl m_areaList = new AreaListImpl(this, "AreaList", true, 100);
    private final AssignmentListImpl m_assignmentList = new AssignmentListImpl(this, "AssignmentList", true, 100);
    private final PersonnelListImpl m_attendanceList = new PersonnelListImpl(this, "AttendanceList", true, 100);
    private final BriefingListImpl m_briefingList = new BriefingListImpl(this, "BriefingList", true, 100);
    private final CalloutListImpl m_calloutList = new CalloutListImpl(this, "CalloutList", true, 100);
    private final CheckpointListImpl m_checkpointList = new CheckpointListImpl(this, "CheckpointList", true, 100);
    private final DataSourceListImpl m_dataSourceList = new DataSourceListImpl(this, "DataSourceList", true, 100);
    private final EnvironmentListImpl m_environmentList = new EnvironmentListImpl(this, "EnvironmentList", true, 100);
    private final EquipmentListImpl m_equipmentList = new EquipmentListImpl(this, "EquipmentList", true, 100);
    private final EventLogImpl m_eventLog = new EventLogImpl(this, "EventLog", true, 100);
    private final ForecastListImpl m_forecastList = new ForecastListImpl(this, "ForecastList", true, 100);
    private final HypothesisListImpl m_hypothesisList = new HypothesisListImpl(this, "HypothesisList", true, 100);
    private final IntelligenceListImpl m_intelligenceList = new IntelligenceListImpl(this, "IntelligenceList", true, 100);
    private final MessageLogImpl m_messageLog = new MessageLogImpl(this, "MessageLog", true, 100);
    private final MessageLineListImpl m_messageLineList = new MessageLineListImpl(this, "MessageLineList", true, 1000);
    private final OperationAreaListImpl m_operationAreaList = new OperationAreaListImpl(this, "OperationAreaList", true, 100);
    private final POIListImpl m_pOIList = new POIListImpl(this, "POIList", true, 100);
    private final RouteListImpl m_routeList = new RouteListImpl(this, "RouteList", true, 100);
    private final SearchAreaListImpl m_searchAreaList = new SearchAreaListImpl(this, "SearchAreaList", true, 100);
    private final SketchListImpl m_sketchList = new SketchListImpl(this, "SketchList", true, 100);
    private final SubjectListImpl m_subjectList = new SubjectListImpl(this, "SubjectList", true, 100);
    private final TaskListImpl m_taskList = new TaskListImpl(this, "TaskList", true, 100);
    private final TrackListImpl m_trackList = new TrackListImpl(this, "TrackList", true, 100);
    private final UnitListImpl m_unitList = new UnitListImpl(this, "UnitList", true, 100);

    public CmdPostImpl(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(anObjectId);
        m_status.setValue(CmdPostStatus.IDLE);

        m_communicatorList = createCommunicatorList();
    }

    AbstractDerivedList<ICommunicatorIf> createCommunicatorList()
    {
        return new AbstractDerivedList<ICommunicatorIf>()
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
        addList(m_dataSourceList);
        addList(m_environmentList);
        addList(m_equipmentList);
        addList(m_eventLog);
        addList(m_forecastList);
        addList(m_hypothesisList);
        addList(m_intelligenceList);
        addList(m_messageLog);
        addList(m_messageLineList);
        addList(m_operationAreaList);
        addList(m_pOIList);
        addList(m_routeList);
        addList(m_searchAreaList);
        addList(m_sketchList);
        addList(m_subjectList);
        addList(m_taskList);
        addList(m_trackList);
        addList(m_unitList);
    }

    protected void defineReferences()
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

    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return true;
    }

    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return true;
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

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
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

    public IMsoModelIf.ModificationState getStatusState()
    {
        return m_status.getState();
    }

    public IAttributeIf.IMsoEnumIf<CmdPostStatus> getStatusAttribute()
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

    public IMsoModelIf.ModificationState getEstablishedState()
    {
        return m_established.getState();
    }

    public IAttributeIf.IMsoCalendarIf getEstablishedAttribute()
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

    public IMsoModelIf.ModificationState getCallSignState()
    {
        return m_callSign.getState();
    }

    public IAttributeIf.IMsoStringIf getCallSignAttribute()
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

    public IMsoModelIf.ModificationState getToneIDState()
    {
        return m_toneId.getState();
    }

    public IAttributeIf.IMsoStringIf getToneIDAttribute()
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

    public IMsoModelIf.ModificationState getReleasedState()
    {
        return m_released.getState();
    }

    public IAttributeIf.IMsoCalendarIf getReleasedAttribute()
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

    public IMsoModelIf.ModificationState getShiftState()
    {
        return m_shift.getState();
    }

    public IAttributeIf.IMsoIntegerIf getShiftAttribute()
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

    public IMsoModelIf.ModificationState getTelephone1State()
    {
        return m_telephone1.getState();
    }

    public IAttributeIf.IMsoStringIf getTelephone1Attribute()
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

    public IMsoModelIf.ModificationState getTelephone2State()
    {
        return m_telephone2.getState();
    }

    public IAttributeIf.IMsoStringIf getTelephone2Attribute()
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

    public IMsoModelIf.ModificationState getTelephone3State()
    {
        return m_telephone3.getState();
    }

    public IAttributeIf.IMsoStringIf getTelephone3Attribute()
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

    public IMsoModelIf.ModificationState getAreaListState(IAreaIf anIAreaIf)
    {
        return m_areaList.getState(anIAreaIf);
    }

    public Collection<IAreaIf> getAreaListItems()
    {
        return m_areaList.getItems();
    }

    public IAssignmentListIf getAssignmentList()
    {
        return m_assignmentList;
    }

    public IMsoModelIf.ModificationState getAssignmentListState(IAssignmentIf anIAssignmentIf)
    {
        return m_assignmentList.getState(anIAssignmentIf);
    }

    public Collection<IAssignmentIf> getAssignmentListItems()
    {
        return m_assignmentList.getItems();
    }

    public IPersonnelListIf getAttendanceList()
    {
        return m_attendanceList;
    }

    public IMsoModelIf.ModificationState getAttendanceListState(IPersonnelIf anIPersonnelIf)
    {
        return m_attendanceList.getState(anIPersonnelIf);
    }

    public Collection<IPersonnelIf> getAttendanceListItems()
    {
        return m_attendanceList.getItems();
    }

    public IBriefingListIf getBriefingList()
    {
        return m_briefingList;
    }

    public IMsoModelIf.ModificationState getBriefingListState(IBriefingIf anIBriefingIf)
    {
        return m_briefingList.getState(anIBriefingIf);
    }

    public Collection<IBriefingIf> getBriefingListItems()
    {
        return m_briefingList.getItems();
    }

    public ICalloutListIf getCalloutList()
    {
        return m_calloutList;
    }

    public IMsoModelIf.ModificationState getCalloutListState(ICalloutIf anICalloutIf)
    {
        return m_calloutList.getState(anICalloutIf);
    }

    public Collection<ICalloutIf> getCalloutListItems()
    {
        return m_calloutList.getItems();
    }

    public ICheckpointListIf getCheckpointList()
    {
        return m_checkpointList;
    }

    public IMsoModelIf.ModificationState getCheckpointListState(ICheckpointIf anICheckpointIf)
    {
        return m_checkpointList.getState(anICheckpointIf);
    }

    public Collection<ICheckpointIf> getCheckpointListItems()
    {
        return m_checkpointList.getItems();
    }

    public IDataSourceListIf getDataSourceList()
    {
        return m_dataSourceList;
    }

    public IMsoModelIf.ModificationState getDataSourceListState(IDataSourceIf anIDataSourceIf)
    {
        return m_dataSourceList.getState(anIDataSourceIf);
    }

    public Collection<IDataSourceIf> getDataSourceListItems()
    {
        return m_dataSourceList.getItems();
    }

    public IEnvironmentListIf getEnvironmentList()
    {
        return m_environmentList;
    }

    public IMsoModelIf.ModificationState getEnvironmentListState(IEnvironmentIf anIEnvironmentIf)
    {
        return m_environmentList.getState(anIEnvironmentIf);
    }

    public Collection<IEnvironmentIf> getEnvironmentListItems()
    {
        return m_environmentList.getItems();
    }

    public IEquipmentListIf getEquipmentList()
    {
        return m_equipmentList;
    }

    public IMsoModelIf.ModificationState getEquipmentListState(IEquipmentIf anIEquipmentIf)
    {
        return m_equipmentList.getState(anIEquipmentIf);
    }

    public Collection<IEquipmentIf> getEquipmentListItems()
    {
        return m_equipmentList.getItems();
    }

    public IEventLogIf getEventLog()
    {
        return m_eventLog;
    }

    public IMsoModelIf.ModificationState getEventLogState(IEventIf anIEventIf)
    {
        return m_eventLog.getState(anIEventIf);
    }

    public Collection<IEventIf> getEventLogItems()
    {
        return m_eventLog.getItems();
    }

    public IForecastListIf getForecastList()
    {
        return m_forecastList;
    }

    public IMsoModelIf.ModificationState getForecastListState(IForecastIf anIForecastIf)
    {
        return m_forecastList.getState(anIForecastIf);
    }

    public Collection<IForecastIf> getForecastListItems()
    {
        return m_forecastList.getItems();
    }

    public IHypothesisListIf getHypothesisList()
    {
        return m_hypothesisList;
    }

    public IMsoModelIf.ModificationState getHypothesisListState(IHypothesisIf anIHypothesisIf)
    {
        return m_hypothesisList.getState(anIHypothesisIf);
    }

    public Collection<IHypothesisIf> getHypothesisListItems()
    {
        return m_hypothesisList.getItems();
    }

    public IIntelligenceListIf getIntelligenceList()
    {
        return m_intelligenceList;
    }

    public IMsoModelIf.ModificationState getIntelligenceListState(IIntelligenceIf anIIntelligenceIf)
    {
        return m_intelligenceList.getState(anIIntelligenceIf);
    }

    public Collection<IIntelligenceIf> getIntelligenceListItems()
    {
        return m_intelligenceList.getItems();
    }

    public IMessageLogIf getMessageLog()
    {
        return m_messageLog;
    }

    public IMsoModelIf.ModificationState getMessageLogState(IMessageIf anIMessageIf)
    {
        return m_messageLog.getState(anIMessageIf);
    }

    public Collection<IMessageIf> getMessageLogItems()
    {
        return m_messageLog.getItems();
    }


    public IMessageLineListIf getMessageLines()
    {
        return m_messageLineList;
    }

    public IMsoModelIf.ModificationState getMessageLineState(IMessageLineIf anIMessageLineIf)
    {
        return m_messageLineList.getState(anIMessageLineIf);
    }

    public Collection<IMessageLineIf> getMessageLineItems()
    {
        return m_messageLineList.getItems();
    }

    public IOperationAreaListIf getOperationAreaList()
    {
        return m_operationAreaList;
    }

    public IMsoModelIf.ModificationState getOperationAreaListState(IOperationAreaIf anIOperationAreaIf)
    {
        return m_operationAreaList.getState(anIOperationAreaIf);
    }

    public Collection<IOperationAreaIf> getOperationAreaListItems()
    {
        return m_operationAreaList.getItems();
    }

    public IPOIListIf getPOIList()
    {
        return m_pOIList;
    }

    public IMsoModelIf.ModificationState getPOIListState(IPOIIf anIPOIIf)
    {
        return m_pOIList.getState(anIPOIIf);
    }

    public Collection<IPOIIf> getPOIListItems()
    {
        return m_pOIList.getItems();
    }

    public IRouteListIf getRouteList()
    {
        return m_routeList;
    }

    public IMsoModelIf.ModificationState getRouteListState(IRouteIf anIRouteIf)
    {
        return m_routeList.getState(anIRouteIf);
    }

    public Collection<IRouteIf> getRouteListItems()
    {
        return m_routeList.getItems();
    }

    public ISearchAreaListIf getSearchAreaList()
    {
        return m_searchAreaList;
    }

    public IMsoModelIf.ModificationState getSearchAreaListState(ISearchAreaIf anISearchAreaIf)
    {
        return m_searchAreaList.getState(anISearchAreaIf);
    }

    public Collection<ISearchAreaIf> getSearchAreaListItems()
    {
        return m_searchAreaList.getItems();
    }

    public ISketchListIf getSketchList()
    {
        return m_sketchList;
    }

    public IMsoModelIf.ModificationState getSketchListState(ISketchIf anISketchIf)
    {
        return m_sketchList.getState(anISketchIf);
    }

    public Collection<ISketchIf> getSketchListItems()
    {
        return m_sketchList.getItems();
    }

    public ISubjectListIf getSubjectList()
    {
        return m_subjectList;
    }

    public IMsoModelIf.ModificationState getSubjectListState(ISubjectIf anISubjectIf)
    {
        return m_subjectList.getState(anISubjectIf);
    }

    public Collection<ISubjectIf> getSubjectListItems()
    {
        return m_subjectList.getItems();
    }

    public ITaskListIf getTaskList()
    {
        return m_taskList;
    }

    public IMsoModelIf.ModificationState getTaskListState(ITaskIf anITaskIf)
    {
        return m_taskList.getState(anITaskIf);
    }

    public Collection<ITaskIf> getTaskListItems()
    {
        return m_taskList.getItems();
    }

    public ITrackListIf getTrackList()
    {
        return m_trackList;
    }

    public IMsoModelIf.ModificationState getTrackListState(ITrackIf anITrackIf)
    {
        return m_trackList.getState(anITrackIf);
    }

    public Collection<ITrackIf> getTrackListItems()
    {
        return m_trackList.getItems();
    }

    public IUnitListIf getUnitList()
    {
        return m_unitList;
    }

    public IMsoModelIf.ModificationState getUnitListState(IUnitIf anIUnitIf)
    {
        return m_unitList.getState(anIUnitIf);
    }

    public Collection<IUnitIf> getUnitListItems()
    {
        return m_unitList.getItems();
    }

    /*-------------------------------------------------------------------------------------------
    * Other List accessor methods
    *-------------------------------------------------------------------------------------------*/

    public ITimeLineIf getTimeLine()
    {
        return m_timeLine;
    }

    public AbstractDerivedList<ICommunicatorIf> getCommunicatorList()
    {
        return m_communicatorList;
    }


    private final EnumSet<UnitStatus> m_activeUnitStatusSet = EnumSet.range(UnitStatus.READY, UnitStatus.PENDING);
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
                if (m_activeUnitStatusSet.contains(unit.getStatus()))
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

    public IMsoModelIf.ModificationState getSuperiorUnitState()
    {
        return IMsoModelIf.ModificationState.STATE_UNDEFINED;
    }

    public IMsoReferenceIf<IHierarchicalUnitIf> getSuperiorUnitAttribute()
    {
        return null;
    }

    public List<IHierarchicalUnitIf> getSubOrdinates()
    {
        return AbstractUnit.getSubOrdinates(this);
    }

}