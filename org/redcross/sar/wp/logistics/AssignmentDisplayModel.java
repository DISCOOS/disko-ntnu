package org.redcross.sar.wp.logistics;

import org.redcross.sar.gui.AbstractPopupHandler;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.AssignmentImpl;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.mso.Selector;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 *  Model used for the Assignment Display part of the Logistics Panel
 */
public class AssignmentDisplayModel implements IMsoUpdateListenerIf, ComponentListener
{
    private AssignmentScrollPanel m_selectableAssignments;
    private AssignmentScrollPanel m_priAssignments;
    private IAssignmentListIf m_AssignmentList;

    /**
     * Initial value for selection
     */
    private IAssignmentIf.AssignmentStatus m_assigmentSelection = IAssignmentIf.AssignmentStatus.READY;

    /**
     * Selectors for left panel
     */
    private Selector<IAssignmentIf> m_selectableSelector;


    /**
     * Status for left panel
     */
    private IAssignmentIf.AssignmentStatus m_selectableStatus;

    /**
     * Selectors for right panel
     */
    private Selector<IAssignmentIf> m_priSelector;

    /**
     * Popup handler for selection
     */
    SelectionPopupHandler m_selectionMenu;

    /**
     * Value for determinining in which panel a ready assignment shall be placed (divider between right and left part)
     */
    private int m_readyDivider;

    /**
     * Constructor
     *
     * @param panel1 Reference to left panel
     * @param panel2 Reference to right panel
     * @param anEventManager MSO model's event manager
     * @param anAssignmentList MSO model's assignment list.
     */
    public AssignmentDisplayModel(AssignmentScrollPanel panel1, AssignmentScrollPanel panel2, IMsoEventManagerIf anEventManager, IAssignmentListIf anAssignmentList)
    {
        m_selectableAssignments = panel1;
        m_selectableAssignments.setLastIndex(-1);
        m_selectableSelector = IAssignmentIf.READY_SELECTOR;
        m_selectableAssignments.setHeaderPopupHandler(new SelectionPopupHandler(this));

        m_priAssignments = panel2;
        m_priAssignments.setFirstIndex(0);
        m_priSelector = IAssignmentIf.READY_SELECTOR;
        m_priAssignments.getHeaderLabel().setText("Pri");
        m_priAssignments.setSelectedStatus(IAssignmentIf.AssignmentStatus.READY);

        anEventManager.addClientUpdateListener(this);

        m_AssignmentList = anAssignmentList;

        panel2.getParent().addComponentListener(this);

        // set header text
        m_selectableAssignments.getHeaderLabel().setText(Internationalization.translate(m_assigmentSelection));
        //setSelectedStatus(IAssignmentIf.AssignmentStatus.READY);

        handleMsoUpdateEvent(null);
    }

    void reInitModel(IAssignmentListIf anAssgnmentList)
    {
        m_AssignmentList = anAssgnmentList;
        handleMsoUpdateEvent(null);
    }

    /**
     * Handle change of data in the MSO model event
     * @param e The event
     */
    public void handleMsoUpdateEvent(MsoEvent.Update e)
    {
        java.util.List<IAssignmentIf> priList = selectAssignments(m_priSelector);
        m_priAssignments.setAssignmentList(priList);

        setSelectableList();

        calculateDivider();
        renderData();
    }

    private final EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT);

    /**
     * Decide if  this object is interesting for update events
     * @param aMsoObject The actual object
     * @return <code><code/> if interesting, <code>false<code/> otherwise
     */
    public boolean hasInterestIn(IMsoObjectIf aMsoObject)
    {
        return myInterests.contains(aMsoObject.getMsoClassCode());
    }

    /**
     * Set the list that is source for the left panel
     */
    private void setSelectableList()
    {
        Collection<IAssignmentIf> selectionList = m_assigmentSelection == IAssignmentIf.AssignmentStatus.READY ? m_priAssignments.getAssignmens() : selectAssignments(m_selectableSelector);
        //Collection<IAssignmentIf> selectionList = selectAssignments(m_selectableSelector);
        m_selectableAssignments.setAssignmentList(selectionList);
        m_selectableAssignments.setSelectedStatus(m_selectableStatus);
    }

    /**
     * Generate al list of assignments based on a selector, sorted by priority and assignment number.
     * @param aSelector The Selector
     * @return The list of assignments
     */
    private java.util.List<IAssignmentIf> selectAssignments(Selector<IAssignmentIf> aSelector)
    {
        return m_AssignmentList.selectItems(aSelector, IAssignmentIf.PRIORITY_AND_NUMBER_COMPARATOR);
    }

    public void componentResized(ComponentEvent e)
    {
        calculateDivider();
        renderData();
    }

    public void componentMoved(ComponentEvent e)
    {
    }

    public void componentShown(ComponentEvent e)
    {
    }

    public void componentHidden(ComponentEvent e)
    {
    }

    /**
     * Draw the two panels.
     */
    private void renderData()
    {
        renderPriPanel();
        renderSelectablePanel();
    }

    /**
     * Calculate value of divider, depending on height of panel.
     */
    private void calculateDivider()
    {
        m_readyDivider = m_priAssignments.getMaxNonscrollItems();
    }

    /**
     * Set selection status for left panel.
     * @param aSelection Status value to set
     */
    public void setSelectedStatus(IAssignmentIf.AssignmentStatus aSelection)
    {
        //if (m_assigmentSelection != aSelection)
        //{
            m_assigmentSelection = aSelection;
            m_selectableAssignments.getHeaderLabel().setText(Internationalization.translate(m_assigmentSelection));
            selectionChanged();
        //}
    }

    /**
     * Get selected status for left panel
     * @return Selected status.
     */
    public IAssignmentIf.AssignmentStatus getSelectedStatus()
    {
        return m_assigmentSelection;
    }

    /**
     * Handle change of selection.
     * Sets selector and redraws left panel.
     */
    private void selectionChanged()
    {
        switch (m_assigmentSelection)
        {
            case READY:
                m_selectableSelector = IAssignmentIf.READY_SELECTOR;
                m_selectableStatus = IAssignmentIf.AssignmentStatus.READY;
                break;
            case ALLOCATED:
                m_selectableSelector = IAssignmentIf.ALLOCATED_SELECTOR;
                m_selectableStatus = IAssignmentIf.AssignmentStatus.ALLOCATED;
                break;
            case ASSIGNED:
                m_selectableSelector = IAssignmentIf.ASSIGNED_SELECTOR;
                m_selectableStatus = IAssignmentIf.AssignmentStatus.ASSIGNED;
                break;
            case EXECUTING:
                m_selectableSelector = AssignmentImpl.EXECUTING_SELECTOR;
                m_selectableStatus = IAssignmentIf.AssignmentStatus.EXECUTING;
                break;
            case FINISHED:
                m_selectableSelector = AssignmentImpl.FINISHED_SELECTOR;
                m_selectableStatus = IAssignmentIf.AssignmentStatus.FINISHED;
                break;
        }
        setSelectableList();
        renderSelectablePanel();
    }

    /**
     * Draw right panel.
     */
    private void renderPriPanel()
    {
        m_priAssignments.setLastIndex(m_readyDivider - 1);
        m_priAssignments.renderPanel();
    }

    /**
     * Draw left panel.
     */
    private void renderSelectablePanel()
    {
        if (m_assigmentSelection == IAssignmentIf.AssignmentStatus.READY)
        {
            m_selectableAssignments.setFirstIndex(m_readyDivider);
        } else
        {
            m_selectableAssignments.setFirstIndex(0);
        }
        m_selectableAssignments.renderPanel();
    }

    /**
     * RadioButton used in the popup menu
     */
    public static class SelectButton extends JRadioButtonMenuItem
    {
        final IAssignmentIf.AssignmentStatus m_selection;

        public SelectButton(AbstractAction anAction, IAssignmentIf.AssignmentStatus aSelection)
        {
            super(anAction);
            m_selection = aSelection;
        }
    }

    /**
     * Handler of popup events, after they are detected by a {@link org.redcross.sar.gui.PopupListener}
     */
    public static class SelectionPopupHandler extends AbstractPopupHandler
    {
        private final JPopupMenu m_menu = new JPopupMenu();
        private final AssignmentDisplayModel m_model;
        private final Vector<SelectButton> m_buttons = new Vector<SelectButton>(5);
        private final ButtonGroup m_buttonGroup = new ButtonGroup();

        /**
         * Constructor.
         *
         * Defines the buttons for the handler.
         *
         * @param aModel Reference to the {@link AssignmentDisplayModel} that shall be handled.
         */
        public SelectionPopupHandler(AssignmentDisplayModel aModel)
        {
            super();
            m_model = aModel;

            addButton(buttonWithAction(IAssignmentIf.AssignmentStatus.READY));
            addButton(buttonWithAction(IAssignmentIf.AssignmentStatus.ALLOCATED));
            addButton(buttonWithAction(IAssignmentIf.AssignmentStatus.ASSIGNED));
            addButton(buttonWithAction(IAssignmentIf.AssignmentStatus.EXECUTING));
            addButton(buttonWithAction(IAssignmentIf.AssignmentStatus.FINISHED));
        }

        /**
         * Add a radio button to the menu
         *
         * @param aButton The button to add
         */
        private void addButton(SelectButton aButton)
        {
            m_menu.add(aButton);
            m_buttons.add(aButton);
            m_buttonGroup.add(aButton);
        }

        /**
         * Button factory
         *
         * @param aSelection The {@link IAssignmentIf.AssignmentStatus} the button shall select.
         * @return The creatd button.
         */
        private SelectButton buttonWithAction(final IAssignmentIf.AssignmentStatus aSelection)
        {
            String buttonText = Internationalization.translate(aSelection);
            AbstractAction action = new AbstractAction(buttonText)
            {
                public void actionPerformed(ActionEvent e)
                {
                    m_model.setSelectedStatus(aSelection);
                }
            };
            return new SelectButton(action, aSelection);
        }

        /**
         * Show the popup menu.
         * @param e Event that was triggered.
         */
        public JPopupMenu getMenu(MouseEvent e)
        {
            IAssignmentIf.AssignmentStatus s = m_model.getSelectedStatus();
            for (SelectButton b : m_buttons)
            {
                if (b.m_selection == s)
                {
                    b.setSelected(true);
                    break;
                }
            }
            return m_menu;
        }
    }
}
