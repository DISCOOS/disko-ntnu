package org.redcross.sar.wp.logistics;

import org.redcross.sar.gui.AbstractPopupHandler;
import org.redcross.sar.gui.PopupAdapter;
import org.redcross.sar.gui.dnd.AssignmentTransferable;
import org.redcross.sar.gui.dnd.DiskoDragSourceAdapter;
import org.redcross.sar.gui.dnd.DiskoDropTargetAdapter;
import org.redcross.sar.gui.dnd.IconDragGestureListener;
import org.redcross.sar.gui.model.DiskoTableModel;
import org.redcross.sar.gui.renderer.IconRenderer;
import org.redcross.sar.gui.renderer.IconRenderer.AssignmentIcon;
import org.redcross.sar.gui.renderer.IconRenderer.IconActionHandler;
import org.redcross.sar.gui.renderer.IconRenderer.UnitIcon;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitListIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.util.AssignmentTransferUtilities;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.Selector;
import org.redcross.sar.wp.IDiskoWpModule;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTable;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 12.apr.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 * Table model for unit table in logistics WP
 */
public class UnitTableModel extends DiskoTableModel implements IMsoUpdateListenerIf
{
	private static final long serialVersionUID = 1L;
	
	private static DataFlavor m_flavor = null;
	
	private final JTable m_table;
	
    private IMsoEventManagerIf m_eventManager;
    private IUnitListIf m_unitList;
    private ArrayList<Icon[]> m_iconRows = new ArrayList<Icon[]>();
    private int m_actualUnitCount;
    private int m_selectedRow = -1;
    private int m_selectedCol = -1;
    private int m_dropRow = -1;
    private int m_dropCol = -1;
    private UnitTableRowSorter m_rowSorter;
    private EnumSet<IUnitIf.UnitType> m_unitTypeSelection;
    private IDiskoWpLogistics m_wpModule;
    private IconActionHandler m_actionHandler;
    private boolean consume = false;
    
    private final EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT, IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT);

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
    public UnitTableModel(JTable aTable, IDiskoWpLogistics aWp, 
    		IUnitListIf aUnitList, IconRenderer.IconActionHandler anActionHandler)
    {

    	// forward
    	super(getNames("A",6),getCaptions(aWp, 6),getTooltips(aWp, 6));
    	
		// initialize
    	m_table = aTable;
        m_wpModule = aWp;
        m_eventManager = aWp.getMsoEventManager();
        m_eventManager.addClientUpdateListener(this);
        m_unitList = aUnitList;
        m_actionHandler = anActionHandler;
        m_actualUnitCount = 0;
        m_unitTypeSelection = EnumSet.allOf(IUnitIf.UnitType.class);

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
    public String getColumnName(int column)
    {
        return m_wpModule.getBundleText(MessageFormat.format("UnitTable_hdr_{0}.text", column));
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        if (columnIndex == 0)
        {
            return IconRenderer.UnitIcon.class;
        } else if (columnIndex == 5)
        {
            return IconRenderer.InfoIcon.class;
        } else
        {
            return IconRenderer.AssignmentIcon.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        return true;
    }    
    
    @Override
    public int getRowCount()
    {
        return m_actualUnitCount;
    }
    
    @Override
    public int getColumnCount()
    {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (rowIndex < 0 || rowIndex >= m_iconRows.size() || columnIndex < 0 || columnIndex > 5)
        {
            return null;
        }
        Icon[] buttons = m_iconRows.get(rowIndex);
        return buttons[columnIndex];
    }    
    
    /* ===========================================================
     * Public methods
     * ===========================================================*/
    
    public UnitTableRowSorter getRowSorter()
    {
        return m_rowSorter;
    }

    public void setRowSorter()
    {
        m_rowSorter = new UnitTableRowSorter(this);
        m_rowSorter.setMaxSortKeys(1);
        m_table.setRowSorter(m_rowSorter);
    }
    
    public void handleMsoUpdateEvent(MsoEvent.Update e)
    {
    	if(e.isClearAllEvent()) {
    		m_iconRows.clear();
    	}
    	else {
	        buildTable();
    	}
        fireTableDataChanged();
    }

	public boolean hasInterestIn(IMsoObjectIf aMsoObject, UpdateMode mode) 
	{
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
        return myInterests.contains(aMsoObject.getMsoClassCode());
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

    public static Collection<IAssignmentIf> getSelectedAssignments(IUnitIf aUnit, int aSelectorIndex)
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
    			IconRenderer icon = (IconRenderer)getValueAt(i, j);
    			if(icon instanceof UnitIcon) {
    				UnitIcon unitIcon = (UnitIcon)icon;
    				if(unitIcon.getUnit()==msoObject) {
    					setSelection(i,j);
        				return true;
    				}
    			}
    			else if(icon instanceof AssignmentIcon) {
    				AssignmentIcon assignmentIcon = (AssignmentIcon)icon;
    				if(assignmentIcon.isSingleAssigmentIcon() && assignmentIcon.getAssignment()==msoObject) {
    					setSelection(i,j);    					
    					return true;
    				}
    				if(assignmentIcon.getAssignmentList().contains(msoObject)) {
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
	                if (value instanceof IconRenderer)
	                {
	                    ((IconRenderer) value).iconSelected();
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
        buildTable();
        fireTableDataChanged();
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
        buildTable();
        fireTableDataChanged();
    }

    public IUnitIf getUnitAt(int aRow)
    {
        return ((IconRenderer.UnitIcon) m_iconRows.get(aRow)[0]).getUnit();
    }    
    
    /* ===========================================================
     * Protected methods
     * ===========================================================*/
    
    protected void buildTable()
    {
        m_actualUnitCount = 0;
        for (IUnitIf unit : m_unitList.selectItems(m_unitSelector, IUnitIf.UNIT_TYPE_AND_NUMBER_COMPARATOR))
        {
            m_actualUnitCount++;
            if (m_iconRows.size() < m_actualUnitCount)
            {
                m_iconRows.add(newIconRow(unit));
            } else
            {
                assignIconRow(m_actualUnitCount - 1, unit);
            }
        }
    }

    protected void reInitModel(IUnitListIf aUnitList)
    {
        m_unitList = aUnitList;
        buildTable();
        fireTableDataChanged();
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
    
    private final Selector<IUnitIf> m_unitSelector = new Selector<IUnitIf>()
    {
        public boolean select(IUnitIf aUnit)
        {
            return (IUnitIf.ACTIVE_RANGE.contains(aUnit.getStatus()) &&
                    m_unitTypeSelection.contains(aUnit.getType()));
        }
    };

    private Icon[] newIconRow(IUnitIf aUnit)
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

    private void assignIconRow(int i, IUnitIf aUnit)
    {
        Icon[] icons = m_iconRows.get(i);
        ((IconRenderer.UnitIcon) icons[0]).setUnit(aUnit);
        ((IconRenderer.AssignmentIcon) icons[1]).setAssignments(aUnit, 0);
        ((IconRenderer.AssignmentIcon) icons[2]).setAssignments(aUnit, 1);
        ((IconRenderer.AssignmentIcon) icons[3]).setAssignments(aUnit, 2);
        ((IconRenderer.AssignmentIcon) icons[4]).setAssignments(aUnit, 3);
        ((IconRenderer.InfoIcon) icons[5]).setInfo(aUnit.getRemarks());  // todo getInfo
    }

    private IconRenderer.UnitIcon createUnitIcon(IUnitIf aUnit)
    {
        return new IconRenderer.UnitIcon(aUnit, false, m_actionHandler);
    }

    private IconRenderer.AssignmentIcon createAssignmentIcon(IUnitIf aUnit, int aSelectorIndex)
    {
        return new IconRenderer.AssignmentIcon(aUnit, aSelectorIndex, false, m_actionHandler);
    }

    private IconRenderer.InfoIcon createInfoIcon(String anInfo)
    {
        return new IconRenderer.InfoIcon(anInfo, false);
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
    
    public static final Comparator<IconRenderer.AssignmentIcon> ListLengthComparator = new Comparator<IconRenderer.AssignmentIcon>()
    {
        public int compare(IconRenderer.AssignmentIcon o1, IconRenderer.AssignmentIcon o2)
        {
            int l1 = o1.getAssignmentList() != null ? o1.getAssignmentList().size() : 0;
            int l2 = o2.getAssignmentList() != null ? o2.getAssignmentList().size() : 0;
            return l1 - l2;
        }
    };

    public static final Comparator<IconRenderer.AssignmentIcon> PriorityComparator = new Comparator<IconRenderer.AssignmentIcon>()
    {
        public int compare(IconRenderer.AssignmentIcon o1, IconRenderer.AssignmentIcon o2)
        {
            IAssignmentIf.AssignmentPriority p1 = getHighestPriority(o1.getAssignmentList());
            IAssignmentIf.AssignmentPriority p2 = getHighestPriority(o2.getAssignmentList());
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

    public abstract static class TimeComparator implements Comparator<IconRenderer.AssignmentIcon>
    {
        public int compare(IconRenderer.AssignmentIcon o1, IconRenderer.AssignmentIcon o2)
        {
            Calendar c1 = getCompareTime(o1.getAssignmentList());
            Calendar c2 = getCompareTime(o2.getAssignmentList());
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

    public static final TimeComparator AssignmentTimeComparator = new TimeComparator()
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

    public static final TimeComparator StartTimeComparator = new TimeComparator()
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

    public static final TimeComparator EstimatedEndTimeComparator = new TimeComparator()
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

    public static final Comparator<IconRenderer.UnitIcon> UnitTypeAndNumberComparator = new Comparator<IconRenderer.UnitIcon>()
    {
        public int compare(IconRenderer.UnitIcon o1, IconRenderer.UnitIcon o2)
        {
            IUnitIf u1 = o1.getUnit();
            IUnitIf u2 = o2.getUnit();
            int i1 = u1.getNumberPrefix();
            int i2 = u2.getNumberPrefix();
            if (i1 == i2)
            {
                return u1.getNumber() - u2.getNumber();
            } else
            {
                return i1 - i2;
            }
        }
    };

    public static final Comparator<IconRenderer.UnitIcon> UnitSpeedComparator = new Comparator<IconRenderer.UnitIcon>()
    {
        public int compare(IconRenderer.UnitIcon o1, IconRenderer.UnitIcon o2)
        {
            return (int)(o1.getUnit().getSpeed() - o2.getUnit().getSpeed());
        }
    };

    public static final Comparator<IconRenderer.UnitIcon> UnitPauseTimeComparator = new Comparator<IconRenderer.UnitIcon>()
    {
        public int compare(IconRenderer.UnitIcon o1, IconRenderer.UnitIcon o2)
        {
            return Double.valueOf(o1.getUnit().getDuration(UnitStatus.PAUSED,true))
            			.compareTo(Double.valueOf(o2.getUnit().getDuration(UnitStatus.PAUSED,true)));
        }
    };

    public static final Comparator<IconRenderer.UnitIcon> UnitWorkTimeComparator = new Comparator<IconRenderer.UnitIcon>()
    {
        public int compare(IconRenderer.UnitIcon o1, IconRenderer.UnitIcon o2)
        {
            return Double.valueOf(o1.getUnit().getDuration(UnitStatus.WORKING,true))
						.compareTo(Double.valueOf(o2.getUnit().getDuration(UnitStatus.WORKING,true)));
        }
    };

    public static final Comparator<IconRenderer.UnitIcon> UnitIdleTimeComparator = new Comparator<IconRenderer.UnitIcon>()
    {
        public int compare(IconRenderer.UnitIcon o1, IconRenderer.UnitIcon o2)
        {
            return Double.valueOf(o1.getUnit().getDuration(UnitStatus.READY,true))
						.compareTo(Double.valueOf(o2.getUnit().getDuration(UnitStatus.READY,true)));
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

        public Comparator<?> getComparator(int column)
        {
            switch (column)
            {
                case 0:
                    switch (m_sortKeys[column])
                    {
                        case 2:
                            return UnitSpeedComparator;
                        case 3:
                            return UnitPauseTimeComparator;
                        case 4:
                            return UnitWorkTimeComparator;
                        case 5:
                            return UnitIdleTimeComparator;
                        default:
                            return UnitTypeAndNumberComparator;
                    }
                case 1:
                case 2:
                case 3:
                case 4:
                    switch (m_sortKeys[column])
                    {
                        case 1:
                            return ListLengthComparator;
                        case 3:
                            return AssignmentTimeComparator;
                        case 4:
                            return StartTimeComparator;
                        case 5:
                            return EstimatedEndTimeComparator;
                        default:
                            return PriorityComparator;
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
	            	if(icon.getAssignment()!=null) {
	            		// return data
	            		return new AssignmentTransferable(icon.getAssignment());
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
				    	Object[] ans = AssignmentTransferUtilities.verifyMove(assignment, unit, newStatus);
				    	
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
