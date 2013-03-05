package org.redcross.sar.wp.messageLog;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageIf.MessageStatus;

import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

/**
 * Custom cell renderer for message log table
 *
 * @author vinjar, thomasl, kennetgu
 */
public class MessageStringConverter extends TableStringConverter
{
	private static final long serialVersionUID = 1L;

	@Override
	public String toString(TableModel data, int row, int col)
	{

		// get model
		MessageTableModel model = (MessageTableModel)data;

		// get value
		Object value = model.getValueAt(row, col);

		if(value!=null) {
			// translate
			switch(col)
			{
				case 0: // NUMBER
				{

					// cast to IMessageIf
					IMessageIf message = (IMessageIf)value;

					// update text
					return Integer.toString(message.getNumber());

				}
				case 2:	// FROM
				case 3: // TO
				{
		            if(value instanceof ICommunicatorIf) {

						// cast to ICommunicatorIf
						ICommunicatorIf c = (ICommunicatorIf)value;
						// convert to text
						return c.getCommunicatorNumberPrefix() + " " + c.getCommunicatorNumber();

		            }
		            else if(value instanceof Integer[]) {
		            	// cast to integer array
		            	Integer[] values = (Integer[])value;
						// convert to broadcast status
		            	return String.format(model.getBundleText("BroadcastLabel.text"),values[0],values[1]);
		            }
		            break;

				}
				case 4: // LINES
				case 5: // TASKS
				{

					// cast to IMessageIf
					IMessageIf message = model.getMessage(row);

					// get expanded/collapsed state
			        Boolean expanded = model.isMessageExpanded(message.getObjectId());
			        if (expanded == null)
			        {
			            expanded = false;
			        }

					// Message lines
		        	StringBuilder messageString = new StringBuilder();
		        	String[] messageLines = (String[]) value;

		        	if(expanded)
		        	{
		        		// Show lines in expanded mode
		        		for (int i = 0; i < messageLines.length; i++)
		                {
		                    messageString.append(messageLines[i]);
		                    if(i+1<messageLines.length) messageString.append("<br>");
		                }
		        		return "<html>"+messageString.toString()+"</html>";
		        	}
		        	else
		        	{
		        		// Show lines in compressed mode
		        		for (int i = 0; i < messageLines.length; i++)
		                {
		                    messageString.append(messageLines[i]);
		                    if(i+1<messageLines.length) messageString.append(". ");
		                }
		        		return messageString.toString();
		        	}
				}
				case 6:
				{
					// cast to status
					MessageStatus status = (MessageStatus)value;

					// Message status
					return DiskoEnumFactory.getText(status);

				}
			}
			return value.toString();
		}
		return "";
	}

}
