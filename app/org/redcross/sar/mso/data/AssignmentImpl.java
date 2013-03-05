package org.redcross.sar.mso.data;

import org.redcross.sar.Application;
import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.util.AssignmentUtilities;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.except.MsoCastException;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Unit assignments
 */
public class AssignmentImpl extends AbstractMsoObject implements IAssignmentIf
{
    private final AttributeImpl.MsoString m_remarks = new AttributeImpl.MsoString(this, "Remarks");
    private final AttributeImpl.MsoInteger m_prioritySequence = new AttributeImpl.MsoInteger(this, "PrioritySequence");
    private final AttributeImpl.MsoCalendar m_timeEstimatedFinished = new AttributeImpl.MsoCalendar(this, "TimeEstimatedFinished");


    private final AttributeImpl.MsoEnum<AssignmentPriority> m_priority = new AttributeImpl.MsoEnum<AssignmentPriority>(this, "Priority", 1, AssignmentPriority.NORMAL);
    private final AttributeImpl.MsoEnum<AssignmentStatus> m_status = new AttributeImpl.MsoEnum<AssignmentStatus>(this, "Status", 0, AssignmentStatus.EMPTY);
    private final AttributeImpl.MsoEnum<AssignmentType> m_type = new AttributeImpl.MsoEnum<AssignmentType>(this, "Type", 0, AssignmentType.GENERAL);

    private final AttributeImpl.MsoInteger m_number = new AttributeImpl.MsoInteger(this, "Number", true);

    private final EquipmentListImpl m_assignmentEquipment = new EquipmentListImpl(this, "AssignmentEquipment", false);
    private final POIListImpl m_assignmentFindings = new POIListImpl(this, "AssignmentFindings", false);

    private final MsoRelationImpl<IBriefingIf> m_assignmentBriefing = new MsoRelationImpl<IBriefingIf>(this, "AssignmentBriefing", 0, true, null);
    private final MsoRelationImpl<IAreaIf> m_plannedArea = new MsoRelationImpl<IAreaIf>(this, "PlannedArea", 0, true, null);
    private final MsoRelationImpl<IAreaIf> m_reportedArea = new MsoRelationImpl<IAreaIf>(this, "ReportedArea", 0, true, null);

    private final static TypeMessageLineSelector messageLineTypeSelector = new TypeMessageLineSelector();

    private final static SelfSelector<IAssignmentIf, IUnitIf> owningUnitSelector = new SelfSelector<IAssignmentIf, IUnitIf>()
    {
        public boolean select(IUnitIf anObject)
        {
            return (anObject.getUnitAssignments().exists(m_object));
        }
    };

    public static String getText(String aKey)
    {
        return Internationalization.getString(Internationalization.getBundle(IAssignmentIf.class), aKey);
    }

    public AssignmentImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(theMsoModel, anObjectId);
        setNumber(aNumber);
        setType(getTypeBySubclass());
    }

    protected void defineAttributes()
    {
        addAttribute(m_remarks);
        addAttribute(m_prioritySequence);
        addAttribute(m_timeEstimatedFinished);
        addAttribute(m_number);
        addAttribute(m_status);
        addAttribute(m_priority);
        addAttribute(m_type);
    }

    protected void defineLists()
    {
        addList(m_assignmentEquipment);
        addList(m_assignmentFindings);
    }

    protected void defineObjects()
    {
        addObject(m_assignmentBriefing);
        addObject(m_plannedArea);
        addObject(m_reportedArea);
    }

    public void addListRelation(IMsoObjectIf anObject, String aReferenceListName)
    {
        if (anObject instanceof IPOIIf)
        {
            m_assignmentFindings.add((IPOIIf) anObject);
        }
        if (anObject instanceof IEquipmentIf)
        {
            m_assignmentEquipment.add((IEquipmentIf) anObject);
        }
    }

    public void removeListRelation(IMsoObjectIf anObject, String aReferenceListName)
    {
        if (anObject instanceof IPOIIf)
        {
            m_assignmentFindings.remove((IPOIIf) anObject);
        }
        if (anObject instanceof IEquipmentIf)
        {
            m_assignmentEquipment.remove((IEquipmentIf) anObject);
        }
    }

    protected AssignmentType getTypeBySubclass()
    {
        return AssignmentType.GENERAL;
    }

    /**
     * Local implementation of {@link AbstractMsoObject#registerModifiedData()}
     * Resets correct subclass in case of incorrect changes by application or others.
     * Renumber duplicate numbers
     */
    @Override
    public void registerModifiedData(IChangeIf aChange)
    {
        if (getType() != getTypeBySubclass())
        {
            setType(getTypeBySubclass());
        }
        /*
        if (getPlannedArea() != null)
        {
            ((AreaImpl) getPlannedArea()).registerModifiedData(
            		getPlannedArea(),aMode,updateServer,isLoopback,isRollback);
        }
        if (getReportedArea() != null)
        {
            ((AreaImpl) getReportedArea()).registerModifiedData(
            		getReportedArea(),aMode,updateServer,isLoopback,isRollback);
        }
        */
        super.registerModifiedData(aChange);
    }

    public static AssignmentImpl implementationOf(IAssignmentIf anInterface) throws MsoCastException
    {
        try
        {
            return (AssignmentImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to AssignmentImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT;
    }

    public int POICount()
    {
        return m_assignmentFindings.size();
    }

    /*-------------------------------------------------------------------------------------------
     * Methods for IEnumStatusHolder<IAssignmentIf>
     *-------------------------------------------------------------------------------------------*/

    public void setStatus(AssignmentStatus aStatus) throws IllegalOperationException
    {
        setOwningUnit(aStatus, getOwningUnit());
    }

    public void setStatus(String aStatus) throws IllegalOperationException
    {
        AssignmentStatus status = getStatusAttribute().enumValue(aStatus);
        if (status == null)
        {
            return;
        }
        setOwningUnit(status, getOwningUnit());
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public AssignmentStatus getStatus()
    {
        return m_status.get();
    }

    public IData.DataOrigin getStatusState()
    {
        return m_status.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<AssignmentStatus> getStatusAttribute()
    {
        return m_status;
    }

    public String getStatusText()
    {
        return m_status.getInternationalName();
    }

    public void setPriority(AssignmentPriority aPriority)
    {
        m_priority.set(aPriority);
    }

    public void setPriority(String aPriority)
    {
        m_priority.set(aPriority);
    }

    public int comparePriorityTo(IEnumPriorityHolder<AssignmentPriority> anObject)
    {
        return getPriority().compareTo(anObject.getPriority());
    }

    public AssignmentPriority getPriority()
    {
        return m_priority.get();
    }

    public IData.DataOrigin getPriorityState()
    {
        return m_priority.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<AssignmentPriority> getPriorityAttribute()
    {
        return m_priority;
    }

    public String getPriorityText()
    {
        return m_priority.getInternationalName();
    }


    protected void setType(AssignmentType aType)
    {
        m_type.set(aType);
    }

    public AssignmentType getType()
    {
        return m_type.get();
    }

    public IData.DataOrigin getTypeState()
    {
        return m_type.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<AssignmentType> getTypeAttribute()
    {
        return m_type;
    }

    public String getInternationalTypeName()
    {
        return m_type.getInternationalName();
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

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

    public void setPrioritySequence(int aPrioritySequence)
    {
        m_prioritySequence.setValue(aPrioritySequence);
    }

    public int getPrioritySequence()
    {
        return m_prioritySequence.intValue();
    }

    public IData.DataOrigin getPrioritySequenceState()
    {
        return m_prioritySequence.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getPrioritySequenceAttribute()
    {
        return m_prioritySequence;
    }

    // From ISerialNumberedIf
    public void setNumber(int aNumber)
    {
        setNumber(m_number,aNumber);
    }

    public int getNumber()
    {
        return m_number.intValue();
    }

    public IData.DataOrigin getNumberState()
    {
        return m_number.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getNumberAttribute()
    {
        return m_number;
    }

    public void setTimeEstimatedFinished(Calendar aTimeEstimatedFinished)
    {
        m_timeEstimatedFinished.set(aTimeEstimatedFinished);
    }

    public Calendar getTimeEstimatedFinished()
    {
        return m_timeEstimatedFinished.getCalendar();
    }

    public IData.DataOrigin getTimeEstimatedFinishedState()
    {
        return m_timeEstimatedFinished.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getTimeEstimatedFinishedAttribute()
    {
        return m_timeEstimatedFinished;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addAssignmentEquipment(IEquipmentIf anIEquipmentIf)
    {
        m_assignmentEquipment.add(anIEquipmentIf);
    }

    public IEquipmentListIf getAssignmentEquipment()
    {
        return m_assignmentEquipment;
    }

    public IData.DataOrigin getAssignmentEquipmentState(IEquipmentIf anIEquipmentIf)
    {
        return m_assignmentEquipment.getOrigin(anIEquipmentIf);
    }

    public Collection<IEquipmentIf> getAssignmentEquipmentItems()
    {
        return m_assignmentEquipment.getObjects();
    }

    public void addAssignmentFinding(IPOIIf anIPOIIf)
    {
        m_assignmentFindings.add(anIPOIIf);
    }

    public IPOIListIf getAssignmentFindings()
    {
        return m_assignmentFindings;
    }

    public IData.DataOrigin getAssignmentFindingsState(IPOIIf anIPOIIf)
    {
        return m_assignmentFindings.getOrigin(anIPOIIf);
    }

    public Collection<IPOIIf> getAssignmentFindingsItems()
    {
        return m_assignmentFindings.getObjects();
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public IUnitIf getOwningUnit()
    {
        owningUnitSelector.setSelfObject(this);
        return Application.getInstance().getMsoModel().getMsoManager().getCmdPost().getUnitList().selectSingleItem(owningUnitSelector);
    }

    public void setOwningUnit(AssignmentStatus aStatus, IUnitIf aUnit) throws IllegalOperationException
    {
    	Object[] ans = AssignmentUtilities.verifyMove(this, aUnit, aStatus);
        if (Integer.valueOf(ans[0].toString())<0)
        {
            throw new IllegalOperationException(ans[1].toString());
        }
        changeOwnership(aUnit, aStatus);
    }

    public void setAssignmentBriefing(IBriefingIf aBriefing)
    {
        m_assignmentBriefing.set(aBriefing);
    }

    public IBriefingIf getAssignmentBriefing()
    {
        return m_assignmentBriefing.get();
    }

    public IData.DataOrigin getAssignmentBriefingState()
    {
        return m_assignmentBriefing.getOrigin();
    }

    public IMsoRelationIf<IBriefingIf> getAssignmentBriefingAttribute()
    {
        return m_assignmentBriefing;
    }

    public void setPlannedArea(IAreaIf anArea) throws IllegalOperationException
    {
        anArea.verifyAssignable(this);
        m_plannedArea.set(anArea);
    }

    public IAreaIf getPlannedArea()
    {
        return m_plannedArea.get();
    }

    public IData.DataOrigin getPlannedAreaState()
    {
        return m_plannedArea.getOrigin();
    }

    public IMsoRelationIf<IAreaIf> getPlannedAreaAttribute()
    {
        return m_plannedArea;
    }

    public void setReportedArea(IAreaIf anArea) throws IllegalOperationException
    {
        anArea.verifyAssignable(this);
        m_reportedArea.set(anArea);
    }

    public IAreaIf getReportedArea()
    {
        return m_reportedArea.get();
    }

    public IData.DataOrigin getReportedAreaState()
    {
        return m_reportedArea.getOrigin();
    }

    public IMsoRelationIf<IAreaIf> getReportedAreaAttribute()
    {
        return m_reportedArea;
    }

    /*-------------------------------------------------------------------------------------------
     * Other Methods
     *-------------------------------------------------------------------------------------------*/

	public String getDefaultName()
	{
		return (getInternationalTypeName() + " " + getNumber()).trim();
	}

    public Calendar getTime(Enum<?> aStatus)
    {
    	// translate status type
    	if(aStatus instanceof AssignmentStatus)
    	{
	        return getStatusAttribute().getLastTime((AssignmentStatus)aStatus);
    	}
    	else if(aStatus instanceof MessageLineType)
    	{
            return getMessageLineTime((MessageLineType)aStatus);
    	}
        // failed
        return null;
    }

    public boolean isNotReady()
    {
        return getStatus().ordinal() < AssignmentStatus.READY.ordinal();
    }

    public boolean hasBeenEnqueued()
    {
        return getStatus().ordinal() >= AssignmentStatus.QUEUED.ordinal();
    }

    public boolean hasBeenAllocated()
    {
        return getStatus().ordinal() >= AssignmentStatus.ALLOCATED.ordinal();
    }

    public boolean hasBeenStarted()
    {
    	return getStatus().ordinal() >= AssignmentStatus.EXECUTING.ordinal();
    }

    public boolean hasBeenFinished()
    {
    	return getStatus().ordinal() >= AssignmentStatus.FINISHED.ordinal();
    }

    public boolean hasBeenAborted() {
    	return AssignmentStatus.ABORTED.equals(getStatus());
    }

    public boolean hasBeenReported() {
    	return AssignmentStatus.REPORTED.equals(getStatus());
    }

    public IMessageLineIf getLatestStatusChangeMessageLine(MessageLineType aType)
    {
        messageLineTypeSelector.setSelectionCriteria(this, aType);
        List<IMessageLineIf> retVal = m_model.getMsoManager()
        	.getCmdPost().getMessageLines().selectItems(
        			messageLineTypeSelector, IMessageLineIf.MESSAGE_LINE_TIME_COMPARATOR);
        return (retVal.size() == 0) ? null : retVal.get(0);
    }

    public boolean transferMessageConfirmed()
    {
        return transferMessageConfirmed(getStatus());
    }

    public boolean transferMessageConfirmed(AssignmentStatus aStatus)
    {
        MessageLineType searchLineType;
        switch (aStatus)
        {
            case ALLOCATED:
                searchLineType = MessageLineType.ALLOCATED;
                break;
            case EXECUTING:
                searchLineType = MessageLineType.STARTED;
                break;
            case FINISHED:
            case REPORTED:
                searchLineType = MessageLineType.COMPLETED;
                break;
            default:
                return true;
        }
        IMessageLineIf foundMessageLine = getLatestStatusChangeMessageLine(searchLineType);
        if (foundMessageLine == null)
        {
            return true;
        }
        IMessageIf owningMessage = foundMessageLine.getOwningMessage();
        return owningMessage == null || owningMessage.getStatus().equals(IMessageIf.MessageStatus.CONFIRMED);
    }

    private final static SelfSelector<IAssignmentIf, IMessageLineIf> referringMesssageLineSelector = new SelfSelector<IAssignmentIf, IMessageLineIf>()
    {
        public boolean select(IMessageLineIf anObject)
        {
            return (m_object.equals(anObject.getLineAssignment()));
        }
    };

    public Set<IMessageLineIf> getReferringMessageLines()
    {
        referringMesssageLineSelector.setSelfObject(this);
        return Application.getInstance().getMsoModel().getMsoManager().getCmdPost().getMessageLines().selectItems(referringMesssageLineSelector);
    }

    public Set<IMessageLineIf> getReferringMessageLines(Collection<IMessageLineIf> aCollection)
    {
        referringMesssageLineSelector.setSelfObject(this);
        return MsoListImpl.selectItemsInCollection(referringMesssageLineSelector,aCollection);
    }

    /*-------------------------------------------------------------------------------------------
     * Helper Methods
     *-------------------------------------------------------------------------------------------*/

    private Calendar getMessageLineTime(MessageLineType aLineType)
    {
        IMessageLineIf line = getLatestStatusChangeMessageLine(aLineType);
        if (line != null)
        {
            return line.getLineTime();
        }
        return null;
    }

    private void changeOwnership(IUnitIf aUnit, AssignmentStatus aStatus) throws IllegalOperationException
    {

    	// initialize
        IUnitIf owner = getOwningUnit();
        AssignmentStatus oldStatus = getStatus();

        // is a change in ownership required?
        boolean changeOwner = aUnit != owner;

        // suspend MSO update
		suspendChange();

		// remove this from owner?
		if (changeOwner && owner != null) {
			((AbstractUnit) owner).removeUnitAssignment(this);
		}
		// update status
		m_status.set(aStatus);
		/*
		 * if assignment is going to be enqueue, then
		 * add as tail which is default
		 */
		if (aStatus == AssignmentStatus.QUEUED) {
			setPrioritySequence(Integer.MAX_VALUE);
		}
		if (aUnit != null) {
			// was owner changed?
			if (changeOwner) {
				/*
				 * add to unit using the helper method. This ensures
				 * that unit status is updated accordingly.
				 */
				((AbstractUnit) aUnit).addUnitAssignment(this);

			} else {
				// notify unit that assignment has changed
				((AbstractUnit) aUnit).assignmentChanged(this, oldStatus, true);
			}
		}
		// resume MSO update
		resumeChange(true);

    }

    /*-------------------------------------------------------------------------------------------
     * Inner classes
     *-------------------------------------------------------------------------------------------*/

    static class TypeMessageLineSelector extends SelfSelector<IAssignmentIf, IMessageLineIf>
    {
        MessageLineType m_type;

        void setSelectionCriteria(IAssignmentIf anAssignment,MessageLineType aType)
        {
            setSelfObject(anAssignment);
            m_type = aType;
        }

        public boolean select(IMessageLineIf anObject)
        {
            return (anObject.getLineAssignment() == m_object && anObject.getLineType() == m_type && anObject.getLineTime() != null);
        }
    }



}