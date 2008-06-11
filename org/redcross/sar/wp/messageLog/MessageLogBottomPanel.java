package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITaskListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IMessageIf.MessageStatus;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.AssignmentTransferUtilities;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.wp.messageLog.IDiskoWpMessageLog.MessageLogActionType;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.JTableHeader;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Top edit panel
 *
 * @author thomasl
 */
public class MessageLogBottomPanel extends JPanel implements IMsoUpdateListenerIf, IDiskoWorkListener
{
	private static final long serialVersionUID = 1L;

	public static final int PANEL_HEIGHT = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL).height * 6;
	public static final int SMALL_PANEL_WIDTH = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL).width;

	private static final String EMPTY_PANEL_ID = "EMPTY_PANEL";
	private static final String TEXT_PANEL_ID = "TEXT_PANEL";
	private static final String POSITION_PANEL_ID = "POSITION_PANEL";
	private static final String FINDING_PANEL_ID = "FINDING_PANEL";
	private static final String ASSIGNED_PANEL_ID = "ASSIGNED_PANEL";
	private static final String STARTED_PANEL_ID = "STARTED_PANEL";
	private static final String COMPLETED_PANEL_ID = "COMPLETED_PANEL";
	private static final String SHOW_ASSIGNMENT_LIST_PANEL_ID = "SHOW_ASSIGNMENT_LIST_PANEL";

	private static IDiskoWpMessageLog m_wpMessageLog;

	// Current message Singleton
	private static boolean m_newMessage;
	private static IMessageIf m_currentMessage = null;
	private static boolean m_messageDirty = false;
	
	/**
	 * Get current message. Singleton

	 * @param create Whether to create new or not if current message is {@code null}
	 * 
	 * @return The message
	 */
	public static IMessageIf getCurrentMessage(boolean create)
	{		
		return getCurrentMessage(create,false);
	}

	/**
	 * Get current message. Singleton
	 * 
	 * @param create Whether to create new or not if current message is {@code null}
	 * @param isBroadcast Sets Broadcast flag if message is created
	 * 
	 * @return The message
	 */
	public static IMessageIf getCurrentMessage(boolean create, boolean isBroadcast)
	{
		if(m_currentMessage == null && create)
		{
			m_wpMessageLog.getMsoModel().suspendClientUpdate();
			m_currentMessage = m_wpMessageLog.getMsoManager().createMessage();
			m_currentMessage.setCreated(Calendar.getInstance());
			m_currentMessage.setOccuredTime(Calendar.getInstance());
			m_currentMessage.setBroadcast(isBroadcast);
			m_wpMessageLog.getMsoModel().resumeClientUpdate();
			m_newMessage = true;
			m_messageDirty = false;
		}
		return m_currentMessage;
	}
	
	/**
	 *
	 * @return Whether the current message is new or not, i.e. not stored in MSO
	 */
	public static boolean isNewMessage()
	{
		return m_newMessage;
	}

	private MessageLineType m_currentMessageLineType = null;

	static List<IEditMessageComponentIf> m_editComponents;

	private static ButtonGroup m_buttonGroup;

	private JPanel m_nrPanel;
	private static JLabel m_nrLabel;

	private JPanel m_dtgPanel;
	private static JLabel m_dtgLabel;
	private ChangeDTGDialog m_changeDTGDialog;
    private JToggleButton m_changeDTGButton;

    private JPanel m_fromPanel;
    private static JLabel m_fromLabel;
    private UnitFieldSelectionDialog m_fieldFromDialog;
    private SingleUnitListSelectionDialog m_listFromDialog;
    private JToggleButton  m_changeFromButton;

    private JPanel m_toPanel;
    private static JLabel m_toLabel;
    private ChangeToDialog m_changeToDialog;
    private JToggleButton  m_changeToButton;

    private JPanel m_messagePanel;

    private JPanel m_buttonRow;
	private static JToggleButton  m_textButton;
	private static JToggleButton  m_positionButton;
	private static JToggleButton  m_poiButton;
	private static JToggleButton  m_assignButton;
	private static JToggleButton  m_startButton;
	private static JToggleButton  m_completedButton;
	private static JToggleButton  m_listButton;
	private JButton m_deleteButton;

	private static JPanel m_cardsPanel;
	private static TextPanel m_textPanel;
	private static MessagePositionPanel m_messagePositionPanel;
	private static MessagePOIPanel m_messagePOIPanel;
	private static AbstractAssignmentPanel m_messageAssignedPanel;
	private static AbstractAssignmentPanel m_messageStartedPanel;
	private static AbstractAssignmentPanel m_messageCompletedPanel;
	private static MessageLinePanel m_messageListPanel;

    private JPanel m_taskPanel;
    private static JLabel m_taskLabel;
    private ChangeTasksDialog m_changeTasksDialog;
    private JToggleButton  m_changeTasksButton;

    private static JPanel m_statusPanel;
    private static JButton m_cancelStatusButton;
    private static JButton m_waitEndStatusButton;
    private static JButton m_finishedStatusButton;

    /**
     * Constructor
     */
    public MessageLogBottomPanel()
    {
    	m_newMessage = true;
    	m_editComponents = new LinkedList<IEditMessageComponentIf>();
    	
    }

    /**
	 * Initialize GUI components
	 */
	public void initialize(JTable logTable)
	{
		initButtons();
    	initPanels(logTable);
    	initComponents();
	}

    private JPanel createPanel(int width, int height)
    {
    	JPanel panel = new JPanel();
    	panel.setLayout(new BorderLayout());

    	panel.setMinimumSize(new Dimension(width, height));
    	panel.setPreferredSize(new Dimension(width, height));
    	panel.setMaximumSize(new Dimension(width, height));

    	return panel;
    }

    private ChangeDTGDialog getChangeDTGDialog()
    {
    	if(m_changeDTGDialog == null)
    	{
    		m_changeDTGDialog = new ChangeDTGDialog(m_wpMessageLog);
    		m_changeDTGDialog.addDiskoWorkListener(this);
    		m_editComponents.add(m_changeDTGDialog);
    	}
    	return m_changeDTGDialog;
    }

    private UnitFieldSelectionDialog getFieldChangeFromDialog()
    {
    	if(m_fieldFromDialog == null)
    	{
    		m_fieldFromDialog = new UnitFieldSelectionDialog(m_wpMessageLog, true);
    		m_fieldFromDialog.addDiskoWorkListener(this);
    		m_fieldFromDialog.getOKButton().addActionListener(new ActionListener()
    		{
				public void actionPerformed(ActionEvent e)
				{
					ICommunicatorIf sender = m_fieldFromDialog.getCommunicator();
					if(sender != null)
					{
						getCurrentMessage(true).setSender(sender);
					}
					m_fieldFromDialog.hideComponent();
					m_listFromDialog.hideComponent();
				}
    			
    		});
    		m_editComponents.add(m_fieldFromDialog);
    	}

    	return m_fieldFromDialog;
    }

    private SingleUnitListSelectionDialog getListChangeFromDialog()
    {
    	if(m_listFromDialog == null)
    	{
    		m_listFromDialog = new SingleUnitListSelectionDialog(m_wpMessageLog, true);
    		m_listFromDialog.addDiskoWorkListener(this);
    		m_editComponents.add(m_listFromDialog);
    	}
    	return m_listFromDialog;
    }

    private ChangeToDialog getChangeToDialog()
	{
		if(m_changeToDialog == null)
		{
			m_changeToDialog = new ChangeToDialog(m_wpMessageLog);
			m_changeToDialog.addDiskoWorkListener(this);
			m_editComponents.add(m_changeToDialog);
		}
		return m_changeToDialog;
	}

    private TextPanel getMessageTextPanel()
    {
    	if(m_textPanel == null)
    	{
    		m_textPanel = new TextPanel(m_wpMessageLog);
    		m_editComponents.add(m_textPanel);
    		m_cardsPanel.add(m_textPanel, TEXT_PANEL_ID);
    	}
    	return m_textPanel;
    }

    private MessagePositionPanel getMessagePositionPanel()
    {
    	if(m_messagePositionPanel == null)
    	{
    		m_messagePositionPanel = new MessagePositionPanel(m_wpMessageLog);
    		m_editComponents.add(m_messagePositionPanel);
    		m_cardsPanel.add(m_messagePositionPanel, POSITION_PANEL_ID);
    	}
    	return m_messagePositionPanel;
    }

    private MessagePOIPanel getMessagePOIPanel()
    {
    	if(m_messagePOIPanel == null)
    	{
    		POIType[] poiTypes = {POIType.GENERAL,POIType.INTELLIGENCE,POIType.OBSERVATION,POIType.FINDING, POIType.SILENT_WITNESS};
    		m_messagePOIPanel = new MessagePOIPanel(m_wpMessageLog, poiTypes);
    		m_editComponents.add(m_messagePOIPanel);
    		m_cardsPanel.add(m_messagePOIPanel, FINDING_PANEL_ID);
    	}
    	return m_messagePOIPanel;
    }

    private void getAssignedPanel()
    {
    	if(m_messageAssignedPanel == null)
    	{
    		m_messageAssignedPanel = new AssignedAssignmentPanel(m_wpMessageLog);
    		m_editComponents.add(m_messageAssignedPanel);
    		m_cardsPanel.add(m_messageAssignedPanel, ASSIGNED_PANEL_ID);
    	}
    }

    private void getStartedPanel()
    {
    	if(m_messageStartedPanel == null)
    	{
    		m_messageStartedPanel = new StartedAssignmentPanel(m_wpMessageLog);
    		m_editComponents.add(m_messageStartedPanel);
    		m_cardsPanel.add(m_messageStartedPanel, STARTED_PANEL_ID);
    	}
    }
    private void getCompletedPanel()
	{
		if(m_messageCompletedPanel == null)
		{
			m_messageCompletedPanel = new CompletedAssignmentPanel(m_wpMessageLog);
			m_editComponents.add(m_messageCompletedPanel);
			m_cardsPanel.add(m_messageCompletedPanel, COMPLETED_PANEL_ID);
		}

	}

    private MessageLinePanel getMessageListPanel()
    {
    	if(m_messageListPanel == null)
    	{
    		m_messageListPanel = new MessageLinePanel(m_wpMessageLog);
    		m_editComponents.add(m_messageListPanel);
    		m_cardsPanel.add(m_messageListPanel, SHOW_ASSIGNMENT_LIST_PANEL_ID);
    	}
    	return m_messageListPanel;
    }

    private ChangeTasksDialog getChangeTasksDialog()
	{
		if(m_changeTasksDialog == null)
		{
			m_changeTasksDialog = new ChangeTasksDialog(m_wpMessageLog);
			m_changeTasksDialog.addDiskoWorkListener(this);
			m_editComponents.add(m_changeTasksDialog);
		}
		return m_changeTasksDialog;
	}

    protected static void clearPanelContents()
	{
    	m_nrLabel.setText("");
		m_dtgLabel.setText("");
		m_fromLabel.setText("");
		m_toLabel.setText("");
		m_taskLabel.setText("");

		for(IEditMessageComponentIf component : m_editComponents)
		{
			component.hideComponent();
			component.clearContents();
		}
	}

    private void initPanels(JTable logTable)
    {
    	this.setLayout(new GridBagLayout());
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weightx = 1.0;
    	gbc.weighty = 0.0;
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	
    	// Add table header
    	gbc.gridwidth = 15;
    	JTableHeader header = logTable.getTableHeader();
    	// Remove mouse listeners, hack to bypass Swing bug 4178930
    	for(MouseListener ml : header.getMouseListeners())
    	{
    		header.removeMouseListener(ml);
    	}
    	this.add(header, gbc);
    	logTable.setTableHeader(null);
    	
    	gbc.gridwidth = 1;
    	gbc.weighty = 1.0;
    	gbc.weightx = 0.0;

    	// Nr panel
        m_nrPanel = createPanel(SMALL_PANEL_WIDTH - 3, PANEL_HEIGHT - header.getHeight());
        m_nrLabel = new JLabel();
        m_nrPanel.add(m_nrLabel, BorderLayout.CENTER);
        m_nrPanel.add(Box.createRigidArea(DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL)), BorderLayout.SOUTH);
        gbc.gridy = 1;
        this.add(m_nrPanel, gbc);
        gbc.gridx++;
        this.add(new JSeparator(JSeparator.VERTICAL), gbc);

        // DTG panel
        m_dtgPanel = createPanel(SMALL_PANEL_WIDTH + 9, PANEL_HEIGHT - header.getHeight());
        m_dtgLabel = new JLabel();
        m_dtgPanel.add(m_dtgLabel, BorderLayout.CENTER);
        m_dtgPanel.add(m_changeDTGButton, BorderLayout.SOUTH);
        gbc.gridx++;
        this.add(m_dtgPanel, gbc);
        gbc.gridx++;
        this.add(new JSeparator(JSeparator.VERTICAL), gbc);
        
        // From panel
        m_fromPanel = createPanel(SMALL_PANEL_WIDTH, PANEL_HEIGHT - header.getHeight());
        m_fromLabel = new JLabel();
        m_fromPanel.add(m_fromLabel, BorderLayout.CENTER);
        m_fromPanel.add(m_changeFromButton, BorderLayout.SOUTH);
        gbc.gridx++;
        this.add(m_fromPanel, gbc);
        gbc.gridx++;
        this.add(new JSeparator(JSeparator.VERTICAL), gbc);
        
        // To panel
        m_toPanel = createPanel(SMALL_PANEL_WIDTH, PANEL_HEIGHT - header.getHeight());
        m_toLabel = new JLabel();
        m_toPanel.add(m_toLabel, BorderLayout.CENTER);
        m_toPanel.add(m_changeToButton, BorderLayout.SOUTH);
        gbc.gridx++;
        this.add(m_toPanel, gbc);
        gbc.gridx++;
        this.add(new JSeparator(JSeparator.VERTICAL), gbc);

        // Message panel
        gbc.weightx = 1.0;
        m_messagePanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(m_messagePanel, BoxLayout.Y_AXIS);
        m_messagePanel.setLayout(boxLayout);
        m_cardsPanel = new JPanel();
        m_cardsPanel.setLayout(new CardLayout());
        m_cardsPanel.setPreferredSize(new Dimension(SMALL_PANEL_WIDTH*9, PANEL_HEIGHT - header.getHeight()-100));
        m_cardsPanel.setAlignmentX(0.0f);
        m_cardsPanel.add(new JPanel(), EMPTY_PANEL_ID);
        CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
        layout.show(m_cardsPanel, EMPTY_PANEL_ID);
        m_messagePanel.add(m_cardsPanel);

        m_buttonRow.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        m_buttonRow.setMaximumSize(new Dimension(SMALL_PANEL_WIDTH*9,
        		(int)DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL).height));
        m_buttonRow.setAlignmentX(0.0f);
        m_messagePanel.add(m_buttonRow);
        gbc.gridx++;
        this.add(m_messagePanel, gbc);
        gbc.weightx = 0.0;
        gbc.gridx++;
        this.add(new JSeparator(JSeparator.VERTICAL), gbc);

        // Task panel
        gbc.weightx = 0.0;
        m_taskPanel = createPanel(2*SMALL_PANEL_WIDTH - 1, PANEL_HEIGHT - header.getHeight());
        m_taskLabel = new JLabel();
        m_taskPanel.add(m_taskLabel, BorderLayout.CENTER);
        JPanel taskButtonPanel = new JPanel();
        taskButtonPanel.setLayout(new BoxLayout(taskButtonPanel, BoxLayout.PAGE_AXIS));
        taskButtonPanel.setBorder(null);
        taskButtonPanel.add(m_changeTasksButton);
        m_taskPanel.add(taskButtonPanel, BorderLayout.SOUTH);
        gbc.gridx++;
        this.add(m_taskPanel, gbc);
        gbc.gridx++;
        this.add(new JSeparator(JSeparator.VERTICAL), gbc);
        
        // Fill to match table scroll-bar
        gbc.gridx++;
        this.add(Box.createRigidArea(new Dimension(43, 10)), gbc);
        
        // Status panel
        m_statusPanel = createPanel(SMALL_PANEL_WIDTH + 18, PANEL_HEIGHT - header.getHeight());
        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new BoxLayout(actionButtonPanel, BoxLayout.PAGE_AXIS));
        actionButtonPanel.add(m_cancelStatusButton);
        actionButtonPanel.add(m_waitEndStatusButton);
        actionButtonPanel.add(m_finishedStatusButton);
        m_statusPanel.add(actionButtonPanel, BorderLayout.SOUTH);
        gbc.gridx++;
        this.add(m_statusPanel, gbc);
    }

    private void initComponents()
    {
    	getChangeDTGDialog();
    	getFieldChangeFromDialog();
    	getListChangeFromDialog();
    	getChangeToDialog();
    	getMessageTextPanel();
    	getMessagePositionPanel();
    	getMessagePOIPanel();
    	getAssignedPanel();
    	getStartedPanel();
    	getCompletedPanel();
    	getChangeTasksDialog();
    	getMessageListPanel();
    	
    	// Register listeners
		m_fieldFromDialog.addActionListener(m_listFromDialog);
    }

    private void initButtons()
    {
    	m_buttonRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
    	m_buttonGroup = new ButtonGroup();

    	getChangeDTGButton();
        getChangeFromButton();
        getChangeToButton();
        getTextButton();
        getPositionButton();
        getPOIButton();
        getAssignedButton();
        getStartedButton();
        getCompletedButton();
        getListButton();
        getDeleteButton();
        getChangeTasksButton();
        getCancelButton();
        getWaitEndButton();
        getFinishedButton();
    }

    /**
     * An existing message is selected in the message log for editing.
     * @param aMessage The selected message
     */
	public void newMessageSelected(IMessageIf aMessage)
	{
		// Have user confirm message overwrite
		if(m_currentMessage != null && (m_currentMessage != aMessage) && m_messageDirty)
		{

			Object[] options = {m_wpMessageLog.getBundleText("yes.text"), m_wpMessageLog.getBundleText("no.text")};
			int n = JOptionPane.showOptionDialog(m_wpMessageLog.getApplication().getFrame(),
					m_wpMessageLog.getBundleText("DirtyMessageWarning.text"),
					m_wpMessageLog.getBundleText("DirtyMessageWarning.header"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			if(n == JOptionPane.NO_OPTION)
			{
				return;
			}
			else
			{
				// Roll back any changes made to the message
				if(m_newMessage)
				{
					m_currentMessage.deleteObject();
				}
				else
				{
					rollback();
				}
			}
		}
        m_messageDirty = false;
        m_newMessage = false;
        m_currentMessage = aMessage;
        updateMessageGUI();
	}

	private void updateMessageGUI()
	{
		// has current message?
		if(m_currentMessage != null)
		{
			
			// Update panel contents
			m_nrLabel.setText(Integer.toString(m_currentMessage.getNumber()));
			m_dtgLabel.setText(DTG.CalToDTG(m_currentMessage.getOccuredTime()));

			// Update FROM
			ICommunicatorIf sender = m_currentMessage.getSender();
			if(sender == null)
			{
				sender = (ICommunicatorIf)m_wpMessageLog.getCmdPost();
			}
			m_fromLabel.setText(sender.getCommunicatorNumberPrefix() + " " + sender.getCommunicatorNumber());

			// Update TO
			if(m_currentMessage.isBroadcast())
			{
				int unconfirmed = m_currentMessage.getUnconfirmedReceivers().size();
				int count = unconfirmed + m_currentMessage.getConfirmedReceivers().size();
				m_toLabel.setText(String.format(m_wpMessageLog.getBundleText("BroadcastLabel.text"),
						(count-unconfirmed),count));
			}
			else
			{
				ICommunicatorIf receiver = m_currentMessage.getSingleReceiver();
				if(receiver == null)
				{
					receiver = (ICommunicatorIf)m_wpMessageLog.getMsoManager().getCmdPost();
				}
				m_toLabel.setText(receiver.getCommunicatorNumberPrefix() + " " + receiver.getCommunicatorNumber());
			}
			
			// get TASKs
			ITaskListIf tasks = m_currentMessage.getMessageTasks();
			StringBuilder tasksString = new StringBuilder("<html>");
			for(ITaskIf task : tasks.getItems())
			{
				tasksString.append(task.getTaskText() + "<br>");
			}
			tasksString.append("</html>");
			m_taskLabel.setText(tasksString.toString());

			// Update dialogs
			for(int i=0; i<m_editComponents.size(); i++)
			{
				m_editComponents.get(i).newMessageSelected(m_currentMessage);
			}
		}
		setButtonColors();
	}

    /**
     * Set the message log reference
     * @param wp
     */
	public void setWp(IDiskoWpMessageLog wp)
	{
		m_wpMessageLog = wp;
	}

	/**
	 * {@link IMsoUpdateListenerIf#handleMsoUpdateEvent(Update)}
	 */
	public void handleMsoUpdateEvent(Update e)
	{
		updateMessageGUI();
	}

	private final EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of(
			IMsoManagerIf.MsoClassCode.CLASSCODE_MESSAGE,
			IMsoManagerIf.MsoClassCode.CLASSCODE_MESSAGELINE,
			IMsoManagerIf.MsoClassCode.CLASSCODE_POI);

	/**
	 * {@link IMsoUpdateListenerIf#hasInterestIn(IMsoObjectIf)}
	 */
	public boolean hasInterestIn(IMsoObjectIf msoObject)
	{
		 return myInterests.contains(msoObject.getMsoClassCode());
	}

	public void onWorkPerformed(DiskoWorkEvent e)
	{
		if(e.isFinish()) {
			// If no message is selected a new one should be created once a field is edited
			if(m_currentMessage == null)
			{
				getCurrentMessage(true);
			}
			hideEditPanels();
			m_buttonGroup.clearSelection();
			m_messageDirty = true;
			updateMessageGUI();			
		}
		else if(e.isCancel()) {
			//Object sender = e.getSource();
			m_buttonGroup.clearSelection();
			hideEditPanels();
		}
		// forward
		m_wpMessageLog.onWorkPerformed(e);
	}


	private static void setButtonColors() {
		// get flag
		boolean isDirty = m_currentMessage!=null && m_newMessage || m_messageDirty;
		// reset color
		((DiskoIcon)m_cancelStatusButton.getIcon()).setColored(isDirty);
		m_cancelStatusButton.repaint();
		((DiskoIcon)m_finishedStatusButton.getIcon()).setColored(isDirty);
		m_finishedStatusButton.repaint();
	}
	
	public static boolean validateMessage() {
		// has any data to commit?
		if(m_currentMessage == null) {
			Utils.showWarning(m_wpMessageLog.getBundleText("NoMessageData.header"),
					m_wpMessageLog.getBundleText("NoMessageData.text"));
			return false;
		}
		// has lines?
		if(m_currentMessage != null && m_currentMessage.getLines().length==0) {
			Utils.showWarning(m_wpMessageLog.getBundleText("NoMessageLines.header"),
					m_wpMessageLog.getBundleText("NoMessageLines.text"));
			return false;
		}
		// is valid
		return true;			
	}
	
	private JButton getWaitEndButton()
    {
    	if(m_waitEndStatusButton == null)
    	{
    		m_waitEndStatusButton = DiskoButtonFactory.createButton("GENERAL.WAIT",ButtonSize.NORMAL);
    				    				
    		m_waitEndStatusButton.addActionListener(new ActionListener()
    		{
				public void actionPerformed(ActionEvent arg0)
				{
					// validate
					if(validateMessage())
					{
						
						// set status to postponed
						m_currentMessage.setStatus(MessageStatus.POSTPONED);

						// reload data?
						if(m_newMessage)
							MessageLogPanel.setTableData();
						
						// forward
						commit();

						m_messageDirty = false;

						for(IEditMessageComponentIf dialog : m_editComponents)
						{
							dialog.hideComponent();
						}

						m_buttonGroup.clearSelection();
						clearPanelContents();
						
    					// reset color
						setButtonColors();
    					
					}
				}
    		});
    	}
    	return m_waitEndStatusButton;
    }

    private JButton getFinishedButton()
    {
    	if(m_finishedStatusButton == null)
    	{
    		m_finishedStatusButton = DiskoButtonFactory.createButton("SYSTEM.COMMIT",ButtonSize.NORMAL);
    		m_finishedStatusButton.setIcon(new DiskoIcon(m_finishedStatusButton.getIcon(),Color.GREEN,0.4f));
			((DiskoIcon)m_finishedStatusButton.getIcon()).setColored(false);
    		m_finishedStatusButton.addActionListener(new ActionListener()
    		{
				// Commit current message
				public void actionPerformed(ActionEvent arg0)
				{
					// forward
					apply(true);
					
				}
    		});
    	}
    	return m_finishedStatusButton;
    }

    public static boolean apply(boolean isConfirmed) {

    	// validate
		if(validateMessage())
		{
			// initialize
			MessageStatus status = (isConfirmed ? MessageStatus.CONFIRMED : MessageStatus.POSTPONED);
			
			// Set message status
			if(m_currentMessage.isBroadcast()){


				// apply limits
				if(isConfirmed && m_currentMessage.getUnconfirmedReceivers().size() > 0){
					// If broadcast all units have to confirm to get confirmed status
					Utils.showWarning(m_wpMessageLog.getBundleText("UnconfirmedUnitsExists.header"),
							m_wpMessageLog.getBundleText("UnconfirmedUnitsExists.text"));
					// keep current status
					status = m_currentMessage.getStatus();
				}
			}

			// apply status
			m_currentMessage.setStatus(status);				

			// reload data?
			if(m_newMessage)
				MessageLogPanel.setTableData();
			
			// Handle assignments
			updateAssignments();
			
			// Commit changes
			commit();

			m_currentMessage = null;
			m_messageDirty = false;

			// GUI clean-up
			clearPanelContents();
			m_buttonGroup.clearSelection();
			
			// reset color
			setButtonColors();
			
			// success
			return true;
			
		}
		// failed
		return true;    	
    }
    
    private static void commit() {
		m_wpMessageLog.getMsoModel().commit();
		m_wpMessageLog.onWorkPerformed(new DiskoWorkEvent(m_wpMessageLog,DiskoWorkEvent.EVENT_COMMIT));
    }
    
    private static void rollback() {
		m_wpMessageLog.getMsoModel().rollback();
		m_wpMessageLog.onWorkPerformed(new DiskoWorkEvent(m_wpMessageLog,DiskoWorkEvent.EVENT_ROLLBACK));
    }
    
    private JToggleButton getChangeDTGButton()
    {
    	m_changeDTGButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.CHANGE_DTG,ButtonSize.NORMAL);
    	m_changeDTGButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    	m_changeDTGButton.addActionListener(new ActionListener()
    	{
    		// Display the change DTG dialog when button is pressed
    		public void actionPerformed(ActionEvent e)
    		{
    			if(getChangeDTGDialog().isVisible())
    			{
    				getChangeDTGDialog().hideComponent();
    				m_buttonGroup.clearSelection();
    			}
    			else
    			{
    				hideEditPanels();
        			getChangeDTGDialog();
        			Point location = m_changeDTGButton.getLocationOnScreen();
        			location.y -= m_changeDTGDialog.getHeight();
        			m_changeDTGDialog.setLocation(location);
        			m_changeDTGDialog.showComponent();
    			}
    		}
    	});
    	m_buttonGroup.add(m_changeDTGButton);
    	return m_changeDTGButton;
    }

    private JToggleButton  getChangeTasksButton()
    {
    	if(m_changeTasksButton == null)
    	{
    		m_changeTasksButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.CHANGE_TASKS,ButtonSize.NORMAL);
    		m_changeTasksButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    		m_changeTasksButton.addActionListener(new ActionListener()
    		{
				public void actionPerformed(ActionEvent e)
				{
					if(getChangeTasksDialog().isVisible())
					{
						getChangeTasksDialog().hideComponent();
						m_buttonGroup.clearSelection();
					}
					else
					{
						// Create new message if null
						getCurrentMessage(true);
						m_messageDirty = true;

						getChangeTasksDialog();
						hideEditPanels();
						Point location = m_changeTasksButton.getLocationOnScreen();
		    			location.y -= m_changeTasksDialog.getHeight();
		    			location.x -= m_changeTasksDialog.getWidth();
		    			m_changeTasksDialog.setLocation(location);
						m_changeTasksDialog.showComponent();
					}
				}
    		});
    		m_buttonGroup.add(m_changeTasksButton);
    	}
    	return m_changeTasksButton;
    }

    private JToggleButton  getChangeFromButton()
    {
    	if(m_changeFromButton == null)
    	{
    		m_changeFromButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.CHANGE_FROM,ButtonSize.NORMAL);
    		m_changeFromButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    		m_changeFromButton.addActionListener(new ActionListener()
    		{
				public void actionPerformed(ActionEvent arg0)
				{
					if(getFieldChangeFromDialog().isVisible())
					{
						// Toggle dialogs if visible
						getFieldChangeFromDialog().hideComponent();
						getListChangeFromDialog().hideComponent();
						m_buttonGroup.clearSelection();
					}
					else
					{
						hideEditPanels();
						
						// Initialize fields
						if(m_currentMessage != null)
						{
							ICommunicatorIf sender = m_currentMessage.getSender();
							if(sender != null)
							{
								m_fieldFromDialog.setCommunicatorNumber(sender.getCommunicatorNumber());
								m_fieldFromDialog.setCommunicatorNumberPrefix(sender.getCommunicatorNumberPrefix());
							}
						}
						
						Point location = m_changeFromButton.getLocationOnScreen();
						location.y -= m_fieldFromDialog.getHeight();
						m_fieldFromDialog.setLocation(location);
						m_fieldFromDialog.showComponent();
						
						location = m_changeFromButton.getLocationOnScreen();
						location.y -= m_listFromDialog.getHeight();
						location.x += m_fieldFromDialog.getWidth();
						m_listFromDialog.setLocation(location);
						m_listFromDialog.showComponent();
					}
				}
    		});
    		m_buttonGroup.add(m_changeFromButton);
    	}
    	return m_changeFromButton;
    }

    private JToggleButton  getChangeToButton()
    {
    	if(m_changeToButton == null)
    	{
    		m_changeToButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.CHANGE_TO,ButtonSize.NORMAL);
    		m_changeToButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    		m_changeToButton.addActionListener(new ActionListener()
    		{
				public void actionPerformed(ActionEvent e)
				{
					if(getChangeToDialog().isVisible())
					{
						getChangeToDialog().hideComponent();
						m_buttonGroup.clearSelection();
					}
					else
					{
						getChangeToDialog();
						hideEditPanels();
						Point location = m_changeToButton.getLocationOnScreen();
						location.y -= DiskoButtonFactory.getButtonSize(ButtonSize.LONG).height;
						m_changeToDialog.setLocation(location);
						m_changeToDialog.showComponent();
					}
				}
    		});
    		m_buttonGroup.add(m_changeToButton);
    	}
    	return m_changeToButton;
    }

    private JButton getCancelButton()
    {
    	if(m_cancelStatusButton == null)
    	{
    		try {
    			m_cancelStatusButton = DiskoButtonFactory.createButton(MessageLogActionType.CANCEL,ButtonSize.NORMAL);
    			m_cancelStatusButton.setIcon(new DiskoIcon(m_cancelStatusButton.getIcon(),Color.RED,0.4f));
    			((DiskoIcon)m_cancelStatusButton.getIcon()).setColored(false);
        		m_cancelStatusButton.addActionListener(new ActionListener()
        		{
    				public void actionPerformed(ActionEvent e)
    				{
    					// forward
    					clearCurrentMessage();
    					// reset color
						setButtonColors();
    				}
        		});    			
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    	return m_cancelStatusButton;
    }

    private void getDeleteButton()
	{
    	m_deleteButton = DiskoButtonFactory.createButton(MessageLogActionType.DELETE,ButtonSize.NORMAL);
		m_deleteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Delete if message is not committed, don't create new message when pressing delete
				if(m_currentMessage != null && m_currentMessageLineType != null && m_newMessage)
				{
					// If line that has other assignment lines depending on it is deleted, delete them as well
					switch(m_currentMessageLineType)
					{
					case ASSIGNED:
						for(IMessageLineIf line : m_messageAssignedPanel.getAddedLines())
						{
							line.deleteObject();
						}
						m_messageAssignedPanel.lineRemoved(null);
						break;
					case STARTED:
						for(IMessageLineIf line : m_messageStartedPanel.getAddedLines())
						{
							line.deleteObject();
						}
						m_messageStartedPanel.lineRemoved(null);
						break;
					case COMPLETED:
						for(IMessageLineIf line : m_messageCompletedPanel.getAddedLines())
						{
							line.deleteObject();
						}
						m_messageCompletedPanel.lineRemoved(null);
						break;
					default:
						IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
						IMessageLineIf line = null;
						if(message != null)
						{
							line = message.findMessageLine(m_currentMessageLineType, false);
						}
						if(line != null)
						{
							line.deleteObject();
						}
						else
							Utils.showWarning("Ingenting å slette");
						break;
					}
				}
				else
				{
					Utils.showWarning(m_wpMessageLog.getBundleText("CanNotDeleteMessageLine.header"),
							m_wpMessageLog.getBundleText("CanNotDeleteMessageLine.details"));
				}
			}
		});

		m_buttonGroup.add(m_deleteButton);
		m_buttonRow.add(m_deleteButton);
	}

	private void getListButton()
	{
		m_listButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SHOW_LIST,ButtonSize.NORMAL);
		m_listButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				getMessageListPanel();
				hideEditPanels();

				// show?
				if(m_currentMessage!=null && m_currentMessage.getLines().length>0) {
					// get panel
					CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
					layout.show(m_cardsPanel, SHOW_ASSIGNMENT_LIST_PANEL_ID);
					// show component
					m_messageListPanel.showComponent();
				}
			}

		});
		m_buttonGroup.add(m_listButton);
		m_buttonRow.add(m_listButton);
	}

	private void getStartedButton()
	{
		m_startButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_STARTED,ButtonSize.NORMAL);
		m_startButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				hideEditPanels();

				if(getCurrentMessage(true).isBroadcast())
				{
					Utils.showWarning(m_wpMessageLog.getBundleText("StartedError.header"),
							m_wpMessageLog.getBundleText("StartedError.details"));
				}
				else if(isSenderCommandPost())
				{
					// Not possible to assign when receiver is CP
					Utils.showWarning(m_wpMessageLog.getBundleText("SenderCommandPostError.header"),
							m_wpMessageLog.getBundleText("SenderCommandPostError.details"));
				}
				else if(!isAssignmentOperationLegal())
				{
					// Require certain message status
					Utils.showWarning(m_wpMessageLog.getBundleText("MessageTaskOperationError.header"),
							m_wpMessageLog.getBundleText("MessageTaskOperationError.details"));
				}
				else
				{
					getStartedPanel();
					m_currentMessageLineType = MessageLineType.STARTED;

					CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
					layout.show(m_cardsPanel, STARTED_PANEL_ID);
					m_messageStartedPanel.showComponent();
				}
			}
		});
		m_buttonGroup.add(m_startButton);
		m_buttonRow.add(m_startButton);
	}

	private void getAssignedButton()
	{
		m_assignButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_ASSIGNMENT,ButtonSize.NORMAL);
		m_assignButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				hideEditPanels();

				if(getCurrentMessage(true).isBroadcast())
				{
					// Only legal if message isn't broadcast
					Utils.showWarning(m_wpMessageLog.getBundleText("AssignmentError.header"),
							m_wpMessageLog.getBundleText("AssignmentError.details"));
				}
				else if(isReceiverCommandPost())
				{
					// Not possible to assign when receiver is CP
					Utils.showWarning(m_wpMessageLog.getBundleText("ReceiverCommandPostError.header"),
							m_wpMessageLog.getBundleText("ReceiverCommandPostError.details"));
				}
				else if(!isAssignmentOperationLegal())
				{
					// Require certain message status
					Utils.showWarning(m_wpMessageLog.getBundleText("MessageTaskOperationError.header"),
							m_wpMessageLog.getBundleText("MessageTaskOperationError.details"));
				}
				else
				{
					// Task can be changed
					getAssignedPanel();
					m_currentMessageLineType = MessageLineType.ASSIGNED;

					CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
					layout.show(m_cardsPanel, ASSIGNED_PANEL_ID);
					m_messageAssignedPanel.showComponent();
				}
			}
		});
		m_buttonGroup.add(m_assignButton);
		m_buttonRow.add(m_assignButton);
	}
	
	private void getCompletedButton()
	{
		m_completedButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_COMPLETED,ButtonSize.NORMAL);
		m_completedButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				hideEditPanels();

				if(getCurrentMessage(true).isBroadcast())
				{
					// de-select button
					m_completedButton.setSelected(false);
					// not possible to assign when message is a broadcast
					Utils.showWarning(m_wpMessageLog.getBundleText("CompletedError.header"),
							m_wpMessageLog.getBundleText("CompletedError.details"));
				}
				else if(isSenderCommandPost())
				{
					// de-select button
					m_completedButton.setSelected(false);
					// Not possible to assign when receiver is CP
					Utils.showWarning(m_wpMessageLog.getBundleText("ReceiverCommandPostError.header"),
							m_wpMessageLog.getBundleText("ReceiverCommandPostError.details"));					
				}
				else if(!isAssignmentOperationLegal())
				{
					// de-select button
					m_completedButton.setSelected(false);
					// Require certain message status
					Utils.showWarning(m_wpMessageLog.getBundleText("MessageTaskOperationError.header"),
							m_wpMessageLog.getBundleText("MessageTaskOperationError.details"));
				}
				else
				{
					getCompletedPanel();
					m_currentMessageLineType = MessageLineType.COMPLETED;

					CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
					layout.show(m_cardsPanel, COMPLETED_PANEL_ID);
					m_messageCompletedPanel.showComponent();
					
				}
			}
		});
		m_buttonGroup.add(m_completedButton);
		m_buttonRow.add(m_completedButton);
	}
	
	private boolean isSenderCommandPost()
	{
		if(m_currentMessage != null)
		{
			ICommunicatorIf sender = m_currentMessage.getSender();
			if(sender != null)
			{
				return sender instanceof ICmdPostIf;
			}
			else
			{
				// sender is, by default, command post
				return true;
			}
		}
		
		return false;
	}

	private boolean isReceiverCommandPost()
	{
		if(m_currentMessage != null)
		{
			ICommunicatorIf receiver = m_currentMessage.getSingleReceiver();
			if(receiver != null)
			{
				return receiver instanceof ICmdPostIf;
			}
			else
			{
				// Receiver is, by default, command post
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isAssignmentOperationLegal()
	{
		if(m_newMessage)
		{
			return true;
		}

		if(m_currentMessage == null)
		{
			return false;
		}
		
		MessageStatus status = m_currentMessage.getStatus();
		return (status == MessageStatus.UNCONFIRMED || status == MessageStatus.POSTPONED);
	}

	/**
	 * Ensures that units and assignments affected by the added message lines in the current message are
	 * updated (status, etc.). Message lines are also updated according to what operations are legal
	 */
	private static void updateAssignments()
	{

		if(m_currentMessage != null && !m_currentMessage.isBroadcast())
		{
			// initialize
			IUnitIf unit = null;
			IAssignmentIf assignment = null;
			ICommunicatorIf communicator = null;			
			List<IMessageLineIf> messageLines = new LinkedList<IMessageLineIf>();

			// Get all assignment lines. Lines from complete is placed first, started second, assign last.
			// This should ensure that unit statuses are updated in the correct order
			messageLines.addAll(m_messageCompletedPanel.getAddedLines());
			messageLines.addAll(m_messageStartedPanel.getAddedLines());
			messageLines.addAll(m_messageAssignedPanel.getAddedLines());

			// Update status
			for(IMessageLineIf line : messageLines)
			{
				assignment = line.getLineAssignment();

				switch(line.getLineType())
				{
				case ASSIGNED:
					try
					{
						communicator = m_currentMessage.getSingleReceiver();
						if(communicator instanceof IUnitIf)
						{
							AssignmentTransferUtilities.assignAssignmentToUnit(assignment, (IUnitIf)communicator);
						}
					}
					catch(IllegalOperationException e)
					{
						line.deleteObject();
						Utils.showWarning(m_wpMessageLog.getBundleText("CanNotAssignError.header"),
								String.format(m_wpMessageLog.getBundleText("CanNotAssignError.details"), unit.getTypeAndNumber(), assignment.getTypeAndNumber()));
					}
					break;
				case STARTED:
					try
					{
						communicator = m_currentMessage.getSender();
						if(communicator instanceof IUnitIf)
						{
							AssignmentTransferUtilities.unitStartAssignment((IUnitIf)communicator, assignment);
						}
					}
					catch(IllegalOperationException e)
					{
						line.deleteObject();
						Utils.showWarning(m_wpMessageLog.getBundleText("CanNotStartError.header"),
								String.format(m_wpMessageLog.getBundleText("CanNotStartError.details"), unit.getTypeAndNumber(), assignment.getTypeAndNumber()));
					}
					break;
				case COMPLETED:
					try
					{
						communicator = m_currentMessage.getSender();
						if(communicator instanceof IUnitIf)
						{
							AssignmentTransferUtilities.unitCompleteAssignment((IUnitIf)communicator, assignment);
						}
					}
					catch(IllegalOperationException e)
					{
						line.deleteObject();
						Utils.showWarning(m_wpMessageLog.getBundleText("CanNotCompleteError.header"),
								String.format(m_wpMessageLog.getBundleText("CanNotCompleteError.details"), unit.getTypeAndNumber(), assignment.getTypeAndNumber()));
					}
					break;
				default:
					continue;
				}
			}

			// Keep track of which lines are added
			m_messageAssignedPanel.lineRemoved(null);
			m_messageStartedPanel.lineRemoved(null);
			m_messageCompletedPanel.lineRemoved(null);
		}
	}

	private void getPOIButton()
	{
		m_poiButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_POI,ButtonSize.NORMAL);
		m_poiButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				getMessagePOIPanel();
				hideEditPanels();
				m_currentMessageLineType = MessageLineType.POI;
				m_messagePOIPanel.setMapTool();

				CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
				layout.show(m_cardsPanel, FINDING_PANEL_ID);

				m_messagePOIPanel.showComponent();
			}
		});
		m_buttonGroup.add(m_poiButton);
		m_buttonRow.add(m_poiButton);
	}

	private void getPositionButton()
	{
		m_positionButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_POSITION,ButtonSize.NORMAL);
		m_positionButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				getMessagePositionPanel();
				hideEditPanels();
				m_currentMessageLineType = MessageLineType.POSITION;
				m_messagePositionPanel.setMapTool();

				CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
				layout.show(m_cardsPanel, POSITION_PANEL_ID);

				m_messagePositionPanel.showComponent();
			}
		});
		m_buttonGroup.add(m_positionButton);
		m_buttonRow.add(m_positionButton);
	}

	private void getTextButton()
	{
		m_textButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_TEXT,ButtonSize.NORMAL);
		m_textButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				getMessageTextPanel();
				hideEditPanels();
				m_currentMessageLineType = MessageLineType.TEXT;

				CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
				layout.show(m_cardsPanel, TEXT_PANEL_ID);

				m_textPanel.showComponent();
			}
		});
		m_buttonGroup.add(m_textButton);
		m_buttonRow.add(m_textButton);
	}

	/**
	 * Hides all panels
	 */
	public void hideEditPanels()
	{
		boolean hideAll = (m_currentMessage==null); 
		for(int i=0; i<m_editComponents.size(); i++)
		{
			// get component
			IEditMessageComponentIf edit = m_editComponents.get(i); 
			if(hideAll || !(edit instanceof MessageLinePanel))
				edit.hideComponent();
		}
	}

	/**
	 * Unselect all field edit buttons
	 */
	public void clearSelection()
	{
		m_buttonGroup.clearSelection();
	}

	public static void showAssignPanel()
	{
		m_assignButton.doClick();
	}

	public static void setIsDirty()
	{
		// set flag
		m_messageDirty = (m_currentMessage!=null);
		
		// forward
		setButtonColors();
		
	}
		
	public static void showListPanel()
	{
		m_listButton.doClick();
	}

	public static void showCompletePanel()
	{
		m_completedButton.doClick();
	}

	public static void showStartPanel()
	{
		m_startButton.doClick();
	}

	public static void showTextPanel()
	{
		m_textButton.doClick();
	}

	public static void showPositionPanel()
	{
		m_positionButton.doClick();
	}

	public static void showPOIPanel()
	{
		m_poiButton.doClick();
	}

	public static void cancelAssign()
	{
		m_messageAssignedPanel.cancelUpdate();
	}

	public static void cancelStarted()
	{
		m_messageStartedPanel.cancelUpdate();
	}

	public static boolean isMessageDirty()
	{
		return m_messageDirty || m_wpMessageLog.getMsoModel().hasUncommitedChanges();
	}

	/**
	 * Remove any changes since last commit. Clear panel contents
	 */
	public static void clearCurrentMessage()
	{
		if(m_newMessage && m_currentMessage != null)
		{
			m_currentMessage.deleteObject();
		}
		
		m_currentMessage = null;
		m_messageDirty = false;
		m_buttonGroup.clearSelection();

		clearPanelContents();
		
		rollback();
		
	}
}