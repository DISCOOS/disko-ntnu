package org.redcross.sar.mso.data;

import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.except.IllegalOperationException;

/**
 *
 */
public interface IAssignmentIf extends IMsoObjectIf, ISerialNumberedIf, IEnumStatusHolder<IAssignmentIf.AssignmentStatus>, IEnumPriorityHolder<IAssignmentIf.AssignmentPriority>
{
    public static final String bundleName = "org.redcross.sar.mso.data.properties.Assignment";

    public enum AssignmentStatus
    {
        EMPTY,
        DRAFT,
        READY,
        QUEUED,
        ALLOCATED,
        EXECUTING,
        ABORTED,
        FINISHED,
        REPORTED
    }

    public enum AssignmentPriority
    {
        HIGH,
        NORMAL,
        LOW,
        NONE
    }

    public enum AssignmentType
    {
        GENERAL,
        ASSISTANCE,
        SEARCH
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public AssignmentType getType();

    public IMsoModelIf.ModificationState getTypeState();

    public IAttributeIf.IMsoEnumIf<AssignmentType> getTypeAttribute();

    public String getInternationalTypeName();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IMsoModelIf.ModificationState getRemarksState();

    public IAttributeIf.IMsoStringIf getRemarksAttribute();

    public void setPrioritySequence(int aPrioritySequence);

    public int getPrioritySequence();

    public IMsoModelIf.ModificationState getPrioritySequenceState();

    public IAttributeIf.IMsoIntegerIf getPrioritySequenceAttribute();

    public void setTimeEstimatedFinished(Calendar aTimeEstimatedFinished);

    public Calendar getTimeEstimatedFinished();

    public IMsoModelIf.ModificationState getTimeEstimatedFinishedState();

    public IAttributeIf.IMsoCalendarIf getTimeEstimatedFinishedAttribute();


    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addAssignmentEquipment(IEquipmentIf anIEquipmentIf);

    public IEquipmentListIf getAssignmentEquipment();

    public IMsoModelIf.ModificationState getAssignmentEquipmentState(IEquipmentIf anIEquipmentIf);

    public Collection<IEquipmentIf> getAssignmentEquipmentItems();

    public void addAssignmentFinding(IPOIIf anIPOIIf);

    public IPOIListIf getAssignmentFindings();

    public IMsoModelIf.ModificationState getAssignmentFindingsState(IPOIIf anIPOIIf);

    public Collection<IPOIIf> getAssignmentFindingsItems();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public IUnitIf getOwningUnit();

    public void setOwningUnit(AssignmentStatus aStatus, IUnitIf aUnit) throws IllegalOperationException;

    public void setAssignmentBriefing(IBriefingIf aBriefing);

    public IBriefingIf getAssignmentBriefing();

    public IMsoModelIf.ModificationState getAssignmentBriefingState();

    public IMsoReferenceIf<IBriefingIf> getAssignmentBriefingAttribute();

    /**
     * Assign planned area
     *
     * @param anArea The area to assign
     * @throws IllegalOperationException If the area has been allocated to another assignment.
     */
    public void setPlannedArea(IAreaIf anArea) throws IllegalOperationException;

    public IAreaIf getPlannedArea();

    public IMsoModelIf.ModificationState getPlannedAreaState();

    public IMsoReferenceIf<IAreaIf> getPlannedAreaAttribute();

    /**
     * Assign reported area
     *
     * @param anArea The area to assign
     * @throws IllegalOperationException If the area has been allocated to another assignment.
     */
    public void setReportedArea(IAreaIf anArea) throws IllegalOperationException;

    public IAreaIf getReportedArea();

    public IMsoModelIf.ModificationState getReportedAreaState();

    public IMsoReferenceIf<IAreaIf> getReportedAreaAttribute();

    /*-------------------------------------------------------------------------------------------
    * Other methods
    *-------------------------------------------------------------------------------------------*/

	public String getDefaultName();

    public Calendar getTime(Enum<?> aStatus);

    public boolean isNotReady();

    public boolean hasBeenEnqueued();

    public boolean hasBeenAllocated();

    public boolean hasBeenStarted();

    public boolean hasBeenFinished();

    public boolean hasBeenAborted();

    public boolean hasBeenReported();

    /**
     * Often uses status sets
     */
    public static final EnumSet<AssignmentStatus> PENDING_SET = EnumSet.range(AssignmentStatus.DRAFT, AssignmentStatus.ALLOCATED);
    public static final EnumSet<AssignmentStatus> ACTIVE_SET = EnumSet.range(AssignmentStatus.READY, AssignmentStatus.EXECUTING);
    public static final EnumSet<AssignmentStatus> WORKING_SET = EnumSet.of(AssignmentStatus.EXECUTING);
    public static final EnumSet<AssignmentStatus> FINISHED_SET = EnumSet.of(AssignmentStatus.ABORTED, AssignmentStatus.FINISHED);
    public static final EnumSet<AssignmentStatus> FINISHED_AND_REPORTED_SET = EnumSet.of(AssignmentStatus.ABORTED, AssignmentStatus.FINISHED, AssignmentStatus.REPORTED);
    public static final EnumSet<AssignmentStatus> DELETABLE_SET = EnumSet.range(AssignmentStatus.EMPTY, AssignmentStatus.ALLOCATED);

    /**
     * Often uses selectors
     */
    public static final Selector<IAssignmentIf> ALL_SELECTOR = new Selector<IAssignmentIf>() {

		public boolean select(IAssignmentIf anObject) {
			return true;
		}

    };

    public static final AbstractMsoObject.StatusSelector<IAssignmentIf, AssignmentStatus> READY_SELECTOR = new AbstractMsoObject.StatusSelector<IAssignmentIf, AssignmentStatus>(AssignmentStatus.READY);
    public static final AbstractMsoObject.StatusSelector<IAssignmentIf, AssignmentStatus> QUEUED_SELECTOR = new AbstractMsoObject.StatusSelector<IAssignmentIf, AssignmentStatus>(AssignmentStatus.QUEUED);
    public static final AbstractMsoObject.StatusSelector<IAssignmentIf, AssignmentStatus> ALLOCATED_SELECTOR = new AbstractMsoObject.StatusSelector<IAssignmentIf, AssignmentStatus>(AssignmentStatus.ALLOCATED);
    public static final AbstractMsoObject.StatusSelector<IAssignmentIf, AssignmentStatus> EXECUTING_SELECTOR = new AbstractMsoObject.StatusSelector<IAssignmentIf, AssignmentStatus>(AssignmentStatus.EXECUTING);
    public static final AbstractMsoObject.StatusSetSelector<IAssignmentIf, AssignmentStatus> ACTIVE_SELECTOR = new AbstractMsoObject.StatusSetSelector<IAssignmentIf, AssignmentStatus>(ACTIVE_SET);
    public static final AbstractMsoObject.StatusSetSelector<IAssignmentIf, AssignmentStatus> FINISHED_SELECTOR = new AbstractMsoObject.StatusSetSelector<IAssignmentIf, AssignmentStatus>(FINISHED_AND_REPORTED_SET);

    /**
     * Often used comparators
     */

	public static final Comparator<IAssignmentIf> ASSIGNMENT_TYPE_NUMBER_COMPERATOR = new Comparator<IAssignmentIf>()
	{
		public int compare(IAssignmentIf a1, IAssignmentIf a2)
		{
			Enum<?> e1 = MsoUtils.getType(a1, true);
			Enum<?> e2 = MsoUtils.getType(a2, true);
			if(e2 == e1)
			{
				return a1.getNumber() - a2.getNumber();
			}
			else
			{
				return e1.ordinal() - e2.ordinal();
			}
		}
	};

	public static final Comparator<IAssignmentIf> ASSIGNMENT_NAME_NUMBER_COMPERATOR = new Comparator<IAssignmentIf>()
	{
		public int compare(IAssignmentIf a1, IAssignmentIf a2)
		{
			Enum<?> e1 = MsoUtils.getType(a1, true);
			Enum<?> e2 = MsoUtils.getType(a2, true);
			if(e2 == e1)
			{
				return a1.getNumber() - a2.getNumber();
			}
			else
			{
				String s1 = MsoUtils.getAssignmentName(a1, 0);
				String s2 = MsoUtils.getAssignmentName(a2, 0);
				return s1.compareTo(s2);
			}
		}
	};


    public static final Comparator<IAssignmentIf> PRIORITY_SEQUENCE_COMPARATOR = new Comparator<IAssignmentIf>()
    {
        public int compare(IAssignmentIf o1, IAssignmentIf o2)
        {
            return o1.getPrioritySequence() - o2.getPrioritySequence();
        }
    };

    public static final Comparator<IAssignmentIf> PRIORITY_COMPARATOR = new Comparator<IAssignmentIf>()
    {
        public int compare(IAssignmentIf o1, IAssignmentIf o2)
        {
            return o1.comparePriorityTo(o2);
        }
    };

    public static final Comparator<IAssignmentIf> PRIORITY_AND_NUMBER_COMPARATOR = new Comparator<IAssignmentIf>()
    {
        public int compare(IAssignmentIf o1, IAssignmentIf o2)
        {
            if (o1.getPriority().equals(o2.getPriority()))
            {
                return o1.getNumber() - o2.getNumber();
            } else
            {
                // Compare priorities so that high priority comes first (high priority is "smaller than" low priority)
                return o1.comparePriorityTo(o2);
            }
        }
    };

    public IMessageLineIf getLatestStatusChangeMessageLine(final IMessageLineIf.MessageLineType aType);

    public boolean transferMessageConfirmed();

    public boolean transferMessageConfirmed(AssignmentStatus aStatus);

    public Set<IMessageLineIf> getReferringMessageLines();

    public Set<IMessageLineIf> getReferringMessageLines(Collection<IMessageLineIf> aCollection);

}