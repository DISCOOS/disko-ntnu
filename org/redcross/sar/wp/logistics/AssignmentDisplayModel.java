package org.redcross.sar.wp.logistics;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import org.redcross.sar.data.DataModel;
import org.redcross.sar.data.Selector;
import org.redcross.sar.data.event.DataEvent;
import org.redcross.sar.data.event.IDataListener;
import org.redcross.sar.gui.AbstractPopupHandler;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoBinder;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentListIf;
import org.redcross.sar.util.Internationalization;

/**
 *  Model used for the Assignment Display part of the Logistics Panel
 */
public class AssignmentDisplayModel implements ComponentListener
{

	private IMsoModelIf m_model;
	private DataModel<IAssignmentIf, IAssignmentIf> m_assignments;

    private AssignmentTilesPanel m_selectableAssignments;
    private AssignmentTilesPanel m_priAssignments;

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
     * Value for determining in which panel a ready assignment shall be placed (divider between right and left part)
     */
    private int m_readyDivider;

    /**
     * Constructor
     *
     * @param selectable Reference to left panel
     * @param priority Reference to right panel
     * @param anEventManager MSO model's event manager
     * @param anAssignmentList MSO model's assignment list.
     */
    public AssignmentDisplayModel(
    		AssignmentTilesPanel selectable,
    		AssignmentTilesPanel priority,
    		IMsoModelIf model)
    {

    	// prepare
    	m_model = model;

    	// set selectable assignments panel
        m_selectableAssignments = selectable;
        m_selectableAssignments.setLastIndex(-1);
        m_selectableSelector = IAssignmentIf.READY_SELECTOR;
        m_selectableAssignments.setHeaderPopupHandler(new SelectionPopupHandler(this));

    	// set priority assignments panel
        m_priAssignments = priority;
        m_priAssignments.setFirstIndex(0);
        m_priSelector = IAssignmentIf.READY_SELECTOR;
        m_priAssignments.getHeaderPanel().setCaptionText("Pri");
        m_priAssignments.setSelectedStatus(IAssignmentIf.AssignmentStatus.READY);

        // set header text
        m_selectableAssignments.getHeaderPanel().setCaptionText(Internationalization.translate(m_assigmentSelection));

        // connect to MSO model
        getDataModel();

    }

    public void load()
    {
    	// forward
    	m_assignments.getBinder(m_model).load();
    }

    public DataModel<IAssignmentIf, IAssignmentIf> getDataModel() {
    	if(m_assignments==null) {

            // bind assignments in MSO model to data model
            m_assignments = new DataModel<IAssignmentIf, IAssignmentIf>(IAssignmentIf.class);
            MsoBinder<IAssignmentIf> binder = new MsoBinder<IAssignmentIf>(IAssignmentIf.class);
            IAssignmentListIf list = m_model.getMsoManager().getCmdPost().getAssignmentList();
            binder.setSelector(IAssignmentIf.ACTIVE_SELECTOR);
            binder.connect(m_model);
            m_assignments.connect(binder);
        	m_assignments.addDataListener(new IDataListener() {

    			@Override
    			public void onDataChanged(DataEvent e) {

    		    	if(e.getType()==DataEvent.CLEAR_EVENT) {
    		    		m_priAssignments.setAssignmentList(new ArrayList<IAssignmentIf>());
    		    	}
    		    	else {

    		    		List<IAssignmentIf> priList = selectAssignments(m_priSelector);
    			        m_priAssignments.setAssignmentList(priList);

    		    	}
    		        setSelectableList();
    		        calculateDivider();
    		        renderData();

    			}

        	});

            // load assignments into model
            binder.load(list);

    	}
    	return m_assignments;
    }

    private final EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT);

	public EnumSet<MsoClassCode> getInterests() {
		return myInterests;
	}

    /**
     * Set the list that is source for the left panel
     */
    private void setSelectableList()
    {
        Collection<IAssignmentIf> selectionList =
        	(m_assigmentSelection == IAssignmentIf.AssignmentStatus.READY ?
        			m_priAssignments.getAssignmens() : selectAssignments(m_selectableSelector));
        m_selectableAssignments.setAssignmentList(selectionList);
        m_selectableAssignments.setSelectedStatus(m_selectableStatus);
    }

    /**
     * Generate al list of assignments based on a selector, sorted by priority and assignment number.
     * @param aSelector The Selector
     * @return The list of assignments
     */
    private List<IAssignmentIf> selectAssignments(Selector<IAssignmentIf> aSelector)
    {
        return (List<IAssignmentIf>)m_assignments.getObjects(aSelector,IAssignmentIf.PRIORITY_AND_NUMBER_COMPARATOR);
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
        m_readyDivider = m_priAssignments.getMaxNonScrollItems();
    }

    /**
     * Set selection status for left panel.
     * @param aSelection Status value to set
     */
    public void setSelectedStatus(IAssignmentIf.AssignmentStatus aSelection)
    {
        m_assigmentSelection = aSelection;
        m_selectableAssignments.getHeaderPanel().setCaptionText(Internationalization.translate(m_assigmentSelection));
        selectionChanged();

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
            case QUEUED:
                m_selectableSelector = IAssignmentIf.QUEUED_SELECTOR;
                m_selectableStatus = IAssignmentIf.AssignmentStatus.QUEUED;
                break;
            case ALLOCATED:
                m_selectableSelector = IAssignmentIf.ALLOCATED_SELECTOR;
                m_selectableStatus = IAssignmentIf.AssignmentStatus.ALLOCATED;
                break;
            case EXECUTING:
                m_selectableSelector = IAssignmentIf.EXECUTING_SELECTOR;
                m_selectableStatus = IAssignmentIf.AssignmentStatus.EXECUTING;
                break;
            case FINISHED:
                m_selectableSelector = IAssignmentIf.FINISHED_SELECTOR;
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
		private static final long serialVersionUID = 1L;

		final IAssignmentIf.AssignmentStatus m_selection;

        public SelectButton(AbstractAction anAction, IAssignmentIf.AssignmentStatus aSelection)
        {
            super(anAction);
            m_selection = aSelection;
        }
    }

    /**
     * Handler of popup events, after they are detected by a {@link org.redcross.sar.gui.PopupAdapter}
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
            addButton(buttonWithAction(IAssignmentIf.AssignmentStatus.QUEUED));
            addButton(buttonWithAction(IAssignmentIf.AssignmentStatus.ALLOCATED));
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
         * @return The created button.
         */
        private SelectButton buttonWithAction(final IAssignmentIf.AssignmentStatus aSelection)
        {
            String buttonText = Internationalization.translate(aSelection);
            AbstractAction action = new AbstractAction(buttonText)
            {
				private static final long serialVersionUID = 1L;

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
