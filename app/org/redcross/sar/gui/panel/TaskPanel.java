package org.redcross.sar.gui.panel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.redcross.sar.Application;
import org.redcross.sar.IApplication;
import org.redcross.sar.IDiskoRole;
import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.field.ComboBoxField;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.TextAreaField;
import org.redcross.sar.gui.field.TextField;
import org.redcross.sar.gui.renderer.BundleListCellRenderer;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITaskIf.TaskPriority;
import org.redcross.sar.mso.data.ITaskIf.TaskStatus;
import org.redcross.sar.mso.data.ITaskIf.TaskType;
import org.redcross.sar.mso.data.TaskImpl;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.mso.DTG;
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
    
    private static final Logger m_logger = Logger.getLogger(TaskPanel.class);

	private ITaskIf m_currentTask;

	private TextField m_nameField;
	private ComboBoxField m_typeCombo;
	private DTGField m_createdField;
	private ComboBoxField m_priorityCombo;
	private ComboBoxField m_dueCombo;
	private ComboBoxField m_responsibleCombo;
	private ComboBoxField m_alertCombo;
	private ComboBoxField m_statusCombo;
	private ComboBoxField m_progressCombo;
	private JButton m_useSourceButton;
	private TextAreaField m_descriptionArea;
	private TextAreaField m_sourceArea;
	private TextField m_objectField;
	private JPanel m_centerPanel;
	private JPanel m_westPanel;
	private JPanel m_eastPanel;
	private JPanel m_bottomPanel;
	
	

    public TaskPanel()
	{
		// forward
    	super("Oppgave");

		// initialize gui
		initialize();
	}

    private void initialize() {
		
		// prepare
		this.setNotScrollBars();

		// get body component
		JPanel panel = (JPanel)getContainer();
		panel.setPreferredSize(new Dimension(500, 320));
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// add panels
		panel.add(getNameField());
		panel.add(Box.createVerticalStrut(5));
		panel.add(getCenterPanel());
		panel.add(Box.createVerticalStrut(5));
		panel.add(getBottomPanel());
		panel.add(Box.createVerticalGlue());

	}

	private JPanel getNameField() {
		if(m_nameField==null) {
			m_nameField = new TextField("name",m_resources.getString("Task.text"),true);
			//Utils.setFixedSize(m_nameField,490,25);
		}
		return m_nameField;
	}

	private JPanel getCenterPanel() {

		if(m_centerPanel == null) {
			m_centerPanel = new JPanel();
			m_centerPanel.setLayout(new BoxLayout(m_centerPanel,BoxLayout.X_AXIS));
			m_centerPanel.add(getWestPanel());
			m_centerPanel.add(Box.createHorizontalStrut(10));
			m_centerPanel.add(getEastPanel());
		}
		return m_centerPanel;
	}

	private JPanel getWestPanel() {

		if(m_westPanel == null) {

			// create panel
			m_westPanel = new JPanel();
			m_westPanel.setLayout(new BoxLayout(m_westPanel,BoxLayout.Y_AXIS));

			// Type (0,0)
			m_typeCombo = new ComboBoxField("type",m_resources.getString("TaskType.text"),false);
			m_typeCombo.fill(TaskType.values());
			m_typeCombo.getEditComponent().setSelectedIndex(3);
			m_typeCombo.getEditComponent().setRenderer(new BundleListCellRenderer(m_resources));
			m_typeCombo.getEditComponent().addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					updateTaskText();
				}
			});
			m_typeCombo.addFlowListener(this);
			m_westPanel.add(m_typeCombo);

			// Created date
			m_createdField = new DTGField("created",m_resources.getString("TaskCreated.text"),false);
			m_createdField.addFlowListener(this);
			m_westPanel.add(Box.createVerticalStrut(5));
			m_westPanel.add(m_createdField);

			// Responsible (2,0)
			Object[] responsible = {};
			try
			{
				responsible = Application.getInstance().getModuleManager().getRoleTitles(false);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
			m_responsibleCombo = new ComboBoxField("responsible",
					m_resources.getString("TaskResponsible.text"),false);
			m_responsibleCombo.fill(responsible);
			m_responsibleCombo.getEditComponent().setSelectedIndex(0);
			m_responsibleCombo.addFlowListener(this);
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
			m_statusCombo = new ComboBoxField("status",m_resources.getString("TaskStatus.text"),false);
			m_statusCombo.fill(statusItems);
			m_statusCombo.getEditComponent().setSelectedIndex(0);
			m_statusCombo.getEditComponent().setRenderer(new BundleListCellRenderer(m_resources));
			m_statusCombo.getEditComponent().addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// If status is set to finished, progress should be set to 100%
					TaskStatus selectedStatus = (TaskStatus)m_statusCombo.getValue();
					if(selectedStatus == TaskStatus.FINISHED)
					{
						m_progressCombo.getEditComponent().setSelectedIndex(10);
					}
					updateFieldsEditable();
				}
			});
			m_statusCombo.addFlowListener(this);
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
			m_priorityCombo = new ComboBoxField("priority",m_resources.getString("TaskPriority.text"),false);
			m_priorityCombo.fill(TaskPriority.values());
			m_priorityCombo.getEditComponent().setSelectedIndex(3);
			m_priorityCombo.getEditComponent().setRenderer(new BundleListCellRenderer(m_resources));
			m_priorityCombo.addFlowListener(this);
			m_eastPanel.add(m_priorityCombo);

			// Due (2,1)
			m_dueCombo = new ComboBoxField("due", m_resources.getString("TaskDue.text"),true);
			updateDueComboBox();
			m_dueCombo.getEditComponent().setSelectedIndex(2);
			JTextField field = (JTextField)m_dueCombo.getEditComponent()
									.getEditor().getEditorComponent();
			field.setDocument(new NumericDocument(6,0,false));
			m_dueCombo.addFlowListener(this);
			m_eastPanel.add(Box.createVerticalStrut(5));
			m_eastPanel.add(m_dueCombo);

			// Alert (2,1)
			m_alertCombo = new ComboBoxField("alert",m_resources.getString("TaskAlert.text"),true);
			updateAlertComboBox();
			m_alertCombo.getEditComponent().setSelectedIndex(2);
			m_alertCombo.addFlowListener(this);
			m_eastPanel.add(Box.createVerticalStrut(5));
			m_eastPanel.add(m_alertCombo);

			// Progress
			String[] progressItems = {"0%", "10%", "20%", "30%", "40%", "50%", "60%", "50%", "80%", "90%", "100%"};
			m_progressCombo = new ComboBoxField("progress",m_resources.getString("TaskProgress.text"),false);
			m_progressCombo.fill(progressItems);
			m_progressCombo.getEditComponent().setSelectedIndex(0);
			m_progressCombo.addFlowListener(this);
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
			m_descriptionArea = new TextAreaField("description",
					m_resources.getString("TaskDescription.text"),true);
			m_descriptionArea.setFixedHeight(100);
			m_descriptionArea.getEditComponent().setRows(0);

			// add button
			//String text = m_resources.getString("TaskUseSource.text");
			m_useSourceButton = DiskoButtonFactory.createButton("GENERAL.EQUAL",getButtonSize());
			m_useSourceButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String source = m_sourceArea.getValue();
					source = (source==null || source.isEmpty() ? "" : source);
					String[] split = source.split(":");
					if(split.length>1) {
						source = split[1];
						if(split.length>2) {
							for(int i=2;i<split.length;i++) {
								source.concat(":"+split[i]);
							}
						}
					}
					m_descriptionArea.setValue(source.trim());
				}
			});
			m_descriptionArea.installButton(m_useSourceButton, true);
			m_descriptionArea.addFlowListener(this);
			m_bottomPanel.add(m_descriptionArea);


			// Source
			m_sourceArea = new  TextAreaField("source",
					m_resources.getString("TaskSource.text"),false);
			m_sourceArea.setFixedHeight(100);
			m_sourceArea.getEditComponent().setRows(0);
			m_sourceArea.addFlowListener(this);
			m_bottomPanel.add(Box.createVerticalStrut(5));
			m_bottomPanel.add(m_sourceArea);

			// Object
			m_objectField = new TextField("object",
					m_resources.getString("TaskObject.text"),false);
			m_objectField.addFlowListener(this);
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


	@Override
	public void setMsoObject(IMsoObjectIf msoObj) {
		// forward?
		if(msoObj instanceof ITaskIf) setTask((ITaskIf)msoObj);
	}

	public void setTask(ITaskIf task)
	{
		// forward
		super.setMsoObject(task);

		// save locally
		m_currentTask = task;

		// prepare
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

			Calendar alertTime = Calendar.getInstance();
			String alertString = selectedAlertTimeString.split(" ")[0];
			String dueString = (String)m_dueCombo.getValue();
			alertTime = DTG.DTGToCal(alertTime.get(Calendar.YEAR),
					alertTime.get(Calendar.MONTH),dueString);
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
		IDiskoRole role = Application.getInstance().getCurrentRole();
		boolean canChangeFields = TaskUtilities.canChangeFields(status);
		boolean canChangeStatus = TaskUtilities.canChangeStatus(role, m_currentTask);
		boolean editable = canChangeFields && canChangeStatus;

		m_statusCombo.setEnabled(canChangeStatus);
		m_alertCombo.setEnabled(editable);
		m_descriptionArea.clearEditableCount();
		m_descriptionArea.setEditable(editable);
		m_useSourceButton.setEnabled(editable);
		m_dueCombo.setEnabled(editable);
		m_priorityCombo.setEnabled(editable);
		m_progressCombo.setEnabled(editable);
		m_responsibleCombo.setEnabled(editable);
		m_nameField.clearEditableCount();
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
		Calendar dueTime = Calendar.getInstance();
		try
		{
			dueTime = DTG.DTGToCal(dueTime.get(Calendar.YEAR),
					dueTime.get(Calendar.MONTH), dueTimeString);
		}
		catch (IllegalMsoArgumentException e1)
		{
			dueTime = Calendar.getInstance();
			dueTime.add(Calendar.MINUTE, 30);
		}

		// Create new task if current is set to null
		if(m_currentTask == null)
		{
			m_currentTask = Application.getInstance().getMsoModel().getMsoManager().createTask(dueTime);
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

        try {
        	IApplication app = Application.getInstance();
        	app.getMsoModel().commit(app.getTransactionManager().getChanges(m_currentTask));
		} catch (TransactionException ex) {
			m_logger.error("Failed to commit unit detail changes",ex);
		}            

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
			m_progressCombo.getEditComponent().setSelectedIndex(progress/10);

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
					// get source text
					source = MsoUtils.getMessageText(message);
					// default message
					source = "Melding " + message.getNumber() + ": " + (source!=null && !source.isEmpty() ? source : "<Ingen meldingslinjer>");
				}
			}
			else {
			}

			// save source text
			m_sourceArea.setValue(source);

			// Object
			IMsoObjectIf object = m_currentTask.getDependentObject();
			m_objectField.setValue(object == null ? "" : MsoUtils.getMsoObjectName(object,1));

		}
		else
		{
			// Updating a new task
			m_typeCombo.setValue(TaskType.GENERAL);
			m_createdField.setValue(DTG.CalToDTG(Calendar.getInstance()));
			m_priorityCombo.setValue(TaskPriority.NORMAL);
			m_responsibleCombo.setValue(null);
			m_alertCombo.getEditComponent().setSelectedIndex(0);
			m_statusCombo.setValue(TaskStatus.UNPROCESSED);
			m_progressCombo.getEditComponent().setSelectedIndex(0);
			m_descriptionArea.setValue("");
			m_sourceArea.setValue("");
			m_objectField.setValue("");
		}

		updateDueComboBox();
		updateAlertComboBox();
	}

	private void updateDueComboBox()
	{
		m_dueCombo.getEditComponent().removeAllItems();
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
			m_dueCombo.getEditComponent().addItem(DTG.CalToDTG(dueItem));
			dueItem.add(Calendar.MINUTE, intervalSize);
		}
		m_dueCombo.getEditComponent().setSelectedIndex(0);
	}

	private void updateAlertComboBox()
	{
		int[] alertItems = {1, 5, 15, 30, 60};

		m_alertCombo.getEditComponent().removeAllItems();

		m_alertCombo.getEditComponent().addItem(m_resources.getString("TaskNoAlert.text"));

		String alertItem = null;
		for(int i=0; i<alertItems.length; i++)
		{
			alertItem = String.valueOf(alertItems[i]) + " " + m_resources.getString("TaskAlertItem.text");
			m_alertCombo.getEditComponent().addItem(alertItem);
		}

		if(m_currentTask != null)
		{
			Calendar alertTime = m_currentTask.getAlert();
			if(alertTime == null)
			{
				m_alertCombo.getEditComponent().setSelectedIndex(0);
			}
			else
			{
				long dueTimeMillis = m_currentTask.getDueTime().getTimeInMillis();
				long alertTimeMillis = alertTime.getTimeInMillis();
				int diffMin = (int)(dueTimeMillis - alertTimeMillis)/60000;
				if(diffMin == 0)
				{
					// No alert
					m_alertCombo.getEditComponent().setSelectedIndex(0);
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
							m_alertCombo.getEditComponent().setSelectedIndex(i+1);
							customTime = false;
						}
					}

					if(customTime)
					{
						// User has written in own time, add this to combo box items
						String oldAlertTime = diffMin + " " + m_resources.getString("TaskAlertItem.text");
						m_alertCombo.getEditComponent().addItem(oldAlertTime);
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

}
