package org.redcross.sar.mso.util;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.util.except.IllegalOperationException;

import java.util.Calendar;
import java.util.EnumSet;
/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 19.jun.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 * Class for handling assignment transfers.
 */
public class AssignmentTransferUtilities
{
    final static EnumSet<IAssignmentIf.AssignmentStatus> acceptedStatuses = EnumSet.range(IAssignmentIf.AssignmentStatus.ASSIGNED, IAssignmentIf.AssignmentStatus.REPORTED);

    /**
     * Create an assignment transfer message and put it in the message log.
     * <p/>
     * The message is generated with status {@link org.redcross.sar.mso.data.IMessageIf.MessageStatus#UNCONFIRMED}.
     * <p/>
     * For each status change between {@link org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus#READY} and {@link org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus#FINISHED}
     * is generated one message line.
     *
     * @param anMsoManager The Mso Manager.
     * @param aUnit        Unit receiving the assignment.
     * @param anAssignment The assignment that is transferred
     * @param oldStatus    Former assignmnet status
     */
    public static void createAssignmentChangeMessage(IMsoManagerIf anMsoManager, IUnitIf aUnit, IAssignmentIf anAssignment, IAssignmentIf.AssignmentStatus oldStatus)
    {
        IMessageLineIf.MessageLineType firstLineType;
        switch (oldStatus)
        {
            case READY:
            case QUEUED:
                firstLineType = IMessageLineIf.MessageLineType.ASSIGNED;
                break;
            case ASSIGNED:
                // Special consideration when moving assigned assignments between units.
                if (anAssignment.getStatus() ==  IAssignmentIf.AssignmentStatus.ASSIGNED)
                {
                    firstLineType = IMessageLineIf.MessageLineType.ASSIGNED;
                } else
                {
                    firstLineType = IMessageLineIf.MessageLineType.STARTED;
                }
                break;
            case EXECUTING:
                firstLineType = IMessageLineIf.MessageLineType.COMPLETE;
                break;
            default:
                return;
        }

        IMessageLineIf.MessageLineType finalLineType;
        switch (anAssignment.getStatus())
        {
            case ASSIGNED:
                finalLineType = IMessageLineIf.MessageLineType.ASSIGNED;
                break;
            case EXECUTING:
                finalLineType = IMessageLineIf.MessageLineType.STARTED;
                break;
            case ABORTED:
            case FINISHED:
            case REPORTED:
                finalLineType = IMessageLineIf.MessageLineType.COMPLETE;
                break;
            default:
                return;
        }
        Calendar now = Calendar.getInstance();
        IMessageIf message = anMsoManager.createMessage();
        message.setCreated(now);
        message.setOccuredTime(now);
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        message.addConfirmedReceiver(aUnit);
        message.setSender(anMsoManager.getCmdPostCommunicator());
        createAssignmentChangeMessageLines(message, firstLineType, finalLineType, now, anAssignment);
    }

    final static IMessageLineIf.MessageLineType[] types = {IMessageLineIf.MessageLineType.ASSIGNED,
            IMessageLineIf.MessageLineType.STARTED,
            IMessageLineIf.MessageLineType.COMPLETE};

    /**
     * Create a set of message lines for assignment transfers.
     *
     * @param message       The message where the lines shall be put. Possible existing lines of the same type in the message will be overwritten.
     * @param firstLineType First line type to generate.
     * @param lastLineType  Last line type to generate.
     * @param aDTG          Time when the message was created.
     * @param anAssignment  The assignment that is transferred
     */
    public static void createAssignmentChangeMessageLines(IMessageIf message, IMessageLineIf.MessageLineType firstLineType, IMessageLineIf.MessageLineType lastLineType, Calendar aDTG, IAssignmentIf anAssignment)
    {
        for (IMessageLineIf.MessageLineType t : types)
        {
            if (t.ordinal() >= firstLineType.ordinal() && t.ordinal() <= lastLineType.ordinal())
            {
                IMessageLineIf line = message.findMessageLine(t, anAssignment, true);
                if (line != null)
                {
                    line.setOperationTime(aDTG);
                    line.setLineAssignment(anAssignment);
                }
            }
        }
    }

    /**
     * Check if a unit can accept an assignment with a given status.
     *
     * @param aUnit   The unit that receives the assignment.
     * @param aStatus The status to be checked.
     * @return <code>true</code> if accepted, <code>false </code> otherwise.
     */
    public static boolean unitCanAccept(IUnitIf aUnit, IAssignmentIf.AssignmentStatus aStatus)
    {
        switch (aUnit.getStatus())
        {
            case READY:
            case INITIALIZING:
            case PAUSED:
            case WORKING:
            case PENDING:
                if (aStatus == IAssignmentIf.AssignmentStatus.QUEUED)
                {
                    return true;
                } else if (aStatus == IAssignmentIf.AssignmentStatus.ASSIGNED)
                {
                    return (aUnit.getAssignedAssignments().size() == 0);
                } else if (aStatus == IAssignmentIf.AssignmentStatus.EXECUTING)
                {
                    return (aUnit.getExecutingAssigments().size() == 0);
                } else
                {
                    return true;
                }
            case RELEASED:
                return aStatus == IAssignmentIf.AssignmentStatus.REPORTED;
            default:
                return false;
        }
    }

    /**
     * Test if an assignment can change satus and owner.
     *
     * @param anAssignment The assignment to change
     * @param newStatus    The new status
     * @param newUnit      The new owner
     * @return <code>true</code> if the change is legal, <code>false</code> otherwise.
     */
    public static boolean assignmentCanChangeToStatus(IAssignmentIf anAssignment, String newStatus, IUnitIf newUnit)
    {
        IAssignmentIf.AssignmentStatus status = anAssignment.getStatusAttribute().enumValue(newStatus);
        if (status == null)
        {
            return false;
        }
        return assignmentCanChangeToStatus(anAssignment, status, newUnit);
    }

    /**
     * Check if an assignment can change to a new status and be transferred to a given unit.
     *
     * @param anAssignment The assignment to check.
     * @param newStatus    New status for assignment.
     * @param newUnit      Unit that shall receive the assignment.
     * @return <code>true</code> if can change, <code>false </code> otherwise.
     */
    public static boolean assignmentCanChangeToStatus(IAssignmentIf anAssignment, IAssignmentIf.AssignmentStatus newStatus, IUnitIf newUnit)
    {
        IUnitIf currentUnit = anAssignment.getOwningUnit();
        IAssignmentIf.AssignmentStatus currentStatus = anAssignment.getStatus();

        if (newStatus == currentStatus && newUnit == currentUnit)
        {
            return newStatus == IAssignmentIf.AssignmentStatus.QUEUED;     // Can drop on the same in order to change priority
        }

        switch (currentStatus)
        {
            case EMPTY:
                return newUnit == null && (newStatus == IAssignmentIf.AssignmentStatus.DRAFT || newStatus == IAssignmentIf.AssignmentStatus.READY);
            case DRAFT:
                return newUnit == null && newStatus == IAssignmentIf.AssignmentStatus.READY;
            case READY:
                return newUnit != null && IAssignmentIf.ACTIVE_SET.contains(newStatus) && AssignmentTransferUtilities.unitCanAccept(newUnit, newStatus);
            case QUEUED:
            case ASSIGNED:
                return newUnit == null ? newStatus == IAssignmentIf.AssignmentStatus.READY : (IAssignmentIf.ACTIVE_SET.contains(newStatus) && AssignmentTransferUtilities.unitCanAccept(newUnit, newStatus));
            case EXECUTING:
                return newUnit == currentUnit && IAssignmentIf.FINISHED_AND_REPORTED_SET.contains(newStatus);
            case ABORTED:
            case FINISHED:
                return newUnit == currentUnit && newStatus == IAssignmentIf.AssignmentStatus.REPORTED;
        }
        return false;
    }


    /**
     * Assigns an assignment to some unit. All statuses are updated.
     * @param assignment The assignment
     * @param unit The unit to get the assignment
     * @throws IllegalOperationException 
     */
    public static void assignAssignmentToUnit(IAssignmentIf assignment, IUnitIf unit) throws IllegalOperationException
    {
    	unit.addUnitAssignment(assignment, AssignmentStatus.ASSIGNED);
    	unit.setStatus(UnitStatus.INITIALIZING);
    }

    /**
     * Starts an assignment, updates unit and assignment status
     * @param unit
     * @param assignment
     * @throws IllegalOperationException 
     */
    public static void unitStartAssignment(IUnitIf unit, IAssignmentIf assignment) throws IllegalOperationException
    {
		unit.addUnitAssignment(assignment, AssignmentStatus.EXECUTING);
		unit.setStatus(UnitStatus.WORKING);
    }

    /**
     * Marks an assignment as completed. Unit and assignment statuses are updated
     * @param unit
     * @param assignment
     * @throws IllegalOperationException 
     */
    public static void unitCompleteAssignment(IUnitIf unit, IAssignmentIf assignment) throws IllegalOperationException
    {
		assignment.setStatusAndOwner(AssignmentStatus.FINISHED, unit);
    	unit.setStatus(UnitStatus.READY);
    }
}
