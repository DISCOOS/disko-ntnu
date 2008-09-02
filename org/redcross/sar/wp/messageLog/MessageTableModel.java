package org.redcross.sar.wp.messageLog;


import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineListIf;
import org.redcross.sar.mso.data.IMessageLogIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IMessageIf.MessageStatus;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.util.mso.Selector;
import org.redcross.sar.wp.messageLog.ChangeTasksDialog.TaskSubType;

/**
 * Table model providing log table with data
 */
public class MessageTableModel extends AbstractTableModel implements IMsoUpdateListenerIf
{
    private static final long serialVersionUID = 1L;

    List<IMessageIf> m_messageList;
    JTable m_table;

    private IMsoEventManagerIf m_eventManager;
    IDiskoWpMessageLog m_wpModule;

    private HashMap<String, Boolean> m_rowExpandedMap;

    /**
     * @param aTable   Log table
     * @param aModule  Message log work process
     * @param listener Selection listener
     */
    public MessageTableModel(JTable aTable, IDiskoWpMessageLog aModule)
    {
        m_table = aTable;
        m_wpModule = aModule;
        m_eventManager = aModule.getMsoEventManager();
        m_eventManager.addClientUpdateListener(this);
        m_rowExpandedMap = new HashMap<String, Boolean>();
    }

    /**
     *
     */
    public int getRowCount()
    {
        return m_messageList.size();
    }

    /**
     *
     */
    public int getColumnCount()
    {
        return 7;
    }

    /**
     * Get messages, update expanded hash map
     */
    void buildTable(IMessageIf exclude)
    {
    	// get command post
    	ICmdPostIf cmdPost = m_wpModule.getCmdPost();
    	// has command post?
    	if(cmdPost!=null) {
	    	// get message log
	        IMessageLogIf messageLog = m_wpModule.getCmdPost().getMessageLog();
	        // exclude message when selection
	        m_messageSelector.exclude(null);//MessageLogBottomPanel.isNewMessage() ? null : exclude);
	        // select messages
	        m_messageList = messageLog.selectItems(m_messageSelector, IMessageIf.MESSAGE_NUMBER_COMPARATOR);        
	        // Update hash map
	        HashMap<String, Boolean> tempMap = new HashMap<String, Boolean>(m_rowExpandedMap);
	        m_rowExpandedMap.clear();
	        int numMessages = m_messageList.size();
	        for (int i = 0; i < numMessages; i++)
	        {
	        	IMessageIf message = m_messageList.get(i);
	            String messageId = message.getObjectId();
	            Boolean expanded = tempMap.get(messageId);
	            if (expanded != null)
	            {
	                m_rowExpandedMap.put(messageId, expanded);
	            } else
	            {
	                m_rowExpandedMap.put(messageId, false);
	            }
	        }
    	}
    }

    /**
     * Get value of message field
     *
     * @param rowIndex    Message number
     * @param columnIndex Index of field
     */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
    	// invalid index?
    	if(rowIndex>=m_messageList.size()) return null;
    	
    	// get message
        IMessageIf message = m_messageList.get(rowIndex);

        switch (columnIndex)
        {
            case 0: return message;
            
            case 1: return DTG.CalToDTG(message.getTimeStamp());
            
            case 2: 
            	
                ICommunicatorIf sender = message.getSender();
                if (sender == null)
                {
                	sender = (ICommunicatorIf) m_wpModule.getCmdPost();
                }
                return sender;
                     
            case 3:
            	
                if (message.isBroadcast())
                {
    				int unconfirmed = message.getUnconfirmedReceivers().size();
    				int count = unconfirmed + message.getConfirmedReceivers().size();
                    return new Integer[]{count-unconfirmed,count};
                } else
                {
                    ICommunicatorIf receiver = message.getSingleReceiver();
                    if (receiver == null)
                    {
                        receiver = (ICommunicatorIf) m_wpModule.getCmdPost();
                    }
                    return receiver;
                }                                
                
            case 4:
            	
            	// initialize
            	StringBuilder stringBuilder = new StringBuilder();

            	// get message lines
            	IMessageLineListIf lines = message.getMessageLines();
            	
            	// loop over all lines
            	for(IMessageLineIf line : lines.getItems())
            	{
                     stringBuilder.append(line.toString() + "LINEEND");
                }
                return stringBuilder.toString().split("LINEEND");
                
            case 5:
            	
                StringBuilder taskBuilder = new StringBuilder();
                for (ITaskIf task : message.getMessageTasksItems())
                {
                    if (ChangeTasksDialog.getSubType(task) == TaskSubType.FINDING)
                    {
                        String taskString = null;
                        IMessageLineIf line = message.findMessageLine(MessageLineType.POI, false);
                        if (line != null)
                        {

                            IPOIIf poi = line.getLinePOI();
                            if (poi != null && poi.getType() == POIType.SILENT_WITNESS)
                            {
                                taskString = String.format(m_wpModule.getBundleText("TaskSubType.FINDING.text"),
                                        m_wpModule.getBundleText("SilentWitness.text"));
                            } else
                            {
                                taskString = String.format(m_wpModule.getBundleText("TaskSubType.FINDING.text"),
                                        m_wpModule.getBundleText("Finding.text"));
                            }
                        } else
                        {
                            // Set task finding to finding if no message line added
                            taskString = String.format(m_wpModule.getBundleText("TaskSubType.FINDING.text"),
                                    m_wpModule.getBundleText("Finding.text"));
                        }
                        taskBuilder.append(taskString);
                    } else
                    {
                        taskBuilder.append(task.getTaskText());
                    }

                    taskBuilder.append("\n");
                }                
                return taskBuilder.toString().split("\\n");
                
            case 6: return message.getStatus();
            
            case 7: return message;
            
            default: return null;
            
        }
    }

    @Override
    public String getColumnName(int column)
    {
        switch (column)
        {
            case 0:
                return m_wpModule.getBundleText("Number.text");
            case 1:
                return m_wpModule.getBundleText("DTG.text");
            case 2:
                return m_wpModule.getBundleText("From.text");
            case 3:
                return m_wpModule.getBundleText("To.text");
            case 4:
                return m_wpModule.getBundleText("MessageLines.text");
            case 5:
                return m_wpModule.getBundleText("Tasks.text");
            case 6:
                return m_wpModule.getBundleText("Status.text");
        }
        return null;
    }

    /**
     * Rebuild table data model when MSO changes
     */
    public void handleMsoUpdateEvent(Update e)
    {
        if(e.isClearAllEvent()) {
        	m_messageList.clear();
        	fireTableDataChanged();        	
        }
        else {
        	boolean bFlag = false;
            Object source = e.getSource();
	        if (source instanceof IMessageIf)
	        {
	        	bFlag = handleMessageEvent((IMessageIf) source, e);
	        } else if (source instanceof IMessageLineIf)
	        {
	        	bFlag = handleMessageLineEvent((IMessageLineIf) source);
	        } else if (source instanceof IUnitIf)
	        {
	        	bFlag = handleUnitEvent((IUnitIf) source);
	        } else if (source instanceof IPOIIf)
	        {
	        	bFlag = handlePOIEvent((IPOIIf) source);
	        } else if (source instanceof IAssignmentIf)
	        {
	            bFlag = handleAssignmentEvent((IAssignmentIf) source);
	        }
	        if(bFlag) 
	        	updateRowHeights();

        }
    }

    private boolean handleMessageEvent(IMessageIf aMessage, Update e)
    {
        if (e.isCreateObjectEvent() || e.isDeleteObjectEvent())
        {
            rebuildTable(aMessage);
            return true;
        } else
        {
            return messageChanged(aMessage);           
        }
    }

    private void rebuildTable(IMessageIf aMessage)
    {
        buildTable(aMessage);
        fireTableDataChanged();
    }

    private boolean messageChanged(IMessageIf aMessage)
    {
        int messageIndex = m_messageList.indexOf(aMessage);
        if (messageIndex >= 0)
        {
            fireTableRowsUpdated(messageIndex, messageIndex);
            return true;
        }
        return false;
    }

    private boolean messagesChanged(Collection<IMessageIf> theMessages)
    {
    	int count = 0;
        for (IMessageIf m : theMessages)
        {
            if(messageChanged(m)) count++;
        }
        return count>0;
    }

    private boolean handleMessageLineEvent(IMessageLineIf aLine)
    {
        return messageChanged(aLine.getOwningMessage());
    }

    private boolean handleUnitEvent(IUnitIf aUnit)
    {
        return messagesChanged(aUnit.getReferringMessages(m_messageList));
    }

    private boolean handlePOIEvent(IPOIIf aPoi)
    {
    	int count = 0;
        for (IMessageIf m : m_messageList)
        {
            if (aPoi.getReferringMessageLines(m.getMessageLineItems()).size() > 0)
            {
                if(messageChanged(m)) count++;
            }
        }
        return count>0;
    }

    private boolean handleAssignmentEvent(IAssignmentIf anAssignment)
    {
    	int count = 0;
        for (IMessageIf m : m_messageList)
        {
            if (anAssignment.getReferringMessageLines(m.getMessageLineItems()).size() > 0)
            {
                if(messageChanged(m)) count++;
            }
        }
        return count>0;
    }


    private final EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of(
            IMsoManagerIf.MsoClassCode.CLASSCODE_MESSAGE,
            IMsoManagerIf.MsoClassCode.CLASSCODE_MESSAGELINE,
            IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT,
            IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT,
            IMsoManagerIf.MsoClassCode.CLASSCODE_POI);

    /**
     * Interested in when messages, message lines, assignments, unit and POI updates.
     */
	public boolean hasInterestIn(IMsoObjectIf aMsoObject, UpdateMode mode) 
	{
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
        return myInterests.contains(aMsoObject.getMsoClassCode());
    }

    private final MessageSelector m_messageSelector = new MessageSelector();
    
    private class MessageSelector implements Selector<IMessageIf>
    {
    	
    	private IMessageIf m_exclude = null;
    	
        public boolean select(IMessageIf aMessage)
        {
            return m_exclude==null || !m_exclude.equals(aMessage);
        }
        
        public void exclude(IMessageIf aMessage) {
        	m_exclude = aMessage;
        }               
        
    };

    public IMessageIf getMessage(int row)
    {
        return m_messageList.get(row);
    }
    
    /**
     * @param messageId
     * @return Whether or not the message is extended in the message log table, i.e. display entire message in log
     */
    public Boolean isMessageExpanded(String messageId)
    {
        return m_rowExpandedMap.get(messageId);
    }

    public int findRow(String messageId) 
    {
    	for(int i=0;i<m_messageList.size();i++) {
    		if(m_messageList.get(i).getObjectId().equals(messageId))
    			return i;
    	}
    	return -1;
    	
    }
    
    /**
     * Sets whether the message is extended in log view or not
     *
     * @param messageId
     * @param expanded
     */
    public void setMessageExpanded(String messageId, Boolean expanded)
    {    	
    	int row = findRow(messageId);
    	if(row!=-1) {
    		if(numRows(row)>1)
    			m_rowExpandedMap.put(messageId, expanded);
    		else
    			m_rowExpandedMap.put(messageId, false);
    	}
        
    }

    public void updateRowHeights()
    {
        for (int i = 0; i < m_messageList.size(); i++)
        {
            IMessageIf message = m_messageList.get(i);
            Boolean expanded = m_rowExpandedMap.get(message.getObjectId());

            if (expanded)
            {
                setRowExpanded(i);
            } else
            {
                setRowCollapsed(i);
            }
        }
    }

    /**
     * Expands a row so that it encompasses all text in message lines
     *
     * @param rowIndex Row identifier
     */
    public void setRowExpanded(int rowIndex)
    {
        // Calculate row height so that all text is visible in cell without changing column width
        int defaultRowHeight = 26; //m_messageTable.getRowHeight();
        int numRows = numRows(rowIndex);
        int rowHeight = numRows==0 ? defaultRowHeight : defaultRowHeight * numRows; // + (numRows - 1) * 2 + 4;
        m_table.setRowHeight(rowIndex, rowHeight);
    }

    /**
     * Collapses a row to the default size
     *
     * @param rowIndex Row identifier
     */
    public void setRowCollapsed(int rowIndex)
    {
        m_table.setRowHeight(rowIndex, 26);
    }

    /**
     * @param rowIndex Identifies the message line
     * @return Number of rows in the table need to display the entire contents of the message lines
     *         or task, whichever is longer
     */
    public int numRows(int rowIndex)
    {
        MessageCellRenderer renderer = (MessageCellRenderer) m_table.getDefaultRenderer(Object.class);
        Font font = renderer.getFont();
        FontMetrics fm = renderer.getFontMetrics(font);

        // Message lines
        int columnWidth = m_table.getColumnModel().getColumn(4).getWidth();
        int numMessageLines = 0;
        String[] messageLineStrings = (String[]) getValueAt(rowIndex, 4);
        for (String line : messageLineStrings)
        {
            int lineWidth = fm.stringWidth(line);
            numMessageLines += (lineWidth / columnWidth + 1);
        }

        // Tasks
        columnWidth = m_table.getColumnModel().getColumn(5).getWidth();
        String[] taskStrings = (String[]) getValueAt(rowIndex, 5);
        int numTaskLines = 0;
        for (String task : taskStrings)
        {
            int lineWidth = fm.stringWidth(task);
            numTaskLines += (lineWidth / columnWidth + 1);
        }

        return Math.max(numMessageLines, numTaskLines);
    }
    
    public String getBundleText(String aKey) {
    	return m_wpModule.getBundleText(aKey);    	
    }
    
    public boolean isRowCurrentMessage(int row) {
        IMessageIf rowMessage = m_messageList.get(row);
        IMessageIf selectedMessage = MessageLogBottomPanel.getCurrentMessage(false);
        return (selectedMessage != null && selectedMessage.equals(rowMessage));    	
    }
    
    public MessageStatus getMessageStatus(int row) {
        IMessageIf message = m_messageList.get(row);
        return message.getStatus();
    }
    
    
}