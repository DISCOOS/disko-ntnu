package org.redcross.sar.wp.messageLog;

import no.cmr.tools.Log;

import org.redcross.sar.gui.DiskoButtonFactory;
import org.redcross.sar.gui.DiskoButtonFactory.ButtonType;
import org.redcross.sar.gui.NumPadDialog;
import org.redcross.sar.gui.renderers.IconRenderer;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.util.mso.Selector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.*;
import java.util.List;


/**
 * Abstract panel handling all updates to assignment. Implemented as template pattern.
 *
 * @author thomasl
 */
public abstract class AbstractAssignmentPanel extends JPanel implements IEditMessageComponentIf
{
    protected static final String ASSIGNMENT_OVERVIEW_ID = "ASSIGNMENT OVERVIEW";
    protected static final String EDIT_ASSIGNMENT_ID = "EDIT ASSIGNMENT";
    protected static final String NEXT_ASSIGNMENT_ID = "NEXT ASSIGNMENT";
    protected static final String ASSIGNMENT_POOL_ID = "ASSIGNMENT POOL";

    protected IDiskoWpMessageLog m_wpMessageLog = null;

    protected JPanel m_cardsPanel = null;

    protected JPanel m_overviewPanel = null;
    protected JList m_assignmentLineList = null;
    protected JButton m_newButton = null;

    protected JPanel m_editAssignmentPanel = null;
    protected JLabel m_assignmentLabel = null;
    protected JLabel m_assignmentTextLabel = null;
    protected JLabel m_timeLabel = null;
    protected JTextField m_timeTextField = null;
    protected JButton m_okEditButton = null;
    protected JButton m_cancelEditButton = null;

    protected JPanel m_nextAssignmentsPanel = null;
    protected JPanel m_nextAssignmentsButtonPanel = null;
    protected JScrollPane m_nextAssignmentScrollPane = null;
    protected ButtonGroup m_nextAssignmentButtonGroup = null;
    protected JButton m_okAddNextAssignmentButton = null;
    protected JButton m_cancelAddNextAssignmentButton = null;

    protected JPanel m_assignmentPoolPanel = null;
    protected JPanel m_assignmentPoolButtonPanel = null;
    protected JScrollPane m_assignmentPoolScrollPane = null;
    protected ButtonGroup m_assignmentPoolButtonGroup = null;
    protected JButton m_okAddPoolAssignmentButton = null;
    protected JButton m_cancelAddPoolAssignmentButton = null;

    protected IAssignmentIf m_selectedAssignment = null;
    protected IMessageLineIf m_editingLine = null;

    protected List<IMessageLineIf> m_addedLines = new LinkedList<IMessageLineIf>();

    protected boolean m_notebookMode = true;

    public AbstractAssignmentPanel(IDiskoWpMessageLog wp)
    {
        m_wpMessageLog = wp;

        initialize();
    }

    protected void initialize()
    {
        this.setLayout(new BorderLayout());
        m_cardsPanel = new JPanel(new CardLayout());

        initAssignmentOverviewPanel();
        initEditAssignmentPanel();
        initNextAssignmentPanel();
        initAssignmentPoolPanel();

        this.add(m_cardsPanel, BorderLayout.CENTER);
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
                Calendar time = DTG.DTGToCal(m_timeTextField.getText());
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
                m_timeTextField.setText(DTG.CalToDTG(line.getOperationTime()));
            }
        }
    }

    protected void initAssignmentOverviewPanel()
    {
        m_overviewPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        m_assignmentLineList = new JList(new AssignmentListModel(m_wpMessageLog));
        m_assignmentLineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_assignmentLineList.addListSelectionListener(new AssignmentLineSelectionListener(m_assignmentLineList, this));
        m_assignmentLineList.setBorder(BorderFactory.createLineBorder(Color.black));
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        m_overviewPanel.add(m_assignmentLineList, gbc);

        m_newButton = DiskoButtonFactory.createNormalButton(m_wpMessageLog.getText("NewButton.text"),
                m_wpMessageLog.getText("NewButton.icon"));
        m_newButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                addNewMessageLine();
            }
        });
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        m_overviewPanel.add(m_newButton, gbc);

        m_cardsPanel.add(m_overviewPanel, ASSIGNMENT_OVERVIEW_ID);
    }

    protected void initEditAssignmentPanel()
    {
        m_editAssignmentPanel = new JPanel();
        m_editAssignmentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        m_assignmentLabel = new JLabel(m_wpMessageLog.getText("AssignmentLabel.text") + ":");
        m_editAssignmentPanel.add(m_assignmentLabel, gbc);

        gbc.gridx++;
        m_assignmentTextLabel = new JLabel();
        m_editAssignmentPanel.add(m_assignmentTextLabel, gbc);

        gbc.gridx--;
        gbc.gridy++;
        m_timeLabel = new JLabel();
        m_editAssignmentPanel.add(m_timeLabel, gbc);

        gbc.gridx++;
        m_timeTextField = new JTextField(6);
        if (m_notebookMode)
        {
            m_timeTextField.addFocusListener(new FocusListener()
            {
                public void focusGained(FocusEvent e)
                {
                    NumPadDialog numPad = m_wpMessageLog.getApplication().getUIFactory().getNumPadDialog();
                    // Don't display dialog again if returning from it
                    Component component = e.getOppositeComponent();
                    if (component != numPad.getOkButton())
                    {
                        numPad.setTextField(m_timeTextField);
                        Point location = m_timeTextField.getLocationOnScreen();
                        location.x += m_timeTextField.getWidth();
                        numPad.setLocation(location);
                        numPad.setVisible(true);
                    }
                }

                public void focusLost(FocusEvent e)
                {
                }
            });
        }

        m_editAssignmentPanel.add(m_timeTextField, gbc);

        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridy--;
        gbc.gridx++;
        m_cancelEditButton = DiskoButtonFactory.createNormalButton(ButtonType.CancelButton);
        m_cancelEditButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //Revert to values stored in MSO
                revertEditPanel();
            }
        });
        m_editAssignmentPanel.add(m_cancelEditButton, gbc);

        gbc.gridy++;
        m_okEditButton = DiskoButtonFactory.createNormalButton(ButtonType.OkButton);
        m_okEditButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updateMessageLine();
            }
        });
        m_editAssignmentPanel.add(m_okEditButton, gbc);

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

        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new BoxLayout(actionButtonPanel, BoxLayout.PAGE_AXIS));

        m_cancelAddNextAssignmentButton = DiskoButtonFactory.createNormalButton(ButtonType.CancelButton);
        m_cancelAddNextAssignmentButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                showComponent();
            }
        });
        actionButtonPanel.add(m_cancelAddNextAssignmentButton);

        m_okAddNextAssignmentButton = DiskoButtonFactory.createNormalButton(ButtonType.OkButton);
        m_okAddNextAssignmentButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addSelectedAssignment();
            }
        });
        actionButtonPanel.add(m_okAddNextAssignmentButton);
        m_nextAssignmentsPanel.add(actionButtonPanel, BorderLayout.EAST);

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

        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new BoxLayout(actionButtonPanel, BoxLayout.PAGE_AXIS));
        m_cancelAddPoolAssignmentButton = DiskoButtonFactory.createNormalButton(ButtonType.CancelButton);
        m_cancelAddPoolAssignmentButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                showComponent();
            }
        });
        actionButtonPanel.add(m_cancelAddPoolAssignmentButton);
        m_okAddPoolAssignmentButton = DiskoButtonFactory.createNormalButton(ButtonType.OkButton);
        m_okAddPoolAssignmentButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addSelectedAssignment();
            }
        });
        actionButtonPanel.add(m_okAddPoolAssignmentButton);
        m_assignmentPoolPanel.add(actionButtonPanel, BorderLayout.EAST);

        m_cardsPanel.add(m_assignmentPoolPanel, ASSIGNMENT_POOL_ID);
    }

    /**
     *
     */
    public void clearContents()
    {
    }

    /**
     *
     */
    public void hideComponent()
    {
        this.setVisible(false);
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
        CardLayout layout = (CardLayout) m_cardsPanel.getLayout();
        layout.show(m_cardsPanel, ASSIGNMENT_OVERVIEW_ID);
    }

    /**
     * Show all assignments available from command post
     */
    EnumSet<AssignmentStatus> m_assignmentPoolSet = EnumSet.of
            (
                    AssignmentStatus.ABORTED,
                    AssignmentStatus.DRAFT,
                    AssignmentStatus.EMPTY,
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
        int numButtonsInRow = m_assignmentPoolButtonPanel.getWidth() / DiskoButtonFactory.NORMAL_BUTTON_SIZE.width;
        for (final IAssignmentIf assignment : assignments)
        {
            JToggleButton button = DiskoButtonFactory.createNormalAssignmentToggleButton(assignment);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // Select button
                    JToggleButton sourceButton = (JToggleButton) e.getSource();
                    selectAssignmentButton(sourceButton, m_assignmentPoolButtonGroup);
                    m_selectedAssignment = assignment;
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
            m_selectedAssignment = assignmentIt.hasNext() ? assignmentIt.next() : null;
        }
        catch (Exception e)
        {
        }

        CardLayout layout = (CardLayout) m_cardsPanel.getLayout();
        layout.show(m_cardsPanel, ASSIGNMENT_POOL_ID);
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
            JToggleButton button = DiskoButtonFactory.createLongToggleButton(assignment);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // Mark icon as selected
                    JToggleButton sourceButton = (JToggleButton) e.getSource();
                    selectAssignmentButton(sourceButton, m_nextAssignmentButtonGroup);
                    m_selectedAssignment = assignment;
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
            m_selectedAssignment = assignmentIt.hasNext() ? assignmentIt.next() : null;
        }
        catch (Exception e)
        {
        }

        CardLayout layout = (CardLayout) m_cardsPanel.getLayout();
        layout.show(m_cardsPanel, NEXT_ASSIGNMENT_ID);
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
    protected void showEditAssignment(int selectedAssignment)
    {
        AssignmentListModel model = (AssignmentListModel) m_assignmentLineList.getModel();
        m_editingLine = (IMessageLineIf) model.getElementAt(selectedAssignment);
        if (m_editingLine == null)
        {
            Log.error("showEditAssignment: edit line null");
            return;
        }
        IAssignmentIf assignment = m_editingLine.getLineAssignment();

        m_assignmentTextLabel.setText(assignment.getTypeAndNumber());
        m_timeTextField.setText(DTG.CalToDTG(m_editingLine.getOperationTime()));

        CardLayout layout = (CardLayout) m_cardsPanel.getLayout();
        layout.show(m_cardsPanel, EDIT_ASSIGNMENT_ID);
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
}
