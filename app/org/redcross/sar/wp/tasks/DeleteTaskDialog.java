package org.redcross.sar.wp.tasks;

import org.apache.log4j.Logger;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.field.TextAreaField;
import org.redcross.sar.gui.field.TextField;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITaskIf.TaskStatus;
import org.redcross.sar.util.except.TransactionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for deleting a task
 * @author thomasl
 */
public class DeleteTaskDialog extends DefaultDialog
{
	private final static long serialVersionUID = 1L;
	
    private static final Logger m_logger = Logger.getLogger(DeleteTaskDialog.class);
	

	protected DefaultPanel m_contentsPanel;
	protected TextField m_taskAttr;
	protected TextAreaField m_descAttr;
	protected JPanel m_attributesPanel;
	protected JButton m_deleteButton;
	
	protected IDiskoWpTasks m_wp;

	public DeleteTaskDialog(IDiskoWpTasks wp)
	{
		// forward
		super(wp.getApplication().getFrame());
		
		// prepare
		m_wp = wp;

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
			this.setPreferredSize(new Dimension(400,200));
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
	private DefaultPanel getContentPanel() {
		if (m_contentsPanel == null) {
			try {
				// create content panel
				m_contentsPanel = new DefaultPanel(m_wp.getBundleText("DeleteTask.text"),false,true) {

					private static final long serialVersionUID = 1L;

					@Override
					protected boolean beforeCancel() {
						setMsoObject(null);
						return true;
					}
					
					@Override
					public void setMsoObject(IMsoObjectIf msoObj) {
						// consume changes
						setChangeable(false);
						
						// initialize
						ITaskIf task = null;
						// dispatch type
						if (msoObj instanceof ITaskIf) {
							task = (ITaskIf)msoObj;
						}						
						// update
						m_msoObject = task;

						// resume changes
						setChangeable(true);
						
						// update
						setDirty(false,false);
						update();
												
					}	
					
				};
				m_contentsPanel.insertButton("finish", getDeleteButton(), "delete");
				m_contentsPanel.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						if("delete".equalsIgnoreCase(cmd))
							delete();
					}
					
				});
				// add components
				m_contentsPanel.setContainer(getAttributesPanel());
				m_contentsPanel.setScrollBarPolicies(
						BasePanel.VERTICAL_SCROLLBAR_NEVER, 
						BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_contentsPanel;
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
	 * This method initializes DeleteButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getDeleteButton() {
		if (m_deleteButton == null) {
			try {
				m_deleteButton = DiskoButtonFactory.createButton(
						"GENERAL.DELETE",
						getContentPanel().getButtonSize());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_deleteButton;
	}
	
	/**
	 * This method initializes TaskAttr
	 *
	 * @return {@link TextField}
	 */
	private TextField getTaskAttr() {
		if (m_taskAttr == null) {
			try {
				m_taskAttr = new TextField("Task",
						m_wp.getBundleText("Task.text"),
						false,100,25,null);
				m_taskAttr.setPreferredSize(new Dimension(300,30));
				m_taskAttr.getEditComponent().setBorder(BorderFactory.createLineBorder(Color.lightGray));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_taskAttr;
	}	
	
	/**
	 * This method initializes DescAttr
	 *
	 * @return {@link TextAreaField}
	 */
	private TextAreaField getDescAttr() {
		if (m_descAttr == null) {
			try {
				m_descAttr = new TextAreaField("Description",
						m_wp.getBundleText("TaskDescription.text"),
						false,100,100,null);
				m_descAttr.setPreferredSize(new Dimension(300,100));
				m_descAttr.getEditComponent().setRows(5);
				m_descAttr.getEditComponent().setColumns(30);
				m_descAttr.getEditComponent().setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_descAttr;
	}
		
	/**
	 * Finish delete action, delete selected task
	 */
	private void delete() {
		ITaskIf task = (ITaskIf)getMsoObject();
		if(task != null)
		{
			task.setStatus(TaskStatus.DELETED);
            try {
    			m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(task));
    		} catch (TransactionException ex) {
    			m_logger.error("Failed to commit test data changes",ex);
    		}            
		}
		this.setVisible(false);
	}

	public void setTask(ITaskIf task)
	{
		setMsoObject(task);
		updateFieldContents();
	}

	private void updateFieldContents()
	{
		ITaskIf task = (ITaskIf)getMsoObject();
		if(task != null)
		{
			m_taskAttr.setValue(task.getTaskText());
			m_descAttr.setValue(task.getDescription());
		}
	}
}
