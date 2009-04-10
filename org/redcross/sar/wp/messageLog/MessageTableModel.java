package org.redcross.sar.wp.messageLog;

import java.awt.Font;
import java.awt.FontMetrics;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JTable;

import org.redcross.sar.data.IData;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.model.AbstractMsoTableModel;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IMessageIf.MessageStatus;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.data.ITaskIf.TaskType;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.wp.IDiskoWpModule;
import org.redcross.sar.wp.messageLog.ChangeTasksDialog.TaskSubType;

/**
 * Table model providing log table with data
 */
public class MessageTableModel extends AbstractMsoTableModel<IMessageIf>
{
    private static final long serialVersionUID = 1L;

    private JTable m_table;
    private IDiskoWpMessageLog m_wp;

    private final HashMap<String, Boolean> m_toggleMap = new HashMap<String, Boolean>();

	/* ================================================================
	 *  Constructors
	 * ================================================================ */

    public MessageTableModel(JTable aTable, IDiskoWpMessageLog aWp)
    {
    	// forward
    	super(IMessageIf.class,getNames("A",7),getCaptions(aWp, 7),false);
    	// prepare
        m_table = aTable;
        m_wp = aWp;
        // add to head of list
        setAddToTail(false);
        // forward
        connect(aWp.getMsoModel(),IMessageIf.SELECT_ALL,IMessageIf.MESSAGE_NUMBER_COMPARATOR);
        // add co-classes
        getMsoBinder().addCoClass(IMessageLineIf.class,null);
        getMsoBinder().addCoClass(IUnitIf.class,null);
        getMsoBinder().addCoClass(IAssignmentIf.class,null);
        getMsoBinder().addCoClass(IPOIIf.class,null);
        // load data
        load(aWp.getMsoModel().getMsoManager().getCmdPost().getMessageLog());
    }

	/* =============================================================
	 * MsoTableModel implementation
	 * ============================================================= */

	protected Object getCellValue(int row, String column) {
		// Since update algorithm is overridden, this method is never called.
		return null;
	}

	/**
	 * Update algorithm in MsoTableModel is overridden
	 */
	@Override
	protected Object[] update(IMessageIf id, IMessageIf obj, Object[] data) {

		// get row index
		int i = findRowFromId(id);

		// get message
        IMessageIf message = getId(i);

        // get column count
		int jCount= getColumnCount();

		// loop over all columns
		for(int j=0; j<jCount; j++) {

			// translate
	        switch (j)
	        {
            case 0:
            	data[j] = message;
                break;

            case 1:
            	data[j] = DTG.CalToDTG(message.getTimeStamp());
                break;

            case 2:

                ICommunicatorIf sender = message.getSender();
                if (sender == null)
                {
                	sender = (ICommunicatorIf) m_wp.getCmdPost();
                }
                data[j] = sender;
                break;

            case 3:

                if (message.isBroadcast())
                {
    				int unconfirmed = message.getUnconfirmedReceivers().size();
    				int count = unconfirmed + message.getConfirmedReceivers().size();
                    data[j] = new Integer[]{count-unconfirmed,count};
                } else
                {
                    ICommunicatorIf receiver = message.getReceiver();
                    if (receiver == null)
                    {
                        receiver = (ICommunicatorIf) m_wp.getCmdPost();
                    }
                    data[j] = receiver;
                }
                break;

            case 4:

            	// initialize
            	StringBuilder stringBuilder = new StringBuilder();

            	// get message lines
            	Collection<IMessageLineIf> lines = message.getMessageLines().selectItems(
            			IMessageLineIf.ALL_SELECTOR,IMessageLineIf.LINE_NUMBER_COMPARATOR);

            	// loop over all lines
            	for(IMessageLineIf line : lines)
            	{
                     stringBuilder.append(line.toString() + "LINEEND");
                }
                data[j] = stringBuilder.toString().split("LINEEND");
                break;

            case 5:

                StringBuilder taskBuilder = new StringBuilder();
                for (ITaskIf task : message.getMessageTasksItems())
                {
                    if (getSubType(task) == TaskSubType.FINDING)
                    {
                        String taskString = null;
                        IMessageLineIf line = message.findMessageLine(MessageLineType.POI, false);
                        if (line != null)
                        {

                            IPOIIf poi = line.getLinePOI();
                            if (poi != null && poi.getType() == POIType.SILENT_WITNESS)
                            {
                                taskString = String.format(DiskoEnumFactory.getText(TaskSubType.FINDING),
                                        m_wp.getBundleText("SilentWitness.text"));
                            } else
                            {
                                taskString = String.format(DiskoEnumFactory.getText(TaskSubType.FINDING),
                                        m_wp.getBundleText("Finding.text"));
                            }
                        } else
                        {
                            // Set task finding to finding if no message line added
                            taskString = String.format(DiskoEnumFactory.getText(TaskSubType.FINDING),
                                    m_wp.getBundleText("Finding.text"));
                        }
                        taskBuilder.append(taskString);
                    } else
                    {
                        taskBuilder.append(task.getTaskText());
                    }

                    taskBuilder.append("\n");
                }
                data[j] = taskBuilder.toString().split("\\n");
                break;

            case 6:
            	data[j] = message.getStatus();
                break;

            case 7:
            	data[j] = message;
            	break;

            default:
            	data[j] = null;
            	break;
	        }
		}
		// finished
		return data;
	}

	public int getRowCount() {
		return super.getRowCount();
	}


	/* ================================================================
	 *  DiskoTableModel implementation
	 * ================================================================ */

	@Override
	protected IMessageIf[] translate(IData[] data) {
		if(data!=null) {
			List<IMessageIf> list = new ArrayList<IMessageIf>(data.length);
			List<IMessageIf> found = new ArrayList<IMessageIf>(data.length);
			for(int i=0; i<data.length; i++) {
				found.clear();
				IData item = data[i];
				if(item instanceof IMessageIf){
					found.add((IMessageIf)item);
				}
				else if(item instanceof IMessageLineIf){
					found.add(((IMessageLineIf)item).getOwningMessage());
				}
				else if (item instanceof IUnitIf) {
					IUnitIf unit = (IUnitIf)item;
					found.addAll(unit.getReferringMessages(getIds()));
				}
				else if (item instanceof IPOIIf) {
					IPOIIf poi = (IPOIIf)item;
					for(IMessageIf it : getIds()) {
						if(poi.getReferringMessageLines(it.getMessageLineItems()).size()>0) {
							found.add(it);
						}
					}
				}
				else if (item instanceof IAssignmentIf) {
					IAssignmentIf assignment = (IAssignmentIf)item;
					for(IMessageIf it : getIds()) {
						if(assignment.getReferringMessageLines(it.getMessageLineItems()).size()>0) {
							found.add(it);
						}
					}
				}
				// search for messages
				for(IMessageIf it : found) {
					if(findRowFromId(it)!=-1) {
						list.add(it);
					}
				}
			}

			// any found?
			if(list.size()>0) {
				IMessageIf[] idx = new IMessageIf[list.size()];
				list.toArray(idx);
				return idx;
			}

		}
		// default action
		return super.translate(data);
	}

	/* ================================================================
	 *  AbstractTableModel implementation
	 * ================================================================ */

	@Override
	public void fireTableDataChanged() {
		updateToggleMap();
	}

	/* ================================================================
	 *  Public methods
	 * ================================================================ */

    public IMessageIf getMessage(int row)
    {
        return getId(row);
    }

    /**
     * @param messageId
     * @return Whether or not the message is extended in the message log table, i.e. display entire message in log
     */
    public Boolean isMessageExpanded(String messageId)
    {
        return m_toggleMap.get(messageId);
    }

    public int findRow(String messageId)
    {
    	int count = getRowCount();
    	for(int i=0;i<count;i++) {
    		if(getId(i).getObjectId().equals(messageId))
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
    			m_toggleMap.put(messageId, expanded);
    		else
    			m_toggleMap.put(messageId, false);
    	}

    }

    public void updateRowHeights()
    {
    	int count = getRowCount();
    	for(int i=0;i<count;i++)
        {
            IMessageIf message = getId(i);
            Boolean expanded = m_toggleMap.get(message.getObjectId());
            if (expanded==null || expanded)
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
        if(messageLineStrings!=null) {
	        for (String line : messageLineStrings)
	        {
	            int lineWidth = fm.stringWidth(line);
	            numMessageLines += (lineWidth / columnWidth + 1);
	        }
        }

        // Tasks
        columnWidth = m_table.getColumnModel().getColumn(5).getWidth();
        int numTaskLines = 0;
        String[] taskStrings = (String[]) getValueAt(rowIndex, 5);
        if(taskStrings!=null) {
	        for (String task : taskStrings)
	        {
	            int lineWidth = fm.stringWidth(task);
	            numTaskLines += (lineWidth / columnWidth + 1);
	        }
        }

        return Math.max(numMessageLines, numTaskLines);
    }

    public String getBundleText(String aKey) {
    	return m_wp.getBundleText(aKey);
    }

    public boolean isRowCurrentMessage(int row) {
        IMessageIf rowMessage = getId(row);
        IMessageIf selectedMessage = MessageLogBottomPanel.getCurrentMessage(false);
        return (selectedMessage != null && selectedMessage.equals(rowMessage));
    }

    public MessageStatus getMessageStatus(int row) {
        IMessageIf message = getId(row);
        return message.getStatus();
    }

	/* ================================================================
	 *  Helper methods
	 * ================================================================ */

    private static String[] getNames(String prefix, int count) {
    	String[] names = new String[count];
    	for(int i=0;i<count;i++) {
    		names[i] = prefix + i;
    	}
    	return names;
    }

    private static String[] getCaptions(IDiskoWpModule wp, int count) {
    	String[] captions = new String[count];
    	for(int i=0;i<count;i++) {
    		captions[i] = wp.getBundleText(MessageFormat.format("MessageTable_hdr_{0}.text", i));
    	}
    	return captions;
    }

    /**
     * Get messages, update expanded hash map
     */
    private void updateToggleMap()
    {
        // save current map
        HashMap<String, Boolean> tempMap = new HashMap<String, Boolean>(m_toggleMap);
        // clear current
        m_toggleMap.clear();
        // loop over all messages
        for (IMessageIf it : getIds())
        {
            String messageId = it.getObjectId();
            Boolean expanded = tempMap.get(messageId);
            if (expanded != null)
            {
                m_toggleMap.put(messageId, expanded);
            } else
            {
                m_toggleMap.put(messageId, false);
            }
        }
    }

	/**
	 * Used to identify which of the tasks in this dialog, if any, a specific task is. General types
	 * does not provide sufficient information to determine that
	 * @param task
	 * @return
	 */
	public static TaskSubType getSubType(ITaskIf task)
	{
		TaskType taskType = task.getType();
		String taskText = task.getTaskText();
		switch(taskType)
		{
		case TRANSPORT:
			if(taskText.equals(DiskoEnumFactory.getText("TaskSubType.SEND_TRANSPORT.text",null)))
			{
				return TaskSubType.SEND_TRANSPORT;
			}
		case RESOURCE:
			if(taskText.equals(DiskoEnumFactory.getText("TaskSubType.GET_TEAM.text",null)))
			{
				return TaskSubType.GET_TEAM;
			}
			if(taskText.equals(DiskoEnumFactory.getText("TaskSubType.CREATE_ASSIGNMENT.text",null)))
			{
				return TaskSubType.CREATE_ASSIGNMENT;
			}
		case INTELLIGENCE:
			if(taskText.equals(DiskoEnumFactory.getText("TaskSubType.CONFIRM_INTELLIGENCE.text",null)))
			{
				return TaskSubType.CONFIRM_INTELLIGENCE;
			}
			try
			{
				if(taskText.split(":")[0].equals(DiskoEnumFactory.getText("TaskSubType.FINDING.text",null).split(":")[0]))
				{
					return TaskSubType.FINDING;
				}
			}catch(Exception e){}
		case GENERAL:
			if(taskText.equals(DiskoEnumFactory.getText("TaskSubType.GENERAL.text",null)))
			{
				return TaskSubType.GENERAL;
			}
		}
		// not identified
		return null;
	}


}