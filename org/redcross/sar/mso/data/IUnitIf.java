package org.redcross.sar.mso.data;

import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.TimePos;

import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public interface IUnitIf extends IHierarchicalUnitIf, IAssociationIf, ICommunicatorIf, ISerialNumberedIf, IEnumStatusHolder<IUnitIf.UnitStatus>
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.Unit";

    /**
     * Often used status ranges
     */
    public static final EnumSet<UnitStatus> OWNER_RANGE = EnumSet.range(UnitStatus.EMPTY, UnitStatus.PAUSED);
    public static final EnumSet<UnitStatus> ACTIVE_RANGE = EnumSet.range(UnitStatus.READY, UnitStatus.PAUSED);
    public static final EnumSet<UnitStatus> HISTORY_RANGE = EnumSet.of(UnitStatus.RELEASED);
    public static final EnumSet<UnitStatus> OCCUPIED_RANGE = EnumSet.range(UnitStatus.INITIALIZING, UnitStatus.PENDING);
    public static final EnumSet<UnitStatus> IDLE_RANGE = EnumSet.of(UnitStatus.READY, UnitStatus.PAUSED);
    public static final EnumSet<UnitStatus> MANAGED_RANGE = ACTIVE_RANGE.clone();
    public static final EnumSet<UnitStatus> DELETEABLE_SET = EnumSet.of(UnitStatus.EMPTY);

    /**
     * Often used selectors
     */

    public static final Selector<IUnitIf> ALL_SELECTOR = new Selector<IUnitIf>()
    {
        public boolean select(IUnitIf aUnit)
        {
            return true;
        }
    };

    public static final Selector<IUnitIf> OWNER_SELECTOR = new Selector<IUnitIf>()
    {
        public boolean select(IUnitIf aUnit)
        {
            return (IUnitIf.OWNER_RANGE.contains(aUnit.getStatus()));
        }
    };

    public static final Selector<IUnitIf> HISTORY_SELECTOR = new Selector<IUnitIf>()
    {
        public boolean select(IUnitIf aUnit)
        {
            return (IUnitIf.HISTORY_RANGE.contains(aUnit.getStatus()));
        }
    };

    public static final Selector<IUnitIf> ACTIVE_SELECTOR = new Selector<IUnitIf>()
    {
        public boolean select(IUnitIf aUnit)
        {
            return (IUnitIf.ACTIVE_RANGE.contains(aUnit.getStatus()));
        }
    };

    /**
     * Often used comparators
     */
    public static final Comparator<IUnitIf> TYPE_AND_NUMBER_COMPARATOR = new Comparator<IUnitIf>()
    {
        public int compare(IUnitIf u1, IUnitIf u2)
        {
            int typeCompare = u1.getType().compareTo(u2.getType());
            if (typeCompare != 0)
            {
                return typeCompare;
            }
            return u1.getNumber() - u2.getNumber();
        }
    };

    public enum UnitType
    {
        CP,
        TEAM,
        DOG,
        VEHICLE,
        AIRCRAFT,
        BOAT
    }

    public enum UnitStatus
    {
        EMPTY,
        READY,
        INITIALIZING,
        WORKING,
        PENDING,
        PAUSED,
        RELEASED
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    /**
     * Sets a specified status. </p>
     *
     * Only a subset of the available status values are allowed to change manually. These
     * statuses are  EMPTY, READY and RELEASED. EMPTY and READY can be toggled as long
     * as no active assignment exists or the unit is not paused. If an active assignment
     * exist or the unit is paused, an org.redcross.sar.util.except.IllegalOperationException
     * will be thrown. If RELEASED is set, the unit status is irrevocable. Any further
     * attempts to change the status will throw IllegalOperationException. This complies to the
     * history rules of MSO model. </p>
     *
     * The remaining status values are managed by the MSO model. The MSO model ensures that
     * the unit status at all times corresponds to the active assignment state. If an active
     * assignment exists, INITIALIZING or WORKING is chosen depending if the active assignment
     * is allocated or executing. The PAUSED status is managed according to the pause/resume
     * rules for IUnitIf. The methods <code>pause()</code> and <code>resume()</code> is supplied
     * to control the pause state. If active assignment status has changed since PAUSED was entered,
     * MSO model will ensure that a legal status is set automatically.
     *
     * @throws org.redcross.sar.util.except.IllegalOperationException
     */
    public void setStatus(String aStatus) throws IllegalOperationException;

    public void setStatus(UnitStatus aStatus) throws IllegalOperationException;

    public UnitType getType();

    public IMsoModelIf.ModificationState getTypeState();

    public IAttributeIf.IMsoEnumIf<UnitType> getTypeAttribute();

    public String getInternationalTypeName();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setName(String aName);

    public String getName();

    public IMsoModelIf.ModificationState getNameState();

    public IAttributeIf.IMsoStringIf getNameAttribute();

    public void setCallSign(String aCallSign);

    public String getCallSign();

    public IMsoModelIf.ModificationState getCallSignState();

    public IAttributeIf.IMsoStringIf getCallSignAttribute();

	public void setToneID(String toneId);

	public String getToneID();

	public IMsoModelIf.ModificationState getToneIDState();

	public IAttributeIf.IMsoStringIf getToneIDAttribute();
	
    public void setTrackingID(String aTrackingID);

    public String getTrackingID();

    public IMsoModelIf.ModificationState getTrackingIDState();

    public IAttributeIf.IMsoStringIf getTrackingIDAttribute();
    
    public void setPosition(Position aPosition);

    public Position getPosition();

    public IMsoModelIf.ModificationState getPositionState();

    public IAttributeIf.IMsoPositionIf getPositionAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IMsoModelIf.ModificationState getRemarksState();

    public IAttributeIf.IMsoStringIf getRemarksAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public IAssignmentListIf getUnitAssignments();

    public IMsoModelIf.ModificationState getUnitAssignmentsState(IAssignmentIf anAssignment);

    public Collection<IAssignmentIf> getUnitAssignmentsItems();

    public void addUnitPersonnel(IPersonnelIf anPersonnel);

    public void removeUnitPersonnel(IPersonnelIf anPersonnel);

    public IPersonnelListIf getUnitPersonnel();

    public IMsoModelIf.ModificationState getUnitPersonnelState(IPersonnelIf anPersonnel);

    public Collection<IPersonnelIf> getUnitPersonnelItems();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setUnitLeader(IPersonnelIf aPersonnel);

    public IPersonnelIf getUnitLeader();

    public IMsoModelIf.ModificationState getUnitLeaderState();

    public IMsoReferenceIf<IPersonnelIf> getUnitLeaderAttribute();

    public void setTrack(ITrackIf aTrack);

    public ITrackIf getTrack();

    public IMsoModelIf.ModificationState getTrackState();

    public IMsoReferenceIf<ITrackIf> geTrackAttribute();

    /*-------------------------------------------------------------------------------------------
    * Other methods
    *-------------------------------------------------------------------------------------------*/

    /**
     * Get short name of unit
     *
     * @return The concatenation <code>getNumberPrefix() + " " + getNumber()</code>
     */
    public String getShortName();

    /**
     * Get default name of unit
     *
     * @return The concatenation <code>getInternationalTypeName() + " " + getNumber()</code>
     */
    public String getDefaultName();

    /**
     *
     * @return Prefix used in <code>getShortName()</code>
     */
    public char getNumberPrefix();

    /**
     * Pauses the unit if possible
     *
     * @return Paused unit status
     * @throws org.redcross.sar.util.except.IllegalOperationException is the unit is EMPTY or RELEASED.
     */
    public UnitStatus pause() throws IllegalOperationException;

    /**
     * Resumes the unit if paused
     *
     * @return Paused unit status
     * @throws org.redcross.sar.util.except.IllegalOperationException is the unit is not paused.
     */
    public void resume() throws IllegalOperationException;

    /**
     * Indicates the unit is paused. A paused unit can only be resumed by <code>resume()</code>. Any status
     * change attempted on a paused unit will produce an org.redcross.sar.util.except.IllegalOperationException.
     * Use this method to check if a exception will be produced before an status change is attempted.
     *
     * @return <code>true</code> if paused, <code>false</code> else.
     */
    public boolean isPaused();


    /**
     * Indicates the unit is released. A released unit can not change status and only finished and reported
     * assignments can be added. Use this method to check if a status change, adding or removing a specific assignment
     * will produce a org.redcross.sar.util.except.IllegalOperationException.
     *
     * @return <code>true</code> if released, <code>false</code> else.
     */
    public boolean isReleased();

    /**
     * Tries to release the active assignment. Only an allocated assignment
     * is released. If no assignment is allocated, or if the active assignment is
     * started, no release will occur. Any released assignment is given the status READY.
     *
     * @param anAssignment - the assignment to reclaim
     * @return The releases assignment, <code>null</code> else.
     * @throws org.redcross.sar.util.except.IllegalOperationException
     */
    public IAssignmentIf releaseAssignment() throws IllegalOperationException;

    /**
     * This method releases any enqueued or assigned assignment. Any released
     * assignment is given the status READY.
     *
     * @param anAssignment - the assignment to reclaim
     * @return <code>true</code> if successfully released, <code>else</code>.
     * @throws org.redcross.sar.util.except.IllegalOperationException
     */
    public boolean releaseAssignment(IAssignmentIf anAssignment) throws IllegalOperationException;

    /**
     * Add an assignment to the unit assignment queue at a given place in the list
     *
     * @param anAssignment The assignment to add
     * @return <code>true</code> if successfully enqueued, <code>false</code> otherwise.
     * @throws org.redcross.sar.util.except.IllegalOperationException
     */
    public boolean enqueueAssignment(IAssignmentIf anAssignment) throws IllegalOperationException;

    /**
     * Add an assignment to the unit assignment queue at a given place in the list
     *
     * @param newAssignment The assignment to add
     * @param beforeAssignment Place the new assignment before this, if null, place to the end.
     * @return <code>true</code> if successfully enqueued, <code>false</code> otherwise.
     * @throws org.redcross.sar.util.except.IllegalOperationException
     */
    public boolean enqueueAssignment(IAssignmentIf newAssignment, IAssignmentIf beforeAssignment) throws IllegalOperationException;

    /**
     * Remove an assignment from the unit assignment queue
     *
     * @param anAssignment The assignment to remove
     * @return <code>true</code> if successfully dequeued, <code>false</code> otherwise.
     * @throws org.redcross.sar.util.except.IllegalOperationException
     */
    public boolean dequeueAssignment(IAssignmentIf anAssignment) throws IllegalOperationException;

    public List<IAssignmentIf> getEnqueuedAssignments();

    public IAssignmentIf allocateAssignment() throws IllegalOperationException;

    public IAssignmentIf allocateAssignment(IAssignmentIf anAssignment) throws IllegalOperationException;

    public IAssignmentIf getAllocatedAssignment();

    public Set<IAssignmentIf> getAllocatedAssignments();

    public IAssignmentIf startAssignment() throws IllegalOperationException;

    public IAssignmentIf startAssignment(IAssignmentIf anAssignment) throws IllegalOperationException;

    public IAssignmentIf getExecutingAssigment();

    public Set<IAssignmentIf> getExecutingAssigments();

    public IAssignmentIf finishAssignment() throws IllegalOperationException;

    public IAssignmentIf finishAssignment(IAssignmentIf anAssignment) throws IllegalOperationException;

    public Set<IAssignmentIf> getFinishedAssigments();

    public IAssignmentIf getActiveAssignment();

    public boolean logPosition();

    public boolean logPosition(Calendar aTime);

    public boolean logPosition(Position aPosition, Calendar aTime);

    public TimePos getLastKnownPosition();

    public Set<IMessageIf> getReferringMessages();

    public Set<IMessageIf> getReferringMessages(Collection<IMessageIf> aCollection);


    /**
     * Get duration of given unit status. </p>
     *
     * @param aStatus - The status to get duration for
     * @param total - If <code>true</code> the sum of all durations for a given status
     * is returned, the duration of the last occurrence otherwise.
     *
     * @return Duration (second)
     */
    public double getDuration(UnitStatus aStatus, boolean total);


    /**
     * Get duration of given set of unit status values. </p>
     *
     * @param aList - The status to get duration for
     * @param total - If <code>true</code> the sum of all durations for a given status
     * is returned, the duration of the last occurrence otherwise.
     *
     * @return Duration (second)
     */
    public double getDuration(EnumSet<UnitStatus> aList, boolean total);

    /**
     * Get bearing from last known position to current position. </p>
     *
     * If current position is unknown, the bearing of the last leg in track is used.
     *
     * @return Bearing (degrees)
     */
    public double getBearing();

    /**
     * Get current speed
     *
     * @return Speed (m/s)
     */
    public double getSpeed();

    /**
     * Get average speed from first known to from last known position
     *
     * @return Speed (m/s)
     */
    public double getAverageSpeed();

    /**
     * Get maximum speed from first known to from last known position
     *
     * @return Speed (m/s)
     */
    public double getMaximumSpeed();

    /**
     * Get minimum speed from first known to from last known position
     *
     * @return Speed (m/s)
     */
    public double getMinimumSpeed();

    /**
     * Get total distance traveled. This distance do not include the leg from last known
     * position to current position.
     *
     * @return Distance (m)
     */
    public double getDistance();


}
