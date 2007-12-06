package org.redcross.sar.wp.messageLog;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.mso.data.IMessageLineIf;

/**
 * Shows fields in edit mode based on which elements are selected in the message line list
 * @author thomasl
 */
public class MessageLineSelectionListener implements ListSelectionListener
{
	protected LineListTableModel m_tableModel;
	
	/**
	 * @param listTableModel Line table model {@link LineListTableModel}
	 */
	public MessageLineSelectionListener(LineListTableModel listTableModel)
	{
		m_tableModel = listTableModel;
	}
	
	/**
	 * Display selected message line in edit mode
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		
		// Get selected row index
		Integer rowIndex = new Integer(lsm.getMinSelectionIndex());
		if(rowIndex > -1)
		{
			IMessageLineIf line = m_tableModel.getMessageLine(rowIndex);
			
			switch(line.getLineType())
			{
			case TEXT:
				MessageLogBottomPanel.showTextPanel();
				break;
			case POSITION:
				MessageLogBottomPanel.showPositionPanel();
				break;
			case POI:
				MessageLogBottomPanel.showPOIPanel();
				break;
			case ASSIGNED:
				MessageLogBottomPanel.showAssignPanel();
				break;
			case STARTED:
				MessageLogBottomPanel.showStartPanel();
				break;
			case COMPLETE:
				MessageLogBottomPanel.showCompletePanel();
				break;
			}
		}
	}
}
