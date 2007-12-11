package org.redcross.sar.wp.tasks;

import org.redcross.sar.mso.data.ITaskIf.TaskPriority;
import org.redcross.sar.mso.data.ITaskIf.TaskStatus;
import org.redcross.sar.mso.data.TaskImpl;
import org.redcross.sar.util.mso.DTG;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

import java.awt.Color;
import java.awt.Component;
import java.util.Calendar;

/**
 * Renders task table
 * - Due time reached and status not finished: red
 * - Untreated tasks in bold
 *
 * @author thomasl
 */
public class TaskTableRenderer extends JLabel implements TableCellRenderer
{
	private static final long serialVersionUID = 1L;

	public TaskTableRenderer()
	{
		JTextArea area = new JTextArea();
		this.setFont(area.getFont());
		this.setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		TaskStatus taskStatus = (TaskStatus)table.getValueAt(row, 5);
		Calendar dueTime = (Calendar)table.getValueAt(row, 4);

		String cellText = null;
		switch(column)
		{
		case 0:
			cellText = String.valueOf(table.getValueAt(row, 0));
			break;
		case 1:
			TaskPriority priority = (TaskPriority)table.getValueAt(row, 1);
			if(priority != null)
			{
				cellText = TaskImpl.getEnumText(priority);
			}
			break;
		case 2:
			cellText = (String)table.getValueAt(row, 2);
			break;
		case 3:
			cellText = (String)table.getValueAt(row, column);
			break;
		case 4:
			if(dueTime != null)
			{
				cellText = DTG.CalToDTG(dueTime);
			}
			break;
		case 5:
			if(taskStatus != null)
			{
				cellText = TaskImpl.getEnumText(taskStatus);
			}
			break;
		}

		// Set background to pink for expired tasks
		if(isSelected)
		{
			this.setBackground(table.getSelectionBackground());
		}
		else
		{

			if(taskStatus != TaskStatus.FINISHED && dueTime != null &&
					dueTime.before(Calendar.getInstance()))
			{
				this.setBackground(Color.pink);
			}
			else
			{
				this.setBackground(table.getBackground());
			}
		}

		// Set bold text for unprocessed tasks
		if(value != null)
		{
			if(isSelected)
			{
				this.setForeground(table.getSelectionForeground());
			}
			else
			{
				this.setForeground(table.getForeground());
			}

			if(taskStatus == TaskStatus.UNPROCESSED)
			{
				this.setText("<html><b>" + cellText + "</b></html>");
			}
			else
			{
				this.setText(cellText);
			}
		}
		else
		{
			this.setText("");
		}

		return this;
	}
}
