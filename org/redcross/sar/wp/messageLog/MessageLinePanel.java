package org.redcross.sar.wp.messageLog;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineListIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.util.mso.Selector;

/**
 * Panel displaying message lines in the currently selected message
 *
 * @author thomasl
 */
public class MessageLinePanel extends JPanel implements IEditorIf
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
		// forward
		super();
		
		// prepare
		m_wpMessageLog = wp;

		// add empty border
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));		
		
		// set border layout
		setLayout(new BorderLayout());
		
		// create table
		m_messageTableModel = new MessageLineTableModel(m_wpMessageLog);
		m_messageListTable = new JTable(m_messageTableModel);
		m_messageListTable.setBorder(null);
		m_messageListTable.setDefaultRenderer(IMessageLineIf.class, new MessageLineTableRenderer());
		m_messageListTable.setTableHeader(null);
		m_messageListTable.setRowHeight(32);
		m_textScrollPane = new JScrollPane(m_messageListTable);
		m_textScrollPane.setBorder(new DiskoBorder());
		m_messageListTable.setFillsViewportHeight(true);
		m_messageListTable.setColumnSelectionAllowed(false);
		m_messageListTable.setRowSelectionAllowed(true);
		m_messageListTable.setShowVerticalLines(false);
		m_messageListTable.setShowHorizontalLines(true);

		m_messageListTable.addMouseListener(new MessageLineMouseAdapter(m_messageTableModel));

		add(m_textScrollPane, BorderLayout.CENTER);
	}

	/**
	 * Updates message line list model with message lines in message
	 */
	public void setMessage(IMessageIf message)
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
	public void showEditor()
	{
		this.setVisible(true);
	}

	/**
	 *
	 */
	public void hideEditor()
	{
		this.setVisible(false);
	}

	/**
	 *
	 */
	public void reset()
	{
		m_messageTableModel.clearMessageLines();
	}
	
	public MessageLineType getSelectedMessageLineType() {
		int row = m_messageListTable.getSelectedRow();
		int col = m_messageListTable.getSelectedColumn();
		if(row!=-1 && col!=-1) {
			IMessageLineIf line = (IMessageLineIf)m_messageTableModel.getValueAt(row, col);
			return line!=null ? line.getLineType() : null;
		}
		return null;
	}
}
