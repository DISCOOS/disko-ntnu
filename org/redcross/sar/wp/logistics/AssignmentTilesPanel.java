package org.redcross.sar.wp.logistics;

import org.redcross.sar.gui.dnd.DiskoDragSourceAdapter;
import org.redcross.sar.gui.dnd.DiskoDropTargetAdapter;
import org.redcross.sar.gui.dnd.IDiskoDropTarget;
import org.redcross.sar.gui.dnd.IconDragGestureListener;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.DiskoTilesPanel;
import org.redcross.sar.gui.renderer.IconRenderer;
import org.redcross.sar.gui.renderer.IconRenderer.AssignmentIcon;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.AssignmentUtilities;
import org.redcross.sar.util.Utils;
import org.redcross.sar.wp.logistics.AssignmentLabel.AssignmentLabelActionHandler;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * Scroll panel for assignments.
 * <p/>
 * The panel shows assignment labels for a given list of assignments.
 */
public class AssignmentTilesPanel extends DiskoTilesPanel implements IDiskoDropTarget
{

	private static final long serialVersionUID = 1L;

    /**
	 * Drop data flavor object (assignment)
	 */
	private static DataFlavor m_flavor;

	/**
     * List of assignments to show.
     */
    private Collection<IAssignmentIf> m_assignments;

    /**
     * Index of first item to show.
     */
    private int m_firstIndex = 0;

    /**
     * Index of last item to show.
     * if equal to -1, show all items.
     */
    private int m_lastIndex = -1;

    /**
     * Pool of icons
     */
    private final Vector<IconRenderer.AssignmentIcon> m_icons = new Vector<IconRenderer.AssignmentIcon>();

    /**
     * Pool of labels
     */
    private final Vector<AssignmentDragDropLabel> m_labels = new Vector<AssignmentDragDropLabel>();

    /**
     * Set of (user) selected assignments.
     */
    private final Set<IAssignmentIf> m_selected = new HashSet<IAssignmentIf>();

    /**
     * Pointer to wp module
     */
    private final IDiskoWpLogistics m_wpModule;

    /**
     * Status of assignments listed.
     */
    private IAssignmentIf.AssignmentStatus m_selectedStatus;

    /**
     * Selected unit if the panel contains assignments for a specific unit.
     */
    private IUnitIf m_selectedUnit;

    /**
     * Tell if icons shall be shown in the labels.
     */
    private final boolean m_showIcons;

    /**
     * Action handler for the labels
     */
    private final AssignmentLabelActionHandler m_actionHandler;

    /**
     * Icon sizr
     */
    private final Dimension m_iconSize = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL);

    /**
     * Constructor. </p>
     *
     * Define a panel with a given FlowLayout manager.
     *
     * @param aLayoutManager  The layout manager used by the panel.
     * @param anActionHandler The {@link AssignmentLabel.AssignmentLabelActionHandler} that shall be used by te labels.
     * @param showIcons       <code>true</code> if icons shall be shown in the labels, <code>false</code> otherwise.
     * @see DiskoTilesPanel
     */
    public AssignmentTilesPanel(IDiskoWpLogistics wp, FlowLayout aLayoutManager, AssignmentLabelActionHandler anActionHandler, boolean showIcons)
    {
        super(aLayoutManager);
        m_showIcons = showIcons;
        m_actionHandler = anActionHandler;
        m_wpModule = wp;
        initialize();
    }

    /**
     * Constructor. </p>
     *
     * Define a panel with a given GridLayout manager.
     *
     * @param aLayoutManager  The layout manager used by the panel.
     * @param anActionHandler The {@link AssignmentLabel.AssignmentLabelActionHandler} that shall be used by the labels.
     * @param showIcons       <code>true</code> if icons shall be shown in the labels, <code>false</code> otherwise.
     * @see DiskoTilesPanel
     */
    public AssignmentTilesPanel(IDiskoWpLogistics wp, GridLayout aLayoutManager, AssignmentLabelActionHandler anActionHandler, boolean showIcons)
    {
        super(aLayoutManager);
        m_showIcons = showIcons;
        m_actionHandler = anActionHandler;
        m_wpModule = wp;
        initialize();
    }

    /**
     * Constructor. </p>
     *
     * Define a panel with a given layout manager.
     *
     * @param aScrollPane      The surrounding scroll pane.
     * @param aLayoutManager   The layout manager used by the panel.
     * @param aHgap            Horizontal gap between labels.
     * @param aVgap            Vertical gap between labels.
     * @param isHorizontalFlow <code>true</code> if labels shall be displayed in horizontal rows, <code>false</code> if the
     *                         shall be shown in vertical columns.
     * @param anActionHandler  The {@link AssignmentLabelActionHandler} that shall be used by te labels.
     * @param showIcons        <code>true</code> if icons shall be shown in the labels, <code>false</code> otherwise.
     * @see DiskoTilesPanel
     */
    public AssignmentTilesPanel(IDiskoWpLogistics wp, LayoutManager aLayoutManager, int aHgap, int aVgap, boolean isHorizontalFlow, AssignmentLabelActionHandler anActionHandler, boolean showIcons)
    {
        super(aLayoutManager, aHgap, aVgap, isHorizontalFlow);
        m_showIcons = showIcons;
        m_actionHandler = anActionHandler;
        m_wpModule = wp;
        initialize();
    }

    private void initialize()
    {
    	try {
    		// initialize
            setEnabled(true);
            setFocusable(true);
	    	// create flavor
	    	m_flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=org.redcross.sar.mso.data.IAssignmentIf");
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}

    	// create gesture recognizer
    	DragSource ds = DragSource.getDefaultDragSource();
    	ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE,
    			new IconDragGestureListener(new AssignmentScrollPanelDragSourceListener()));

    	// create drop target
    	setDropTarget(new DropTarget(this, new AssignmentScrollPanelDropTargetListener()));

    	// add focus listener
        addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e){}
            public void focusLost(FocusEvent e){}
        });


    }

    /**
     * Set collection of assignments to show
     *
     * @param anAssignmentCollection The actual collection.
     */
    public void setAssignmentList(Collection<IAssignmentIf> anAssignmentCollection)
    {
        m_assignments = anAssignmentCollection;
    }

    /**
     * Get list of assignments to show
     *
     * @return The actual list.
     */
    public Collection<IAssignmentIf> getAssignmens()
    {
        return m_assignments;
    }

    /**
     * Get first index to show.
     */
    public int getFirstIndex()
    {
        return m_firstIndex;
    }

    /**
     * Set first index to show.
     */
    public void setFirstIndex(int anIndex)
    {
        m_firstIndex = anIndex;
    }


    /**
     * Get last index to show.
     */
    public int getLastIndex()
    {
        return m_lastIndex;
    }

    /**
     * Set last index to show.
     * <p/>
     * If equal to -1, show all items.
     */
    public void setLastIndex(int anIndex)
    {
        m_lastIndex = anIndex;
    }

    /**
     * Get the current {@link IAssignmentIf.AssignmentStatus} for the assigments in the panel.
     */
    public IAssignmentIf.AssignmentStatus getSelectedStatus()
    {
        return m_selectedStatus == null ? AssignmentStatus.READY : m_selectedStatus;
    }

    /**
     * Set the current {@link IAssignmentIf.AssignmentStatus} for the assigments in the panel.
     */
    public void setSelectedStatus(IAssignmentIf.AssignmentStatus aSelectedStatus)
    {
        m_selectedStatus = aSelectedStatus;
    }


    /**
     * Get the {@link org.redcross.sar.mso.data.IUnitIf} owning the assigments in the panel.
     */
    public IUnitIf getSelectedUnit()
    {
        return m_selectedUnit;
    }

    /**
     * Set the {@link org.redcross.sar.mso.data.IUnitIf} owning the assigments in the panel.
     */
    public void setSelectedUnit(IUnitIf aSelectedUnit)
    {
        m_selectedUnit = aSelectedUnit;
    }

    /**
     * Clear the set of selected assignments.
     */
    public void clearSelected()
    {
        m_selected.clear();
    }

    /**
     * Add an assignments to the set of selected assignments.
     */
    public void addSelected(IAssignmentIf anAsg)
    {
        m_selected.add(anAsg);
    }

    /**
     * Draw the panel.
     * Goes through the actual list from min to max, and draws the labels into the panel.
     * Finally the panel is resized to fit the list.
     */
    public void renderPanel()
    {
        removeAllTiles();
        if (m_assignments == null)
        {
            return;
        }

        AssignmentIcon icon = null;
        int firstIndex = Math.max(m_firstIndex, 0);
        int lastIndex = m_lastIndex < 0 ? m_assignments.size() - 1 : Math.min(m_assignments.size() - 1, m_lastIndex);
        int iv = 0;

        IAssignmentIf[] items = new IAssignmentIf[m_assignments.size()];
        m_assignments.toArray(items);

        Iterator<IAssignmentIf> iterator = m_assignments.iterator();
        int i = -1;
        while (iterator.hasNext())
        {
            i++;
            IAssignmentIf asg = items[i];
            asg = iterator.next();
            if (i < firstIndex) continue;
            if (i > lastIndex) break;

            if (m_showIcons){
                if (m_icons.size() == iv){
                    icon = new AssignmentIcon(asg, m_selected.contains(asg), null);
                    m_icons.ensureCapacity(lastIndex - firstIndex + 1);
                    m_icons.add(icon);
                }
                else {
                    icon = m_icons.get(iv);
                    icon.setAssignment(asg);
                    icon.setSelected(m_selected.contains(asg));
                }
                if (m_labels.size() == iv){
                    m_labels.ensureCapacity(lastIndex - firstIndex + 1);
                    m_labels.add(new AssignmentDragDropLabel(icon, m_actionHandler));
                }
                else {
                    m_labels.get(iv).setAssignmentIcon(icon);
                }
            }
            else {
                if (m_labels.size() == iv){
                    m_labels.ensureCapacity(lastIndex - firstIndex + 1);
                    m_labels.add(new AssignmentDragDropLabel(asg, m_actionHandler));
                }
                else {
                    m_labels.get(iv).setAssignment(asg);
                }
            }
            JLabel label = m_labels.get(iv);
            addTile(label);
            iv++;
        }
        resizePanel(true);
    }

    /**
     * Calculate the number of labels that can be located within the pane without scrolling.
     *
     * @return The calculated number.
     */
    public int getMaxNonScrollItems()
    {
        return super.getMaxNonScrollItems(m_iconSize);
    }

    public void setSelectedAssignment(IAssignmentIf anAssignment)
    {
        for (AssignmentDragDropLabel label : m_labels)
        {
            if (label.getAssignment() == anAssignment)
            {
                label.setSelected(true);
            } else if (label.isSelected())
            {
                label.setSelected(false);
            }
        }
    }

    public Component getComponent() {
		return this;
	}

    public boolean transfer(Transferable data) {
		// get data
		try{
			if(canTransfer(data)) {
				// get assignment
				IAssignmentIf assignment = (IAssignmentIf)data.getTransferData(m_flavor);
				// do the transfer
		        return m_wpModule.transfer(assignment, getSelectedStatus(), getSelectedUnit());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// can not transfer
		return false;
	}

	public boolean canTransfer(Transferable data) {
		// get data
		try{

			// try to get assignment
			IAssignmentIf assignment = (IAssignmentIf)data.getTransferData(m_flavor);

			// valid assignment?
			if(assignment!=null) {

				// get status change
				AssignmentStatus newStatus = getSelectedStatus()==null ? AssignmentStatus.READY : getSelectedStatus();

				// validate
		    	Object[] ans = AssignmentUtilities.verifyMove(assignment, null, newStatus);

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
		catch(UnsupportedFlavorException e1) {
			Utils.showWarning("Mottatt objekt er ikke et oppdrag");
		}
		catch (Exception e2) {
			e2.printStackTrace();
		}
		// can not transfer
		return false;
	}

	private class AssignmentScrollPanelDragSourceListener extends DiskoDragSourceAdapter {

	    public Component getComponent() {
			return AssignmentTilesPanel.this;
		}

		public Transferable getTransferable() {
			// Only assignment labels are draggable!!
			return null;
		}

		@Override
		public Icon getIcon() {
			// Only assignment labels are draggable!!
	    	return null;
		}
	}

	private class AssignmentScrollPanelDropTargetListener extends DiskoDropTargetAdapter {

		@Override
		public void dragOver(DropTargetDragEvent e) {
			// forward
			dragEnter(e);
		}

		@Override
		public void dragEnter(DropTargetDragEvent e) {
			// always allow drag
			return;
		}

		@Override
		public void drop(DropTargetDropEvent e) {
			// is supported?
			if (e.isDataFlavorSupported(m_flavor)) {
            	// try to transfer data to source
            	if(transfer(e.getTransferable())) {
            		// success!
                	e.acceptDrop(DnDConstants.ACTION_MOVE);
        			e.dropComplete(true);
                	return;
            	}
			}
			// reject request
			e.rejectDrop();
		}
	}
}