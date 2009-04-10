package org.redcross.sar.wp.messageLog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import no.cmr.tools.Log;

import org.redcross.sar.data.Selector;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.renderer.IconRenderer;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;

import com.esri.arcgis.interop.AutomationException;

/**
 * Abstract panel handling all updates to assignment. Implemented as template pattern.
 *
 * @author thomasl
 */
public abstract class AbstractAssignmentPanel extends JSplitPane implements IEditorIf
{
    protected static final String MESSAGE_LINES_ID = "MESSAGE_LINES";
    protected static final String EDIT_ASSIGNMENT_ID = "EDIT ASSIGNMENT";
    protected static final String NEXT_ASSIGNMENT_ID = "NEXT ASSIGNMENT";
    protected static final String ASSIGNMENT_POOL_ID = "ASSIGNMENT POOL";

    protected String m_currentView;

    protected IDiskoWpMessageLog m_aWp;

    protected JPanel m_cardsPanel;

    protected BasePanel m_messageLinesPanel;
    protected JList m_messageLineList;

    protected IAssignmentIf m_selectedAssignment;
    protected FieldsPanel m_selectedPanel;
    protected IUnitIf m_owningUnit;

    protected JPanel m_buttonPanel;
    protected JButton m_cancelButton;
    protected JButton m_centerAtButton;
    protected JButton m_addButton;
    protected JButton m_okButton;

    protected ButtonSize buttonSize = ButtonSize.SMALL;

    protected FieldsPanel m_editAssignmentPanel;
    protected IMessageLineIf m_editingLine;

    protected BasePanel m_nextAssignmentsPanel;
    protected JPanel m_nextAssignmentsButtonPanel;
    protected ButtonGroup m_nextAssignmentButtonGroup;

    protected BasePanel m_assignmentPoolPanel;
    protected JPanel m_assignmentPoolButtonPanel;
    protected ButtonGroup m_assignmentPoolButtonGroup;


    protected static List<IMessageLineIf> m_addedLines = new LinkedList<IMessageLineIf>();

    protected boolean m_notebookMode = true;

    public AbstractAssignmentPanel(IDiskoWpMessageLog wp)
    {
    	// forward
    	super();

    	// prepare
        m_aWp = wp;

        initialize();
    }

    protected void initialize()
    {

		// add empty border
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// create cards panel
        m_cardsPanel = new JPanel(new CardLayout());

        // initialize panels
        initSelectedPanel();
        initMessageLinesPanel();
        initEditAssignmentPanel();
        initNextAssignmentPanel();
        initAssignmentPoolPanel();
        initButtonPanel();

        // initialize split pane
        initSplitPane();

    }

    /**
     * Updates fields in an already added assignment message line
     */
    protected void updateMessageLine()
    {
        if (m_editingLine != null)
        {

        	Calendar time = (Calendar)m_editAssignmentPanel
            		.getValue("Time");

            m_editingLine.setLineTime(time);
        }
    }

    /**
     * Remove last additions
     */
    public void cancelUpdate() {

    	reset();

    }

    /**
     * Update assignment list model with the relevant lines. E.g. in the started dialog only message lines of type
     * started should be shown in the message line list
     */
    public abstract void updateAssignmentLineList();

    /**
     * Add new message line(s). Rules and logic is handled in sub-classes
     */
    protected abstract void addNewMessageLine();

    /**
     * Adds an message line with the currently selected assignment.
     */
    protected abstract void addSelectedAssignment();

    /**
     * Revert edit panel content to current MSO
     */
    protected void revertEditPanel()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IMessageLineIf line = null;
            if (this instanceof AllocatedAssignmentPanel)
            {
                line = message.findMessageLine(MessageLineType.ALLOCATED, false);
            } else if (this instanceof StartedAssignmentPanel)
            {
                line = message.findMessageLine(MessageLineType.STARTED, false);
            } else if (this instanceof CompletedAssignmentPanel)
            {
                line = message.findMessageLine(MessageLineType.COMPLETED, false);
            }

            if (line != null)
            {
            	m_editAssignmentPanel.setValue("Time",line.getLineTime());
            }
        }
        showEditor();
    }

    protected void initSplitPane() {
    	setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setLeftComponent(m_selectedPanel);
        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.add(m_cardsPanel, BorderLayout.CENTER);
        panel.add(m_buttonPanel, BorderLayout.EAST);
        setRightComponent(panel);
    	resetToPreferredSizes();
    }

    protected void initSelectedPanel() {
    	// create panel
    	m_selectedPanel = new FieldsPanel("Oppdragsinformasjon","Ingen oppdrag tilgjengelig",false,false);
    	m_selectedPanel.setNotScrollBars();
    	Utils.setFixedWidth(m_selectedPanel,250);
    }

    protected void initMessageLinesPanel()
    {
        m_messageLinesPanel = new BasePanel("Oppdrag");
        m_messageLineList = new JList(new MessageLineListModel(m_aWp));
        m_messageLineList.setCellRenderer(new MessageLineListRenderer());
        m_messageLineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_messageLineList.addListSelectionListener(new AssignmentLineSelectionListener(m_messageLineList, this));
        m_messageLinesPanel.setContainer(m_messageLineList);

        m_cardsPanel.add(m_messageLinesPanel, MESSAGE_LINES_ID);
    }

    protected void initEditAssignmentPanel()
    {

    	m_editAssignmentPanel = new FieldsPanel("Endre oppdragslinje","Ingen oppdrag funnet",false,false);

    	// add attributes
    	m_editAssignmentPanel.addField(new TextLineField("Assignment",
    			m_aWp.getBundleText("AssignmentLabel.text"),false,120,25,"<velg oppdrag>"));
    	m_editAssignmentPanel.addField(new DTGField("Time",
    			m_aWp.getBundleText("AllocatedTimeLabel.text"),true,120,25,Calendar.getInstance()));
    	m_editAssignmentPanel.setCaptionWidth(100);
		m_editAssignmentPanel.update();

        m_cardsPanel.add(m_editAssignmentPanel, EDIT_ASSIGNMENT_ID);
    }

    protected void initNextAssignmentPanel()
    {
        m_nextAssignmentsPanel = new BasePanel("Velg i fra kø");
        m_nextAssignmentsPanel.setScrollBarPolicies(
        		BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED,
        		BasePanel.HORIZONTAL_SCROLLBAR_NEVER);

        m_nextAssignmentsButtonPanel = new JPanel(new GridBagLayout());
        m_nextAssignmentsButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_nextAssignmentsButtonPanel.setBackground(Color.WHITE);

        m_nextAssignmentsPanel.setContainer(m_nextAssignmentsButtonPanel);

        m_nextAssignmentButtonGroup = new ButtonGroup();

        m_cardsPanel.add(m_nextAssignmentsPanel, NEXT_ASSIGNMENT_ID);

    }

    protected void initAssignmentPoolPanel()
    {
        m_assignmentPoolPanel = new BasePanel("Velg et oppdrag");
        m_assignmentPoolPanel.setScrollBarPolicies(
        		BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED,
        		BasePanel.HORIZONTAL_SCROLLBAR_NEVER);

        m_assignmentPoolButtonPanel = new JPanel(new GridBagLayout());
        m_assignmentPoolButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_assignmentPoolButtonPanel.setBackground(Color.WHITE);

        m_assignmentPoolButtonGroup = new ButtonGroup();

        m_assignmentPoolPanel.setContainer(m_assignmentPoolButtonPanel);

        m_cardsPanel.add(m_assignmentPoolPanel, ASSIGNMENT_POOL_ID);
    }


    protected void initButtonPanel() {

    	m_buttonPanel = new JPanel();
    	m_buttonPanel.setLayout(new BoxLayout(m_buttonPanel,BoxLayout.Y_AXIS));

        m_cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",buttonSize);

        m_cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
        		// update view
            	if(MESSAGE_LINES_ID.equals(m_currentView)) {
                	// remove all
            		cancelUpdate();
                	// forward
            		MessageLogBottomPanel.showListPanel();
            	}
            	else if(EDIT_ASSIGNMENT_ID.equals(m_currentView))
                    revertEditPanel();
            	else if(NEXT_ASSIGNMENT_ID.equals(m_currentView))
                    showEditor();
            	else if(ASSIGNMENT_POOL_ID.equals(m_currentView))
            		showEditor();
            }
        });

        m_buttonPanel.add(m_cancelButton);

    	// create CENTERAT button
    	m_centerAtButton = DiskoButtonFactory.createButton("MAP.CENTERAT",buttonSize);

    	m_centerAtButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	centerAtSelected();
            }
        });

        m_buttonPanel.add(m_centerAtButton);

    	// create add button
    	m_addButton = DiskoButtonFactory.createButton("GENERAL.PLUS",buttonSize);

    	m_addButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	if(MESSAGE_LINES_ID.equals(m_currentView))
        			addNewMessageLine();
            }
        });

        m_buttonPanel.add(m_addButton);

    	// create OK button
    	m_okButton = DiskoButtonFactory.createButton("GENERAL.FINISH",buttonSize);

    	m_okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	if(MESSAGE_LINES_ID.equals(m_currentView)) {
            		MessageLogBottomPanel.showListPanel();
            	}
            	else if(EDIT_ASSIGNMENT_ID.equals(m_currentView)) {
                    updateMessageLine();
            	}
            	else if(NEXT_ASSIGNMENT_ID.equals(m_currentView)) {
            		if(validateSelection()) addSelectedAssignment();
            	}
            	else if(ASSIGNMENT_POOL_ID.equals(m_currentView)) {
            		if(validateSelection()) addSelectedAssignment();
            	}
            }
        });

        m_buttonPanel.add(m_okButton);


    }

    private boolean validateSelection() {
    	if(m_selectedAssignment==null) {
    		Utils.showWarning("Begrensing","Du må først velge et oppdrag");
    		return false;
    	}
    	IUnitIf unit = m_selectedAssignment.getOwningUnit();
    	if(unit!=null) {
    		if(m_owningUnit!=unit) {
    			// get message
    			String msg = "Du er i ferd med å flytte oppdrag " +
				Utils.getBold(MsoUtils.getAssignmentName(m_selectedAssignment,1)) + " fra " +
				Utils.getBold(MsoUtils.getUnitName(unit, false)) + ". Vil du fortsette?";
    			// prompt user
	    		int ans = Utils.showConfirm("Bekreftelse",Utils.getHtml(msg),JOptionPane.YES_NO_OPTION);
	    		// validate choice
	    		return (ans==JOptionPane.YES_OPTION);
    		}
    	}
    	// is valid
    	return true;
    }

    /**
     *
     */
    public void reset()
    {
    	removeAddedLines(null,true);
    	updateAssignmentLineList();
    }

    /**
     * Updates list of assignments based on message line type {@link #updateAssignmentLineList()}
     */
    public void setMessage(IMessageIf message)
    {
    	// forward
        updateAssignmentLineList();
    }

    public void showEditor()
    {

    	// update list
        updateAssignmentLineList();

        // show message lines
        setView(MESSAGE_LINES_ID);

        // initialize buttons
        m_addButton.setEnabled(true);

    	// show me
    	this.setVisible(true);

    }

    /**
    *
    */
   public void hideEditor()
   {
		MessageLogPanel.hideMap();
       this.setVisible(false);
   }

    private void setView(String key) {
    	// save view key
    	m_currentView = key;
    	// do view spesific tasks
    	if(MESSAGE_LINES_ID.equals(m_currentView)) {
    		showSelectionInfo(false);
    		m_messageLinesPanel.setCaptionText (
    				m_messageLineList.getModel().getSize()==0 ?
    					"Ingen endringer i oppdragsstatus registrert"
    				:   "Endringer i oppdragsstatus");
    		m_centerAtButton.setEnabled(false);
    		MessageLogPanel.hideMap();
    	}
    	else if(EDIT_ASSIGNMENT_ID.equals(m_currentView)) {
    		showSelectionInfo(false);
    		m_centerAtButton.setEnabled(true);
    	}
    	else if(NEXT_ASSIGNMENT_ID.equals(m_currentView)) {
    		m_centerAtButton.setEnabled(false);
    		showSelectionInfo(true);

    	}
    	else if(ASSIGNMENT_POOL_ID.equals(m_currentView)) {
    		m_centerAtButton.setEnabled(false);
    		showSelectionInfo(true);
    	}
    	// show view
        CardLayout layout = (CardLayout) m_cardsPanel.getLayout();
        layout.show(m_cardsPanel, key);
    }

	/**
	 * Center map on selected assignment
	 */
	private void centerAtSelected()
	{

		// has selected?
		if(m_selectedAssignment != null)
		{

			MessageLogPanel.showMap();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						IDiskoMap map = m_aWp.getMap();
						map.suspendNotify();
						map.centerAtMsoObject(m_selectedAssignment);
						map.flashMsoObject(m_selectedAssignment);
						map.refreshMsoLayers();
						map.resumeNotify();
					} catch (AutomationException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		else {
			Utils.showWarning("Du må først velge ett oppdrag");
		}

	}

    /**
     * Show all assignments available from command post
     */
    EnumSet<AssignmentStatus> m_assignmentPoolSet = EnumSet.of(AssignmentStatus.READY,AssignmentStatus.QUEUED,AssignmentStatus.ALLOCATED);

	Selector<IMessageLineIf> m_lineSelector = new Selector<IMessageLineIf>()
	{
		public boolean select(IMessageLineIf anObject)
		{
			return (anObject!=null && anObject.getLineType().equals(MessageLineType.ALLOCATED));
		}
	};

    Selector<IAssignmentIf> m_assignmentPoolSelector = new Selector<IAssignmentIf>()
    {
        public boolean select(IAssignmentIf anObject)
        {
            return m_assignmentPoolSet.contains(anObject.getStatus());
        }
    };

    protected void showSelectionInfo(boolean isVisible) {
    	m_selectedPanel.setVisible(isVisible);
    	setDividerSize(isVisible ? 5 : 0);
    	resetToPreferredSizes();
    }

    protected void showAssignmentPool()
    {
    	m_addButton.setEnabled(false);
    	m_centerAtButton.setEnabled(false);
        m_assignmentPoolButtonPanel.removeAll();

        m_assignmentPoolButtonGroup = new ButtonGroup();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;

        ICmdPostIf cmdPost = null;
		if(m_aWp.getMsoManager().operationExists()) {
			cmdPost = m_aWp.getMsoManager().getCmdPost();
		}

		// exists?
        if (cmdPost == null)
        {
            return;
        }

        int i = 0;
        int w = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL).width;

        // get number of buttons in each row
        int columns = (getWidth() - 250 - 10) / w;

        // get available assignments in priority order
        Collection<IAssignmentIf> assignments = cmdPost.getAssignmentList().selectItems(
        		m_assignmentPoolSelector,IAssignmentIf.PRIORITY_AND_NUMBER_COMPARATOR);

        // populate panel with assignments
        for (final IAssignmentIf it : assignments)
        {
        	// create button and selection handler
            JToggleButton button = DiskoButtonFactory.createToggleButton(it,ButtonSize.NORMAL);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // Select button
                    JToggleButton sourceButton = (JToggleButton) e.getSource();
                    selectAssignmentButton(sourceButton, m_assignmentPoolButtonGroup);
                    selectAssignment(it);
                }
            });
            m_assignmentPoolButtonGroup.add(button);

            // overflow?
            if (i % columns == 0)
            {
            	// next row
                gbc.gridx = 0;
                gbc.gridy++;
            }
            // next column
            gbc.gridx++;
            // add button to panel
            m_assignmentPoolButtonPanel.add(button, gbc);
            // increment index
            i++;

        }

        // apply preferred size
        m_assignmentPoolButtonPanel.setPreferredSize(new Dimension(w*columns,w*gbc.gridy));

        // Select button with highest priority
        if(i>0) {
            JToggleButton selectedButton = (JToggleButton) m_assignmentPoolButtonGroup.getElements().nextElement();
            selectAssignmentButton(selectedButton, m_assignmentPoolButtonGroup);
            Iterator<IAssignmentIf> assignmentIt = assignments.iterator();
            selectAssignment(assignmentIt.hasNext() ? assignmentIt.next() : null);
	        setView(ASSIGNMENT_POOL_ID);
	    	m_centerAtButton.setEnabled(true);
        }
        else {
        	Utils.showWarning("Det er ingen flere ledige oppdrag");
        }

    }

    /**
     * Show assignments currently in unit's assignment queue
     */
    protected void showNextAssignment()
    {
        // initialize
    	m_nextAssignmentsButtonPanel.removeAll();
        m_nextAssignmentButtonGroup = new ButtonGroup();
        List<IAssignmentIf> assignments = new ArrayList<IAssignmentIf>();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;

        // get button size
        int w = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL).width;

        // get number of buttons in each row
        int columns = (getWidth() - 250 - 10) / (w + 5);

        // initialize column index
        int i = 0;

        // Get assignments in receiving unit's queue
        ICommunicatorIf com = MessageLogBottomPanel.getCurrentMessage(true).getReceiver();
        if(com instanceof IUnitIf) {
        	IUnitIf unit = (IUnitIf)com;
	        assignments = unit.getEnqueuedAssignments();
	        for (final IAssignmentIf assignment : assignments)
	        {
	            JToggleButton button = DiskoButtonFactory.createToggleButton(assignment,ButtonSize.NORMAL);
	            button.addActionListener(new ActionListener()
	            {
	                public void actionPerformed(ActionEvent e)
	                {
	                    // Mark icon as selected
	                    JToggleButton sourceButton = (JToggleButton) e.getSource();
	                    selectAssignmentButton(sourceButton, m_nextAssignmentButtonGroup);
	                    selectAssignment(assignment);
	                }
	            });
	            m_nextAssignmentButtonGroup.add(button);
	            if (i % columns == 0)
	            {
	                gbc.gridx = 0;
	                gbc.gridy++;
	            }
	            gbc.gridx++;
	            m_nextAssignmentsButtonPanel.add(button, gbc);
	            i++;
	        }
        }

        // apply preferred size
        m_assignmentPoolButtonPanel.setPreferredSize(new Dimension(w*columns,w*gbc.gridy));

        // Select button with highest priority
        if(i>0) {
            JToggleButton selectedButton = (JToggleButton) m_nextAssignmentButtonGroup.getElements().nextElement();
            m_nextAssignmentButtonGroup.setSelected(selectedButton.getModel(), true);
            selectAssignmentButton(selectedButton, m_nextAssignmentButtonGroup);
            Iterator<IAssignmentIf> assignmentIt = assignments.iterator();
            selectAssignment(assignmentIt.hasNext() ? assignmentIt.next() : null);
        }
        else {
        	Utils.showWarning("Det er ingen i kø");
        }

        // show view
        setView(NEXT_ASSIGNMENT_ID);

    }

    private void selectAssignment(IAssignmentIf selected) {
    	// forward
    	m_selectedAssignment = selected;
		m_centerAtButton.setEnabled(true);
    	// is assignment selected?
    	if(selected!=null) {
    		m_selectedPanel.clearFields();
	    	// create?
    		if(m_selectedPanel.getFieldCount()==0) {
	    		// add attributes (this should only occur once)
    			m_selectedPanel.addField(new TextLineField("Assignment",
    					m_aWp.getBundleText("AssignmentLabel.text"),false,
    					120,25, MsoUtils.getAssignmentName(selected, 1)));
    			m_selectedPanel.addField(new TextLineField("Priority",
    					m_aWp.getBundleText("PriorityLabel.text"),false,
    					120,25,selected.getPriorityText()));
    			IUnitIf unit = selected.getOwningUnit();
    			m_selectedPanel.addField(new TextLineField("Owner",
    					m_aWp.getBundleText("OwnerLabel.text"),false,
    					120,25,unit!=null ? MsoUtils.getUnitName(unit, false) : ""));
    			m_selectedPanel.addField(selected.getRemarksAttribute(),
    					m_aWp.getBundleText("RemarksLabel.text"),false,120,40);
    			m_selectedPanel.setCaptionWidth(100);
    			m_selectedPanel.update();
    			m_selectedPanel.revalidate();
	    	}
    		else {
    			// update static properties
    			m_selectedPanel.setValue("Assignment",MsoUtils.getAssignmentName(selected, 1));
    			m_selectedPanel.setValue("Priority",selected.getPriorityText());
    			// update dynamic properties
    			m_selectedPanel.reset();
    		}
    	}
    	else {
    		// reset panel
    		m_selectedPanel.clearFields();
    	}
    }

    /**
     * Updates button selection for the given button group
     *
     * @param button
     * @param buttonGroup
     */
    protected void selectAssignmentButton(JToggleButton button, ButtonGroup buttonGroup)
    {
        // Mark one icon as selected
        try
        {
            Enumeration<AbstractButton> buttons = buttonGroup.getElements();
            JToggleButton buttonIt = null;
            while (buttons.hasMoreElements())
            {
                buttonIt = (JToggleButton) buttons.nextElement();
                IconRenderer.AssignmentIcon icon = (IconRenderer.AssignmentIcon) buttonIt.getIcon();
                icon.setSelected(false);
            }

            IconRenderer.AssignmentIcon icon = (IconRenderer.AssignmentIcon) button.getIcon();
            icon.setSelected(true);
            repaint();
        }
        catch (Exception e){
        	e.printStackTrace();
        }

        buttonGroup.setSelected(button.getModel(), true);
    }

    /**
     * Show current message assignment
     */
    protected void showEditAssignment(int index)
    {
        MessageLineListModel model = (MessageLineListModel) m_messageLineList.getModel();
        m_editingLine = (IMessageLineIf) model.getElementAt(index);

        m_addButton.setEnabled(false);


        if (m_editingLine == null)
        {
            Log.error("showEditAssignment: edit line null");
            return;
        }

        // get selected assignment
        m_selectedAssignment = m_editingLine.getLineAssignment();

        m_editAssignmentPanel.setValue(
        		"Assignment", MsoUtils.getAssignmentName(m_selectedAssignment, 1));
		m_editAssignmentPanel.setValue("Time",m_editingLine.getLineTime());

        setView(EDIT_ASSIGNMENT_ID);
    }

    protected IUnitIf getAvailableUnit(IMessageIf message) {
    	IUnitIf unit = null;

    	// get unit if exists
		ICommunicatorIf com = message!=null ? message.getReceiver() : null;
		unit =(com instanceof IUnitIf) ? (IUnitIf)com : null;

		// found
		if(unit!=null) return unit;

		// get unit if exists
		message = MessageLogBottomPanel.getCurrentMessage(false);
		com = message!=null ? message.getSender() : null;
		unit =(com instanceof IUnitIf) ? (IUnitIf)com : null;

		// finished
    	return unit;
    }

    /**
     * @return Whether sending unit has next assignment in assignment queue
     */
    protected static boolean unitEnqueuedAssignment(IUnitIf unit)
    {
        if (unit != null)
        {
            return unit!=null && unit.getEnqueuedAssignments().size() != 0;
        } else
        {
            return false;
        }
    }

    /**
     * @return Whether sending unit has any Allocated assignments
     */
    protected static boolean unitHasAllocatedAssignment(IUnitIf unit)
    {
        if (unit != null)
        {
            return unit!=null && !unit.getAllocatedAssignments().isEmpty() || linesAdded(MessageLineType.ALLOCATED)>0;
        } else
        {
            return false;
        }
    }

    protected static boolean unitHasStartedAssignment(IUnitIf unit)
    {
        if (unit != null)
        {
            return unit!=null && unit.getExecutingAssigment() != null || linesAdded(MessageLineType.STARTED)>0;
        } else
        {
            return false;
        }
    }

    protected static boolean unitHasCompletedAssignment()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IMessageLineIf line = message.findMessageLine(MessageLineType.COMPLETED, false);
            return line!=null || linesAdded(MessageLineType.COMPLETED)>0;
        } else
        {
            return false;
        }
    }

    public static boolean messageHasAssignments() {
    	return messageHasAllocatedAssignment()
    		|| messageHasStartedAssignment()
    		|| messageHasCompletedAssignment();
    }

    /**
     * @return Whether the current message already has a Allocated message line
     */
    protected static boolean messageHasAllocatedAssignment()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IMessageLineIf messageLine = message.findMessageLine(MessageLineType.ALLOCATED, false);
            return messageLine != null;
        }
        return false;
    }

    /**
     * @return Whether the current message has a started message line
     */
    protected static boolean messageHasStartedAssignment()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IMessageLineIf messageLine = message.findMessageLine(MessageLineType.STARTED, false);
            return messageLine != null;
        }
        return false;
    }

    protected static boolean messageHasCompletedAssignment()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IMessageLineIf messageLine = message.findMessageLine(MessageLineType.COMPLETED, false);
            return messageLine != null;
        }
        return false;
    }

    /**
     * @return Current assignment to sending unit. null if unit does not have an assignment
     */
    protected IAssignmentIf getUnitAssignment()
    {
        IAssignmentIf assignment = null;
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IUnitIf unit = (IUnitIf) message.getSender();
            assignment = unit!=null ? unit.getAllocatedAssignment() : null;
        }

        return assignment;
    }


    public static List<IMessageLineIf> getAddedLines()
    {
        return m_addedLines;
    }

    public static int linesAdded(MessageLineType type)
    {
    	int count=0;
    	for(IMessageLineIf it: m_addedLines) {
    		if(it.getLineType().equals(type)) count++;
    	}
        return count;
    }

    public static IMessageLineIf getAddedLine(MessageLineType type)
    {
    	for(IMessageLineIf it: m_addedLines) {
    		if(it.getLineType().equals(type)) return it;
    	}
        return null;
    }

    /**
     * Removes an added line
     *
     * @param line If {@code null} all lines are removed
     */
    public static void removeAddedLines(IMessageLineIf line, boolean delete)
    {
        if (line == null)
        {
        	if(delete) {
	        	for(IMessageLineIf it: m_addedLines) {
	        		it.delete();
	        	}
        	}
            m_addedLines.clear();
        } else
        {
        	if(delete) line.delete();
            m_addedLines.remove(line);
        }
    }

    public static void removeAddedLineType(MessageLineType type, boolean delete)
    {
        if (type == null)
        {
        	if(delete) {
	        	for(IMessageLineIf it: m_addedLines) {
	        		it.delete();
	        	}
        	}
            m_addedLines.clear();
        } else
        {
        	List<IMessageLineIf> removed = new ArrayList<IMessageLineIf>();
        	for(IMessageLineIf it: m_addedLines) {
        		if(it.getLineType().equals(type))
        			removed.add(it);
        	}
        	for(IMessageLineIf it: removed) {
        		if(delete) it.delete();
        		m_addedLines.remove(it);
        	}
        }
    }

    /*===================================================================
     * Inner classes
     *===================================================================
     */

}
