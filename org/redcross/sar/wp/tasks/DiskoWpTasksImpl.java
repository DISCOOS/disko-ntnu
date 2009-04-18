package org.redcross.sar.wp.tasks;

import org.redcross.sar.IDiskoRole;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.menu.MainMenu;
import org.redcross.sar.gui.menu.SubMenu;
import org.redcross.sar.gui.mso.dialog.TaskDialog;
import org.redcross.sar.gui.renderer.DefaultHeaderRenderer;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITaskIf.TaskStatus;
import org.redcross.sar.mso.data.ITaskListIf;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.WorkPool;
import org.redcross.sar.wp.AbstractDiskoWpModule;
import org.redcross.sar.wp.IDiskoWpModule;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.instrument.IllegalClassFormatException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of the tasks work process
 *
 * @author thomasl
 */
public class DiskoWpTasksImpl extends AbstractDiskoWpModule implements IDiskoWpTasks
{

	private boolean m_oldTransactionMode = false;

	private JPanel m_contentsPanel;

    private DiskoTable m_taskTable;

    private JButton m_newButton;
    private JButton m_changeButton;
    private JButton m_deleteButton;
    private JButton m_performedButton;

    private DeleteTaskDialog m_deleteTaskDialog;
    private List<DefaultDialog> m_dialogs;

    private ITaskIf m_currentTask;

    private final static int TASK_ALERT_TIME = 60000; // check every 60 seconds
    private long m_timeCounter = 0;

    public DiskoWpTasksImpl() throws IllegalClassFormatException
    {
        super();

        m_dialogs = new LinkedList<DefaultDialog>();

        setTaskAlertTimer();

        initialize();
    }

    private void initialize()
    {
        assignWpBundle(IDiskoWpTasks.class);

        m_contentsPanel = new JPanel(new BorderLayout());
        m_contentsPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        initTable();
        initButtons();
        layoutComponent(m_contentsPanel);

        m_deleteTaskDialog = new DeleteTaskDialog(this);
        m_dialogs.add(m_deleteTaskDialog);
        m_dialogs.add(this.getApplication().getUIFactory().getTaskDialog());
    }

    /**
     * Checking if any tasks have reached their alert time, and give the appropriate role a warning
     */
    private void setTaskAlertTimer()
    {
        this.addTickEventListener(new ITickEventListenerIf()
        {
            public long getInterval()
            {
                return TASK_ALERT_TIME;
            }

            public void setTimeCounter(long counter)
            {
                m_timeCounter = counter;
            }

            public long getTimeCounter()
            {
                return m_timeCounter;
            }

            @SuppressWarnings("unchecked")
            public void handleTick(TickEvent e)
            {
            	try {
            		if(m_taskTable!=null && m_taskTable.getRowCount()>0)
            			WorkPool.getInstance().schedule(new TaskTickWork());
            	}
            	catch(Exception ex) {
            		ex.printStackTrace();
            	}
            }
        });
    }

    /**
     * Used by the alert timer
     *
     * @return
     */
    protected IDiskoWpModule getDiskoWpTasks()
    {
        return this;
    }

    @Override
    public String getCaption()
    {
        return getBundleText("TASKS");
    }

    private void initButtons()
    {

		Enum<?> key = TaskActionType.NEW_TASK;
		m_newButton = DiskoButtonFactory.createButton(key, ButtonSize.NORMAL, wpBundle);
        m_newButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                newTask();
            }
        });
        layoutButton(m_newButton, true);

		key = TaskActionType.EDIT_TASK;
		m_changeButton = DiskoButtonFactory.createButton(key, ButtonSize.NORMAL, wpBundle);
        m_changeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                changeTask();
            }
        });
        layoutButton(m_changeButton, true);

		key = TaskActionType.DELETE_TASK;
		m_deleteButton = DiskoButtonFactory.createButton(key, ButtonSize.NORMAL, wpBundle);
        m_deleteButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                deleteTask();
            }
        });
        layoutButton(m_deleteButton, true);

		key = TaskActionType.TASK_FINISHED;
		m_performedButton = DiskoButtonFactory.createButton(key, ButtonSize.NORMAL, wpBundle);
        m_performedButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                performedTask();
            }
        });
        layoutButton(m_performedButton);
    }

    private void initTable()
    {
        m_taskTable = new DiskoTable();
        TaskTableModel model = new TaskTableModel(this, m_taskTable);
        m_taskTable.setModel(model);
        m_taskTable.getSelectionModel().addListSelectionListener(new TaskSelectionListener(this, m_taskTable));
        m_taskTable.setDefaultRenderer(Object.class, new TaskTableRenderer());
        m_taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        m_taskTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) {
					changeTask();
				}
			}

		});

        TableColumn column = m_taskTable.getColumnModel().getColumn(0);
        column.setMaxWidth(75);
        column.setPreferredWidth(75);
        column = m_taskTable.getColumnModel().getColumn(1);
        column.setPreferredWidth(150);
        column.setMaxWidth(150);
        column = m_taskTable.getColumnModel().getColumn(3);
        column.setPreferredWidth(150);
        column.setMaxWidth(150);
        column = m_taskTable.getColumnModel().getColumn(4);
        column.setPreferredWidth(100);
        column.setMaxWidth(100);
        column = m_taskTable.getColumnModel().getColumn(5);
        column.setPreferredWidth(100);
        column.setMaxWidth(100);

        m_taskTable.setRowMargin(2);
        m_taskTable.setRowHeight(22);

        JTableHeader tableHeader = m_taskTable.getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setDefaultRenderer(new DefaultHeaderRenderer());


        JScrollPane tableScrollPane = UIFactory.createScrollPane(m_taskTable,true);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        m_contentsPanel.add(tableScrollPane, BorderLayout.CENTER);
    }

    @Override
	public void activate(IDiskoRole role) {

		// forward
		super.activate(role);

		// set role in task table model (enables popup menus)
		((TaskTableModel)m_taskTable.getModel()).setRole(role);

		// setup of navbar needed?
		if(isNavMenuSetupNeeded()) {
			// forward
			setupNavMenu(Utils.getListNoneOf(MapToolType.class),false);
		}

        SubMenu subMenu = this.getApplication().getUIFactory().getSubMenu();
        m_oldTransactionMode = subMenu.setTransactionMode(false);
    }

    @Override
    public void deactivate()
    {
        super.deactivate();
        SubMenu subMenu = this.getApplication().getUIFactory().getSubMenu();
        subMenu.setTransactionMode(m_oldTransactionMode);

        TaskDialog taskDialog = getApplication().getUIFactory().getTaskDialog();
        taskDialog.setVisible(false);
    }

    @Override
    public boolean confirmDeactivate()
    {
    	if(getMsoModel().hasUncommitedChanges())
    	{
    		Object[] dialogOptions = {getBundleText("Yes.text"), getBundleText("No.text")};
    		int n = JOptionPane.showOptionDialog(this.getApplication().getFrame(),
    				getBundleText("UncommittedChanges.text"),
    				getBundleText("UncommittedChanges.header"),
    				JOptionPane.YES_NO_OPTION,
    				JOptionPane.QUESTION_MESSAGE,
    				null,
    				dialogOptions,
    				dialogOptions[0]);
    		return (n == JOptionPane.YES_OPTION);
    	}
    	else
    	{
    		return true;
    	}
    }

    public void cancel()
    {
    }

    public void finish()
    {
    }

    /**
     * Adds a new task
     */
    private void newTask()
    {
        hideDialogs();
        TaskDialog taskDialog = this.getApplication().getUIFactory().getTaskDialog();
        taskDialog.setSnapToLocation(m_contentsPanel, DefaultDialog.POS_CENTER, 0, true, false);
        taskDialog.getTaskPanel().setTask(null);
        taskDialog.setVisible(true);
    }

    /**
     * Change task selected in tasks table
     */
    private void changeTask()
    {
        if (m_currentTask != null)
        {
            hideDialogs();
            TaskDialog taskDialog = this.getApplication().getUIFactory().getTaskDialog();
            taskDialog.setSnapToLocation(m_contentsPanel, DefaultDialog.POS_CENTER, 0, true, false);
            taskDialog.getTaskPanel().setTask(m_currentTask);
            taskDialog.setVisible(true);
        }
        else {
        	if(m_taskTable.getRowCount()==0)
        		Utils.showWarning("Begrensning", "Du må først opprette en oppgave");
        	else
        		Utils.showWarning("Begrensning", "Du må først velge en oppgave i listen");
        }
    }

    /**
     * Delete selected task in tasks table
     */
    private void deleteTask()
    {
        if (m_currentTask != null)
        {
            if (m_currentTask.getStatus() == TaskStatus.FINISHED)
            {
            	Utils.showError(this.getBundleText("CanNotDeleteTaskError.header"),
                        this.getBundleText("CanNotDeleteTaskError.text"));
            } else
            {
                hideDialogs();
                m_deleteTaskDialog.setTask(m_currentTask);
                m_deleteTaskDialog.setSnapToLocation(m_contentsPanel, DefaultDialog.POS_CENTER, 0, true, false);
                m_deleteTaskDialog.setVisible(true);
            }
        }
        else {
        	if(m_taskTable.getRowCount()==0)
        		Utils.showWarning("Begrensning", "Ingen oppgaver i listen");
        	else
        		Utils.showWarning("Begrensning", "Du må først velge en oppgave i listen");
        }
    }

    /**
     * Mark selected task as performed. Also possible to change task status in the change task
     * dialog
     */
    private void performedTask()
    {
        if (m_currentTask != null)
        {
            m_currentTask.setProgress(100);
            m_currentTask.setStatus(TaskStatus.FINISHED);
            this.getMsoModel().commit();
        }
        else {
        	if(m_taskTable.getRowCount()==0)
        		Utils.showWarning("Begrensning", "Du må først opprette en oppgave");
        	else
        		Utils.showWarning("Begrensning", "Du må først velge en oppgave i listen");
        }

        hideDialogs();
    }

    private void hideDialogs()
    {
        for (DefaultDialog dialog : m_dialogs)
        {
            dialog.setVisible(false);
        }
    }

    public void setCurrentTask(ITaskIf task)
    {
        m_currentTask = task;
    }

	class TaskTickWork extends ModuleWork {

		/**
		 * Constructor
		 *
		 * @param task
		 */
		TaskTickWork() throws Exception {
			super("Sjekker oppgaver",false,false);
		}

		@Override
		public Boolean doWork() {
			try {

                ICmdPostIf cmdPost = getCmdPost();
                if (cmdPost == null)
                {
                    return false;
                }
                ITaskListIf tasks = cmdPost.getTaskList();

                Calendar currentTime = Calendar.getInstance();
                IDiskoRole role = getDiskoRole();

                boolean alert = false;
                boolean isAlertTime = false;
                boolean isAlertStatus = false;
                boolean isAlertRole = false;
                for (ITaskIf task : tasks.getItems())
                {
                    Calendar alertTime = task.getAlert();
                    isAlertTime = alertTime != null && alertTime.before(currentTime);
                    isAlertStatus = task.getStatus() != TaskStatus.FINISHED && task.getStatus() != TaskStatus.DELETED;
                    isAlertRole = task.getResponsibleRole() == null ||
                            task.getResponsibleRole().equals("") ||
                            task.getResponsibleRole().equals(role.getTitle());
                    if (isAlertTime && isAlertStatus && isAlertRole)
                    {
                        alert = true;
                        break;
                    }
                }
                return alert;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		public Boolean get() {
			return (Boolean)super.get();
		}

		@Override
		public void beforeDone() {

			try {
				// get current role
                IDiskoRole role = getDiskoRole();
                if(role!=null) {
	                List<IDiskoWpModule> modules = role.getDiskoWpModules();
	                int index = modules.indexOf(getDiskoWpTasks());
	                MainMenu mainMenu = getApplication().getUIFactory().getMainMenu();
	                AbstractButton button = mainMenu.getButton(role.getName(), index);
	                // set button icon color mask according to
	                // the alert flag from DiskWork result
	            	((DiskoIcon)button.getIcon()).setColored(get());
                }
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
