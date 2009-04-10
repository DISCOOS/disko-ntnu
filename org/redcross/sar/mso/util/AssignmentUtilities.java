package org.redcross.sar.mso.util;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IMessageIf.MessageStatus;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.util.Utils;

import java.util.Calendar;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar, kennetgu
 * Date: 19.jun.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 * Class for handling assignment operations.
 */
public class AssignmentUtilities
{
	final static ResourceBundle resource = ResourceBundle.getBundle("org.redcross.sar.mso.util.assignmentTransferUtilities");

	/**
	 * Moves are validated by the following checks
	 *
	 * 0 = never allowed (no checks made)
	 * 1 = always allowed (no checks made)
	 * 2 = only allowed if no change (same unit and status)
	 * 3 = only allowed if move to unit exists
	 * 4 = only allowed if move to unit exists and is available
	 *
	 * moves[i][j] == move status i >> j
	 *
	 */

	final static int[][] moves = {
		{2,1,1,3,4,4,3,3,0},	// EMPTY -> j
		{0,2,1,3,4,4,3,3,0}, 	// DRAFT -> j
		{0,1,2,3,4,4,3,3,0}, 	// READY -> j
		{0,1,1,3,4,4,3,3,0},	// QUEUED -> j
		{0,1,1,3,4,4,3,3,0},	// ALLOCATED -> j
		{0,0,0,0,0,2,3,3,0},	// EXECUTING -> j
		{0,0,0,0,0,0,2,0,1},	// ABORTED -> j
		{0,0,0,0,0,0,0,2,1},	// FINISHED -> j
		{0,0,0,0,0,0,0,0,2}		// REPORTED -> j
	};


	private static boolean isChange(int fromStatus, int toStatus, IUnitIf fromUnit, IUnitIf toUnit) {
		return !isSameStatus(fromStatus,toStatus) || !isSameUnit(fromUnit,toUnit);
	}

	private static boolean isSameStatus(int fromStatus, int toStatus) {
		return fromStatus==toStatus;
	}

	private static boolean isSameUnit(IUnitIf fromUnit, IUnitIf toUnit) {
		return fromUnit==toUnit || fromUnit!=null && fromUnit.equals(toUnit);
	}

	private static boolean unitExists(IUnitIf unit) {
		return unit!=null;
	}

	private static boolean isUnitAvailable(IUnitIf toUnit) {
		// exists?
		if(unitExists(toUnit)) {
            return 	toUnit.getAllocatedAssignments().size() == 0 &&
            toUnit.getExecutingAssigments().size() == 0;
		}
		return false;
	}

	private static int getCheck(int from, int to) {
		return moves[from][to];
	}

	public static Object[] verifyMove(IAssignmentIf assignment, IUnitIf toUnit, AssignmentStatus toStatus) {
		// initialize
		int ans = -1;
		// required parameters exist?
		if(assignment!=null && toStatus!=null) {
			// get from unit
			IUnitIf fromUnit = assignment.getOwningUnit();
			// get status indexes
			int from = assignment.getStatus().ordinal();
			int to = toStatus.ordinal();
			// translate to check
			switch(getCheck(from, to)) {
			case 0: 			// never allowed
				ans = -1;
				break;
			case 1: 			// always allowed (no checks made)
				ans = 1;
				break;
			case 2: 			// only allowed if no change (same unit and status)
				ans = isChange(from,to,fromUnit,toUnit) ? (unitExists(toUnit) ? -1 : -2) : 0;
				break;
			case 3: 			// only allowed if move to unit exists
				ans = unitExists(toUnit) ? (isChange(from,to,fromUnit,toUnit) ? 1 : 0) : -2;
				break;
			case 4: 			// only allowed if move to unit exists and is available
				ans = isSameUnit(fromUnit, toUnit) || isUnitAvailable(toUnit) ? (isChange(from,to,fromUnit,toUnit) ? 1 : 0) : -3;
				break;
			}
		}
		// finished
		return new Object[]{ans,getExplanation(ans, assignment, toUnit, toStatus, true)};
	}

	public static String getExplanation(int ans, IAssignmentIf assignment, IUnitIf toUnit, AssignmentStatus toStatus, boolean isHtml) {

		// get initialize
    	String template ="";
    	String s1 = assignment!=null ? MsoUtils.getAssignmentName(assignment, 1) : "";
    	String s2 = assignment!=null ? DiskoEnumFactory.getText(assignment.getStatus()) : "<missing>";
    	String s3 = toStatus!=null ? DiskoEnumFactory.getText(toStatus) : "<missing>";
    	String s4 = toUnit!=null ? MsoUtils.getUnitName(toUnit, false) : "<missing>";

		// translate
		if(ans<0)
			template = resource.getString("Status.ERROR."+Math.abs(ans)+".text");
		else
			template = resource.getString("Status.CHANGE."+ans+".text");

    	// finished
    	if(isHtml)
    		return String.format(template,Utils.getBold(s1),Utils.getBold(s2),Utils.getBold(s3),Utils.getBold(s4));
    	else
    		return String.format(template,s1,s2,s3,s4);

	}

    /**
     * Create an assignment transfer message and put it in the message log. </p>
     *
     * For each status change between {@link AssignmentStatus#READY} and
     * {@link AssignmentStatus#FINISHED} is one message line generated. </p>
     *
     * If an assignment is reverted, or moved from a unit, two messages is created. </p>
     *
     * New messages are generated with status {@link MessageStatus#UNCONFIRMED}. </p>
     *
     * @param anMsoManager - The MSO Manager.
     * @param receiver     - Unit receiving the assignment.
     * @param anAssignment - The assignment that is transferred
     * @param oldStatus    - Former assignment status
     */
    public static IMessageIf createAssignmentChangeMessage(
    		IMsoManagerIf anMsoManager,
    		IAssignmentIf anAssignment, IUnitIf receiver,
    		AssignmentStatus oldStatus, IUnitIf oldUnit)
    {
    	// initialize
    	IMessageIf aMessage = null;
    	// reallocated to new unit?
    	if(oldUnit!=null && receiver!=oldUnit) {
    		// create recall message
            aMessage = createMessage(anMsoManager, anMsoManager.getCmdPostCommunicator(), oldUnit);
            // add recalled message line
            createAssignmentChangeMessageLines(aMessage, MessageLineType.RECALLED, MessageLineType.RECALLED, aMessage.getTimeStamp(), oldUnit, anAssignment);
    		// create change message
    		aMessage = createAssignmentChangeMessage(anMsoManager, anAssignment, receiver, oldStatus);
    	}
    	// is status change recalled within same unit?
    	else if(anAssignment.getStatus().ordinal()<oldStatus.ordinal()) {
    		// create default message
            aMessage = createMessage(anMsoManager, anMsoManager.getCmdPostCommunicator(), receiver);
            // add recalled message line first
            createAssignmentChangeMessageLines(aMessage, MessageLineType.RECALLED, MessageLineType.RECALLED, aMessage.getTimeStamp(), receiver, anAssignment);
            // add the remaining lines
            createAssignmentChangeMessage(aMessage, anAssignment, receiver, oldStatus);
    	}
    	else {
    		// create message
    		aMessage = createAssignmentChangeMessage(anMsoManager, anAssignment, receiver, oldStatus);
    	}
    	// finished
    	return aMessage;
    }

    /**
     * Create an assignment transfer message and put it in the message log. </p>
     *
     * For each status change between {@link AssignmentStatus#READY} and
     * {@link AssignmentStatus#FINISHED} is one message line generated. </p>
     *
     * If an assignment is reverted, or moved from a unit, two messages is created. </p>
     *
     * New messages are generated with status {@link MessageStatus#UNCONFIRMED}. </p>
     *
     * @param anMsoManager - The MSO Manager.
     * @param receiver     - Unit receiving the assignment.
     * @param anAssignment - The assignment that is transferred
     * @param oldStatus    - Former assignment status
     */
    private static IMessageIf createAssignmentChangeMessage(
    		IMsoManagerIf anMsoManager,
    		IAssignmentIf anAssignment, IUnitIf receiver,
    		AssignmentStatus oldStatus)
    {
    	// create message
        IMessageIf aMessage = createMessage(anMsoManager, anMsoManager.getCmdPostCommunicator(), receiver);
        // forward
        createAssignmentChangeMessage(aMessage, anAssignment, receiver, oldStatus);
        // finished
        return aMessage;
    }

    /**
     * Create an assignment transfer message. </p>
     *
     * For each status change between {@link AssignmentStatus#READY} and
     * {@link AssignmentStatus#FINISHED} is one message line generated. </p>
     *
     * If an assignment is reverted, or moved from a unit, two messages is created. </p>
     *
     * New messages are generated with status {@link MessageStatus#UNCONFIRMED}. </p>
     *
     * @param aMessage 	- The MSO Manager.
     * @param receiver     - Unit receiving the assignment.
     * @param anAssignment - The assignment that is transferred
     * @param oldStatus    - Former assignment status
     */
    private static void createAssignmentChangeMessage(
    		IMessageIf aMessage,
    		IAssignmentIf anAssignment, IUnitIf receiver,
    		AssignmentStatus oldStatus)
    {


    	// get first message type
        MessageLineType fromType;
        switch (oldStatus)
        {
            case READY:
            case QUEUED:
                fromType = MessageLineType.ALLOCATED;
                break;
            case ALLOCATED:
                // Special consideration when moving allocated assignments between units.
                if (anAssignment.getStatus() ==  AssignmentStatus.ALLOCATED)
                {
                    fromType = MessageLineType.ALLOCATED;
                } else
                {
                    fromType = MessageLineType.STARTED;
                }
                break;
            case EXECUTING:
                fromType = MessageLineType.COMPLETED;
                break;
            case ABORTED:
                fromType = MessageLineType.ABORTED;
                break;
            default:
                return;
        }

    	// get last message type
        MessageLineType toType;
        switch (anAssignment.getStatus())
        {
            case ALLOCATED:
                toType = MessageLineType.ALLOCATED;
                break;
            case EXECUTING:
                toType = MessageLineType.STARTED;
                break;
            case ABORTED:
            case FINISHED:
            case REPORTED:
                toType = MessageLineType.COMPLETED;
                break;
            default:
                return;
        }
        createAssignmentChangeMessageLines(aMessage, fromType, toType, aMessage.getTimeStamp(), receiver, anAssignment);
    }

    private final static IMessageIf createMessage(
    		IMsoManagerIf anMsoManager, ICommunicatorIf sender, ICommunicatorIf receiver) {

        IMessageIf message = anMsoManager.createMessage();
        message.setStatus(MessageStatus.UNCONFIRMED);
        message.setSender(sender);
        message.setReceiver(receiver);

        return message;

    }

    private final static MessageLineType[] types = {
    		MessageLineType.ALLOCATED,
            MessageLineType.STARTED,
            MessageLineType.COMPLETED,
            MessageLineType.ABORTED,
            MessageLineType.RECALLED
    };

    /**
     * Create a set of message lines for assignment transfers.
     *
     * @param message       The message where the lines shall be put. Possible existing lines of the same type in the message will be overwritten.
     * @param fromType First line type to generate.
     * @param toType  Last line type to generate.
     * @param aDTG          Time when the message was created.
     * @param anAssignment  The assignment that is transferred
     */
    public static void createAssignmentChangeMessageLines(IMessageIf message,
    		MessageLineType fromType, MessageLineType toType, Calendar aDTG,
    		IUnitIf unit, IAssignmentIf anAssignment)
    {
        for (MessageLineType t : types)
        {
            if (t.ordinal() >= fromType.ordinal() && t.ordinal() <= toType.ordinal())
            {
                IMessageLineIf line = message.findMessageLine(t, anAssignment, true);
                if (line != null)
                {
                    line.setLineTime(aDTG);
                    line.setLineUnit(unit);
                    line.setLineAssignment(anAssignment);
                }
            }
        }
    }

}
