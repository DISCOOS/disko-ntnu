package org.redcross.sar.wp.messageLog;

import org.redcross.sar.mso.data.IMessageIf;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This class listens for row selections in {@link MessageLogPanel#m_logTable}
 * and updates the top panel GUI accordingly
 *
 * @author Thomas
 */
public class MessageRowSelectionListener implements ListSelectionListener
{
	protected MessageLogBottomPanel m_messagePanel;
	protected JTable m_messageTable;
	protected MessageTableModel m_tableModel;
	protected boolean m_msoUpdate = false;

	/**
	 * Expand/collapse rows based on selection in message log table
	 */
	public void valueChanged(ListSelectionEvent event)
	{
		ListSelectionModel lsm = (ListSelectionModel)event.getSource();
		if(lsm.getValueIsAdjusting())
		{
			return;
		}

//		if(lsm.isSelectionEmpty())
//			return;

		// Get selected row index
		int rowIndex = lsm.getMinSelectionIndex();

		// Clear selection
		lsm.clearSelection();

		// Check for empty selection
		if(rowIndex == -1)
		{
			return;
		}

        IMessageIf message = (IMessageIf)m_tableModel.getValueAt(rowIndex,7);

        // Get message Object ID
		String messageId = message.getObjectId();

		// Toggle expanded
		Boolean expanded = m_tableModel.isMessageExpanded(messageId);

        expanded = expanded == null || !expanded;

        m_tableModel.setMessageExpanded(messageId, expanded);

		// Set row height
		m_tableModel.updateRowHeights();

		// Update top message panel
		m_messagePanel.newMessageSelected(message);
		MessageLogBottomPanel.showListPanel();
	}

	/**
	 * Set the table
	 * @param table
	 */
	public void setTable(JTable table)
	{
		m_messageTable = table;
	}

	/**
	 * Set the log panel to be updated
	 * @param panel
	 */
	public void setPanel(MessageLogBottomPanel panel)
	{
		m_messagePanel = panel;
	}

	/**
	 * Set the table model
	 * @param model
	 */
	public void setModel(MessageTableModel model)
	{
		m_tableModel = model;
	}
}
