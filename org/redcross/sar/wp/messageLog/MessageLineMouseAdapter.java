package org.redcross.sar.wp.messageLog;

import java.awt.event.MouseEvent;

import javax.swing.JTable;

import org.redcross.sar.gui.event.DiskoMouseAdapter;
import org.redcross.sar.gui.event.DiskoMouseAdapter.DiskoMouseDelayListener;
import org.redcross.sar.mso.data.IMessageLineIf;

/**
 * Listener that shows double clicked message line in edit mode
 * 
 * @author kenneth
 */
public class MessageLineMouseAdapter extends DiskoMouseAdapter implements DiskoMouseDelayListener
{
	protected LineListTableModel m_tableModel;
	
	/**
	 * @param listTableModel Line table model {@link LineListTableModel}
	 */
	public MessageLineMouseAdapter(LineListTableModel listTableModel)
	{
		// forward
		super();
		// prepare
		m_tableModel = listTableModel;
		// add listers
		addDiskoMouseDelayListener(this);
	}
	

	/**
	 * Display selected message line in edit mode
	 */

	@Override
	public void mouseClicked(MouseEvent e) {
		// forward
		super.mouseClicked(e);
		// double click?
		if(e.getClickCount()==2) {
			// forward
			showLine(e);
		}		
	}

	public void mouseDownExpired(MouseEvent e) {
		// forward
		showLine(e);		
	}
	
	private void showLine(MouseEvent e) {
		// is JTable?
		if(e.getSource() instanceof JTable) {
			// cast to JTable
			JTable table = (JTable)e.getSource();
			// get selected index
			int rowIndex = table.getSelectedRow();
			// is row selected?
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
}
