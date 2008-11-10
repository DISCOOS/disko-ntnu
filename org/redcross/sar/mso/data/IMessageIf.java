package org.redcross.sar.mso.data;

import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoModelIf;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public interface IMessageIf extends ITimeItemIf, ISerialNumberedIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.Message";

    public enum MessageStatus
    {
        UNCONFIRMED,
        CONFIRMED,
        POSTPONED
    }

    /**
     * Often used selectors
     */
    public static final Selector<IMessageIf> SELECT_ALL = new Selector<IMessageIf>() {

		public boolean select(IMessageIf anObject) {
			return true;
		}
    };

    /**
     * Often used comparators
     */

    public static final Comparator<IMessageIf> MESSAGE_NUMBER_COMPARATOR = new Comparator<IMessageIf>()
    {
        public int compare(IMessageIf m1, IMessageIf m2)
        {
            return m1.getNumber()-m2.getNumber();
        }
    };


    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(MessageStatus aStatus);

    public void setStatus(String aStatus);

    public MessageStatus getStatus();

    public IMsoModelIf.ModificationState getStatusState();

    public IAttributeIf.IMsoEnumIf<MessageStatus> getStatusAttribute();

    public String getStatusText();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setBroadcast(boolean aBroadcast);

    public boolean isBroadcast();

    public IMsoModelIf.ModificationState getBroadcastState();

    public IAttributeIf.IMsoBooleanIf getBroadcastAttribute();

    /*
    public void setEventTime(Calendar aTime);

    public Calendar getEventTime();

    public IMsoModelIf.ModificationState getEventTimeState();

    public IAttributeIf.IMsoCalendarIf getEventTimeAttribute();
    */

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public IMsoListIf<ICommunicatorIf> getConfirmedReceivers();

    public IMsoModelIf.ModificationState getConfirmedReceiversState(ICommunicatorIf anICommunicatorIf);

    public Collection<ICommunicatorIf> getConfirmedReceiversItems();

    public IMsoListIf<ICommunicatorIf> getUnconfirmedReceivers();

    public IMsoModelIf.ModificationState getUnconfirmedReceiversState(ICommunicatorIf anICommunicatorIf);

    public Collection<ICommunicatorIf> getUnconfirmedReceiversItems();

    public void addMessageTask(ITaskIf anITaskIf);

    public ITaskListIf getMessageTasks();

    public IMsoModelIf.ModificationState getMessageTasksState(ITaskIf anITaskIf);

    public Collection<ITaskIf> getMessageTasksItems();

    public void addMessageLine(IMessageLineIf anIMessageLineIf);

    public IMessageLineListIf getMessageLines();

    public IMsoModelIf.ModificationState getMessageLinesState(IMessageLineIf anIMessageLineIf);

    public Collection<IMessageLineIf> getMessageLineItems();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setSender(ICommunicatorIf aCommunicator);

    public ICommunicatorIf getSender();

    public IMsoModelIf.ModificationState getSenderState();

    public IMsoReferenceIf<ICommunicatorIf> getSenderAttribute();

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    /**
     * Register that the broadcast message is not received by communicator. <p/>
     *
     * This will automatically set <code>isBroadcast()</code> flag <code>true</code>.
     *
     * @param anICommunicatorIf The receiver that has not confirmed the message.
     *
     * @return <code>true</code> if succeeded, false otherwise
     */
    public boolean setUnconfirmed(ICommunicatorIf aReceiver);

    /**
     * Register that the broadcast message is received by communicator. <p/>
     *
     * This will automatically set <code>isBroadcast()</code> flag <code>true</code>
     * if the number of receivers is greater than 1.
     *
     * @param anICommunicatorIf The receiver to transfer.
     *
     * @return <code>true</code> if succeeded, false otherwise.
     */
    public boolean setConfirmed(ICommunicatorIf anICommunicatorIf);

    /**
     * Remove a receiver. If message is a broadcast, the receiver is
     * removed from either unconfirmed or confirmed stack.
     *
     * @param ICommunicatorIf - communicatorIf
     */
    public void removeReceiver(ICommunicatorIf communicator);

    /**
     * Get all receivers of the message
     *
     * @return ICommunicatorIf - the receiver
     */
	public Collection<ICommunicatorIf> getReceivers();

    /**
     * Get the confirmed receiver of a unicast message, or the first receiver in a broadcast message.
     *
     * @return ICommunicatorIf - the receiver
     */
	public ICommunicatorIf getReceiver();

    /**
     * Set a single receiver. This will reset all broadcast information and
     * <code>isBroadcast()</code> to <code>false</code>
     *
     * @param ICommunicatorIf - communicatorIf
     */
    public void setReceiver(ICommunicatorIf communicator);

    /**
     * Find a (optionally create a new) message line of given type.
     *
     * @param aType       Type of line to create.
     * @param makeNewLine If set, create a new line if non-existing.
     * @return Actual line if found or created, otherwise null.
     */
    public IMessageLineIf findMessageLine(IMessageLineIf.MessageLineType aType, boolean makeNewLine);


    /**
     * Find a (optionally create a new) message line of given type (and corresponding assignment for certain line types).
     *
     * @param aType       Type of line to create.
     * @param anAssignment Associated assignment for certain line types.
     * @param makeNewLine If set, create a new line if non-existing.
     * @return Actual line if found or created, otherwise null.
     */
    public IMessageLineIf findMessageLine(IMessageLineIf.MessageLineType aType, IAssignmentIf anAssignment, boolean makeNewLine);

    /**
     * Get all message lines of a given type.
     *
     * @param aType       Type of line.
     * @return List of lines, sorted by line number.
     */
    public List<IMessageLineIf> getTypeMessageLines(IMessageLineIf.MessageLineType aType);

    /**
     * Delete a line
     * @param aLine The line to delete
     * @return
     */
    public boolean deleteMessageLine(IMessageLineIf aLine);

    public boolean deleteMessageLine(int aLineNumber);

    public boolean deleteMessageLine(IMessageLineIf.MessageLineType aType);

    public IMessageLineIf createMessageLine(IMessageLineIf.MessageLineType aType);

    public String[] getLines();
}