package org.redcross.sar.wp.tasks;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.gui.AbstractPopupHandler;
import org.redcross.sar.gui.PopupListener;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITaskIf.TaskPriority;
import org.redcross.sar.mso.data.ITaskIf.TaskStatus;
import org.redcross.sar.mso.data.ITaskListIf;
import org.redcross.sar.mso.data.TaskImpl;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Provides task table with data. Updates contents on MSO taks update. Also contains nested classes for
 * sorting and filtering.
 *
 * @author thomasl
 *
 */
@SuppressWarnings("unchecked")
public class TaskTableModel extends AbstractTableModel implements IMsoUpdateListenerIf
{
	private final static long serialVersionUID = 1L;

//	protected ITaskListIf m_tasks;
	protected IDiskoWpTasks m_wpTasks;

	protected JTable m_table;
	protected TaskTableRowSorter m_rowSorter;

	protected static EnumSet<TaskPriority> m_priorityFilter = EnumSet.of(
			TaskPriority.HIGH,
			TaskPriority.NORMAL,
			TaskPriority.LOW);

	protected static EnumSet<TaskStatus> m_statusFilter = EnumSet.of(
			TaskStatus.FINISHED,
			TaskStatus.UNPROCESSED,
			TaskStatus.POSTPONED,
			TaskStatus.STARTED);

	protected static Set<String> m_responsibleRoleFilter = new HashSet<String>();

	protected HashMap<JCheckBoxMenuItem, Enum> m_menuItemEnumMap = null;

	/**
	 * Compares task numbers
	 */
	private final static Comparator<Integer> m_numberComparator = new Comparator<Integer>()
	{
		public int compare(Integer o1, Integer o2)
		{
			return o1 - o2;
		}
	};

	/**
	 * Order priorities
	 */
	private final static Comparator<TaskPriority> m_priorityComparator = new Comparator<TaskPriority>()
	{
		public int compare(TaskPriority o1, TaskPriority o2)
		{
			return o1.ordinal() - o2.ordinal();
		}
	};

	private static final Comparator<Calendar> m_dueComparator = new Comparator<Calendar>()
	{
		public int compare(Calendar o1, Calendar o2)
		{
			return o1.compareTo(o2);
		}
	};

	private static final Comparator<TaskStatus> m_statusComparator = new Comparator<TaskStatus>()
	{
		public int compare(TaskStatus o1, TaskStatus o2)
		{
			return o1.ordinal() - o2.ordinal();
		}
	};

	public TaskTableModel(IDiskoWpTasks wp, JTable table)
	{
		m_wpTasks = wp;
		m_table = table;

		wp.getMsoEventManager().addClientUpdateListener(this);

		m_menuItemEnumMap = new HashMap<JCheckBoxMenuItem, Enum>();

		m_rowSorter = new TaskTableRowSorter(this);
		m_rowSorter.setSortsOnUpdates(true);
		m_table.setRowSorter(m_rowSorter);

		PopupListener listener = new PopupListener(new TaskTableModel.HeaderPopupHandler(this, m_table));
        JTableHeader tableHeader = m_table.getTableHeader();
        tableHeader.addMouseListener(listener);
	}

	public TaskTableRowSorter getRowSorter()
	{
		return m_rowSorter;
	}

	public int getColumnCount()
	{
		return 6;
	}

	public int getRowCount()
	{
		return m_wpTasks.getCmdPost().getTaskList().size();
	}

    @Override
    public String getColumnName(int column)
    {
    	return m_wpTasks.getBundleText("TableHeader" + column + ".text");
    }

	public Object getValueAt(int row, int column)
	{
		
		ITaskListIf taskList = m_wpTasks.getCmdPost().getTaskList();
		
		if(row >= taskList.size())
		{
			return null;
		}

		ITaskIf task = (ITaskIf)taskList.getItems().toArray()[row];
		switch(column)
		{
		case 0:
			return task.getNumber();
		case 1:
			return task.getPriority();
		case 2:
			return task.getTaskText();
		case 3:
			return task.getResponsibleRole();
		case 4:
			return task.getDueTime();
		case 5:
			return task.getStatus();
		}

		return null;
	}

	public void handleMsoUpdateEvent(Update e)
	{
		// Values has changed
		fireTableDataChanged();
	}

	private final EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of
	(
    		IMsoManagerIf.MsoClassCode.CLASSCODE_TASK
    );
	public boolean hasInterestIn(IMsoObjectIf msoObject)
	{
		return myInterests.contains(msoObject.getMsoClassCode());
	}

	/**
	 * Sorts and filters table
	 *
	 * @author thomasl
	 */
	public class TaskTableRowSorter extends TableRowSorter<TaskTableModel>
	{
		public TaskTableRowSorter(TaskTableModel model)
		{
			super(model);

			RowFilter<TaskTableModel, Object> rf = new RowFilter<TaskTableModel, Object>()
			{
				public boolean include(	javax.swing.RowFilter.Entry<? extends TaskTableModel,
						?  extends Object> entry)
				{
					TaskPriority priority = (TaskPriority)entry.getValue(1);
					String responsible = (String)entry.getValue(3);
					TaskStatus status = (TaskStatus)entry.getValue(5);

					return isRowSelected(priority, responsible, status);
				}
			};
			this.setRowFilter(rf);

			this.setComparator(0, m_numberComparator);
			this.sort();
		}

		@Override
		 public Comparator<?> getComparator(int column)
		 {
			switch (column)
			{
			case 0:
				return m_numberComparator;
			case 1:
				return m_priorityComparator;
			case 2:
				return null;
			case 3:
				return null;
			case 4:
				return m_dueComparator;
			case 5:
				return m_statusComparator;
			default:
				return m_numberComparator;
			}
		 }

		@Override
        public boolean useToString(int column)
        {
            return column == 2 || column == 3;
        }

		public boolean isRowSelected(TaskPriority priority, String responsible, TaskStatus status)
		{
			return m_priorityFilter.contains(priority) &&
			(m_responsibleRoleFilter.contains(responsible) || responsible == null || responsible.equals("")) &&
			m_statusFilter.contains(status);
		}
	}

	/**
	 * Pop-up menu for table header. Pop-up menus sets row sorter and task selection filters
	 *
	 * @author thomasl
	 */
	public class HeaderPopupHandler extends AbstractPopupHandler
    {
        private final TableColumnModel m_columnModel;
        private final JPopupMenu[] m_menus = new JPopupMenu[6];

        public HeaderPopupHandler(TaskTableModel model, JTable table)
        {
        	m_columnModel = table.getColumnModel();
        	m_rowSorter = getRowSorter();

        	// Priority pop-up menu
        	addMenuCheckBox(1, TaskPriority.HIGH, true);
        	addMenuCheckBox(1, TaskPriority.NORMAL, true);
        	addMenuCheckBox(1, TaskPriority.LOW, true);

        	// Responsible pop-up menu and selection filter
			try
			{
				m_menus[3] = new JPopupMenu();
				String[] roles = m_wpTasks.getApplication().getDiskoModuleLoader().getRoleTitles();
				IDiskoRole role = m_wpTasks.getDiskoRole();
	        	String roleName = role.getTitle();

	        	// Show own item
	        	JMenuItem showOwnItem = new JMenuItem();
	        	showOwnItem.setText(m_wpTasks.getBundleText("OwnTasksMenuItem.text"));
	        	showOwnItem.addActionListener(new ActionListener()
	        	{
					public void actionPerformed(ActionEvent arg0)
					{
						IDiskoRole currentRole = m_wpTasks.getDiskoRole();
						String name = currentRole.getTitle();
						m_responsibleRoleFilter.clear();
						m_responsibleRoleFilter.add(name);

						// Set other filters to unselected
						for(Component item : m_menus[3].getComponents())
						{
							if(item instanceof JCheckBoxMenuItem)
							{
								((JCheckBoxMenuItem)item).setSelected(false);
							}
						}

						fireTableDataChanged();
					}
	        	});
	        	m_menus[3].add(showOwnItem);


	        	// Handle responsible item selections, update filter
	        	ActionListener responsibleListener = new ActionListener()
	        	{
					public void actionPerformed(ActionEvent arg0)
					{
						JCheckBoxMenuItem item = (JCheckBoxMenuItem)arg0.getSource();
						String itemText = item.getText();
						if(m_responsibleRoleFilter.contains(itemText))
						{
							m_responsibleRoleFilter.remove(itemText);
						}
						else
						{
							m_responsibleRoleFilter.add(itemText);
						}

			        	fireTableDataChanged();
					}
	        	};

	        	for(int i=0; i<roles.length; i++)
	        	{
	        		// Don't add own role to selection filter, should always view own tasks
	        		if(!roleName.equals(roles[i]))
	        		{
	        			JCheckBoxMenuItem item = new JCheckBoxMenuItem();
	        			item.setText(roles[i]);
	        			item.addActionListener(responsibleListener);
	        			item.setSelected(true);
	        			m_menus[3].add(item);
	        		}
	        		m_responsibleRoleFilter.add(roles[i]);
	        	}

	        	// Show all item
	        	JMenuItem showAllItem = new JMenuItem();
	        	showAllItem.setText(m_wpTasks.getBundleText("AllRolesMenuItem.text"));
	        	showAllItem.addActionListener(new ActionListener()
	        	{
					@SuppressWarnings("null")
					public void actionPerformed(ActionEvent e)
					{
						// Select all roles
						for(Component item : m_menus[3].getComponents())
						{
							if(item instanceof JCheckBoxMenuItem)
							{
								((JCheckBoxMenuItem)item).setSelected(true);
							}
						}

						// Add all roles to set
						String[] allRoles = null;
						try
						{
							allRoles = m_wpTasks.getApplication().getDiskoModuleLoader().getRoleTitles();
						}
						catch (Exception e1)
						{
							e1.printStackTrace();
						}
						m_responsibleRoleFilter.clear();
						for(int i=0; i<allRoles.length; i++)
			        	{
			        		m_responsibleRoleFilter.add(allRoles[i]);
			        	}
						fireTableDataChanged();
					}
	        	});
	        	m_menus[3].add(showAllItem);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

        	// Status pop-up menu
        	addMenuCheckBox(5, TaskStatus.UNPROCESSED, true);
        	addMenuCheckBox(5, TaskStatus.STARTED, true);
        	addMenuCheckBox(5, TaskStatus.FINISHED, true);
        	addMenuCheckBox(5, TaskStatus.POSTPONED, true);
        	addMenuCheckBox(5, TaskStatus.DELETED, false);
        }

		private void addMenuCheckBox(final int menu, Enum taskEnum, boolean selected)
        {
        	if(m_menus[menu] == null)
        	{
        		 m_menus[menu] = new JPopupMenu();
        	}

        	JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem();
        	m_menuItemEnumMap.put(checkBox, taskEnum);
        	checkBox.setText(TaskImpl.getEnumText(taskEnum));
        	checkBox.addActionListener(new ActionListener()
        	{
				public void actionPerformed(ActionEvent arg0)
				{
					// Add/remove item from filter enum set
					JCheckBoxMenuItem item = (JCheckBoxMenuItem)arg0.getSource();
					Enum itemEnum = m_menuItemEnumMap.get(item);

					switch(menu)
					{
					case 1:
						TaskPriority priority = (TaskPriority)itemEnum;
						if(m_priorityFilter.contains(priority))
						{
							m_priorityFilter.remove(priority);
						}
						else
						{
							m_priorityFilter.add(priority);
						}
						break;
					case 5:
						TaskStatus status = (TaskStatus)itemEnum;
						if(m_statusFilter.contains(status))
						{
							m_statusFilter.remove(status);
						}
						else
						{
							m_statusFilter.add(status);
						}
					default:
					}
		        	fireTableDataChanged();
				}
        	});
        	checkBox.setSelected(selected);

        	m_menus[menu].add(checkBox);
        }

		protected JPopupMenu getMenu(MouseEvent e)
		{
			Point p = e.getPoint();
            int index = m_columnModel.getColumnIndexAtX(p.x);
            int realIndex = m_columnModel.getColumn(index).getModelIndex();
            return m_menus[realIndex];
		}
    }

	/**
	 * @param taskNr
	 * @return Reference to task with given number
	 */
	public ITaskIf getTask(int taskNr)
	{
		ITaskListIf tasks = m_wpTasks.getCmdPost().getTaskList();
		for(ITaskIf task : tasks.getItems())
		{
			if(taskNr == task.getNumber())
			{
				return task;
			}
		}
		return null;
	}
}
