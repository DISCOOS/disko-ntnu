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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;

import no.cmr.tools.Log;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.attribute.AttributesPanel;
import org.redcross.sar.gui.attribute.TextFieldAttribute;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.renderers.IconRenderer;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.util.mso.Selector;

import com.esri.arcgis.interop.AutomationException;

/**
 * Abstract panel handling all updates to assignment. Implemented as template pattern.
 *
 * @author thomasl
 */
public abstract class AbstractAssignmentPanel extends JPanel implements IEditMessageComponentIf
{
    protected static final String MESSAGE_LINES_ID = "MESSAGE_LINES";
    protected static final String EDIT_ASSIGNMENT_ID = "EDIT ASSIGNMENT";
    protected static final String NEXT_ASSIGNMENT_ID = "NEXT ASSIGNMENT";
    protected static final String ASSIGNMENT_POOL_ID = "ASSIGNMENT POOL";

    protected String m_currentView = null;
    
    protected IDiskoWpMessageLog m_wpMessageLog = null;

    protected JPanel m_cardsPanel = null;

    protected JPanel m_messageLinesPanel = null;
    protected JList m_messageLineList = null;
    
    protected IAssignmentIf m_selectedAssignment = null;
    protected AttributesPanel m_selectedPanel = null;
    
    protected JPanel m_buttonPanel = null;
    protected JButton m_okButton = null;
    protected JButton m_cancelButton = null;
    protected JButton m_centerAtButton = null;

    protected AttributesPanel m_editAssignmentPanel = null;
    protected IMessageLineIf m_editingLine = null;
    
    protected JPanel m_nextAssignmentsPanel = null;
    protected JPanel m_nextAssignmentsButtonPanel = null;
    protected JScrollPane m_nextAssignmentScrollPane = null;
    protected ButtonGroup m_nextAssignmentButtonGroup = null;

    protected JPanel m_assignmentPoolPanel = null;
    protected JPanel m_assignmentPoolButtonPanel = null;
    protected JScrollPane m_assignmentPoolScrollPane = null;
    protected ButtonGroup m_assignmentPoolButtonGroup = null;


    protected List<IMessageLineIf> m_addedLines = new LinkedList<IMessageLineIf>();

    protected boolean m_notebookMode = true;

    public AbstractAssignmentPanel(IDiskoWpMessageLog wp)
    {
        m_wpMessageLog = wp;

        initialize();
    }

    protected void initialize()
    {
    	BorderLayout bl = new BorderLayout();
    	bl.setVgap(5);
    	bl.setHgap(5);
        
    	this.setLayout(bl);
        
        m_cardsPanel = new JPanel(new CardLayout());

        initSelectedPanel();
        initMessageLinesPanel();
        initEditAssignmentPanel();
        initNextAssignmentPanel();
        initAssignmentPoolPanel();
        initButtonPanel();             
        
        this.add(m_selectedPanel, BorderLayout.WEST);
        this.add(m_cardsPanel, BorderLayout.CENTER);
        this.add(m_buttonPanel, BorderLayout.EAST);
    }

    /**
     * Updates fields in an already added assignment message line
     */
    protected void updateMessageLine()
    {
        if (m_editingLine != null)
        {
            try
            {
                Calendar time = DTG.DTGToCal(m_editAssignmentPanel
                		.getAttribute("Time").getValue().toString());
                m_editingLine.setOperationTime(time);
            }
            catch (IllegalMsoArgumentException e1)
            {
                m_editingLine.setOperationTime(Calendar.getInstance());
            }
        }
    }

    /**
     * Overridden by sub-classes in order to revert the changes. This includes removing any
     * message lines added
     */
    public abstract void cancelUpdate();

    /**
     * Update assignment list model with the relevant lines. E.g. in the started dialog only message lines of type
     * started should be shown in the message line list
     */
    protected abstract void updateAssignmentLineList();

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
            if (this instanceof AssignedAssignmentPanel)
            {
                line = message.findMessageLine(MessageLineType.ASSIGNED, false);
            } else if (this instanceof StartedAssignmentPanel)
            {
                line = message.findMessageLine(MessageLineType.STARTED, false);
            } else if (this instanceof CompletedAssignmentPanel)
            {
                line = message.findMessageLine(MessageLineType.COMPLETE, false);
            }

            if (line != null)
            {
            	m_editAssignmentPanel.getAttribute("Time").setValue(
            			DTG.CalToDTG(line.getOperationTime()));
            }
        }
        showComponent();        
    }
    
    protected void initSelectedPanel() {
    	// create
    	m_selectedPanel = new AttributesPanel("<Velg oppdrag>");
    	m_selectedPanel.setMinimumSize(new Dimension(150,60));
    }
    
    protected void initMessageLinesPanel()
    {
        m_messageLinesPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        m_messageLineList = new JList(new MessageLineListModel(m_wpMessageLog));
        m_messageLineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_messageLineList.addListSelectionListener(new AssignmentLineSelectionListener(m_messageLineList, this));
        m_messageLineList.setBorder(BorderFactory.createLineBorder(Color.black));
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        m_messageLinesPanel.add(m_messageLineList, gbc);
        
        m_cardsPanel.add(m_messageLinesPanel, MESSAGE_LINES_ID);
    }
    
    protected void initEditAssignmentPanel()
    {
        
    	m_editAssignmentPanel = new AttributesPanel("<Velg oppdragslinje>");
    	m_editAssignmentPanel.setMinimumSize(new Dimension(150,60));
    	
    	// add attributes
    	m_editAssignmentPanel.addAttribute(new TextFieldAttribute("Assignment",
    			m_wpMessageLog.getBundleText("AssignmentLabel.text"),150,"<velg oppdrag>",false));
    	m_editAssignmentPanel.addAttribute(new TextFieldAttribute("Time",
    			m_wpMessageLog.getBundleText("AssignedTimeLabel.text"),150,"<velg oppdrag>",true));    	

        m_cardsPanel.add(m_editAssignmentPanel, EDIT_ASSIGNMENT_ID);
    }
    
    protected void initNextAssignmentPanel()
    {
        m_nextAssignmentsPanel = new JPanel();
        m_nextAssignmentsPanel.setLayout(new BorderLayout());
        m_nextAssignmentsButtonPanel = new JPanel();
        m_nextAssignmentsButtonPanel.setLayout(new BoxLayout(m_nextAssignmentsButtonPanel, BoxLayout.PAGE_AXIS));
        m_nextAssignmentScrollPane = new JScrollPane(m_nextAssignmentsButtonPanel);
        m_nextAssignmentsPanel.add(m_nextAssignmentScrollPane, BorderLayout.CENTER);
        m_nextAssignmentButtonGroup = new ButtonGroup();

        m_cardsPanel.add(m_nextAssignmentsPanel, NEXT_ASSIGNMENT_ID);
    }

    protected void initAssignmentPoolPanel()
    {
        m_assignmentPoolPanel = new JPanel();
        m_assignmentPoolPanel.setLayout(new BorderLayout());
        m_assignmentPoolButtonPanel = new JPanel(new GridBagLayout());

        m_assignmentPoolScrollPane = new JScrollPane(m_assignmentPoolButtonPanel);
        m_assignmentPoolButtonGroup = new ButtonGroup();

        m_assignmentPoolPanel.add(m_assignmentPoolScrollPane, BorderLayout.CENTER);

        m_cardsPanel.add(m_assignmentPoolPanel, ASSIGNMENT_POOL_ID);
    }


    protected void initButtonPanel() {
        
    	m_buttonPanel = new JPanel();
    	m_buttonPanel.setLayout(new BoxLayout(m_buttonPanel,BoxLayout.Y_AXIS));
    	
    	// create OK button
    	m_okButton = DiskoButtonFactory.createButton("GENERAL.OK",ButtonSize.NORMAL);
        
    	m_okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	if(MESSAGE_LINES_ID.equals(m_currentView)) 
            		addNewMessageLine();
            	else if(EDIT_ASSIGNMENT_ID.equals(m_currentView)) 
                    updateMessageLine();
            	else if(NEXT_ASSIGNMENT_ID.equals(m_currentView))
                    addSelectedAssignment();
            	else if(ASSIGNMENT_POOL_ID.equals(m_currentView))
            		addSelectedAssignment();
            }
        });
        
        m_buttonPanel.add(m_okButton);
        
    	// create CENTERAT button
    	m_centerAtButton = DiskoButtonFactory.createButton("MAP.CENTERAT",ButtonSize.NORMAL);
        
    	m_centerAtButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	centerAtSelected();
            }
        });
        
        m_buttonPanel.add(m_centerAtButton);
        
        m_cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
        
        m_cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	if(MESSAGE_LINES_ID.equals(m_currentView)) 
            		hideComponent(); 
            	else if(EDIT_ASSIGNMENT_ID.equals(m_currentView))             	
                    revertEditPanel();
            	else if(NEXT_ASSIGNMENT_ID.equals(m_currentView))
                    showComponent();
            	else if(ASSIGNMENT_POOL_ID.equals(m_currentView))
            		showComponent();
            }
        });

        m_buttonPanel.add(m_cancelButton);
        
    }
    
    /**
     *
     */
    public void clearContents()
    {
    }

    /**
     * Updates list of assignments based on message line type {@link #updateAssignmentLineList()}
     */
    public void newMessageSelected(IMessageIf message)
    {
        updateAssignmentLineList();
    }

    public void showComponent()
    {
        this.setVisible(true);
        setView(MESSAGE_LINES_ID);
        
    }
    
    /**
    *
    */
   public void hideComponent()
   {
		MessageLogPanel.hideMap();
       this.setVisible(false);
   }

    private void setView(String key) {
    	// save view key
    	m_currentView = key;
    	// do view spesific tasks
    	if(MESSAGE_LINES_ID.equals(m_currentView)) { 
    		m_selectedPanel.setVisible(false);
    		MessageLogPanel.hideMap();
    	}
    	else if(EDIT_ASSIGNMENT_ID.equals(m_currentView)) {
    		m_selectedPanel.setVisible(false);
    	}
    	else if(NEXT_ASSIGNMENT_ID.equals(m_currentView)) {
    		m_selectedPanel.setVisible(true);
    	}
    	else if(ASSIGNMENT_POOL_ID.equals(m_currentView)) {
    		m_selectedPanel.setVisible(true);
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
			
			try
			{
            	IDiskoMap map = m_wpMessageLog.getMap();
            	map.suspendNotify();
				map.centerAtMsoObject(m_selectedAssignment);
				map.refreshMsoLayers();
            	map.resumeNotify();
			}
			catch (AutomationException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		else {
			Utils.showWarning("Du må først velge ett oppdrag");
		}
		
	}
    
    /**
     * Show all assignments available from command post
     */
    EnumSet<AssignmentStatus> m_assignmentPoolSet = EnumSet.of
            (
                    AssignmentStatus.ABORTED,
                    //AssignmentStatus.DRAFT,
                    //AssignmentStatus.EMPTY,
                    AssignmentStatus.READY
            );
    Selector<IAssignmentIf> m_assignmentPoolSelector = new Selector<IAssignmentIf>()
    {
        public boolean select(IAssignmentIf anObject)
        {
            return m_assignmentPoolSet.contains(anObject.getStatus());
        }
    };

    protected void showAssignmentPool()
    {
        m_assignmentPoolButtonPanel.removeAll();
        m_assignmentPoolButtonGroup = new ButtonGroup();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;

        ICmdPostIf cmdPost = m_wpMessageLog.getMsoManager().getCmdPost();
        if (cmdPost == null)
        {
            return;
        }
        Collection<IAssignmentIf> assignments = cmdPost.getAssignmentList().selectItems(m_assignmentPoolSelector);
        int i = 0;
        int numButtonsInRow = m_assignmentPoolButtonPanel.getWidth() / 
        		DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL).width;
        for (final IAssignmentIf assignment : assignments)
        {
            JToggleButton button = DiskoButtonFactory.createToggleButton(assignment,ButtonSize.NORMAL);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // Select button
                    JToggleButton sourceButton = (JToggleButton) e.getSource();
                    selectAssignmentButton(sourceButton, m_assignmentPoolButtonGroup);
                    selectAssignment(assignment);
                }
            });
            m_assignmentPoolButtonGroup.add(button);


            if (i % numButtonsInRow == 0)
            {
                gbc.gridx = 0;
                gbc.gridy++;
            }
            gbc.gridx++;
            m_assignmentPoolButtonPanel.add(button, gbc);
            i++;
        }

        // Select button with highest priority
        try
        {
            JToggleButton selectedButton = (JToggleButton) m_assignmentPoolButtonGroup.getElements().nextElement();
            selectAssignmentButton(selectedButton, m_assignmentPoolButtonGroup);
            Iterator<IAssignmentIf> assignmentIt = assignments.iterator();
            selectAssignment(assignmentIt.hasNext() ? assignmentIt.next() : null);
        }
        catch (Exception e)
        {
        }

        setView(ASSIGNMENT_POOL_ID);
        
    }

    /**
     * Show assignments currently in unit's assignment queue
     */
    protected void showNextAssignment()
    {
        m_nextAssignmentsButtonPanel.removeAll();
        m_nextAssignmentButtonGroup = new ButtonGroup();

        // Get assignments in receiving unit's queue
        IUnitIf unit = (IUnitIf) MessageLogBottomPanel.getCurrentMessage(true).getSingleReceiver();
        List<IAssignmentIf> assignments = unit.getAllocatedAssignments();
        for (final IAssignmentIf assignment : assignments)
        {
            JToggleButton button = DiskoButtonFactory.createToggleButton(assignment,ButtonSize.LONG);
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
            m_nextAssignmentsButtonPanel.add(button);
        }

        // Select button with highest priority
        try
        {
            JToggleButton selectedButton = (JToggleButton) m_nextAssignmentButtonGroup.getElements().nextElement();
            m_nextAssignmentButtonGroup.setSelected(selectedButton.getModel(), true);
            selectAssignmentButton(selectedButton, m_nextAssignmentButtonGroup);
            Iterator<IAssignmentIf> assignmentIt = assignments.iterator();
            selectAssignment(assignmentIt.hasNext() ? assignmentIt.next() : null);
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }

        setView(NEXT_ASSIGNMENT_ID);
    }

    private void selectAssignment(IAssignmentIf selected) {
    	// forward
    	m_selectedAssignment = selected;
    	// is assignment selected?
    	if(selected!=null) {
    		m_selectedPanel.clearAttributes();
	    	// create?
    		if(m_selectedPanel.getAttributeCount()==0) {
	    		// add attributes (this should only occure once)
    			m_selectedPanel.addAttribute(new TextFieldAttribute("Assignment",
    					m_wpMessageLog.getBundleText("AssignmentLabel.text"),150,
    					MsoUtils.getAssignmentName(selected, 1),false));
    			m_selectedPanel.addAttribute(new TextFieldAttribute("Priority",
    					m_wpMessageLog.getBundleText("PriorityLabel.text"),150,
    					selected.getPriorityText(),false));
    			m_selectedPanel.addAttribute(selected.getRemarksAttribute(),
    					m_wpMessageLog.getBundleText("RemarksLabel.text"),150,false);
    			Dimension size = new Dimension(350,25);
    			m_selectedPanel.setAttributeSize(size);
    			m_selectedPanel.setCaptionWidth(100);
    			size = new Dimension(350,75);
    			m_selectedPanel.setAttributeSize("Remarks",size);
    			m_selectedPanel.doLayout();
    			m_selectedPanel.revalidate();
	    	}
    		else {
    			// update static properties
    			m_selectedPanel.getAttribute("Assignment").setValue(MsoUtils.getAssignmentName(selected, 1));
    			m_selectedPanel.getAttribute("Priority").setValue(selected.getPriorityText());
    			//m_selectedPanel.getAttribute("Remarks").setMsoObject(m_selectedPanel);
    			// update dynamic properties
    			m_selectedPanel.load();
    		}
    	}
    	else {
    		// reset panel
    		m_selectedPanel.clearAttributes();
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
        if (m_editingLine == null)
        {
            Log.error("showEditAssignment: edit line null");
            return;
        }
        IAssignmentIf assignment = m_editingLine.getLineAssignment();

        m_editAssignmentPanel.getAttribute("Assignment").setValue(
        		MsoUtils.getAssignmentName(assignment, 1));
		m_editAssignmentPanel.getAttribute("Time").setValue(
				DTG.CalToDTG(m_editingLine.getOperationTime()));

        setView(EDIT_ASSIGNMENT_ID);
    }

    /**
     * @return Whether sending unit has next assignment in assignment queue
     */
    protected boolean unitHasNextAssignment()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IUnitIf unit = (IUnitIf) message.getSender();
            return unit!=null && unit.getAllocatedAssignments().size() != 0;
        } else
        {
            return false;
        }
    }

    /**
     * @return Whether sending unit has any assigned assignments
     */
    protected boolean unitHasAssignedAssignment()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IUnitIf unit = (IUnitIf) message.getSender();
            return unit!=null && !unit.getAssignedAssignments().isEmpty();
        } else
        {
            return false;
        }
    }

    protected boolean unitHasStartedAssignment()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IUnitIf unit = (IUnitIf) message.getSender();
            return unit!=null && unit.getExecutingAssigment() != null;
        } else
        {
            return false;
        }
    }

    /**
     * @return Whether the current message already has a assigned message line
     */
    protected boolean messageHasAssignedAssignment()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IMessageLineIf messageLine = message.findMessageLine(MessageLineType.ASSIGNED, false);
            return messageLine != null;
        }
        return false;
    }

    /**
     * @return Whether the current message has a started message line
     */
    protected boolean messageHasStartedAssignment()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IMessageLineIf messageLine = message.findMessageLine(MessageLineType.STARTED, false);
            return messageLine != null;
        }
        return false;
    }

    protected boolean messageHasCompletedAssignment()
    {
        IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
        if (message != null)
        {
            IMessageLineIf messageLine = message.findMessageLine(MessageLineType.COMPLETE, false);
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
            assignment = unit!=null ? unit.getAssignedAssignment() : null;
        }

        return assignment;
    }


    public List<IMessageLineIf> getAddedLines()
    {
        return m_addedLines;
    }

    public boolean linesAdded()
    {
        return m_addedLines.size() != 0;
    }

    /**
     * Removes an added line
     *
     * @param line If {@code null} all lines are removed
     */
    public void lineRemoved(IMessageLineIf line)
    {
        if (line == null)
        {
            m_addedLines.clear();
        } else
        {
            m_addedLines.remove(line);
        }
    }
    
    /*===================================================================
     * Inner classes
     *=================================================================== 
     */
    
}
