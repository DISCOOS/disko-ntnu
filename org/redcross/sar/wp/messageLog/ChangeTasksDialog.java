package org.redcross.sar.wp.messageLog;

import no.cmr.tools.Log;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.mso.dialog.TaskDialog;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITaskIf.TaskPriority;
import org.redcross.sar.mso.data.ITaskIf.TaskStatus;
import org.redcross.sar.mso.data.ITaskIf.TaskType;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.MsoUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;

/**
 * Dialog for changing task in current message.
 * Initializes and shows a {@link TaskDialog} when changing task fields
 *
 * @author thomasl
 */
public class ChangeTasksDialog extends DefaultDialog implements IEditorIf, IMsoUpdateListenerIf
{
	private static final long serialVersionUID = 1L;

	protected static IDiskoWpMessageLog m_wpMessageLog;

	protected JPanel m_contentsPanel;

	protected JToggleButton m_sendTransportButton;
	protected JButton m_changeSendTransportButton;

	protected JToggleButton m_getTeamButton;
	protected JButton m_changeGetTeamButton;

	protected JToggleButton m_createAssignmentButton;
	protected JButton m_changeCreateAssignmentButton;

	protected JToggleButton m_confirmIntelligenceButton;
	protected JButton m_changeConfirmIntelligenceButton;

	protected JToggleButton m_findingButton;
	protected JButton m_changeFindingButton;

	protected JToggleButton m_generalTaskButton;
	protected JButton m_changeGeneralTaskButton;

	protected List<JToggleButton> m_toggleButtons;

	protected HashMap<JToggleButton, JButton> m_buttonMap;
	protected HashMap<TaskSubType, JToggleButton> m_typeButtonMap;
	protected HashMap<JToggleButton, TaskSubType> m_buttonTypeMap;

	protected enum TaskSubType
	{
		SEND_TRANSPORT,
		GET_TEAM,
		CREATE_ASSIGNMENT,
		CONFIRM_INTELLIGENCE,
		FINDING,
		GENERAL
	};

	/**
	 * @param wp Message log work process reference
	 */
	public ChangeTasksDialog(IDiskoWpMessageLog wp)
	{
		super(wp.getApplication().getFrame());

		m_wpMessageLog = wp;
		wp.getMsoEventManager().addClientUpdateListener(this);

		m_buttonMap = new HashMap<JToggleButton, JButton>();
		m_buttonTypeMap = new HashMap<JToggleButton, TaskSubType>();
		m_typeButtonMap = new HashMap<TaskSubType, JToggleButton>();

		initialize();
	}

	private void initialize()
	{
		// Initialize contents panel
		m_contentsPanel = new JPanel();
		m_contentsPanel.setLayout(new BoxLayout(m_contentsPanel, BoxLayout.PAGE_AXIS));
		Dimension normDim = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL);
		Dimension longDim = DiskoButtonFactory.getButtonSize(ButtonSize.LONG);
		m_contentsPanel.setPreferredSize(new Dimension(
				longDim.width + normDim.width + 54,longDim.height*6 + 6));
		m_contentsPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));

		initButtons();

		this.add(m_contentsPanel);

		this.pack();
	}

	private void initButtons()
	{
		// Send transport
		m_sendTransportButton = createToggleButton(TaskSubType.SEND_TRANSPORT);
		m_changeSendTransportButton = createChangeButton(TaskSubType.SEND_TRANSPORT);
		addButtonPair(m_sendTransportButton, m_changeSendTransportButton, TaskSubType.SEND_TRANSPORT);

		// Get team
		m_getTeamButton = createToggleButton(TaskSubType.GET_TEAM);
		m_changeGetTeamButton = createChangeButton(TaskSubType.GET_TEAM);
		addButtonPair(m_getTeamButton, m_changeGetTeamButton, TaskSubType.GET_TEAM);

		// Create assignment
		m_createAssignmentButton = createToggleButton(TaskSubType.CREATE_ASSIGNMENT);
		m_changeCreateAssignmentButton = createChangeButton(TaskSubType.CREATE_ASSIGNMENT);
		addButtonPair(m_createAssignmentButton, m_changeCreateAssignmentButton, TaskSubType.CREATE_ASSIGNMENT);

		// Confirm intelligence
		m_confirmIntelligenceButton = createToggleButton(TaskSubType.CONFIRM_INTELLIGENCE);
		m_changeConfirmIntelligenceButton = createChangeButton(TaskSubType.CONFIRM_INTELLIGENCE);
		addButtonPair(m_confirmIntelligenceButton, m_changeConfirmIntelligenceButton, TaskSubType.CONFIRM_INTELLIGENCE);

		// Finding
		m_findingButton = createToggleButton(TaskSubType.FINDING);
		m_changeFindingButton = createChangeButton(TaskSubType.FINDING);
		addButtonPair(m_findingButton, m_changeFindingButton, TaskSubType.FINDING);

		m_contentsPanel.add(new JSeparator(JSeparator.HORIZONTAL));

		// General
		m_generalTaskButton = createToggleButton(TaskSubType.GENERAL);
		m_changeGeneralTaskButton = createChangeButton(TaskSubType.GENERAL);
		addButtonPair(m_generalTaskButton, m_changeGeneralTaskButton, TaskSubType.GENERAL);
	}

	/**
	 * Remove/add tasks as task  buttons are toggled
	 * @param e Event generated by button
	 * @param type The task type for the given button
	 */
	private void toggleTask(ActionEvent e, TaskSubType type)
	{
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);

		JToggleButton button = (JToggleButton)e.getSource();
		JButton changeButton = m_buttonMap.get(button);

		if(button.isSelected())
		{
			// Button is selected, add task
			ITaskIf task = m_wpMessageLog.getMsoManager().createTask(Calendar.getInstance());
			initTaskValues(task, type);
			message.addMessageTask(task);

			// Make corresponding change button active
			changeButton.setEnabled(true);
		}
		else
		{
			int ans = JOptionPane.showConfirmDialog(m_wpMessageLog.getApplication().getFrame(),
					"Du er i ferd med å slette valgt oppgave. Vil du fortsette?","Bekreftelse",JOptionPane.YES_NO_OPTION);
			// prompt user
			if(ans == JOptionPane.YES_OPTION) {
			
				// Button is deselected, remove task
				for(ITaskIf task : message.getMessageTasksItems())
				{
					if(getSubType(task) == type)
					{
						if(!task.delete())
						{
							Log.error("Error removing task " + task);
						}
					}
				}
				
				// Make corresponding change button inactive
				changeButton.setEnabled(false);
			}
			else {
				// reselect button
				button.setSelected(true);
			}

		}
	}

	/**
	 * Initialize task with the correct field values based on the task sub type
	 * @param task
	 * @param type
	 */
	private void initTaskValues(ITaskIf task, TaskSubType type)
	{
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);

		task.setCreated(Calendar.getInstance());

		if(type == TaskSubType.FINDING)
		{
			String taskText = String.format(m_wpMessageLog.getBundleText("TaskSubType.FINDING.text"),
					m_wpMessageLog.getBundleText("Finding.text"));
			task.setTaskText(taskText);
			//task.getSourceClassText()
		}
		else
		{
			task.setTaskText(m_wpMessageLog.getBundleText("TaskSubType." + type.name() + ".text"));
		}

		switch(type)
		{
		case SEND_TRANSPORT:
			task.setType(TaskType.TRANSPORT);
			break;
		case GET_TEAM:
			task.setType(TaskType.RESOURCE);
			break;
		case CREATE_ASSIGNMENT:
			task.setType(TaskType.RESOURCE);
			break;
		case CONFIRM_INTELLIGENCE:
			task.setType(TaskType.INTELLIGENCE);
			break;
		case FINDING:
			task.setType(TaskType.INTELLIGENCE);
			break;
		case GENERAL:
			task.setType(TaskType.GENERAL);
		}
		
		// set description
		task.setDescription(MsoUtils.getMessageText(message));

		// Due time
		Calendar dueTime = Calendar.getInstance();
		dueTime.add(Calendar.MINUTE, 30);
		task.setDueTime(dueTime);

		// Alert time
		Calendar alertTime = Calendar.getInstance();
		alertTime.add(Calendar.MINUTE, 16);
		task.setAlert(alertTime);

		// Priority
		task.setPriority(TaskPriority.NORMAL);

		// Status
		task.setStatus(TaskStatus.UNPROCESSED);

		// Source
		if(message != null)
		{
			task.setSourceClass(message.getMsoClassCode());
		}

		// Progress
		task.setProgress(0);

		// Responsible
		task.setResponsibleRole(null);

		// WP
		task.setCreatingWorkProcess(m_wpMessageLog.getName());

		// Object
		if(message != null)
		{
			task.setDependentObject(message.getSender());
		}
	}

	/**
	 * Adds a pair of task selection and change action button for that task
	 * @param task
	 * @param change
	 */
	private void addButtonPair(JToggleButton task, JButton change, TaskSubType type)
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(task);
		buttonPanel.add(change);
		m_contentsPanel.add(buttonPanel);

		change.setEnabled(false);

		m_buttonMap.put(task, change);
		m_typeButtonMap.put(type, task);
		m_buttonTypeMap.put(task, type);
	}

	/**
	 * Shows the edit task dialog, dialog should have been initialized before this is called
	 */
	private void showEditTaskDialog()
	{
		TaskDialog taskDialog = m_wpMessageLog.getApplication().getUIFactory().getTaskDialog();
		Point location = getLocationOnScreen();
		location.y -= (taskDialog.getHeight() - getHeight());
		location.x -= (taskDialog.getWidth() - getWidth());
		taskDialog.setLocation(location);
		taskDialog.setVisible(true);
	}

	/**
	 *
	 */
	public void hideEditor()
	{
		this.setVisible(false);
		TaskDialog taskDialog = m_wpMessageLog.getApplication().getUIFactory().getTaskDialog();
		taskDialog.setVisible(false);
	}

	/**
	 * Updates button selection based on which tasks exists in the new message
	 */
	public void setMessage(IMessageIf message)
	{
		// Loop through all tasks in new/updated message
		Collection<ITaskIf> messageTasks = message.getMessageTasks().getItems();
		List<TaskSubType> taskTypes = new LinkedList<TaskSubType>();
		for(ITaskIf messageTask : messageTasks)
		{
			taskTypes.add(getSubType(messageTask));
		}

		for(TaskSubType type : TaskSubType.values())
		{
			JToggleButton button = m_typeButtonMap.get(type);
			JButton changeButton = m_buttonMap.get(button);
			boolean hasType = taskTypes.contains(type);
			button.setSelected(hasType);
			changeButton.setEnabled(hasType);
		}
	}

	/**
	 *
	 */
	public void showEditor()
	{
		this.setVisible(true);
	}

	/**
	 * Clear button selection
	 */
	public void resetEditor()
	{
		Iterator<JToggleButton> iterator = m_buttonMap.keySet().iterator();
		while(iterator.hasNext())
		{
			JToggleButton button = iterator.next();
			JButton changeButton = m_buttonMap.get(button);
			button.setSelected(false);
			changeButton.setEnabled(false);
		}
	}

	/**
	 * @return The change button for the given task.
	 */
	private JButton createChangeButton(final TaskSubType type)
	{
		JButton button = DiskoButtonFactory.createButton("GENERAL.EDIT",ButtonSize.NORMAL);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
				for(ITaskIf task : message.getMessageTasksItems())
				{
					if(getSubType(task) == type)
					{
						TaskDialog taskDialog = m_wpMessageLog.getApplication().getUIFactory().getTaskDialog();
						taskDialog.setTask(task);
						break;
					}
				}
				showEditTaskDialog();
			}
		});

		return button;
	}

	/**
	 * Creates a button that toggles a task in the current message
	 * @return Toggle button that add/remove task from current message
	 */
	private JToggleButton createToggleButton(final TaskSubType type)
	{
		String buttonText = null;
		if(type == TaskSubType.FINDING)
		{
			String findingType = m_wpMessageLog.getBundleText("Finding.text");
			buttonText = String.format(m_wpMessageLog.getBundleText("TaskSubType.FINDING.text"), findingType);
		}
		else
		{
			buttonText = m_wpMessageLog.getBundleText("TaskSubType." + type.name() + ".text");
		}
		JToggleButton button = 	DiskoButtonFactory.createToggleButton(
				buttonText,buttonText,null,ButtonSize.LONG, 50,0);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				toggleTask(e, type);
			}
		});

		return button;
	}

	/**
	 * Used to identify which of the tasks in this dialog, if any, a specific task is. General types
	 * does not provide sufficient information to determine that
	 * @param task
	 * @return
	 */
	public static TaskSubType getSubType(ITaskIf task)
	{
		TaskType taskType = task.getType();
		String taskText = task.getTaskText();
		switch(taskType)
		{
		case TRANSPORT:
			if(taskText.equals(m_wpMessageLog.getBundleText("TaskSubType.SEND_TRANSPORT.text")))
			{
				return TaskSubType.SEND_TRANSPORT;
			}
		case RESOURCE:
			if(taskText.equals(m_wpMessageLog.getBundleText("TaskSubType.GET_TEAM.text")))
			{
				return TaskSubType.GET_TEAM;
			}
			if(taskText.equals(m_wpMessageLog.getBundleText("TaskSubType.CREATE_ASSIGNMENT.text")))
			{
				return TaskSubType.CREATE_ASSIGNMENT;
			}
		case INTELLIGENCE:
			if(taskText.equals(m_wpMessageLog.getBundleText("TaskSubType.CONFIRM_INTELLIGENCE.text")))
			{
				return TaskSubType.CONFIRM_INTELLIGENCE;
			}
			try
			{
				if(taskText.split(":")[0].equals(m_wpMessageLog.getBundleText("TaskSubType.FINDING.text").split(":")[0]))
				{
					return TaskSubType.FINDING;
				}
			}catch(Exception e){}
		case GENERAL:
			return TaskSubType.GENERAL;
		}

		return null;
	}

	/**
	 * Translate between task sub types and task types
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unused")
	public static TaskType getType(TaskSubType type)
	{
		switch(type)
		{
		case SEND_TRANSPORT:
			return TaskType.TRANSPORT;
		case GET_TEAM:
		case CREATE_ASSIGNMENT:
			return TaskType.RESOURCE;
		case CONFIRM_INTELLIGENCE:
		case FINDING:
			return TaskType.INTELLIGENCE;
		case GENERAL:
			return TaskType.GENERAL;
		}

		return null;
	}

	/**
	 * Update finding button to correct type if finding type has been changed
	 * @param e
	 */
	public void handleMsoUpdateEvent(Update e)
	{
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		if(message != null)
		{
			for(ITaskIf task : message.getMessageTasksItems())
			{
				TaskSubType type = getSubType(task);
				if(type == TaskSubType.FINDING)
				{
					m_findingButton.setText(task.getTaskText());
				}
			}
		}
	}

	private final EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of(
    		IMsoManagerIf.MsoClassCode.CLASSCODE_TASK);

	public boolean hasInterestIn(IMsoObjectIf msoObject, UpdateMode mode) 
	{
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
		return myInterests.contains(msoObject.getMsoClassCode());
	}
}
