package org.redcross.sar.wp.messageLog;

import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;

import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * Data model for message lines, used to retrieve list depending message line type (assign, start, complete)
 * @author thomasl
 */
public class MessageLineListModel extends AbstractListModel
{
	private static final long serialVersionUID = 1L;

	protected IDiskoWpMessageLog m_wpMessageLog = null;
	protected MessageLineType m_lineType = null;
	protected List<IMessageLineIf> m_messageLines = null;

	/**
	 * @param wp Message log work process
	 */
	public MessageLineListModel(IDiskoWpMessageLog wp)
	{
		m_wpMessageLog = wp;

		m_messageLines = new LinkedList<IMessageLineIf>();
	}

	/**
	 * Sets the assignment line type and updates assignment list
	 * @param lineType The line type
	 */
	public void setMessageLineType(MessageLineType lineType)
	{
		m_lineType = lineType;
		updateList();
	}

	/**
	 *  Rebuild message line list
	 */
	public void updateList()
	{
		m_messageLines.clear();
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		if(message != null)
		{
			Selector<IMessageLineIf> lineSelector = new Selector<IMessageLineIf>()
			{
				public boolean select(IMessageLineIf anObject)
				{
					return (anObject!=null && anObject.getLineType().equals(m_lineType));
				}
			};
			m_messageLines.addAll(message.getMessageLines().selectItems(lineSelector));
			fireContentsChanged(this, 0, m_messageLines.size()-1);
		}
	}

	/**
	 * Returns list element
	 * @param index Index of element to retrieve
	 */
	public Object getElementAt(int index)
	{
		return m_messageLines.size()-1 < index ? null : m_messageLines.get(index);
	}

	/**
	 * Return length of list
	 */
	public int getSize()
	{
		return m_messageLines.size();
	}

}
