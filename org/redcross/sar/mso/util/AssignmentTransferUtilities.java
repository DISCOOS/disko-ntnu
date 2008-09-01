package org.redcross.sar.mso.util;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.util.except.IllegalOperationException;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.ResourceBundle;
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
	final static ResourceBundle resource = ResourceBundle.getBundle("org.redcross.sar.mso.util.assignmentTransferUtilities");
    final static EnumSet<AssignmentStatus> acceptedStatuses = EnumSet.range(AssignmentStatus.ASSIGNED, AssignmentStatus.REPORTED);
    
    /**
     * Create an assignment transfer message and put it in the message log.
     * <p/>
     * The message is generated with status {@link org.redcross.sar.mso.data.IMessageIf.MessageStatus#UNCONFIRMED}.
     * <p/>
     * For each status change between {@link org.redcross.sar.mso.data.AssignmentStatus#READY} and {@link org.redcross.sar.mso.data.AssignmentStatus#FINISHED}
     * is generated one message line.
     *
     * @param anMsoManager The Mso Manager.
     * @param aUnit        Unit receiving the assignment.
     * @param anAssignment The assignment that is transferred
     * @param oldStatus    Former assignmnet status
     */
    public static void createAssignmentChangeMessage(IMsoManagerIf anMsoManager, IUnitIf aUnit, IAssignmentIf anAssignment, AssignmentStatus oldStatus)
    {
        MessageLineType firstLineType;
        switch (oldStatus)
        {
            case READY:
            case QUEUED:
                firstLineType = MessageLineType.ASSIGNED;
                break;
            case ASSIGNED:
                // Special consideration when moving assigned assignments between units.
                if (anAssignment.getStatus() ==  AssignmentStatus.ASSIGNED)
                {
                    firstLineType = MessageLineType.ASSIGNED;
                } else
                {
                    firstLineType = MessageLineType.STARTED;
                }
                break;
            case EXECUTING:
                firstLineType = MessageLineType.COMPLETED;
                break;
            default:
                return;
        }

        MessageLineType finalLineType;
        switch (anAssignment.getStatus())
        {
            case ASSIGNED:
                finalLineType = MessageLineType.ASSIGNED;
                break;
            case EXECUTING:
                finalLineType = MessageLineType.STARTED;
                break;
            case ABORTED:
            case FINISHED:
            case REPORTED:
                finalLineType = MessageLineType.COMPLETED;
                break;
            default:
                return;
        }
        Calendar now = Calendar.getInstance();
        IMessageIf message = anMsoManager.createMessage();
        message.setCreated(now);
        message.setTimeStamp(now);
        message.setStatus(IMessageIf.MessageStatus.UNCONFIRMED);
        message.addConfirmedReceiver(aUnit);
        message.setSender(anMsoManager.getCmdPostCommunicator());
        createAssignmentChangeMessageLines(message, firstLineType, finalLineType, now, aUnit, anAssignment);
    }

    final static MessageLineType[] types = {MessageLineType.ASSIGNED,
            MessageLineType.STARTED,
            MessageLineType.COMPLETED};

    /**
     * Create a set of message lines for assignment transfers.
     *
     * @param message       The message where the lines shall be put. Possible existing lines of the same type in the message will be overwritten.
     * @param firstLineType First line type to generate.
     * @param lastLineType  Last line type to generate.
     * @param aDTG          Time when the message was created.
     * @param anAssignment  The assignment that is transferred
     */
    public static void createAssignmentChangeMessageLines(IMessageIf message, 
    		MessageLineType firstLineType, MessageLineType lastLineType, Calendar aDTG, 
    		IUnitIf unit, IAssignmentIf anAssignment)
    {
        for (MessageLineType t : types)
        {
            if (t.ordinal() >= firstLineType.ordinal() && t.ordinal() <= lastLineType.ordinal())
            {
                IMessageLineIf line = message.findMessageLine(t, anAssignment, true);
                if (line != null)
                {
                    line.setOperationTime(aDTG);
                    line.setLineUnit(unit);
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
     * @return <code>0</code> if accepted, <code>negative</code> otherwise. 
     * <p> Following errors are returned: 
     * <p> <code>(-1)-></code> Unit is already assigned an assignment  
     * <p> <code>(-2)-></code> Unit is already executing an assignment 
     * <p> <code>(-3)-></code> Unit is released. Only reported assignments change is accepted 
     */
    public static int unitCanAcceptChange(IUnitIf aUnit, AssignmentStatus aStatus)
    {
        // is null?
    	if(aUnit==null) return 0;
    	
    	switch (aUnit.getStatus())
        {
        case INITIALIZING:
        case EMPTY:
        case READY:
        case PAUSED: 
        case WORKING:
        case PENDING:
            if (aStatus == AssignmentStatus.QUEUED)
            {
                return 0;
            } else if (aStatus == AssignmentStatus.ASSIGNED )
            {
                if (aUnit.getAssignedAssignments().size() > 0) return -1;
                if (aUnit.getExecutingAssigments().size() > 0) return -2;
            }
            else if(aStatus == AssignmentStatus.EXECUTING) {
                if (aUnit.getExecutingAssigments().size() > 0) return -2;            	
            }
            if(aStatus != null)
            {
                return 0;
            }
            // failed
            throw new IllegalArgumentException("Assignment status is " + aStatus);
        case RELEASED:
            return aStatus == AssignmentStatus.REPORTED ? 0 : -3;
        }
        // failed
        throw new IllegalArgumentException("Unit status is " + aUnit.getStatus());
    }
    
    /**
     * Test if an assignment can change status and owner.
     *
     * @param anAssignment The assignment to change
     * @param newStatus    The new status
     * @param newUnit      The new owner
     * @return <code>0</code> if the change is legal, <code>negative</code> otherwise.
     * <p> Following errors are returned: 
     * <p> <code>(-4)-></code> Assignments can be reordered within same unit if status is QUEUED.  
     * <p> <code>(-5)-></code> Assignment status can only change from <code>EMPTY</code> to <code>DRAFT</code> or <code>READY</code>. No unit can be assigned. 
     * <p> <code>(-6)-></code> Assignment status can only change from <code>DRAFT</code> to <code>READY</code>. No unit can be assigned.
     * <p> <code>(-7)-></code> Assignment status can only change from <code>READY</code> to <code>DRAFT</code> if no unit will be assigned.
     * <p> <code>(-8)-></code> Assignment status can only change from <code>READY</code> to <code>ACTIVE_SET</code> if an unit will be assigned.
     * <p> <code>(-9)-></code> Assignment status can only change from <code>{QUEUED, ASSIGNED}</code> to <code>READY</code> if no unit will be assigned
     * <p> <code>(-10)-></code> Assignment status can only change from <code>{QUEUED, ASSIGNED}</code> to <code>ACTIVE_SET</code> if new status is in <code>ACTIVE_SET</code> and the assigned unit accepts the change.  
     * <p> <code>(-11)-></code> Assignment status can only change from <code>EXECUTING</code> to <code>FINISHED_AND_REPORTED_SET</code> if assigned remains the same.
     * <p> <code>(-12)-></code> Assignment status can only change from <code>{ABORTED, FINISHED}</code> to <code>REPORTED</code> if assigned unit will not change.
     */
    public static int assignmentCanChangeToStatus(IAssignmentIf anAssignment, String newStatus, IUnitIf newUnit)
    {
        // get status from string
    	AssignmentStatus status = anAssignment.getStatusAttribute().enumValue(newStatus);
    	
    	// forward
        return assignmentCanChangeToStatus(anAssignment, status, newUnit);
        
    }

    /**
     * Check if an assignment can change to a new status and be transferred to a given unit.
     *
     * @param anAssignment The assignment to check.
     * @param newStatus    New status for assignment.
     * @param newUnit      Unit that shall receive the assignment.
     * @return <code>0</code> if can change, <code>negative</code> otherwise.
     * <p> Following errors are returned: 
     * <p> <code>(-4)-></code> Assignments can be reordered within same unit if status is QUEUED.  
     * <p> <code>(-5)-></code> Assignment status can only change from <code>EMPTY</code> to <code>DRAFT</code> or <code>READY</code>. No unit can be assigned. 
     * <p> <code>(-6)-></code> Assignment status can only change from <code>DRAFT</code> to <code>READY</code>. No unit can be assigned.
     * <p> <code>(-7)-></code> Assignment status can only change from <code>READY</code> to <code>DRAFT</code> if no unit will be assigned.
     * <p> <code>(-8)-></code> Assignment status can only change from <code>READY</code> to <code>ACTIVE_SET</code> if an unit will be assigned.
     * <p> <code>(-9)-></code> Assignment status can only change from <code>{QUEUED, ASSIGNED}</code> to <code>READY</code> if no unit will be assigned
     * <p> <code>(-10)-></code> Assignment status can only change from <code>{QUEUED, ASSIGNED}</code> to <code>ACTIVE_SET</code> if new status is in <code>ACTIVE_SET</code> and the assigned unit accepts the change.  
     * <p> <code>(-11)-></code> Assignment status can only change from <code>EXECUTING</code> to <code>FINISHED_AND_REPORTED_SET</code> if assigned remains the same.
     * <p> <code>(-12)-></code> Assignment status can only change from <code>{ABORTED, FINISHED}</code> to <code>REPORTED</code> if assigned unit will not change.
     */
    public static int assignmentCanChangeToStatus(IAssignmentIf anAssignment, AssignmentStatus newStatus, IUnitIf newUnit)
    {
        IUnitIf currentUnit = anAssignment.getOwningUnit();
        AssignmentStatus currentStatus = anAssignment.getStatus();

        if (newStatus == currentStatus)
        {
            return (newStatus == AssignmentStatus.QUEUED) ? 0 : -4;     // Can drop on the same in order to change priority
        }

        switch (currentStatus)
        {
            case EMPTY:
                return (newUnit == null && (newStatus == AssignmentStatus.DRAFT 
                		|| newStatus == AssignmentStatus.READY)) ? 0 : -5;
            case DRAFT:
                return (newUnit == null && newStatus == AssignmentStatus.READY) ? 0 : -6;
            case READY:
            	// move to draft?
            	if(newUnit == null) {
	                return (newStatus == AssignmentStatus.DRAFT) ? 0 : -7;            		
            	}
            	// get unit acceptance
            	int ans = unitCanAcceptChange(newUnit, newStatus);
            	if(ans!=0) return ans;
            	// valid status?
            	if(IAssignmentIf.ACTIVE_SET.contains(newStatus)) {
            		// check if new assignment violates the work flow requirements: 
            		int count = newUnit.getAssignedAssignments().size() + newUnit.getExecutingAssigments().size();
            		// allowed without any more checks?
            		if(AssignmentStatus.QUEUED.equals(newStatus) || count==0)                			
            			return 0;
            		if(newUnit.getAssignedAssignments().size()>0)
            			return -1;
					// failed
					return -8;
            	}
            	else if(IAssignmentIf.FINISHED_AND_REPORTED_SET.contains(newStatus)) {
            		return 0;
            	}
            	return -9;
            case QUEUED:
            case ASSIGNED:
            	// move to ready?
                if (newUnit == null) { 
                	return (newStatus == AssignmentStatus.READY ? 0 : -10);
                }
            	// get unit acceptance
            	ans = unitCanAcceptChange(newUnit, newStatus);
            	if(ans!=0) return ans;
            	// valid status?
            	if(IAssignmentIf.ACTIVE_SET.contains(newStatus)) {
            		// check if new assignment violates the work flow requirements: 
            		int count = newUnit.getAssignedAssignments().size() + newUnit.getExecutingAssigments().size();
            		if(count==0 || anAssignment.equals(newUnit.getAssignedAssignment()) 
            					|| anAssignment.equals(newUnit.getExecutingAssigment()))                			
            			return 0;
            	}
            	// failure
            	return -11;
            case EXECUTING:
                return (newUnit == currentUnit && IAssignmentIf.FINISHED_AND_REPORTED_SET.contains(newStatus)) ? 0 : -12;
            case ABORTED:
            case FINISHED:
                return (newUnit == currentUnit && newStatus == AssignmentStatus.REPORTED) ? 0 : -13;
        }
        // failed
        throw new IllegalArgumentException("Status is " + newStatus);
    }

    public static String getErrorMessage(AssignmentStatus changeTo, int reason, IUnitIf unit, IAssignmentIf assignment, boolean isHtml) {
    	// get initialize
    	String unitName = unit!=null ? MsoUtils.getUnitName(unit, false) : "";
    	String assignmentName = assignment!=null ? MsoUtils.getAssignmentName(assignment, 1) : "";
    	String template ="";
    	// get error message
    	if(changeTo!=null)
    		template = resource.getString("ChangeTo."+changeTo.name()+".text");
    	if(reason<0) {
    		if(!template.isEmpty()) template = template.concat(" " + resource.getString("Argument.text") + " ");
    		template = template.concat(resource.getString("Reason."+String.valueOf(Math.abs(reason))+".text"));
    	}
    	// finished
    	if(isHtml)
    		return String.format(template,getBold(unitName),getBold(assignmentName),getBold(unitName));
    	else
    		return String.format(template,unitName,assignmentName,unitName);
    }
    
    private static String getBold(String text) {
    	return "<b>"+text+"</b>";
    }
 
    /**
     * Assigns an assignment to some unit. All statuses are updated.
     * @param assignment The assignment
     * @param unit The unit to get the assignment
     * @throws IllegalOperationException 
     */
    public static void unitAssignAssignment(IUnitIf unit,IAssignmentIf assignment) throws IllegalOperationException
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
    	unit.setPosition(MsoUtils.getStartPosition(assignment));
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
