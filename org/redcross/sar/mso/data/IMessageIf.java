package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

import java.util.Calendar;
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

    public static final Comparator<IMessageIf> MESSAGE_NUMBER_COMPARATOR = new Comparator<IMessageIf>()
    {
        public int compare(IMessageIf m1, IMessageIf m2)
        {
            return m2.getNumber()-m1.getNumber();
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

    public void setCreated(Calendar aCreated);

    public Calendar getCreated();

    public IMsoModelIf.ModificationState getCreatedState();

    public IAttributeIf.IMsoCalendarIf getCreatedAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addConfirmedReceiver(ICommunicatorIf anICommunicatorIf);

    public IMsoListIf<ICommunicatorIf> getConfirmedReceivers();

    public IMsoModelIf.ModificationState getConfirmedReceiversState(ICommunicatorIf anICommunicatorIf);

    public Collection<ICommunicatorIf> getConfirmedReceiversItems();

    public void addMessageTask(ITaskIf anITaskIf);

    public ITaskListIf getMessageTasks();

    public IMsoModelIf.ModificationState getMessageTasksState(ITaskIf anITaskIf);

    public Collection<ITaskIf> getMessageTasksItems();

    public void addMessageLine(IMessageLineIf anIMessageLineIf);

    public IMessageLineListIf getMessageLines();

    public IMsoModelIf.ModificationState getMessageLinesState(IMessageLineIf anIMessageLineIf);

    public Collection<IMessageLineIf> getMessageLineItems();

    public void addUnconfirmedReceiver(ICommunicatorIf anICommunicatorIf);

    public IMsoListIf<ICommunicatorIf> getUnconfirmedReceivers();

    public IMsoModelIf.ModificationState getUnconfirmedReceiversState(ICommunicatorIf anICommunicatorIf);

    public Collection<ICommunicatorIf> getUnconfirmedReceiversItems();

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
     * Confirm receiver.
     * <p/>
     * Trasnfer receiver to list og confirmed receivers.
     *
     * @param anICommunicatorIf The receiver to transfer.
     * @return <code>true</code> if succeeded, false otherwise
     */
    public boolean confirmReceiver(ICommunicatorIf anICommunicatorIf);

    /**
     * Get list of unconfimred receivers.
     *
     * @return the list.
     */
    public MsoListImpl<ICommunicatorIf> getBroadcastUnconfirmed();

    /**
     * Get list of confimred receivers.
     *
     * @return the list.
     */
    public MsoListImpl<ICommunicatorIf> getBroadcastConfirmed();

    public void setSingleReceiver(ICommunicatorIf communicatorIf);

	public ICommunicatorIf getSingleReceiver();

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