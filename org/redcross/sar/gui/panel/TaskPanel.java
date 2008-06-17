package org.redcross.sar.gui.panel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.attribute.ComboAttribute;
import org.redcross.sar.gui.attribute.DTGAttribute;
import org.redcross.sar.gui.attribute.TextAreaAttribute;
import org.redcross.sar.gui.attribute.TextFieldAttribute;
import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITaskIf.TaskPriority;
import org.redcross.sar.mso.data.ITaskIf.TaskStatus;
import org.redcross.sar.mso.data.ITaskIf.TaskType;
import org.redcross.sar.mso.data.TaskImpl;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.wp.messageLog.IDiskoWpMessageLog;
import org.redcross.sar.wp.tasks.TaskUtilities;

/**
 * Dialog for editing/creating tasks.
 * If dialog is initiated with task, it will edit this one, else it will create a new task
 *
 * @author thomasl
 */
public class TaskPanel extends DefaultPanel
{
	private static final long serialVersionUID = 1L;
	
    public final static String bundleName = 
    	"org.redcross.sar.gui.properties.TaskDialog";    
    private static final ResourceBundle m_resources = 
    	Internationalization.getBundle(TaskPanel.class);
	
	private ITaskIf m_currentTask = null;

	private TextFieldAttribute m_nameField = null;
	private ComboAttribute m_typeCombo = null;
	private DTGAttribute m_createdField = null;
	private ComboAttribute m_priorityCombo = null;
	private ComboAttribute m_dueCombo = null;
	private ComboAttribute m_responsibleCombo = null;
	private ComboAttribute m_alertCombo = null;
	private ComboAttribute m_statusCombo = null;
	private ComboAttribute m_progressCombo = null;
	private JButton m_useSourceButton = null;
	private TextAreaAttribute m_descriptionArea = null;
	private TextAreaAttribute m_sourceArea = null;
	private TextFieldAttribute m_objectField = null;
	private JPanel m_centerPanel = null;
	private JPanel m_westPanel = null;
	private JPanel m_eastPanel = null;
	private JPanel m_bottomPanel = null;

    public TaskPanel()
	{
		// forward
    	super("Oppgave");
    	
		// initialize gui
		initialize();
	}

    
    
    
	private void initialize()
	{

		// prepare
		this.setNotScrollBars();
		
		// get body component
		JPanel panel = (JPanel)getBodyComponent();
		BoxLayout bl = new BoxLayout(panel,BoxLayout.Y_AXIS);
		panel.setLayout(bl);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		panel.add(getNamePanel());
		panel.add(Box.createVerticalStrut(5));
		panel.add(getCenterPanel());
		panel.add(Box.createVerticalStrut(5));
		panel.add(getBottomPanel());
		panel.add(Box.createVerticalGlue());

	}
	
	private JPanel getNamePanel() {
		if(m_nameField==null) {
			m_nameField = new TextFieldAttribute("name",m_resources.getString("Task.text"),80,null,true);
			Utils.setFixedSize(m_nameField,490,25);
		}
		return m_nameField;
	}
	
	private JPanel getCenterPanel() {
		
		if(m_centerPanel == null) {
			m_centerPanel = new JPanel();
			Utils.setFixedSize(m_centerPanel,490,120);
			BoxLayout bl = new BoxLayout(m_centerPanel,BoxLayout.X_AXIS);
			m_centerPanel.setLayout(bl);
			m_centerPanel.add(getWestPanel());
			m_centerPanel.add(Box.createHorizontalStrut(5));
			m_centerPanel.add(getEastPanel());
		}
		return m_centerPanel;
	}
	
	private JPanel getWestPanel() {
		
		if(m_westPanel == null) {
			
			// create panel
			m_westPanel = new JPanel();
			m_westPanel.setLayout(new BoxLayout(m_westPanel,BoxLayout.Y_AXIS));
			//Utils.setFixedSize(m_westPanel,245,100);
		
			// Type (0,0)
			m_typeCombo = new ComboAttribute("type",m_resources.getString("TaskType.text"),80,null,false);
			m_typeCombo.fill(TaskType.values());
			m_typeCombo.getComboBox().setSelectedIndex(3);
			m_typeCombo.getComboBox().setRenderer(new TaskEnumListCellRenderer());
			m_typeCombo.getComboBox().addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					updateTaskText();
				}
			});
			m_typeCombo.addDiskoWorkListener(this);
			m_westPanel.add(m_typeCombo);
			
			// Created date
			m_createdField = new DTGAttribute("created",m_resources.getString("TaskCreated.text"),80,"",false);
			m_createdField.addDiskoWorkListener(this);
			m_westPanel.add(Box.createVerticalStrut(5));
			m_westPanel.add(m_createdField);

			// Responsible (2,0)
			Object[] responsible = {};
			try
			{
				responsible = Utils.getApp().getModuleManager().getRoleTitles(false);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
			m_responsibleCombo = new ComboAttribute("responsible", 
					m_resources.getString("TaskResponsible.text"),80,null,false);
			m_responsibleCombo.fill(responsible);
			m_responsibleCombo.getComboBox().setSelectedIndex(0);
			m_responsibleCombo.addDiskoWorkListener(this);
			m_westPanel.add(Box.createVerticalStrut(5));
			m_westPanel.add(m_responsibleCombo);
			
			// Status
			TaskStatus[] statusItems =
			{
				TaskStatus.UNPROCESSED,
				TaskStatus.STARTED,
				TaskStatus.POSTPONED,
				TaskStatus.FINISHED,
			};
			m_statusCombo = new ComboAttribute("status",m_resources.getString("TaskStatus.text"),80,null,false);
			m_statusCombo.fill(statusItems);
			m_statusCombo.getComboBox().setSelectedIndex(0);
			m_statusCombo.getComboBox().setRenderer(new TaskEnumListCellRenderer());
			m_statusCombo.getComboBox().addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// If status is set to finished, progress should be set to 100%
					TaskStatus selectedStatus = (TaskStatus)m_statusCombo.getValue();
					if(selectedStatus == TaskStatus.FINISHED)
					{
						m_progressCombo.getComboBox().setSelectedIndex(10);
					}
					updateFieldsEditable();
				}
			});
			m_statusCombo.addDiskoWorkListener(this);
			m_westPanel.add(Box.createVerticalStrut(5));
			m_westPanel.add(m_statusCombo);

		}
		return m_westPanel;
	}
	
	private JPanel getEastPanel() {
		
		if(m_eastPanel==null) {
			
			// create panel
			m_eastPanel = new JPanel();
			m_eastPanel.setLayout(new BoxLayout(m_eastPanel,BoxLayout.Y_AXIS));
			
			// Priority
			m_priorityCombo = new ComboAttribute("priority",m_resources.getString("TaskPriority.text"),80,null,false);
			m_priorityCombo.fill(TaskPriority.values());
			m_priorityCombo.getComboBox().setSelectedIndex(3);
			m_priorityCombo.getComboBox().setRenderer(new TaskEnumListCellRenderer());
			m_priorityCombo.addDiskoWorkListener(this);
			m_eastPanel.add(m_priorityCombo);

			// Due (2,1)
			m_dueCombo = new ComboAttribute("due", m_resources.getString("TaskDue.text"),80,null,true);
			updateDueComboBox();
			m_dueCombo.getComboBox().setSelectedIndex(2);
			JTextField field = (JTextField)m_dueCombo.getComboBox()
									.getEditor().getEditorComponent();
			field.setDocument(new NumericDocument(6,0,false));
			m_dueCombo.addDiskoWorkListener(this);
			m_eastPanel.add(Box.createVerticalStrut(5));
			m_eastPanel.add(m_dueCombo);

			// Alert (2,1)
			m_alertCombo = new ComboAttribute("alert",m_resources.getString("TaskAlert.text"),80,null,true);
			updateAlertComboBox();
			m_alertCombo.getComboBox().setSelectedIndex(2);
			/*field = (JTextField)m_alertCombo.getComboBox()
				.getEditor().getEditorComponent();
			field.setDocument(new NumericDocument(6,0,false));*/
			m_alertCombo.addDiskoWorkListener(this);
			m_eastPanel.add(Box.createVerticalStrut(5));
			m_eastPanel.add(m_alertCombo);
			
			// Progress 
			String[] progressItems = {"0%", "10%", "20%", "30%", "40%", "50%", "60%", "50%", "80%", "90%", "100%"};
			m_progressCombo = new ComboAttribute("progress",m_resources.getString("TaskProgress.text"),80,null,false);
			m_progressCombo.fill(progressItems);
			m_progressCombo.getComboBox().setSelectedIndex(0);
			m_progressCombo.addDiskoWorkListener(this);
			m_eastPanel.add(Box.createVerticalStrut(5));
			m_eastPanel.add(m_progressCombo);
		
		}
		return m_eastPanel;
	}
	
	private JPanel getBottomPanel() {
		
		if(m_bottomPanel==null) {

			// create panel
			m_bottomPanel = new JPanel();
			m_bottomPanel.setLayout(new BoxLayout(m_bottomPanel,BoxLayout.Y_AXIS));
			Utils.setFixedSize(m_bottomPanel,490,250);
			
			// Description
			m_descriptionArea = new TextAreaAttribute("description",
					m_resources.getString("TaskDescription.text"),80,null,true);
			Utils.setFixedSize(m_descriptionArea,490,100);
			m_descriptionArea.getTextArea().setRows(0);

			// add button
			String text = m_resources.getString("TaskUseSource.text");
			m_useSourceButton = DiskoButtonFactory.createButton(text,text,null,ButtonSize.NORMAL);
			m_useSourceButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_descriptionArea.setValue(m_sourceArea.getValue());
				}
			});
			m_descriptionArea.setButton(m_useSourceButton, true);
			m_descriptionArea.addDiskoWorkListener(this);
			m_bottomPanel.add(m_descriptionArea);
			
			
			// Source
			m_sourceArea = new  TextAreaAttribute("source",
					m_resources.getString("TaskSource.text"),80,null,false);
			Utils.setFixedSize(m_sourceArea,490,100);
			m_sourceArea.getTextArea().setRows(0);
			m_sourceArea.addDiskoWorkListener(this);
			m_bottomPanel.add(Box.createVerticalStrut(5));
			m_bottomPanel.add(m_sourceArea);

			// Object
			m_objectField = new TextFieldAttribute("object",
					m_resources.getString("TaskObject.text"),80,null,false);
			Utils.setFixedSize(m_objectField,490,25);
			m_objectField.addDiskoWorkListener(this);
			m_bottomPanel.add(Box.createVerticalStrut(5));
			m_bottomPanel.add(m_objectField);
		}
		return m_bottomPanel;
	}
	
	protected boolean beforeFinish() {

		// validate 
		if(timesValid())  {
			// apply change
			saveTask();
			// success
			return true;
		}
		// notify
		Utils.showError(
				m_resources.getString("TimeError.header"),
				m_resources.getString("TimeError.text"));
		// failed
		return false;
		
	}

	public void setTask(ITaskIf task)
	{
		m_currentTask = task;
		updateFieldContents();
		updateFieldsEditable();
	}

	private boolean timesValid()
	{
		String selectedAlertTimeString = (String)m_alertCombo.getValue();
		if(selectedAlertTimeString.equals(m_resources.getString("TaskNoAlert.text")))
		{
			return true;
		}
		// Check that alert time is after creation time.
		// Alert time will always be before due time
		try
		{
			Calendar creationTime = null;
			if(m_currentTask == null)
			{
				creationTime = Calendar.getInstance();
			}
			else
			{
				creationTime = m_currentTask.getCreated();
			}

			Calendar alertTime = null;
			String alertString = selectedAlertTimeString.split(" ")[0];
			String dueString = (String)m_dueCombo.getValue();
			alertTime = DTG.DTGToCal(dueString);
			alertTime.add(Calendar.MINUTE, -Integer.valueOf(alertString));
			return alertTime.after(creationTime);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Decides whether or not fields can be edited, given the current message
	 */
	private void updateFieldsEditable()
	{
		// Check status
		TaskStatus status = (TaskStatus)m_statusCombo.getValue();
		IDiskoRole role = Utils.getApp().getCurrentRole();
		boolean canChangeFields = TaskUtilities.canChangeFields(status);
		boolean canChangeStatus = TaskUtilities.canChangeStatus(role, m_currentTask);
		boolean editable =  canChangeFields && canChangeStatus;

		m_statusCombo.setEnabled(canChangeStatus);
		m_alertCombo.setEnabled(editable);
		m_descriptionArea.setEditable(editable);
		m_useSourceButton.setEnabled(editable);
		m_dueCombo.setEnabled(editable);
		m_priorityCombo.setEnabled(editable);
		m_progressCombo.setEnabled(editable);
		m_responsibleCombo.setEnabled(editable);
		m_nameField.setEditable(editable);
		m_typeCombo.setEnabled(editable);
	}

	/**
	 * Extracts values from swing components and updates/creates MSO task object
	 */
	public void saveTask()
	{
		// Get due time
		String dueTimeString = (String)m_dueCombo.getValue();
		Calendar dueTime = null;
		try
		{
			dueTime = DTG.DTGToCal(dueTimeString);
		}
		catch (IllegalMsoArgumentException e1)
		{
			dueTime = Calendar.getInstance();
			dueTime.add(Calendar.MINUTE, 30);
		}

		// Create new task if current is set to null
		if(m_currentTask == null)
		{
			m_currentTask = Utils.getApp().getMsoModel().getMsoManager().createTask(dueTime);
			m_currentTask.setCreated(Calendar.getInstance());
		}
		else
		{
			m_currentTask.setDueTime(dueTime);
		}

		// Get text
		String text = (String)m_nameField.getValue();
		m_currentTask.setTaskText(text);

		// Get type
		TaskType type = (TaskType)m_typeCombo.getValue();
		m_currentTask.setType(type);

		// Get priority
		TaskPriority priority = (TaskPriority)m_priorityCombo.getValue();
		m_currentTask.setPriority(priority);

		// Get responsible
		String responsible = (String)m_responsibleCombo.getValue();
		m_currentTask.setResponsibleRole(responsible);

		// Get alert
		Calendar alertTime = null;
		try
		{
			String alertString = ((String)m_alertCombo.getValue()).split(" ")[0];
			alertTime = new GregorianCalendar();
			alertTime.set(dueTime.get(Calendar.YEAR),
					dueTime.get(Calendar.MONTH),
					dueTime.get(Calendar.DAY_OF_MONTH),
					dueTime.get(Calendar.HOUR_OF_DAY),
					dueTime.get(Calendar.MINUTE),
					dueTime.get(Calendar.SECOND));
			alertTime.add(Calendar.MINUTE, -Integer.valueOf(alertString));
		}
		catch(Exception e)
		{
			// Invalid alert, set to null
			alertTime = null;
		}
		m_currentTask.setAlert(alertTime);

		// Get status
		TaskStatus status = (TaskStatus)m_statusCombo.getValue();
    	m_currentTask.setStatus(status);

		// Get progress
		int progress = Integer.valueOf(((String)m_progressCombo.getValue()).split("%")[0]);
		m_currentTask.setProgress(progress);

		// Get description
		String description = (String)m_descriptionArea.getValue();
		m_currentTask.setDescription(description);

		// update mso model
		Utils.getApp().getMsoModel().commit();

		// Clean up
		m_currentTask = null;
	}

	/**
	 * Updates swing component contents with values stored in current task
	 */
	private void updateFieldContents()
	{
		if(m_currentTask != null)
		{
			// Updating an existing task

			// Task text
			String taskText = m_currentTask.getTaskText();
			m_nameField.setValue(taskText);

			// Type
			TaskType type = m_currentTask.getType();
			m_typeCombo.setValue(type);

			// Created
			Calendar created = m_currentTask.getCreated();
			m_createdField.setValue(DTG.CalToDTG(created));

			// Priority
			TaskPriority priority = m_currentTask.getPriority();
			m_priorityCombo.setValue(priority);

			// Responsible
			String responsible = m_currentTask.getResponsibleRole();
			if(responsible == null || responsible.equals(""))
			{
				m_responsibleCombo.setValue(null);
			}
			else
			{
				m_responsibleCombo.setValue(responsible);
			}

			// Status
			TaskStatus status = m_currentTask.getStatus();
			m_statusCombo.setValue(status);

			// Progress
			int progress = m_currentTask.getProgress();
			m_progressCombo.getComboBox().setSelectedIndex(progress/10);

			// Description
			String description = m_currentTask.getDescription();
			m_descriptionArea.setValue(description);

			// initialize text
			String source = m_currentTask.getSourceClassText();
			
			// get more source information?
			if(m_currentTask instanceof TaskImpl) {
				// get source object
				IMsoObjectIf srcObj = ((TaskImpl)m_currentTask).getSourceObject();
				// get text
				if(srcObj instanceof IMessageIf) {
					// cast
					IMessageIf message = (IMessageIf)srcObj;
					// get resource bundle
					ResourceBundle bundle = Internationalization.getBundle(IDiskoWpMessageLog.class);
					// get source text
					source = MsoUtils.getMessageText(message);
				}				
			}
			else {
			}
			
			// save source text
			m_sourceArea.setValue(source);

			// Object
			IMsoObjectIf object = m_currentTask.getDependentObject();
			m_objectField.setValue(object == null ? "" : object.shortDescriptor());
			
		}
		else
		{
			// Updating a new task
			m_typeCombo.setValue(TaskType.GENERAL);
			m_createdField.setValue(DTG.CalToDTG(Calendar.getInstance()));
			m_priorityCombo.setValue(TaskPriority.NORMAL);
			m_responsibleCombo.setValue(null);
			m_alertCombo.getComboBox().setSelectedIndex(0);
			m_statusCombo.setValue(TaskStatus.UNPROCESSED);
			m_progressCombo.getComboBox().setSelectedIndex(0);
			m_descriptionArea.setValue("");
			m_sourceArea.setValue("");
			m_objectField.setValue("");
		}

		updateDueComboBox();
		updateAlertComboBox();
	}
	
	private void updateDueComboBox()
	{
		m_dueCombo.getComboBox().removeAllItems();
		int intervalSize = 15;
		int numItems = 5;
		Calendar dueItem = null;
		if(m_currentTask == null)
		{
			dueItem = Calendar.getInstance();
		}
		else
		{
			dueItem = new GregorianCalendar();
			Calendar dueTime = m_currentTask.getDueTime();
			dueItem.set(
					dueTime.get(Calendar.YEAR),
					dueTime.get(Calendar.MONTH),
					dueTime.get(Calendar.DATE),
					dueTime.get(Calendar.HOUR_OF_DAY),
					dueTime.get(Calendar.MINUTE),
					dueTime.get(Calendar.SECOND));
		}

		for(int i=0; i<numItems; i++)
		{
			m_dueCombo.getComboBox().addItem(DTG.CalToDTG(dueItem));
			dueItem.add(Calendar.MINUTE, intervalSize);
		}
		m_dueCombo.getComboBox().setSelectedIndex(0);
	}

	private void updateAlertComboBox()
	{
		int[] alertItems = {1, 5, 15, 30, 60};

		m_alertCombo.getComboBox().removeAllItems();

		m_alertCombo.getComboBox().addItem(m_resources.getString("TaskNoAlert.text"));

		String alertItem = null;
		for(int i=0; i<alertItems.length; i++)
		{
			alertItem = String.valueOf(alertItems[i]) + " " + m_resources.getString("TaskAlertItem.text");
			m_alertCombo.getComboBox().addItem(alertItem);
		}

		if(m_currentTask != null)
		{
			Calendar alertTime = m_currentTask.getAlert();
			if(alertTime == null)
			{
				m_alertCombo.getComboBox().setSelectedIndex(0);
			}
			else
			{
				long dueTimeMillis = m_currentTask.getDueTime().getTimeInMillis();
				long alertTimeMillis = alertTime.getTimeInMillis();
				int diffMin = (int)(dueTimeMillis - alertTimeMillis)/60000;
				if(diffMin == 0)
				{
					// No alert
					m_alertCombo.getComboBox().setSelectedIndex(0);
				}
				else
				{
					// Check if alert time is any of the standard times, if so mark it
					boolean customTime = true;
					diffMin++;
					for(int i=0; i<alertItems.length; i++)
					{
						if(diffMin == alertItems[i])
						{
							m_alertCombo.getComboBox().setSelectedIndex(i+1);
							customTime = false;
						}
					}

					if(customTime)
					{
						// User has written in own time, add this to combo box items
						String oldAlertTime = diffMin + " " + m_resources.getString("TaskAlertItem.text");
						m_alertCombo.getComboBox().addItem(oldAlertTime);
						m_alertCombo.setValue(oldAlertTime);
					}
				}
			}
		}
	}

	/**
	 * Update task text field contents based on selected type, etc.
	 */
	private void updateTaskText()
	{
		// TODO Automatically change text in a stored task or not?
		if(m_currentTask == null)
		{
			String text = "";

			TaskType type = (TaskType)m_typeCombo.getValue();
			switch(type)
			{
			case TRANSPORT:
				text = m_resources.getString("TaskTransportText.text");
				break;
			case RESOURCE:
				text = m_resources.getString("TaskResourceText.text");
				break;
			case INTELLIGENCE:
				text = m_resources.getString("TaskIntelligenceText.text");
				break;
			case GENERAL:
				text = m_resources.getString("TaskGeneralText.text");
				break;
			}
			m_nameField.setValue(text);
			// is dirty
			setDirty(true);
		}
	}

	/**
	 * Renders task combo box items as simple label, gets text from {@link TaskImpl#getEnumText(Enum)}
	 *
	 * @author thomasl
	 */
	@SuppressWarnings("unchecked")
	private class TaskEnumListCellRenderer extends JLabel implements ListCellRenderer
	{
		private static final long serialVersionUID = 1L;

		public TaskEnumListCellRenderer()
		{
			this.setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int arg2, boolean isSelected, boolean hasFocus)
		{
			this.setText(TaskImpl.getEnumText((Enum)value));

			if (isSelected)
	        {
	            setBackground(list.getSelectionBackground());
	            setForeground(list.getSelectionForeground());
	        } else
	        {
	            setBackground(list.getBackground());
	            setForeground(list.getForeground());
	        }

			return this;
		}
	}
}
