package org.redcross.sar.wp.messageLog;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Selection listener used when modifying assignment lines
 * @author thomasl
 */
public class AssignmentLineSelectionListener implements ListSelectionListener
{
	AbstractAssignmentPanel m_panel = null;
	JList m_list = null;
	
	/**
	 * @param list List generating the event
	 * @param panel Panel displaying assignment fields
	 */
	public AssignmentLineSelectionListener(JList list, AbstractAssignmentPanel panel)
	{
		m_list = list;
		m_panel = panel;
	}
	
	/**
	 * Displays the selected message line in edit mode
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		int selectedLine = m_list.getSelectedIndex();
		if(selectedLine < 0)
		{
			return;
		}
		
		m_list.clearSelection();
		
		// Show selected assignment in edit mode
		m_panel.showEditAssignment(selectedLine);
	}
}
