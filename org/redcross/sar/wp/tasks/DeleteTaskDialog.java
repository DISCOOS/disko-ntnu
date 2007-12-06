package org.redcross.sar.wp.tasks;

import org.redcross.sar.gui.DiskoButtonFactory;
import org.redcross.sar.gui.DiskoButtonFactory.ButtonType;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITaskIf.TaskStatus;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for deleting a task
 * @author thomasl
 */
public class DeleteTaskDialog extends DiskoDialog
{
	private final static long serialVersionUID = 1L;

	protected IDiskoWpTasks m_wpTasks;

	protected ITaskIf m_currentTask;

	protected JPanel m_contentsPanel;
	protected JTextField m_taskTextField;
	protected JTextArea m_descriptionTextArea;
	protected JButton m_cancelButton;
	protected JButton m_deleteButton;

	public DeleteTaskDialog(IDiskoWpTasks wp)
	{
		super(wp.getApplication().getFrame());
		m_wpTasks = wp;

		initialize();
	}

	/**
	 * Initialize GUI
	 */
	private void initialize()
	{
		m_contentsPanel = new JPanel(new GridBagLayout());
		m_contentsPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.gridx = 0;
		gbc.gridy = 0;

		JLabel headerLabel = new JLabel(m_wpTasks.getText("DeleteTask.text"));
		gbc.gridwidth = 2;
		m_contentsPanel.add(headerLabel, gbc);

		JLabel taskLabel = new JLabel(m_wpTasks.getText("Task.text"));
		gbc.gridwidth = 1;
		gbc.gridy++;
		m_contentsPanel.add(taskLabel, gbc);

		m_taskTextField = new JTextField();
		m_taskTextField.setEditable(false);
		m_taskTextField.setColumns(30);
		m_taskTextField.setBackground(m_contentsPanel.getBackground());
		gbc.gridx++;
		m_contentsPanel.add(m_taskTextField, gbc);

		JLabel descriptionLabel = new JLabel(m_wpTasks.getText("TaskDescription.text"));
		gbc.gridx--;
		gbc.gridy++;
		m_contentsPanel.add(descriptionLabel, gbc);

		m_descriptionTextArea = new JTextArea();
		m_descriptionTextArea.setEditable(false);
		m_descriptionTextArea.setRows(5);
		m_descriptionTextArea.setColumns(30);
		m_descriptionTextArea.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		m_descriptionTextArea.setBackground(m_contentsPanel.getBackground());
		gbc.gridx++;
		m_contentsPanel.add(m_descriptionTextArea, gbc);

		JPanel buttonRowPanel = new JPanel();
		m_deleteButton = DiskoButtonFactory.createNormalButton(ButtonType.DeleteButton);
		m_deleteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				delete();
			}
		});
		buttonRowPanel.add(m_deleteButton);
		m_cancelButton = DiskoButtonFactory.createNormalButton(ButtonType.CancelButton);
		m_cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				cancelDelete();
			}
		});
		buttonRowPanel.add(m_cancelButton);
		gbc.gridwidth = 2;
		gbc.gridx--;
		gbc.gridy++;
		m_contentsPanel.add(buttonRowPanel, gbc);

		this.add(m_contentsPanel);
		this.pack();
	}

	/**
	 * Cancel delete action
	 */
	private void cancelDelete()
	{
		m_currentTask = null;
		this.setVisible(false);
	}

	/**
	 * Finish delete action, delete selected task
	 */
	private void delete()
	{
		if(m_currentTask != null)
		{
			m_currentTask.setStatus(TaskStatus.DELETED);

			m_currentTask = null;

			m_wpTasks.getMsoModel().commit();
		}
		this.setVisible(false);
	}

	public void setTask(ITaskIf task)
	{
		m_currentTask = task;
		updateFieldContents();
	}

	private void updateFieldContents()
	{
		if(m_currentTask != null)
		{
			m_taskTextField.setText(m_currentTask.getTaskText());
			m_descriptionTextArea.setText(m_currentTask.getDescription());
		}
	}
}
