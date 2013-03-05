package org.redcross.sar.wp.tasks;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;

import java.awt.Color;
import java.util.Calendar;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.renderer.DiskoTableCellRenderer;
import org.redcross.sar.mso.data.ITaskIf.TaskStatus;
import org.redcross.sar.util.mso.DTG;


/**
 * Renders task table
 * - Due time reached and status not finished: red
 * - Untreated tasks in bold
 *
 * @author thomasl
 */
public class TaskTableRenderer extends DiskoTableCellRenderer
{
	private static final long serialVersionUID = 1L;

	public TaskTableRenderer()
	{
		JTextArea area = new JTextArea();
		this.setFont(area.getFont());
		this.setOpaque(true);
	}

	public JLabel getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col)
	{
		// initialize
		String text = "";
		Color fg = isSelected ? table.getSelectionForeground() : table.getForeground();
		Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();

		// get information about task expiration
		Calendar dueTime = (Calendar)table.getModel().getValueAt(m_rowInModel, 4);
		TaskStatus status = (TaskStatus)table.getModel().getValueAt(m_rowInModel, 5);

		// get model info
		super.initialize(table, row, col);

		// translate
		if(value!=null) {
			switch(m_colInModel)
			{
			case 0:
				text = value.toString();
				break;
			case 1:
				text = DiskoEnumFactory.getText((Enum<?>)value);
				break;
			case 2:
				text = value.toString();
				break;
			case 3:
				text = value.toString();
				break;
			case 4:
				text = DTG.CalToDTG((Calendar)value);
				break;
			case 5:
				text = DiskoEnumFactory.getText(status);
				if(TaskStatus.UNPROCESSED.equals(status))
				{
					text = "<html><b>" + text + "</b></html>";
				}
				break;
			}
		}

		// mark as expired?
		if(!TaskStatus.FINISHED.equals(status))
		{
			// expired?
			if(dueTime != null && Calendar.getInstance().after(dueTime)) {
				bg = Color.PINK;
			}

		}

		// get renderer
		JLabel renderer = super.prepare(table, text, isSelected, hasFocus, row, col, false, true);

		// update colors
		renderer.setForeground(fg);
		renderer.setBackground(bg);

		// finished
		return renderer;

	}
}
