package org.redcross.sar.wp.logistics;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.redcross.sar.gui.AbstractPopupHandler;
import org.redcross.sar.gui.PopupAdapter;
import org.redcross.sar.gui.dnd.DiskoDragSourceAdapter;
import org.redcross.sar.gui.dnd.DiskoDropTargetAdapter;
import org.redcross.sar.gui.dnd.IconDragGestureListener;
import org.redcross.sar.gui.dnd.TransferableMsoObject;
import org.redcross.sar.gui.model.AbstractMsoTableModel;
import org.redcross.sar.gui.renderer.ObjectIcon;
import org.redcross.sar.gui.renderer.ObjectIcon.AssignmentIcon;
import org.redcross.sar.gui.renderer.ObjectIcon.MsoIconActionHandler;
import org.redcross.sar.gui.renderer.ObjectIcon.UnitIcon;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.util.AssignmentUtilities;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.wp.IDiskoWpModule;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 12.apr.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 * Table model for unit table in logistics WP
 */
public class UnitTableModel extends AbstractMsoTableModel<IUnitIf>
{
	private static final long serialVersionUID = 1L;

	private static DataFlavor m_flavor = null;

	private final JTable m_table;

    private int m_selectedRow = -1;
    private int m_selectedCol = -1;
    private int m_dropRow = -1;
    private int m_dropCol = -1;
    private UnitTableRowSorter m_rowSorter;
    private EnumSet<IUnitIf.UnitType> m_unitTypeSelection;
    private IDiskoWpLogistics m_wpModule;
    private MsoIconActionHandler m_actionHandler;
    private boolean consume = false;

    /* ===========================================================
     * Constructors
     * ===========================================================*/

    /**
     * Creator
     *
     * @param aTable          The displayed table.
     * @param aWp             The related work process.
     * @param aUnitList       Reference to the list of units.
     * @param anActionHandler The handler of icon actions.
     */
    @SuppressWarnings("unchecked")
	public UnitTableModel(JTable aTable, IDiskoWpLogistics aWp, ObjectIcon.MsoIconActionHandler anActionHandler)
    {

    	// forward
    	super(IUnitIf.class, getNames("A",6),getCaptions(aWp, 6),getTooltips(aWp, 6),false);

		// initialize
    	m_table = aTable;
        m_wpModule = aWp;
        m_actionHandler = anActionHandler;
        m_unitTypeSelection = EnumSet.allOf(IUnitIf.UnitType.class);

        // install mso model
        connect(aWp.getMsoModel(), IUnitIf.ACTIVE_SELECTOR, IUnitIf.TYPE_AND_NUMBER_COMPARATOR);
        load(aWp.getMsoModel().getMsoManager().getCmdPost().getUnitList());

        /* -------------------------------------------------
         * Add co-class
         * -------------------------------------------------
         * This ensures that the table is updated every
         * time a assignment is changed
         * ------------------------------------------------- */
        getMsoBinder().addCoClass(IAssignmentIf.class,null);

        // initialize sorting
        setRowSorter();

        // initialize header
        JTableHeader header = m_table.getTableHeader();
        header.addMouseListener(new PopupAdapter(new UnitTableModel.HeaderPopupHandler(this, m_table)));
        setHeaderEditable(5, true);
        setHeaderEditor(5,"button");

    	// create gesture recognizer
    	DragSource ds = DragSource.getDefaultDragSource();
    	ds.createDefaultDragGestureRecognizer(m_table, DnDConstants.ACTION_MOVE,
    			new IconDragGestureListener(new UnitTableDragSourceListener()));

    	// create drop target
    	m_table.setDropTarget(new DropTarget(m_table, new UnitTableDropTargetListener()));

    	try {
	    	// create flavor
	    	m_flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=org.redcross.sar.mso.data.IAssignmentIf");
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}

    }

    /* ===========================================================
     * TableModel implementation
     * ===========================================================*/

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        if (columnIndex == 0)
        {
            return ObjectIcon.UnitIcon.class;
        } else if (columnIndex == 5)
        {
            return ObjectIcon.InfoIcon.class;
        } else
        {
            return ObjectIcon.AssignmentIcon.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return true;
    }

    /* ===========================================================
     * MsoTableModel implementation
     * ===========================================================*/

	@Override
	protected Object[] create(IUnitIf id, IUnitIf obj, int size) {
		/* -------------------------------------------
		 * create icons
		 * ------------------------------------------- */
		return createIcons(obj);
	}

	protected Object getCellValue(int row, String column) {
		// Since update algorithm is overridden, this method is never called.
		return null;
	}

	/**
	 * Update algorithm in MsoTableModel is overridden
	 */
	@Override
	protected Object[] update(IUnitIf id, IUnitIf obj, Object[] data) {
		// get row index
		int row = findRowFromId(id);
		if(row!=-1) {
			data = updateIcons(row, id,(Icon[])data);
		}
		return data;
	}

    /* ===========================================================
     * Public methods
     * ===========================================================*/

	public int getDrowRow() {
		return m_dropRow;
	}

	public int getDrowColumn() {
		return m_dropCol;
	}

    public UnitTableRowSorter getRowSorter()
    {
        return m_rowSorter;
    }

    public void setRowSorter()
    {
        m_rowSorter = new UnitTableRowSorter(this);
        m_rowSorter.setRowFilter(m_rowFilter);
        m_rowSorter.setSortsOnUpdates(true);
        m_table.setRowSorter(m_rowSorter);
        m_table.setUpdateSelectionOnSort(true);
    }

    public void scrollToTableCellPosition(int aRowNumber)
    {
        Rectangle rowRect = m_table.getCellRect(m_table.convertRowIndexToView(aRowNumber), 0, true);
        Rectangle visibleRect = m_table.getVisibleRect();
        if (!visibleRect.contains(rowRect))
        {
            int visibleHeight = visibleRect.height;

            if (rowRect.y > visibleHeight / 2)
            {
                rowRect.y = rowRect.y - visibleHeight / 2;
            } else
            {
                rowRect.y = 0;
            }
            rowRect.height = visibleRect.height;
            m_table.scrollRectToVisible(rowRect);
        }
    }

    public static IAssignmentIf.AssignmentStatus getSelectedAssignmentStatus(int aSelectorIndex)
    {
        switch (aSelectorIndex)
        {
            case 0:
                return IAssignmentIf.AssignmentStatus.QUEUED;
            case 1:
                return IAssignmentIf.AssignmentStatus.ALLOCATED;
            case 2:
                return IAssignmentIf.AssignmentStatus.EXECUTING;
            default:
                return IAssignmentIf.AssignmentStatus.FINISHED;
        }
    }

    public static Collection<IAssignmentIf> getSelectedAssignments1(IUnitIf aUnit, int aSelectorIndex)
    {
        switch (aSelectorIndex)
        {
            case 0:
                return aUnit.getEnqueuedAssignments();
            case 1:
                return aUnit.getAllocatedAssignments();
            case 2:
                return aUnit.getExecutingAssigments();
            default:
                return aUnit.getFinishedAssigments();
        }
    }

    public static String getSelectedAssignmentText(IDiskoWpModule aWpModule, int aSelectorIndex)
    {
        return aWpModule.getBundleText(MessageFormat.format("UnitTable_hdr_{0}.text", aSelectorIndex + 1));
    }

    public boolean setSelected(IMsoObjectIf msoObject, boolean isSelected)
    {
    	// loop over all cells
    	for(int i=0;i<getRowCount();i++) {
    		for(int j=0;j<getColumnCount();j++) {
    			ObjectIcon icon = (ObjectIcon)getValueAt(i, j);
    			if(icon instanceof UnitIcon) {
    				UnitIcon unitIcon = (UnitIcon)icon;
    				if(unitIcon.getMsoObject()==msoObject) {
    					setSelection(i,j);
        				return true;
    				}
    			}
    			else if(icon instanceof AssignmentIcon) {
    				AssignmentIcon assignmentIcon = (AssignmentIcon)icon;
    				if(assignmentIcon.isSingleAssigmentIcon() && assignmentIcon.getMsoObject()==msoObject) {
    					setSelection(i,j);
    					return true;
    				}
    				if(assignmentIcon.getAssignments().contains(msoObject)) {
    					setSelection(i,j);
        				return true;
    				}
    			}
    		}
    	}
    	return false;
    }

    public void setSelectedCell(int aRow, int aColumn, boolean isSelected)
    {
        if (consume || aRow < 0 || aRow >= getRowCount() || aColumn < 0 || aColumn >= getColumnCount())
        {
            return;
        }
        try
        {
            // convert?
        	int row = isSelected ? m_table.convertRowIndexToModel(aRow) : -1;
            int col = isSelected ? m_table.convertColumnIndexToModel(aColumn) : -1;
            // any change?
            if(m_selectedRow != row || m_selectedCol != col)  {
                m_selectedRow = row;
                m_selectedCol = col;
                if(isSelected) {
	                Object value = getValueAt(m_selectedRow, m_selectedCol);
	                if (value instanceof ObjectIcon)
	                {
	                    ((ObjectIcon) value).iconSelected();
	                }
                }
            }
        }
        catch (IndexOutOfBoundsException e)
        {
        	e.printStackTrace();
        }
    }

    public EnumSet<IUnitIf.UnitType> getUnitTypeSelection()
    {
        return m_unitTypeSelection;
    }

    public void setTypeSelections(String aType, boolean aFlag)
    {
        IUnitIf.UnitType t = IUnitIf.UnitType.valueOf(aType);
        if (t != null)
        {
            setTypeSelections(t, aFlag);
        }
    }

    public void setTypeSelections(IUnitIf.UnitType aType, boolean aFlag)
    {
        if (aFlag)
        {
            m_unitTypeSelection.add(aType);
        } else
        {
            m_unitTypeSelection.remove(aType);
        }
        m_rowSorter.sort();
    }

    public void setTypeSelections(IUnitIf.UnitType[] theTypes, boolean aFlag)
    {
        for (IUnitIf.UnitType t : theTypes)
        {
            if (aFlag)
            {
                m_unitTypeSelection.add(t);
            } else
            {
                m_unitTypeSelection.remove(t);
            }
        }
        m_rowSorter.sort();
    }

    public IUnitIf getUnitAt(int aRow)
    {
        return getId(aRow);
    }

    /* ===========================================================
     * Private methods
     * ===========================================================*/

    private static String[] getNames(String prefix, int count) {
    	String[] names = new String[count];
    	for(int i=0;i<count;i++) {
    		names[i] = prefix + i;
    	}
    	return names;
    }

    private static String[] getCaptions(IDiskoWpLogistics wp, int count) {
    	String[] captions = new String[count];
    	for(int i=0;i<count;i++) {
    		captions[i] = wp.getBundleText(MessageFormat.format("UnitTable_hdr_{0}.text", i));
    	}
    	return captions;
    }

    private static String[] getTooltips(IDiskoWpLogistics wp, int count) {
    	String[] captions = new String[count];
    	for(int i=0;i<count;i++) {
    		captions[i] = wp.getBundleText(MessageFormat.format("UnitTable_hdr_{0}.tooltip", i));
    	}
    	return captions;
    }

    private Icon[] createIcons(IUnitIf aUnit)
    {
        Icon[] retVal = new Icon[6];
        retVal[0] = createUnitIcon(aUnit);
        retVal[1] = createAssignmentIcon(aUnit, 0);
        retVal[2] = createAssignmentIcon(aUnit, 1);
        retVal[3] = createAssignmentIcon(aUnit, 2);
        retVal[4] = createAssignmentIcon(aUnit, 3);
        retVal[5] = createInfoIcon(aUnit.getRemarks());  // todo getInfo
        return retVal;
    }

    private Icon[] updateIcons(int i, IUnitIf aUnit,Icon[] icons)
    {
        ((ObjectIcon.UnitIcon) icons[0]).setMsoObject(aUnit);
        ((ObjectIcon.AssignmentIcon) icons[1]).setAssignments(aUnit, 0);
        ((ObjectIcon.AssignmentIcon) icons[2]).setAssignments(aUnit, 1);
        ((ObjectIcon.AssignmentIcon) icons[3]).setAssignments(aUnit, 2);
        ((ObjectIcon.AssignmentIcon) icons[4]).setAssignments(aUnit, 3);
        ((ObjectIcon.InfoIcon) icons[5]).setInfo(aUnit.getRemarks());
        return icons;
    }

    private ObjectIcon.UnitIcon createUnitIcon(IUnitIf aUnit)
    {
        return new ObjectIcon.UnitIcon(aUnit, m_actionHandler, false);
    }

    private ObjectIcon.AssignmentIcon createAssignmentIcon(IUnitIf aUnit, int aSelectorIndex)
    {
        return new ObjectIcon.AssignmentIcon(aUnit, m_actionHandler, aSelectorIndex, false);
    }

    private ObjectIcon.InfoIcon createInfoIcon(String anInfo)
    {
        return new ObjectIcon.InfoIcon(anInfo, false);
    }

    private void setSelection(int row, int col) {
		consume = true;
    	m_table.getSelectionModel().setValueIsAdjusting(true);
		m_table.getSelectionModel().setSelectionInterval(row, row);
		m_table.getColumnModel().getSelectionModel().setSelectionInterval(col, col);
    	m_table.getSelectionModel().setValueIsAdjusting(false);
		consume = false;
		setSelectedCell(row,col,true);
    }

    /* ===========================================================
     * Anonymous classes
     * ===========================================================*/

    private final RowFilter<UnitTableModel,Integer> m_rowFilter = new RowFilter<UnitTableModel,Integer>() {
    	public boolean include(Entry<? extends UnitTableModel, ? extends Integer> entry) {
    		UnitTableModel model = entry.getModel();
    		IUnitIf unit = model.getId(entry.getIdentifier());
            return (IUnitIf.ACTIVE_SET.contains(unit.getStatus()) &&
                    m_unitTypeSelection.contains(unit.getType()));
    	}
    };

    public static final Comparator<ObjectIcon.AssignmentIcon> LIST_LENGTH_COMPARATOR = new Comparator<ObjectIcon.AssignmentIcon>()
    {
        public int compare(ObjectIcon.AssignmentIcon o1, ObjectIcon.AssignmentIcon o2)
        {
            int l1 = o1.getAssignments() != null ? o1.getAssignments().size() : 0;
            int l2 = o2.getAssignments() != null ? o2.getAssignments().size() : 0;
            return l1 - l2;
        }
    };

    public static final Comparator<ObjectIcon.AssignmentIcon> PRIORITY_COMPARATOR = new Comparator<ObjectIcon.AssignmentIcon>()
    {
        public int compare(ObjectIcon.AssignmentIcon o1, ObjectIcon.AssignmentIcon o2)
        {
            IAssignmentIf.AssignmentPriority p1 = getHighestPriority(o1.getAssignments());
            IAssignmentIf.AssignmentPriority p2 = getHighestPriority(o2.getAssignments());
            return p1.compareTo(p2);
        }

        private IAssignmentIf.AssignmentPriority getHighestPriority(Collection<IAssignmentIf> theAssignments)
        {
            IAssignmentIf.AssignmentPriority retVal = IAssignmentIf.AssignmentPriority.NONE;
            if (theAssignments != null)
            {
                for (IAssignmentIf asg : theAssignments)
                {
                    if (asg.getPriority().ordinal() < retVal.ordinal())
                    {
                        retVal = asg.getPriority();
                    }
                }
            }
            return retVal;
        }
    };

    public abstract static class TimeComparator implements Comparator<ObjectIcon.AssignmentIcon>
    {
        public int compare(ObjectIcon.AssignmentIcon o1, ObjectIcon.AssignmentIcon o2)
        {
            Calendar c1 = getCompareTime(o1.getAssignments());
            Calendar c2 = getCompareTime(o2.getAssignments());
            if (c1 != null & c2 != null)
            {
                return c1.compareTo(c2);
            } else if (c1 == null && c1 == null)
            {
                return 0;
            } else if (c1 == null)
            {
                return -1;
            } else
            {
                return 1;
            }
        }

        protected abstract Calendar getCompareTime(Collection<IAssignmentIf> aCollection);
    }

    public static final TimeComparator ALLOCATED_TIME_COMPARATOR = new TimeComparator()
    {
        protected Calendar getCompareTime(Collection<IAssignmentIf> aCollection)
        {
            if (aCollection.size() > 0)
            {
                return aCollection.iterator().next().getTime(AssignmentStatus.ALLOCATED);
            }
            return null;
        }
    };

    public static final TimeComparator START_TIME_COMPARATOR = new TimeComparator()
    {
        protected Calendar getCompareTime(Collection<IAssignmentIf> aCollection)
        {
            if (aCollection.size() > 0)
            {
                return aCollection.iterator().next().getTime(AssignmentStatus.EXECUTING);
            }
            return null;
        }
    };

    public static final TimeComparator ETA_TIME_COMPARATOR = new TimeComparator()
    {
        protected Calendar getCompareTime(Collection<IAssignmentIf> aCollection)
        {
            if (aCollection.size() > 0)
            {
                return aCollection.iterator().next().getTimeEstimatedFinished();
            }
            return null;
        }
    };

    public static final Comparator<ObjectIcon.UnitIcon> UNIT_TYPE_AND_NUMBER_COMPARATOR = new Comparator<ObjectIcon.UnitIcon>()
    {
        public int compare(ObjectIcon.UnitIcon o1, ObjectIcon.UnitIcon o2)
        {
            return IUnitIf.TYPE_AND_NUMBER_COMPARATOR.compare(o1.getMsoObject(), o2.getMsoObject());
        }
    };

    public static final Comparator<ObjectIcon.UnitIcon> UNIT_SPEED_COMPARATOR = new Comparator<ObjectIcon.UnitIcon>()
    {
        public int compare(ObjectIcon.UnitIcon o1, ObjectIcon.UnitIcon o2)
        {
            return (int)(o1.getMsoObject().getSpeed() - o2.getMsoObject().getSpeed());
        }
    };

    public static final Comparator<ObjectIcon.UnitIcon> UNIT_PAUSE_COMPARATOR = new Comparator<ObjectIcon.UnitIcon>()
    {
        public int compare(ObjectIcon.UnitIcon o1, ObjectIcon.UnitIcon o2)
        {
            return Double.valueOf(o1.getMsoObject().getDuration(UnitStatus.PAUSED,true))
            			.compareTo(Double.valueOf(o2.getMsoObject().getDuration(UnitStatus.PAUSED,true)));
        }
    };

    public static final Comparator<ObjectIcon.UnitIcon> UNIT_WORKTIME_COMPARATOR = new Comparator<ObjectIcon.UnitIcon>()
    {
        public int compare(ObjectIcon.UnitIcon o1, ObjectIcon.UnitIcon o2)
        {
            return Double.valueOf(o1.getMsoObject().getDuration(UnitStatus.WORKING,true))
						.compareTo(Double.valueOf(o2.getMsoObject().getDuration(UnitStatus.WORKING,true)));
        }
    };

    public static final Comparator<ObjectIcon.UnitIcon> UNIT_IDLETIME_COMPARATOR = new Comparator<ObjectIcon.UnitIcon>()
    {
        public int compare(ObjectIcon.UnitIcon o1, ObjectIcon.UnitIcon o2)
        {
            return Double.valueOf(o1.getMsoObject().getDuration(UnitStatus.READY,true))
						.compareTo(Double.valueOf(o2.getMsoObject().getDuration(UnitStatus.READY,true)));
        }
    };

    /* ===========================================================
     * Inner classes
     * ===========================================================*/

    public static class UnitTableRowSorter extends TableRowSorter<UnitTableModel>
    {
        int[] m_sortKeys = new int[]{0, 1, 2, 2, 2, 0}; // default initial values

        public UnitTableRowSorter(UnitTableModel aModel)
        {
            super(aModel);
        }

        @Override
        public void setMaxSortKeys(int count) {
        	// forward
            super.setMaxSortKeys(count);
        }

        public void clearSort() {
        	super.setSortKeys(null);
        }

        public Comparator<?> getComparator(int column)
        {
            switch (column)
            {
                case 0:
                    switch (m_sortKeys[column])
                    {
                        case 2:
                            return UNIT_SPEED_COMPARATOR;
                        case 3:
                            return UNIT_PAUSE_COMPARATOR;
                        case 4:
                            return UNIT_WORKTIME_COMPARATOR;
                        case 5:
                            return UNIT_IDLETIME_COMPARATOR;
                        default:
                            return UNIT_TYPE_AND_NUMBER_COMPARATOR;
                    }
                case 1:
                case 2:
                case 3:
                case 4:
                    switch (m_sortKeys[column])
                    {
                        case 1:
                            return LIST_LENGTH_COMPARATOR;
                        case 3:
                            return ALLOCATED_TIME_COMPARATOR;
                        case 4:
                            return START_TIME_COMPARATOR;
                        case 5:
                            return ETA_TIME_COMPARATOR;
                        default:
                            return PRIORITY_COMPARATOR;
                    }
                default:
                    return null;
            }
        }

        @Override
        public boolean useToString(int column)
        {
            return column == 5;
        }

        public void setSortKey(int aColumn, int aKeyIndex)
        {
            m_sortKeys[aColumn] = aKeyIndex;
            List<? extends RowSorter.SortKey> keyList = getSortKeys();
            boolean changeColumn = true;
            boolean sortAscending = true;
            if (keyList.size() > 0)
            {
                RowSorter.SortKey key = keyList.get(0);
                changeColumn = key.getColumn() != aColumn;
                sortAscending = key.getSortOrder() == SortOrder.ASCENDING;
            }
            if (changeColumn)
            {
                toggleSortOrder(aColumn);
                if (!sortAscending)
                {
                    toggleSortOrder(aColumn);
                }
            }
            fireSortOrderChanged();
        }

        public int getFirstSortColumn()
        {
            List<? extends RowSorter.SortKey> keyList = getSortKeys();
            if (keyList.size() > 0)
            {
                RowSorter.SortKey key = keyList.get(0);
                return key.getColumn();
            }
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
	public class HeaderPopupHandler extends AbstractPopupHandler implements ActionListener
    {
        private final TableColumnModel m_columnModel;
        private final JPopupMenu[] m_menus = new JPopupMenu[6];
        private final Vector<JMenuItem>[] m_buttons = new Vector[6];
        private final ButtonGroup[] m_buttonGroups = new ButtonGroup[6];
        private final LinkedHashMap<String, JMenuItem> m_unitSelections = new LinkedHashMap<String, JMenuItem>();
        private JMenuItem m_selectAll;
        private JMenuItem m_deselectAll;
        private JSeparator m_menuSeparator;

        public HeaderPopupHandler(UnitTableModel aModel, JTable aTable)
        {
            m_columnModel = aTable.getColumnModel();

            createUnitSelectionBoxes();
            m_menuSeparator = new JSeparator();

            int column;

            column = 0;
            setupColumn(column);
            addButton(buttonWithAction("UnitTable_menu_unitType.text", column, 1), column);
            addButton(buttonWithAction("UnitTable_menu_speed.text", column, 2), column);
            addButton(buttonWithAction("UnitTable_menu_pauseTime.text", column, 3), column);
            addButton(buttonWithAction("UnitTable_menu_workTime.text", column, 4), column);
            addButton(buttonWithAction("UnitTable_menu_idleTime.text", column, 5), column);
            m_buttons[column].get(0).setSelected(true);
            m_menus[column].add(new JSeparator());

            column++; // 1
            setupColumn(column);
            addButton(buttonWithAction("UnitTable_menu_qty.text", column, 1), column);
            addButton(buttonWithAction("UnitTable_menu_priority.text", column, 2), column);
            m_buttons[column].get(0).setSelected(true);
            m_menus[column].add(new JSeparator());

            column++; // 2
            setupColumn(column);
            addButton(buttonWithAction("UnitTable_menu_priority.text", column, 2), column);
            addButton(buttonWithAction("UnitTable_menu_Allocatedtime.text", column, 3), column);
            m_buttons[column].get(0).setSelected(true);
            m_menus[column].add(new JSeparator());

            column++; // 3
            setupColumn(column);
            addButton(buttonWithAction("UnitTable_menu_priority.text", column, 2), column);
            addButton(buttonWithAction("UnitTable_menu_starttime.text", column, 4), column);
            addButton(buttonWithAction("UnitTable_menu_endtime.text", column, 5), column);
            m_buttons[column].get(0).setSelected(true);
            m_menus[column].add(new JSeparator());

            column++; // 4
            setupColumn(column);
            addButton(buttonWithAction("UnitTable_menu_qty.text", column, 1), column);
            addButton(buttonWithAction("UnitTable_menu_priority.text", column, 2), column);
            m_buttons[column].get(0).setSelected(true);
            m_menus[column].add(new JSeparator());

            column++;  // 5
            setupColumn(column);
        }

        private void setupColumn(int column)
        {
            m_menus[column] = new JPopupMenu();
            m_buttons[column] = new Vector<JMenuItem>();
            m_buttonGroups[column] = new ButtonGroup();
        }

        private void addButton(JRadioButtonMenuItem aButton, int aColumn)
        {
            m_menus[aColumn].add(aButton);
            m_buttons[aColumn].add(aButton);
            m_buttonGroups[aColumn].add(aButton);
        }

        private JRadioButtonMenuItem buttonWithAction(String aText, final int aColumn, final int aKeyIndex)
        {
            String labelText = m_wpModule.getBundleText(aText);
            AbstractAction action = new AbstractAction(labelText)
            {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e)
                {
                    getRowSorter().setSortKey(aColumn, aKeyIndex);
                }
            };
            return new JRadioButtonMenuItem(action);
        }


        private void createUnitSelectionBoxes()
        {
            for (IUnitIf.UnitType t : IUnitIf.UnitType.values())
            {
                if (t.equals(IUnitIf.UnitType.CP))
                {
                    continue;
                }
                String aText = Internationalization.translate(t);
                String aCommand = t.name();

                JMenuItem c = createMenuItem(true, aText, aCommand);
                c.setSelected(true);
                m_unitSelections.put(aCommand, c);
            }

            m_selectAll = createMenuItem(false, m_wpModule.getBundleText("UnitTable_menu_selectAll.text"), "SelectAll");
            m_deselectAll = createMenuItem(false, m_wpModule.getBundleText("UnitTable_menu_deselectAll.text"), "DeselectAll");
        }

        private JMenuItem createMenuItem(boolean makeCheckBox, String aText, String aCommand)
        {
            JMenuItem c = makeCheckBox ? new JCheckBoxMenuItem(aText) : new JMenuItem(aText);
            c.setActionCommand(aCommand);
            c.addActionListener(this);
            return c;
        }

        private void addSelectionBoxes(int aColumn)
        {
            for (JMenuItem c : m_unitSelections.values())
            {
                m_menus[aColumn].add(c);
            }
            m_menus[aColumn].add(m_menuSeparator);
            m_menus[aColumn].add(m_selectAll);
            m_menus[aColumn].add(m_deselectAll);
        }

        public JPopupMenu getMenu(MouseEvent e)
        {
            Point p = e.getPoint();
            int index = m_columnModel.getColumnIndexAtX(p.x);
            int realIndex = m_columnModel.getColumn(index).getModelIndex();
            if (m_buttons[realIndex].size() >= 0)
            {
                addSelectionBoxes(realIndex);
                return m_menus[realIndex];
            }
            return null;
        }


        public void actionPerformed(ActionEvent e)
        {
            String command = e.getActionCommand();
            JMenuItem c = (JMenuItem) e.getSource();
            if ("SelectAll".equals(command))
            {
                selectAll(true);
            } else if ("DeselectAll".equals(command))
            {
                selectAll(false);
            } else if (c != null)
            {
                setTypeSelections(command, c.isSelected());
            }
        }

        private void selectAll(boolean aFlag)
        {
            for (JMenuItem m : m_unitSelections.values())
            {
                m.setSelected(aFlag);
            }
            setTypeSelections(IUnitIf.UnitType.values(), aFlag);
        }
    }

	class UnitTableDragSourceListener extends DiskoDragSourceAdapter {

		@Override
		public Component getComponent() {
			return m_table;
		}

		@Override
		public Transferable getTransferable() {
	        int row = m_table.getSelectedRow();
	        int col = m_table.getSelectedColumn();
	        // anything selected?
	        if (row <getRowCount() && row>=0 && col<getColumnCount() && col>=0){
	        	// get value
	            Object value = getValueAt(row, col);
	            // check value
	            if (value instanceof AssignmentIcon){
	            	// get icon
	            	AssignmentIcon icon = (AssignmentIcon)value;
	            	// return new object?
	            	if(icon.getMsoObject()!=null) {
	            		// return data
	            		return new TransferableMsoObject<IAssignmentIf>(icon.getMsoObject());
	            	}
	            }

	        }
	        // nothing to drag
	    	return null;
		}

		@Override
		public Icon getIcon() {
	        int row = m_table.getSelectedRow();
	        int col = m_table.getSelectedColumn();
	        // anything selected?
	        if (row <getRowCount() && row>=0 && col<getColumnCount() && col>=0){
	        	// get value
	            Object value = getValueAt(row, col);
	            // check value
	            if (value instanceof AssignmentIcon){
	            	// return icon
	            	return (AssignmentIcon)value;
	            }
	        }
	        // nothing to drag
	    	return null;
		}

	}

	class UnitTableDropTargetListener extends DiskoDropTargetAdapter {

		@Override
		public void dragOver(DropTargetDragEvent e) {
			// forward
			dragEnter(e);
		}

		@Override
		public void dragEnter(DropTargetDragEvent e) {
			// always allow
			return;
		}

		@Override
		public void drop(DropTargetDropEvent e) {
			// is supported?
			if (e.isDataFlavorSupported(m_flavor)) {
				// get row and col from cursor location
				Point p =  e.getLocation();
	            TableColumnModel columnModel = m_table.getColumnModel();
	            int col = columnModel.getColumnIndexAtX((int)p.getX());
	            int row = (int)p.getY() / m_table.getRowHeight(0);
	            // anything selected?
	            if (row <getRowCount() && row>=0 && col<getColumnCount() && col>=0){
	            	// get value
	                Object value = getValueAt(row, col);
	                // check value
	                if (value instanceof AssignmentIcon){
	                	// try to transfer data to source
	                	if(transfer(e.getTransferable(),row,col)) {
	                		// success!
		                	e.acceptDrop(DnDConstants.ACTION_MOVE);
		        			e.dropComplete(true);
		                	return;
	                	}
	                }
	            }

			}
			// reset
			m_dropRow = -1;
			m_dropCol = -1;
			// reject request
			e.rejectDrop();
		}

		private boolean transfer(Transferable data, int dropRow, int dropCol) {
			// get data
			try{
            	// validate transfer
            	if(canTransfer(data,dropRow,dropCol)) {
    				// get assignment
    				IAssignmentIf assignment = (IAssignmentIf)data.getTransferData(m_flavor);
        			// update
        			m_dropRow = dropRow;
        			m_dropCol = dropCol;
    				// do the transfer
    		        return m_wpModule.transfer(assignment,
    		        		getSelectedAssignmentStatus(dropCol - 1), getUnitAt(dropRow));
                }
			}
			catch(UnsupportedFlavorException e) {
				Utils.showWarning("Mottatt objekt er ikke et oppdrag");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// can not transfer
			return false;
		}

		private boolean canTransfer(Transferable data, int dropRow, int dropCol) {

			// get data
			try{

				// valid row?
		        if (!(dropCol == 0 || dropCol == 5))
		        {
					// try to get assignment
					IAssignmentIf assignment = (IAssignmentIf)data.getTransferData(m_flavor);

					// valid assignment?
					if(assignment!=null) {

			        	// get unit
				        IUnitIf unit = getUnitAt(dropRow);

				        // get assignment status
				        AssignmentStatus newStatus = UnitTableModel.getSelectedAssignmentStatus(dropCol - 1);

						// validate
				    	Object[] ans = AssignmentUtilities.verifyMove(assignment, unit, newStatus);

				    	// get action
				    	int action = Integer.valueOf(ans[0].toString());

				    	// can move to status?
				        if (action>=0)
				        {
				        	// only change should result in a transfer
				        	return action==0 ? false : true;
				        }


						// notify reason
						Utils.showWarning(ans[1].toString());

					}
		        }
		        else
		        	Utils.showWarning("Du kan ikke flytte oppdrag hit");
			}
			catch(UnsupportedFlavorException e) {
				Utils.showWarning("Mottatt objekt er ikke et oppdrag");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// can not transfer
			return false;
		}
	}

}
