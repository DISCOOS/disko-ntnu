package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.Selector;
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
public interface IUnitIf extends IHierarchicalUnitIf, ICommunicatorIf, ISerialNumberedIf, IEnumStatusHolder<IUnitIf.UnitStatus>
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.Unit";
    
    public static final EnumSet<UnitStatus> ACTIVE_RANGE = EnumSet.range(UnitStatus.READY, UnitStatus.PENDING);
    
    public static final Selector<IUnitIf> ACTIVE_UNIT_SELECTOR = new Selector<IUnitIf>()
    {
        public boolean select(IUnitIf aUnit)
        {
            return (IUnitIf.ACTIVE_RANGE.contains(aUnit.getStatus()));
        }
    };
	    

    public static final Comparator<IUnitIf> UNIT_TYPE_AND_NUMBER_COMPARATOR = new Comparator<IUnitIf>()
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
        AIRCRAFT,
        BOAT,
        VEHICLE
    }

    public enum UnitStatus
    {
        EMPTY,
        READY,
        INITIALIZING,
        PAUSED,
        WORKING,
        PENDING,
        RELEASED
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(UnitStatus aStatus);

    public UnitType getType();

    public IMsoModelIf.ModificationState getTypeState();

    public IAttributeIf.IMsoEnumIf<UnitType> getTypeAttribute();

    public String getTypeText();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public String getUnitNumber();

    public String getTypeAndNumber();

    public char getUnitNumberPrefix();

    public void setAverageSpeed(int anAverageSpeed);

    public int getAverageSpeed();

    public IMsoModelIf.ModificationState getAverageSpeedState();

    public IAttributeIf.IMsoIntegerIf getAverageSpeedAttribute();

    public void setBearing(int aBearing);

    public int getBearing();

    public IMsoModelIf.ModificationState getBearingState();

    public IAttributeIf.IMsoIntegerIf getBearingAttribute();

    public void setCallSign(String aCallSign);

    public String getCallSign();

    public IMsoModelIf.ModificationState getCallSignState();

    public IAttributeIf.IMsoStringIf getCallSignAttribute();

    public void setMaxSpeed(int aMaxSpeed);

    public int getMaxSpeed();

    public IMsoModelIf.ModificationState getMaxSpeedState();

    public IAttributeIf.IMsoIntegerIf getMaxSpeedAttribute();

    public void setPosition(Position aPosition);

    public Position getPosition();

    public IMsoModelIf.ModificationState getPositionState();

    public IAttributeIf.IMsoPositionIf getPositionAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IMsoModelIf.ModificationState getRemarksState();

    public IAttributeIf.IMsoStringIf getRemarksAttribute();

    public void setSpeed(int aSpeed);

    public int getSpeed();

    public IMsoModelIf.ModificationState getSpeedState();

    public IAttributeIf.IMsoIntegerIf getSpeedAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    /**
     * @param anIAssignmentIf The assigment to add
     * @param newStatus       New status for the assignment
     * @throws DuplicateIdException if the list already contains an object with the same object ID.
     * @throws org.redcross.sar.util.except.IllegalOperationException
     *                              if the assignment cannot be assigned.
     */
    public void addUnitAssignment(IAssignmentIf anIAssignmentIf, IAssignmentIf.AssignmentStatus newStatus) throws IllegalOperationException;

    public void addUnitReference(IAssignmentIf anIAssignmentIf);

    public void removeUnitReference(IAssignmentIf anIAssignmentIf);

    public IAssignmentListIf getUnitAssignments();

    public IMsoModelIf.ModificationState getUnitAssignmentsState(IAssignmentIf anIAssignmentIf);

    public Collection<IAssignmentIf> getUnitAssignmentsItems();

    public void addUnitPersonnel(IPersonnelIf anIPersonnelIf);

    public IPersonnelListIf getUnitPersonnel();

    public IMsoModelIf.ModificationState getUnitPersonnelState(IPersonnelIf anIPersonnelIf);

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

    public List<IAssignmentIf> getAllocatedAssignments();

    public IAssignmentIf getAssignedAssignment();

    public Set<IAssignmentIf> getAssignedAssignments();

    public IAssignmentIf getExecutingAssigment();

    public Set<IAssignmentIf> getExecutingAssigments();

    public Set<IAssignmentIf> getFinishedAssigments();

    public IAssignmentIf getActiveAssignment();

    public void rearrangeAsgPrioritiesAfterStatusChange(IAssignmentIf anAssignment, IAssignmentIf.AssignmentStatus oldStatus);

    /**
     * Add an allocated assignment to the unit at a given place in the list
     * @param newAssignment The assignment to add
     * @param beforeAssignment Place the new assignment before this, if null, place to the end.
     * @return <code>false</code> if an error ({@link org.redcross.sar.util.except.IllegalOperationException}) occured, <code>true</code> otherwise.
     */
    public boolean addAllocatedAssignment(IAssignmentIf newAssignment, IAssignmentIf beforeAssignment);

    public long getPauseTimeInMillis();

    public long getWorkTimeInMillis();

    public long getIdleTimeInMillis();
    
    public boolean logPosition();
    
    public boolean logPosition(Calendar aTime);
    
    public boolean logPosition(Position aPosition, Calendar aTime);  
    
    public TimePos getLastKnownPosition();

    public Set<IMessageIf> getReferringMessages();

    public Set<IMessageIf> getReferringMessages(Collection<IMessageIf> aCollection);
}
