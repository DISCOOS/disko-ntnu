package org.redcross.sar.wp.messageLog;

import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageIf.MessageStatus;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Custom cell renderer for message log table
 *
 * @author vinjar/thomasl
 */
public class MessageTableRenderer extends JTextArea implements TableCellRenderer
{
	private static final long serialVersionUID = 1L;

    public MessageTableRenderer()
	{
    	setOpaque(true);
    	setLineWrap(true);
        setWrapStyleWord(true);
	}

    /**
     * Get cell component. Message lines with status postponed have pink background
     */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
		LogTableModel model = (LogTableModel)table.getModel();
		IMessageIf message = (IMessageIf)model.getValueAt(table.convertRowIndexToModel(row), 7);

		if(message!=null) {
			String messageId = message.getObjectId();
	        Boolean expanded = model.isMessageExpanded(messageId);
	        if (expanded == null)
	        {
	            expanded = false;
	        }
	
	        // Set text
			switch(column)
			{
			case 0:
				// Number cell
	        	StringBuilder string = new StringBuilder(Integer.toString(message.getNumber()));
	        	if(model.numRows(row) > 1)
	        	{
	        		if(expanded)
	            	{
	            		string.append(" -");
	            	}
	            	else
	            	{
	            		string.append(" +");
	            	}
	        	}
	        	setText(string.toString());
	        	break;
			case 4:
			case 5:
				// Message lines
	        	StringBuilder messageString = new StringBuilder();
	        	String[] messageLines = (String[]) value;
	
	        	if(expanded)
	        	{
	        		// Show lines in expanded mode
	        		for (int i = 0; i < messageLines.length; i++)
	                {
	                    messageString.append(messageLines[i]);
	                    if(i+1<messageLines.length) messageString.append("\n");
	                }
	        	}
	        	else
	        	{
	        		// Show lines in compressed mode
	        		for (int i = 0; i < messageLines.length; i++)
	                {
	                    messageString.append(messageLines[i]);
	                    if(i+1<messageLines.length) messageString.append(". ");
	                }
	        	}
	
	            setText(messageString.toString());
				break;
			case 6:
				// Message status
				setText(message.getStatusText());
				break;
			default:
				if(value instanceof String)
				{
					setText((String)value);
				}
				break;
			}
	
	        // Colors
	        IMessageIf selectedMessage = MessageLogBottomPanel.getCurrentMessage(false);
	        if(selectedMessage != null && selectedMessage.getObjectId().equals(messageId))
	        {
	        	setForeground(table.getSelectionForeground());
	        	setBackground(table.getSelectionBackground());
	        }
	        else
	        {
	        	setForeground(table.getForeground());
	
	        	if(((MessageStatus)model.getValueAt(table.convertRowIndexToModel(row), 6)) == MessageStatus.POSTPONED)
	        	{
	        		setBackground(Color.ORANGE);
	        	}
	        	else
	        	{
	        		setBackground(table.getBackground());
	        	}
	        }
		}

        return this;
    }

}
