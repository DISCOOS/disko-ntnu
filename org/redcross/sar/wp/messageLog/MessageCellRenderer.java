package org.redcross.sar.wp.messageLog;

import java.awt.Color;

import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.renderer.DiskoTableCellRenderer;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageIf.MessageStatus;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

/**
 * Custom cell renderer for message log table
 *
 * @author vinjar, thomasl, kennetgu
 */
public class MessageCellRenderer extends DiskoTableCellRenderer
{
	private static final long serialVersionUID = 1L;

	private final Icon m_empty;
	private final Icon m_expanded;
	private final Icon m_collapsed;

    public MessageCellRenderer()
	{
    	setOpaque(true);
    	m_empty = DiskoIconFactory.getIcon("GENERAL.EMPTY", "24x24");
    	m_expanded = DiskoIconFactory.getIcon("GENERAL.EXPAND", "24x24");
    	m_collapsed = DiskoIconFactory.getIcon("GENERAL.COLLAPSE", "24x24");
    	setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
    	//setFont(new Font("Courier", Font.PLAIN, 10));
    	//setFont(new Font("Lucida Bright", Font.PLAIN, 12));
    	//setFont(new Font("Lucida Sans Typewriter", Font.PLAIN, 12));
	}

    /**
     * Get cell component. Message lines with status postponed have pink background
     */
	public JLabel getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
    {

		// initialize
		Icon icon = null;
		String text = "";
		Color f = table.getForeground();
		Color b = table.getBackground();

		// prepare
		JLabel renderer = super.prepare(table, null, isSelected, hasFocus, row, col, true, false);

		// set
		setVerticalAlignment(SwingConstants.CENTER);

		// has row?
		if(m_rowInModel!=-1) {

			// get model
			MessageTableModel model = (MessageTableModel)table.getModel();

			// has value?
			if(value!=null)
			{
		        // translate
				switch(m_colInModel)
				{
				case 0: // NUMBER
				{

					// cast to IMessageIf
					IMessageIf message = (IMessageIf)value;

					// has more than one message line?
					if(model.numRows(m_rowInModel)>1) {
						// get expanded/collapsed state
				        Boolean expanded = model.isMessageExpanded(message.getObjectId());
				        if (expanded == null)
				        {
				            expanded = false;
				        }
				        icon = (expanded ? m_collapsed : m_expanded);
					}
					else {
						icon = m_empty;
					}

					// update text
					text = Integer.toString(message.getNumber());

		        	break;
				}
				case 2:	// FROM
				case 3: // TO
				{
		            if(value instanceof ICommunicatorIf) {

						// cast to ICommunicatorIf
						ICommunicatorIf c = (ICommunicatorIf)value;
						// convert to text
						text = c.getCommunicatorNumberPrefix() + " " + c.getCommunicatorNumber();

		            }
		            else if(value instanceof Integer[]) {
		            	// cast to integer array
		            	Integer[] values = (Integer[])value;
						// convert to broadcast status
		            	text = String.format(model.getBundleText("BroadcastLabel.text"),values[0],values[1]);
		            }
		            break;

				}
				case 4: // LINES
				case 5: // TASKS
				{

					// cast to IMessageIf
					IMessageIf message = model.getMessage(m_rowInModel);

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
		        		text = "<html>"+messageString.toString()+"</html>";
		        	}
		        	else
		        	{
		        		// Show lines in compressed mode
		        		for (int i = 0; i < messageLines.length; i++)
		                {
		                    messageString.append(messageLines[i]);
		                    if(i+1<messageLines.length) messageString.append(". ");
		                }
		        		text = messageString.toString();
		        	}

					break;
				}
				case 6:
				{

					// cast to status
					MessageStatus status = (MessageStatus)value;
					// Message status
					text = DiskoEnumFactory.getText(status);
					break;

				}
				default:
					text = value.toString();
				}
		    }

			// update colors
	        if(model.isRowCurrentMessage(m_rowInModel))
	        {
	        	f = table.getSelectionForeground();
	        	b = m_colInModel==0 ? Color.LIGHT_GRAY : table.getSelectionBackground();

	        }
	        else {

	        	// get current status
	        	MessageStatus status = model.getMessageStatus(m_rowInModel);

	        	// update colors
	        	if(MessageStatus.POSTPONED.equals(status))
	        	{
	            	b = Color.ORANGE;
	        	}
	        	else if(MessageStatus.UNCONFIRMED.equals(status))
	        	{
	            	b = Color.YELLOW;
	        	}
	        	f = table.getForeground();

	        }
		}

        // update
        renderer.setText(text);
        renderer.setIcon(icon);
        renderer.setForeground(f);
        renderer.setBackground(b);
        renderer.setBorder(new DiskoBorder(1,5,1,5,b));

        // finished
        return renderer;

    }

}
