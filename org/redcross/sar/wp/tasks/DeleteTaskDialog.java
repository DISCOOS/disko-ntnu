package org.redcross.sar.wp.tasks;

import org.redcross.sar.gui.attribute.TextAreaAttribute;
import org.redcross.sar.gui.attribute.TextFieldAttribute;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITaskIf.TaskStatus;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

	protected DiskoPanel m_contentsPanel;
	protected TextFieldAttribute m_taskAttr;
	protected TextAreaAttribute m_descAttr;
	protected JPanel m_attributesPanel;
	protected JButton m_cancelButton;
	protected JButton m_deleteButton;
	protected JPanel m_actionsPanel;
	
	public DeleteTaskDialog(IDiskoWpTasks wp)
	{
		// forward
		super(wp.getApplication().getFrame());
		
		// prepare
		m_wpTasks = wp;

		// initialize GUI
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		try {
			this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(900, 300));
            this.pack();
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initializes contentPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private DiskoPanel getContentPanel() {
		if (m_contentsPanel == null) {
			try {
				// create content panel
				m_contentsPanel = new DiskoPanel();
				// prepare
				m_contentsPanel.setCaptionText(m_wpTasks.getBundleText("DeleteTask.text"));
				// add components
				m_contentsPanel.addBodyComponent(getAttributesPanel(),BorderLayout.CENTER);
				m_contentsPanel.addBodyComponent(getActionsPanel(),BorderLayout.SOUTH);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_contentsPanel;
	}
	
	/**
	 * This method initializes ActionsPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getActionsPanel() {
		if (m_actionsPanel == null) {
			try {
				// create panel
				m_actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
				// add component
				m_actionsPanel.add(getDeleteButton());
				m_actionsPanel.add(getCancelButton());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_actionsPanel;
	}	
	
	/**
	 * This method initializes getAttributesPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getAttributesPanel() {
		if (m_attributesPanel == null) {
			try {
				// create panel
				m_attributesPanel = new JPanel();
				m_attributesPanel.setLayout(new BoxLayout(m_attributesPanel,BoxLayout.Y_AXIS));
				m_attributesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				// add component
				m_attributesPanel.add(getTaskAttr());
				m_attributesPanel.add(Box.createVerticalStrut(5));
				m_attributesPanel.add(getDescAttr());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_attributesPanel;
	}	
	
	/**
	 * This method initializes CancelButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (m_cancelButton == null) {
			try {
				m_cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
				m_cancelButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						cancel();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_cancelButton;
	}
	
	/**
	 * This method initializes DeleteButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getDeleteButton() {
		if (m_cancelButton == null) {
			try {
				m_cancelButton = DiskoButtonFactory.createButton("GENERAL.DELETE",ButtonSize.NORMAL);
				m_cancelButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						delete();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_cancelButton;
	}
	
	/**
	 * This method initializes TaskAttr
	 *
	 * @return {@link TextFieldAttribute}
	 */
	private TextFieldAttribute getTaskAttr() {
		if (m_taskAttr == null) {
			try {
				m_taskAttr = new TextFieldAttribute("Task",
						m_wpTasks.getBundleText("Task.text"),
						100,null,false);
				m_taskAttr.getTextField().setBorder(BorderFactory.createLineBorder(Color.lightGray));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_taskAttr;
	}	
	
	/**
	 * This method initializes DescAttr
	 *
	 * @return {@link TextAreaAttribute}
	 */
	private TextAreaAttribute getDescAttr() {
		if (m_descAttr == null) {
			try {
				m_descAttr = new TextAreaAttribute("Description",
						m_wpTasks.getBundleText("TaskDescription.text"),
						100,null,false);
				m_descAttr.getTextArea().setRows(5);
				m_descAttr.getTextArea().setColumns(30);
				m_descAttr.getTextArea().setBorder(BorderFactory.createLineBorder(Color.lightGray));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_descAttr;
	}
		
	/**
	 * Cancel delete action
	 */
	private void cancel()
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
			m_taskAttr.setValue(m_currentTask.getTaskText());
			m_descAttr.setValue(m_currentTask.getDescription());
		}
	}
}
