package org.redcross.sar.wp.messageLog;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineListIf;
import org.redcross.sar.util.mso.Selector;

/**
 * Panel displaying message lines in the currently selected message
 *
 * @author thomasl
 */
public class MessageLinePanel extends JPanel implements IEditMessageComponentIf
{
	private final static long serialVersionUID = 1L;

	private JTable m_messageListTable;
	private MessageLineTableModel m_messageTableModel;
	private JScrollPane m_textScrollPane;
	private IDiskoWpMessageLog m_wpMessageLog;

	private Selector<IMessageLineIf> m_messageLineSelector = new Selector<IMessageLineIf>()
	{
		public boolean select(IMessageLineIf anObject)
		{
			return true;
		}
	};

	/**
	 * @param wp Message log work process
	 */
	public MessageLinePanel(IDiskoWpMessageLog wp)
	{
		m_wpMessageLog = wp;

		setLayout(new BorderLayout());

		m_messageTableModel = new MessageLineTableModel(m_wpMessageLog);
		m_messageListTable = new JTable(m_messageTableModel);
		m_messageListTable.setDefaultRenderer(IMessageLineIf.class, new MessageLineTableRenderer());
		m_messageListTable.setTableHeader(null);
		m_messageListTable.setRowHeight(32);
		m_textScrollPane = new JScrollPane(m_messageListTable);
		m_messageListTable.setFillsViewportHeight(true);
		m_messageListTable.setColumnSelectionAllowed(false);
		m_messageListTable.setRowSelectionAllowed(true);
		m_messageListTable.setShowVerticalLines(false);
		m_messageListTable.setShowHorizontalLines(true);

		m_messageListTable.addMouseListener(new MessageLineMouseAdapter(m_messageTableModel));

		this.add(m_textScrollPane, BorderLayout.CENTER);
	}

	/**
	 * Updates message line list model with message lines in message
	 */
	public void newMessageSelected(IMessageIf message)
	{
		m_messageTableModel.clearMessageLines();
		IMessageLineListIf messageLines = message.getMessageLines();

		if(messageLines == null || messageLines.size() == 0)
		{
			return;
		}

		for(IMessageLineIf messageLine : messageLines.selectItems(m_messageLineSelector, IMessageLineIf.LINE_NUMBER_COMPARATOR))
		{
			m_messageTableModel.addMessageLine(messageLine);
		}
		m_messageTableModel.fireTableDataChanged();
	}

	/**
	 *
	 */
	public void showComponent()
	{
		this.setVisible(true);
	}

	/**
	 *
	 */
	public void hideComponent()
	{
		this.setVisible(false);
	}

	/**
	 *
	 */
	public void clearContents()
	{
		m_messageTableModel.clearMessageLines();
	}
}
