package org.redcross.sar.wp.tasks;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.mso.data.ITaskIf;

/**
 * Listen for selections made in task panel, inform work process of which is the current task to
 * manipulate
 * 
 * @author thomasl
 */
public class TaskSelectionListener implements ListSelectionListener
{
	protected IDiskoWpTasks m_wpTasks;
	protected JTable m_table;
	
	public TaskSelectionListener(IDiskoWpTasks wp, JTable table)
	{
		m_wpTasks = wp;
		m_table = table;
	}
	public void valueChanged(ListSelectionEvent lse)
	{
		// Get selected task 
		ListSelectionModel lsm = (ListSelectionModel)lse.getSource();
		
		int index = lsm.getMinSelectionIndex();
		if(index < 0)
		{
			return;
		}
		
		int taskNr = (Integer)m_table.getValueAt(index, 0);
		
		// Inform work process of selection
		TaskTableModel model = (TaskTableModel)m_table.getModel();
		ITaskIf task = model.getTask(taskNr);
		
		m_wpTasks.setCurrentTask(task);
	}
}
