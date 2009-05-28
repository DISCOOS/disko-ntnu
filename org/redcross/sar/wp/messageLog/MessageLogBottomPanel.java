package org.redcross.sar.wp.messageLog;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.LinkedList;
import java.util.List;

import org.redcross.sar.Application;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.HeaderPanel;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.mso.data.ITaskListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IMessageIf.MessageStatus;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.work.event.IFlowListener;
import org.redcross.sar.work.event.FlowEvent;
import org.redcross.sar.wp.IDiskoWp;
import org.redcross.sar.wp.messageLog.IDiskoWpMessageLog.MessageLogActionType;

/**
 * Bottom edit panel
 *
 * @author thomasl
 */
public class MessageLogBottomPanel extends BasePanel implements IMsoUpdateListenerIf, IFlowListener
{
	private static final long serialVersionUID = 1L;

	private static final String TIP_DO_WORK = "<html>Opprett en <b>ny melding</b> eller <b>endre en eksisterende</b></html>";
	private static final String TIP_CREATE_MESSAGE = "<html>Opprett en <b>ny melding</b></html>";
	private static final String TIP_EDIT_MESSAGE = "<html>Endre <b>melding nr %s</b></html>";

	public static final int PANEL_HEIGHT = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL).height * 6 + 10;
	public static final int SMALL_PANEL_WIDTH = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL).width;

	private static final String EMPTY_PANEL_ID = "EMPTY_PANEL";
	private static final String TEXT_PANEL_ID = "TEXT_PANEL";
	private static final String POSITION_PANEL_ID = "POSITION_PANEL";
	private static final String POI_PANEL_ID = "POI_PANEL";
	private static final String Allocated_PANEL_ID = "Allocated_PANEL";
	private static final String STARTED_PANEL_ID = "STARTED_PANEL";
	private static final String COMPLETED_PANEL_ID = "COMPLETED_PANEL";
	private static final String SHOW_ASSIGNMENT_LIST_PANEL_ID = "SHOW_ASSIGNMENT_LIST_PANEL";

	private static IDiskoWpMessageLog m_wp;

	// Current message Singleton
	private static boolean m_newMessage;
	private static IMessageIf m_currentMessage = null;
	private static boolean m_messageDirty = false;

	/**
	 *
	 * @return Whether the current message is new or not, i.e. not stored in MSO
	 */
	public static boolean isNewMessage()
	{
		return m_newMessage;
	}

	private MessageLineType m_currentMessageLineType = null;

	static List<IEditorIf> m_editComponents;
	static List<IEditorIf> m_shownEditComponents;

	private static ButtonGroup m_buttonGroup;
	private static JToggleButton m_dummyButton;

	private static HeaderPanel m_tipPanel;

	private JPanel m_nrPanel;
	private static JLabel m_nrLabel;

	private JPanel m_dtgPanel;
	private static JLabel m_dtgLabel;
	private ChangeDTGDialog m_changeDTGDialog;
    private static JToggleButton m_changeDTGButton;

    private JPanel m_fromPanel;
    private static JLabel m_fromLabel;
    private ChangeFromDialog m_changeFromDialog;
    private static JToggleButton  m_changeFromButton;

    private JPanel m_toPanel;
    private static JLabel m_toLabel;
    private ChangeToDialog m_changeToDialog;
    private static JToggleButton  m_changeToButton;

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
	private static AllocatedAssignmentPanel m_messageAllocatedPanel;
	private static StartedAssignmentPanel m_messageStartedPanel;
	private static CompletedAssignmentPanel m_messageCompletedPanel;
	private static MessageLinePanel m_messageListPanel;

    private JPanel m_taskPanel;
    private static JLabel m_taskLabel;
    private ChangeTasksDialog m_changeTasksDialog;
    private JToggleButton  m_changeTasksButton;

    private static JPanel m_statusPanel;
    private static JLabel m_statusLabel;
    private static JButton m_cancelStatusButton;
    private static JButton m_waitEndStatusButton;
    private static JButton m_finishedStatusButton;

    /**
     * Constructor
     */
    public MessageLogBottomPanel()
    {

    	// forward
    	super(TIP_DO_WORK,ButtonSize.SMALL);

    	// prepare
    	m_newMessage = false;
    	m_editComponents = new LinkedList<IEditorIf>();
    	m_shownEditComponents = new LinkedList<IEditorIf>();

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

    private JPanel createPanel(int minWidth, int maxWidth, int height)
    {
    	JPanel panel = new JPanel();
    	panel.setLayout(new BorderLayout());

    	panel.setMinimumSize(new Dimension(minWidth!=-1 ? minWidth : 0, 0));
    	panel.setPreferredSize(new Dimension(maxWidth!=-1 ? maxWidth : 0, height));
    	panel.setMaximumSize(new Dimension(maxWidth!=-1 ? maxWidth : Integer.MAX_VALUE, Integer.MAX_VALUE));

    	return panel;
    }

    private ChangeDTGDialog getChangeDTGDialog()
    {
    	if(m_changeDTGDialog == null)
    	{
    		m_changeDTGDialog = new ChangeDTGDialog(m_wp);
    		m_changeDTGDialog.addFlowListener(this);
    		m_editComponents.add(m_changeDTGDialog);
    	}
    	return m_changeDTGDialog;
    }

    private ChangeFromDialog getFieldChangeFromDialog()
    {
    	if(m_changeFromDialog == null)
    	{
    		m_changeFromDialog = new ChangeFromDialog(m_wp);
    		m_changeFromDialog.addFlowListener(this);
    		m_editComponents.add(m_changeFromDialog);
    	}

    	return m_changeFromDialog;
    }

    private ChangeToDialog getChangeToDialog()
	{
		if(m_changeToDialog == null)
		{
			m_changeToDialog = new ChangeToDialog(m_wp);
			m_changeToDialog.addFlowListener(this);
			m_editComponents.add(m_changeToDialog);
		}
		return m_changeToDialog;
	}

    private TextPanel getMessageTextPanel()
    {
    	if(m_textPanel == null)
    	{
    		m_textPanel = new TextPanel(m_wp);
    		m_editComponents.add(m_textPanel);
    		m_cardsPanel.add(m_textPanel, TEXT_PANEL_ID);
    	}
    	return m_textPanel;
    }

    private MessagePositionPanel getMessagePositionPanel()
    {
    	if(m_messagePositionPanel == null)
    	{
    		m_messagePositionPanel = new MessagePositionPanel(m_wp);
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
    		m_messagePOIPanel = new MessagePOIPanel(m_wp, poiTypes);
    		m_editComponents.add(m_messagePOIPanel);
    		m_cardsPanel.add(m_messagePOIPanel, POI_PANEL_ID);
    	}
    	return m_messagePOIPanel;
    }

    private AllocatedAssignmentPanel getAllocatedPanel()
    {
    	if(m_messageAllocatedPanel == null)
    	{
    		m_messageAllocatedPanel = new AllocatedAssignmentPanel(m_wp);
    		m_editComponents.add(m_messageAllocatedPanel);
    		m_cardsPanel.add(m_messageAllocatedPanel, Allocated_PANEL_ID);
    	}
    	return m_messageAllocatedPanel;
    }

    private StartedAssignmentPanel getStartedPanel()
    {
    	if(m_messageStartedPanel == null)
    	{
    		m_messageStartedPanel = new StartedAssignmentPanel(m_wp);
    		m_editComponents.add(m_messageStartedPanel);
    		m_cardsPanel.add(m_messageStartedPanel, STARTED_PANEL_ID);
    	}
    	return m_messageStartedPanel;
    }

    private CompletedAssignmentPanel getCompletedPanel()
	{
		if(m_messageCompletedPanel == null)
		{
			m_messageCompletedPanel = new CompletedAssignmentPanel(m_wp);
			m_editComponents.add(m_messageCompletedPanel);
			m_cardsPanel.add(m_messageCompletedPanel, COMPLETED_PANEL_ID);
		}
		return m_messageCompletedPanel;

	}

    private MessageLinePanel getMessageListPanel()
    {
    	if(m_messageListPanel == null)
    	{
    		m_messageListPanel = new MessageLinePanel(m_wp);
    		m_editComponents.add(m_messageListPanel);
    		m_cardsPanel.add(m_messageListPanel, SHOW_ASSIGNMENT_LIST_PANEL_ID);
    	}
    	return m_messageListPanel;
    }

    private ChangeTasksDialog getChangeTasksDialog()
	{
		if(m_changeTasksDialog == null)
		{
			m_changeTasksDialog = new ChangeTasksDialog(m_wp);
			m_changeTasksDialog.addFlowListener(this);
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
		m_statusLabel.setText("");
		m_tipPanel.setCaptionText(TIP_DO_WORK);

		for(IEditorIf component : m_editComponents)
		{
			component.hideEditor();
			component.reset();
		}

		updateMessageGUI();

	}

    private void initPanels(JTable logTable)
    {

    	// prepare base panel
    	setNotScrollBars();
    	setPreferredSize(new Dimension(SMALL_PANEL_WIDTH*16,PANEL_HEIGHT));

    	// save header panel as static
    	m_tipPanel = getHeaderPanel();
    	m_tipPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.INFO", "32x32"));

    	// calculate body height
    	int h = PANEL_HEIGHT - 40;

    	// prepare to add
    	JPanel panel = (JPanel)getContainer();
    	panel.setPreferredSize(new Dimension(SMALL_PANEL_WIDTH*16,h));

    	// Add table header
    	JTableHeader header = logTable.getTableHeader();
    	logTable.setTableHeader(null);
    	header.setTable(logTable);

    	// create top panel
    	JPanel top = new JPanel();
    	top.setLayout(new BorderLayout(0,0));
    	top.add(header,BorderLayout.CENTER);
    	JComponent c = UIFactory.createCorner();
    	c.setPreferredSize(new Dimension(25,header.getHeight()));
    	top.add(c,BorderLayout.EAST);
    	panel.add(top, BorderLayout.NORTH);

    	// create column panel
    	JPanel columns = new JPanel();
    	columns.setLayout(new BoxLayout(columns,BoxLayout.X_AXIS));
    	panel.add(columns, BorderLayout.CENTER);

    	// calculate column height
    	h -= header.getHeight();

    	// set separator width
        int dx = 1;

        // set label position
        String position = BorderLayout.NORTH;

    	// Message Number panel
    	TableColumn column = header.getColumnModel().getColumn(0);
        m_nrPanel = createPanel(column.getMinWidth() - dx,column.getMaxWidth() - dx, h);
        m_nrLabel = new JLabel();
        m_nrLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_nrPanel.add(m_nrLabel, position);
        m_nrPanel.add(Box.createRigidArea(DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL)), BorderLayout.SOUTH);
        columns.add(m_nrPanel);
        columns.add(new JSeparator(JSeparator.VERTICAL));

        // DTG panel
        column = header.getColumnModel().getColumn(1);
        m_dtgPanel = createPanel(column.getMinWidth() - dx*2,column.getMaxWidth() - dx*2, h);
        m_dtgLabel = new JLabel();
        m_dtgLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_dtgPanel.add(m_dtgLabel, position);
        m_dtgPanel.add(m_changeDTGButton, BorderLayout.SOUTH);
        columns.add(m_dtgPanel);
        columns.add(new JSeparator(JSeparator.VERTICAL));

        // From panel
        column = header.getColumnModel().getColumn(2);
        m_fromPanel = createPanel(column.getMinWidth() - dx*2,column.getMaxWidth() - dx*2, h);
        m_fromLabel = new JLabel();
        m_fromLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_fromPanel.add(m_fromLabel, position);
        m_fromPanel.add(m_changeFromButton, BorderLayout.SOUTH);
        columns.add(m_fromPanel);
        columns.add(new JSeparator(JSeparator.VERTICAL));

        // To panel
        column = header.getColumnModel().getColumn(3);
        m_toPanel = createPanel(column.getMinWidth() - dx*2,column.getMaxWidth() - dx*2, h);
        m_toLabel = new JLabel();
        m_toLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_toPanel.add(m_toLabel, position);
        m_toPanel.add(m_changeToButton, BorderLayout.SOUTH);
        columns.add(m_toPanel);
        columns.add(new JSeparator(JSeparator.VERTICAL));

        // Message panel
        m_messagePanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(m_messagePanel, BoxLayout.Y_AXIS);
        m_messagePanel.setLayout(boxLayout);
        m_cardsPanel = new JPanel();
        m_cardsPanel.setLayout(new CardLayout());
        column = header.getColumnModel().getColumn(4);
        m_cardsPanel.setPreferredSize(new Dimension(column.getMaxWidth() - dx*2 + 20, h-100));
        m_cardsPanel.setAlignmentX(0.0f);
        m_cardsPanel.add(new JPanel(), EMPTY_PANEL_ID);
        CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
        layout.show(m_cardsPanel, EMPTY_PANEL_ID);
        m_messagePanel.add(m_cardsPanel);

        m_buttonRow.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        m_buttonRow.setMaximumSize(new Dimension(column.getMaxWidth() - dx*2,
        		(int)DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL).height));
        m_buttonRow.setAlignmentX(0.0f);
        m_messagePanel.add(m_buttonRow);
        columns.add(m_messagePanel);
        columns.add(new JSeparator(JSeparator.VERTICAL));

        // Task panel
        column = header.getColumnModel().getColumn(5);
        m_taskPanel = createPanel(column.getMinWidth() - dx*2,column.getMaxWidth() - dx*2, h);
        m_taskLabel = new JLabel();
        m_taskLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_taskPanel.add(m_taskLabel, position);
        JPanel taskButtonPanel = new JPanel();
        taskButtonPanel.setLayout(new BoxLayout(taskButtonPanel, BoxLayout.PAGE_AXIS));
        taskButtonPanel.setBorder(null);
        taskButtonPanel.add(m_changeTasksButton);
        m_taskPanel.add(taskButtonPanel, BorderLayout.SOUTH);
        columns.add(m_taskPanel);
        columns.add(new JSeparator(JSeparator.VERTICAL));

        // Status panel
        column = header.getColumnModel().getColumn(6);
        m_statusPanel = createPanel(column.getMinWidth() - dx*2 + 25,column.getMaxWidth() - dx*2 + 25, h);
        m_statusLabel = new JLabel();
        m_statusLabel .setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_statusPanel.add(m_statusLabel,position);
        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new BoxLayout(actionButtonPanel, BoxLayout.PAGE_AXIS));
        actionButtonPanel.add(m_cancelStatusButton);
        actionButtonPanel.add(m_waitEndStatusButton);
        actionButtonPanel.add(m_finishedStatusButton);
        m_statusPanel.add(actionButtonPanel, BorderLayout.SOUTH);
        columns.add(m_statusPanel);
    }

    private void initComponents()
    {
    	getChangeDTGDialog();
    	getFieldChangeFromDialog();
    	getChangeToDialog();
    	getMessageTextPanel();
    	getMessagePositionPanel();
    	getMessagePOIPanel();
    	getAllocatedPanel();
    	getStartedPanel();
    	getCompletedPanel();
    	getChangeTasksDialog();
    	getMessageListPanel();
    }

    private void initButtons()
    {
    	m_buttonRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
    	m_buttonGroup = new ButtonGroup();

    	// add dummy button
    	m_dummyButton = new JToggleButton();
    	m_buttonGroup.add(m_dummyButton);

    	initChangeDTGButton();
        initChangeFromButton();
        initChangeToButton();
        initTextButton();
        initPositionButton();
        initPOIButton();
        initAllocatedButton();
        initStartedButton();
        initCompletedButton();
        initListButton();
        initDeleteButton();
        initChangeTasksButton();
        initCancelButton();
        initWaitEndButton();
        initFinishedButton();
    }

    /**
     * An existing message is selected in the message log for editing.
     * @param aMessage The selected message
     */
	public static void newMessageSelected(IMessageIf aMessage)
	{
		// Have user confirm message overwrite
		if(m_currentMessage != null && (m_currentMessage != aMessage) && m_messageDirty)
		{

			Object[] options = {
					m_wp.getBundleText("DirtyMessageWarning.commit"),
					m_wp.getBundleText("DirtyMessageWarning.wait"),
					m_wp.getBundleText("DirtyMessageWarning.cancel")};
			int n = JOptionPane.showOptionDialog(m_wp.getApplication().getFrame(),
					m_wp.getBundleText("DirtyMessageWarning.text"),
					m_wp.getBundleText("DirtyMessageWarning.header"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			// translate to action
			switch(n) {
			case JOptionPane.YES_OPTION:
				apply(true); break;
			case JOptionPane.NO_OPTION:
				apply(false); break;
			case JOptionPane.CANCEL_OPTION: return;
			}
		}
        m_messageDirty = false;
        m_newMessage = false;
        m_currentMessage = aMessage;
        updateMessageGUI();
	}

	private static void updateMessageGUI()
	{
		// has current message?
		if(m_currentMessage != null)
		{

			// Update panel contents
			m_nrLabel.setText(Integer.toString(m_currentMessage.getNumber()));
			m_dtgLabel.setText(DTG.CalToDTG(m_currentMessage.getTimeStamp()));

			// Update FROM
			ICommunicatorIf sender = m_currentMessage.getSender();
			m_fromLabel.setText(sender != null ? sender.getCommunicatorNumberPrefix() + " " + sender.getCommunicatorNumber() : "");

			// Update TO
			if(m_currentMessage.isBroadcast())
			{
				int total = m_currentMessage.getReceivers().size();
				int reminder = total - m_currentMessage.getUnconfirmedReceivers().size();
				m_toLabel.setText(String.format(m_wp.getBundleText("BroadcastLabel.text"),reminder,total));
			}
			else
			{
				ICommunicatorIf receiver = m_currentMessage.getReceiver();
				m_toLabel.setText(receiver!=null ? receiver.getCommunicatorNumberPrefix() + " " + receiver.getCommunicatorNumber() : "");
			}

			// get TASKs
			ITaskListIf tasks = m_currentMessage.getMessageTasks();
			StringBuilder tasksString = new StringBuilder("<html>");
			for(ITaskIf task : tasks.getObjects())
			{
				tasksString.append(task.getTaskText() + "<br>");
			}
			tasksString.append("</html>");
			m_taskLabel.setText(tasksString.toString());

			// set status
			m_statusLabel.setText(m_currentMessage.getStatusText());

			// Update dialogs
			for(int i=0; i<m_editComponents.size(); i++)
			{
				m_editComponents.get(i).setMessage(m_currentMessage);
			}
			m_tipPanel.setCaptionText(m_newMessage ? TIP_CREATE_MESSAGE
					: String.format(TIP_EDIT_MESSAGE,m_currentMessage.getNumber()));
		}
		else {
	    	m_nrLabel.setText("");
			m_dtgLabel.setText("");
			m_fromLabel.setText("");
			m_toLabel.setText("");
			m_taskLabel.setText("");
			m_statusLabel.setText("");
			m_tipPanel.setCaptionText(TIP_DO_WORK);
		}
		setButtonColors();
	}

    /**
     * Set the message log reference
     * @param wp
     */
	public void setWp(IDiskoWpMessageLog wp)
	{
		m_wp = wp;
	}

	/**
	 * {@link IMsoUpdateListenerIf#handleMsoUpdateEvent(Update)}
	 */
	public void handleMsoUpdateEvent(Update e)
	{
		updateMessageGUI();
	}

	public void onFlowPerformed(FlowEvent e)
	{
		if(e.isFinish()) {

			// If no message is selected a new one should be created once a field is edited
			if(m_currentMessage == null)
			{
				getCurrentMessage(true);
			}
			hideEditPanels(false);
			m_buttonGroup.clearSelection();
			m_messageDirty = true;
			updateMessageGUI();
			autoSetCommunicator();
			showNextStep();
		}
		else if(e.isCancel()) {
			//Object sender = e.getSource();
			m_buttonGroup.clearSelection();
			hideEditPanels(true);
		}
		// forward
		m_wp.onFlowPerformed(e);
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

	private void initChangeDTGButton()
    {
    	if(m_changeDTGButton == null)
    	{
	    	m_changeDTGButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.CHANGE_DTG,ButtonSize.NORMAL);
	    	m_changeDTGButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
	    	m_changeDTGButton.setMnemonic(java.awt.event.KeyEvent.VK_F2);
	    	m_changeDTGButton.addActionListener(new ActionListener()
	    	{
	    		// Display the change DTG dialog when button is pressed
	    		public void actionPerformed(ActionEvent e)
	    		{
	    			if(getChangeDTGDialog().isVisible())
	    			{
	    				hideEditPanels(false);
	    				m_buttonGroup.clearSelection();
	    			}
	    			else
	    			{
						// hide other panels
						hideEditPanels(false);

						// place on screen
						m_changeDTGDialog.setSnapToLocation(m_changeDTGButton, DefaultDialog.POS_NORTH, DefaultDialog.SIZE_TO_OFF, false, false);

	        			// show component
	        			showEditComponents(new IEditorIf[]{m_changeDTGDialog});
	    			}
	    		}
	    	});
	    	m_buttonGroup.add(m_changeDTGButton);
	    }
    }

    private void initChangeFromButton()
    {
    	if(m_changeFromButton == null)
    	{
    		m_changeFromButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.CHANGE_FROM,ButtonSize.NORMAL);
    		m_changeFromButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    		m_changeFromButton.setMnemonic(java.awt.event.KeyEvent.VK_F3);
    		m_changeFromButton.addActionListener(new ActionListener()
    		{
				public void actionPerformed(ActionEvent arg0)
				{

					if(getFieldChangeFromDialog().isVisible())
					{
						// Toggle dialogs if visible
						hideEditPanels(false);
						m_buttonGroup.clearSelection();
					}
					else
					{

						// get current message
						IMessageIf message = getCurrentMessage(false);

						// is allowed action?
						if(message!=null) {
							if(AbstractAssignmentPanel.messageHasAssignments() || AbstractAssignmentPanel.getAddedLines().size()>0) {
								Utils.showWarning("Begrensning","Mottaker eller avsender " +
										"kan ikke endres for meldinger med oppdraglinjer");
								m_buttonGroup.clearSelection();
								return;
							}
						}

						// hide other panels
						hideEditPanels(false);

						// place on screen
						m_changeFromDialog.setSnapToLocation(m_changeFromButton, DefaultDialog.POS_NORTH, DefaultDialog.SIZE_TO_SCREEN, false, false);

						// show component
						showEditComponents(new IEditorIf[]{m_changeFromDialog});

					}
				}
    		});
    		m_buttonGroup.add(m_changeFromButton);
    	}
    }

    private void initChangeToButton()
    {
    	if(m_changeToButton == null)
    	{
    		m_changeToButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.CHANGE_TO,ButtonSize.NORMAL);
    		m_changeToButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    		m_changeToButton.setMnemonic(java.awt.event.KeyEvent.VK_F4);
    		m_changeToButton.addActionListener(new ActionListener()
    		{
				public void actionPerformed(ActionEvent e)
				{
					if(getChangeToDialog().isVisible())
					{
						hideEditPanels(false);
						m_buttonGroup.clearSelection();
					}
					else
					{

						// is allowed?
						IMessageIf message = getCurrentMessage(false);
						if(message!=null) {
							if(AbstractAssignmentPanel.messageHasAssignments() || AbstractAssignmentPanel.getAddedLines().size()>0) {
								Utils.showWarning("Begrensning","Mottaker eller avsender kan ikke endres for meldinger med oppdragsendringer");
								m_buttonGroup.clearSelection();
								return;
							}
						}

						// hide other panels
						hideEditPanels(false);

						// place on screen
						m_changeToDialog.setSnapToLocation(m_changeToButton, DefaultDialog.POS_NORTH, DefaultDialog.SIZE_TO_SCREEN, false, false);

						// show component
						showEditComponents(new IEditorIf[]{m_changeToDialog});

					}
				}
    		});
    		m_buttonGroup.add(m_changeToButton);
    	}
    }

	private void initTextButton()
	{
    	if(m_textButton == null)
    	{
			m_textButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_TEXT,ButtonSize.NORMAL);
			m_textButton.setMnemonic(java.awt.event.KeyEvent.VK_F5);
			m_textButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					getMessageTextPanel();
					hideEditPanels(false);
					m_currentMessageLineType = MessageLineType.TEXT;

					CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
					layout.show(m_cardsPanel, TEXT_PANEL_ID);

					// prepare
					getMessageTextPanel().showEditor();


				}
			});
			m_buttonGroup.add(m_textButton);
			m_buttonRow.add(m_textButton);
    	}
	}

	private void initPOIButton()
	{
    	if(m_poiButton == null)
    	{
			m_poiButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_POI,ButtonSize.NORMAL);
			m_poiButton.setMnemonic(java.awt.event.KeyEvent.VK_F6);
			m_poiButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// cleanup
					hideEditPanels(false);

					// set current type
					m_currentMessageLineType = MessageLineType.POI;

					// set tool
					m_messagePOIPanel.setMapTool();

					// show message poi panel
					CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
					layout.show(m_cardsPanel, POI_PANEL_ID);

					// prepare
					getMessagePOIPanel().showEditor();


				}
			});
			m_buttonGroup.add(m_poiButton);
			m_buttonRow.add(m_poiButton);
    	}
	}

	private void initPositionButton()
	{
    	if(m_poiButton == null)
    	{
			m_positionButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_POSITION,ButtonSize.NORMAL);
			m_positionButton.setMnemonic(java.awt.event.KeyEvent.VK_F7);
			m_positionButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// cleanup
					hideEditPanels(false);

					// set current tupe
					m_currentMessageLineType = MessageLineType.POSITION;

					// set tool
					m_messagePositionPanel.setMapTool();

					// show message position panel
					CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
					layout.show(m_cardsPanel, POSITION_PANEL_ID);

					// prepare
					getMessagePositionPanel().showEditor();


				}
			});
			m_buttonGroup.add(m_positionButton);
			m_buttonRow.add(m_positionButton);
    	}
	}

	private void initAllocatedButton()
	{
    	if(m_assignButton == null)
    	{
			m_assignButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_ASSIGNMENT,ButtonSize.NORMAL);
			m_assignButton.setMnemonic(java.awt.event.KeyEvent.VK_F8);
			m_assignButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{

					hideEditPanels(false);

					IMessageIf message = getCurrentMessage(false);

					if(message!=null && message.isBroadcast())
					{
						// de-select button
						m_assignButton.setSelected(false);

						// Only legal if message isn't broadcast
						Utils.showWarning(m_wp.getBundleText("AssignmentError.header"),
								m_wp.getBundleText("AssignmentError.details"));
					}
					else
					{
						// Task can be changed
						m_currentMessageLineType = MessageLineType.ALLOCATED;

						// show assignment panel
						CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
						layout.show(m_cardsPanel, Allocated_PANEL_ID);

						// prepare
						getAllocatedPanel().showEditor();


					}
				}
			});
			m_buttonGroup.add(m_assignButton);
			m_buttonRow.add(m_assignButton);
    	}
	}

	private void initStartedButton()
	{
    	if(m_startButton == null)
    	{
			m_startButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_STARTED,ButtonSize.NORMAL);
			m_startButton.setMnemonic(java.awt.event.KeyEvent.VK_F9);
			m_startButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					hideEditPanels(false);

					// get message
					IMessageIf message = getCurrentMessage(false);

					if(message!=null && message.isBroadcast())
					{
						// de-select button
						m_startButton.setSelected(false);

						Utils.showWarning(m_wp.getBundleText("StartedError.header"),
								m_wp.getBundleText("StartedError.details"));
					}
					else
					{
						// Task can be changed
						m_currentMessageLineType = MessageLineType.STARTED;

						// show started panel
						CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
						layout.show(m_cardsPanel, STARTED_PANEL_ID);

						// prepare
						getStartedPanel().showEditor();

					}
				}
			});
			m_buttonGroup.add(m_startButton);
			m_buttonRow.add(m_startButton);
    	}
	}

	private void initCompletedButton()
	{
    	if(m_completedButton == null)
    	{
			m_completedButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SET_COMPLETED,ButtonSize.NORMAL);
			m_completedButton.setMnemonic(java.awt.event.KeyEvent.VK_F10);
			m_completedButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					hideEditPanels(false);

					IMessageIf message = getCurrentMessage(false);

					if(message!=null && message.isBroadcast())
					{

						// de-select button
						m_completedButton.setSelected(false);

						// not possible to assign when message is a broadcast
						Utils.showWarning(m_wp.getBundleText("CompletedError.header"),
								m_wp.getBundleText("CompletedError.details"));

					}
					else
					{

						// Task can be changed
						m_currentMessageLineType = MessageLineType.COMPLETED;

						// show started panel
						CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
						layout.show(m_cardsPanel, COMPLETED_PANEL_ID);

						// prepare
						getCompletedPanel().showEditor();

					}
				}
			});
			m_buttonGroup.add(m_completedButton);
			m_buttonRow.add(m_completedButton);
    	}
	}

	private void initListButton()
	{
    	if(m_listButton == null)
    	{
			m_listButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.SHOW_LIST,ButtonSize.NORMAL);
			m_listButton.setMnemonic(java.awt.event.KeyEvent.VK_F11);
			m_listButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					getMessageListPanel();
					hideEditPanels(false);

					// show?
					if(m_currentMessage!=null) {
						// get panel
						CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
						layout.show(m_cardsPanel, SHOW_ASSIGNMENT_LIST_PANEL_ID);
						// update selection
						m_currentMessageLineType = m_messageListPanel.getSelectedMessageLineType();
					}
					else {
						// notify
						Utils.showWarning("Begrensning","Du må først legge til en meldingslinje");
						// reset selection
						m_dummyButton.doClick();
					}
				}

			});
			m_buttonGroup.add(m_listButton);
			m_buttonRow.add(m_listButton);
    	}
	}

    private void initDeleteButton()
	{
    	if(m_deleteButton == null)
    	{
	    	m_deleteButton = DiskoButtonFactory.createButton(MessageLogActionType.DELETE,ButtonSize.NORMAL);
			m_deleteButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{

					// update selected type??
					m_currentMessageLineType = (m_currentMessageLineType == null) ?
							m_messageListPanel.getSelectedMessageLineType() : m_currentMessageLineType;

					// select a line?
					if(m_currentMessageLineType==null) {
						Utils.showWarning("Begrensning","Du må velge en meldinglinje først");
						return;
					}

					// Delete if message is not committed, don't create new message when pressing delete
					if(m_currentMessage != null && m_currentMessageLineType != null && m_newMessage)
					{
						// If line that has other assignment lines depending on it is deleted, delete them as well
						switch(m_currentMessageLineType)
						{
						case ALLOCATED:
						case STARTED:
						case COMPLETED:
							// prompt later
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									int ans = Utils.showConfirm("Bekreftelse", "Dette vil slette samtlige endringer i " +
											"oppdrag i denne meldingen. Vil du fortsette?", JOptionPane.YES_NO_OPTION);
									if(ans == JOptionPane.YES_OPTION) {
										// remove all of given type
										AbstractAssignmentPanel.removeAddedLines(null,true);
										m_messageAllocatedPanel.updateAssignmentLineList();
										m_messageStartedPanel.updateAssignmentLineList();
										m_messageCompletedPanel.updateAssignmentLineList();
										m_messageListPanel.setMessage(m_currentMessage);
									}
								}
							});
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
								line.delete(true);
							}
							else
								Utils.showWarning("Ingenting å slette");
							break;
						}
					}
					else
					{
						Utils.showWarning(m_wp.getBundleText("CanNotDeleteMessageLine.header"),
								m_wp.getBundleText("CanNotDeleteMessageLine.details"));
					}
				}
			});
			m_buttonGroup.add(m_deleteButton);
			m_buttonRow.add(m_deleteButton);
    	}
	}

    private void initChangeTasksButton()
    {
    	if(m_changeTasksButton == null)
    	{
    		m_changeTasksButton = DiskoButtonFactory.createToggleButton(MessageLogActionType.CHANGE_TASKS,ButtonSize.NORMAL);
    		m_changeTasksButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    		m_changeTasksButton.setMnemonic(java.awt.event.KeyEvent.VK_F12);
    		m_changeTasksButton.addActionListener(new ActionListener()
    		{
				public void actionPerformed(ActionEvent e)
				{
					if(getChangeTasksDialog().isVisible())
					{
						hideEditPanels(false);
						m_buttonGroup.clearSelection();
					}
					else
					{
						// Create new message if null
						getCurrentMessage(true);
						m_messageDirty = true;

						getChangeTasksDialog();
						hideEditPanels(false);

						// locate on screen
						m_changeTasksDialog.setSnapToLocation(m_changeTasksButton, DefaultDialog.POS_NORTH, DefaultDialog.SIZE_TO_OFF, false, false);

						// show component
						showEditComponents(new IEditorIf[]{m_changeTasksDialog});
					}
				}
    		});
    		m_buttonGroup.add(m_changeTasksButton);
    	}
    }

    private void initCancelButton()
    {
    	if(m_cancelStatusButton == null)
    	{
    		try {
    			m_cancelStatusButton = DiskoButtonFactory.createButton(MessageLogActionType.CANCEL,ButtonSize.NORMAL);
    			m_cancelStatusButton .setMnemonic(java.awt.event.KeyEvent.VK_ESCAPE);
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
    }


	private JButton initWaitEndButton()
    {
    	if(m_waitEndStatusButton == null)
    	{
    		m_waitEndStatusButton = DiskoButtonFactory.createButton("GENERAL.WAIT",ButtonSize.NORMAL);

    		m_waitEndStatusButton.addActionListener(new ActionListener()
    		{
				public void actionPerformed(ActionEvent arg0)
				{
					// forward
					apply(false);;
				}
    		});
    	}
    	return m_waitEndStatusButton;
    }

    private JButton initFinishedButton()
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
			m_wp.getMsoModel().suspendClientUpdate();
			m_currentMessage = m_wp.getMsoManager().createMessage();
			m_currentMessage.setBroadcast(isBroadcast);
			m_wp.getMsoModel().resumeClientUpdate(true);
			m_newMessage = true;
			m_messageDirty = false;
			updateMessageGUI();
			// auto detect next step
			showNextStep();
		}
		return m_currentMessage;
	}

	public static void autoSetCommunicator() {

		ICmdPostIf cmdPost = m_wp.getMsoManager().getCmdPost();

		if(m_currentMessage.getSender()!=null
				&& m_currentMessage.getSender()!=cmdPost && m_currentMessage.getReceivers().size()==0) {
			m_currentMessage.setReceiver(m_wp.getMsoManager().getCmdPostCommunicator());
		}
		else if(m_currentMessage.getSender()==null
				&& m_currentMessage.getReceivers().size()>0) {
			m_currentMessage.setSender(m_wp.getMsoManager().getCmdPostCommunicator());
		}

	}

	public static void showNextStep() {
		// has not message?
		if(m_currentMessage==null) {
			showDTGPanel();
		}
		/* else if(m_currentMessage.getSender() == m_currentMessage.getReceiver()) {
			showToPanel();
		}*/
		else {
			showTextPanel();
		}

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
					Utils.showWarning(m_wp.getBundleText("UnconfirmedUnitsExists.header"),
							m_wp.getBundleText("UnconfirmedUnitsExists.text"));
					// keep current status
					status = m_currentMessage.getStatus();
				}
			}

			// apply status
			m_currentMessage.setStatus(status);

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

	public static boolean validateMessage() {
		// has any data to commit?
		if(m_currentMessage == null) {
			// notify
			Utils.showWarning(m_wp.getBundleText("NoMessageData.header"),
					m_wp.getBundleText("NoMessageData.text"));
			return false;
		}
		// has sender and receiver?
		if(m_currentMessage.getSender()==null || m_currentMessage.getReceivers().size()==0) {
			// notify
			Utils.showWarning(m_wp.getBundleText("NoMessageCommunicators.header"),
					m_wp.getBundleText("NoMessageCommunicators.text"));
			return false;
		}
		// has lines?
		if(m_currentMessage != null && m_currentMessage.getLines().length==0) {
			// notify
			Utils.showWarning(m_wp.getBundleText("NoMessageLines.header"),
					m_wp.getBundleText("NoMessageLines.text"));
			return false;
		}
		// prompt user?
		if(m_currentMessage.getReceivers().contains(m_currentMessage.getSender())) {
			// notify
			Utils.showWarning("Begrensning", "Avsender og mottaker er den samme");
			return false;
		}
		// is valid
		return true;
	}

	/**
	 * Ensures that units and assignments affected by the added message lines in the current message are
	 * updated (status, etc.). Message lines are also updated according to what operations are legal
	 */
	private static void updateAssignments()
	{

		if(m_currentMessage != null && !m_currentMessage.isBroadcast())
		{
			// suspend updates
			Application.getInstance().getMsoModel().suspendClientUpdate();

			// initialize
			List<IMessageLineIf> lines = new LinkedList<IMessageLineIf>();

			// Get all assignment lines. Lines from complete is placed first, started second, assign last.
			// This should ensure that unit statuses are updated in the correct order
			lines.addAll(AbstractAssignmentPanel.getAddedLines());

			// get last assignment line
			IMessageLineIf line = getLastAssignmentLine(lines);

			// found assignment line?
			if(line!=null)
			{
				// initialize
				boolean bFlag = false;
				IUnitIf unit = line.getLineUnit();
				IAssignmentIf assignment = line.getLineAssignment();

				try
				{
					if(unit != null)
					{
						switch(line.getLineType())
						{
						case ALLOCATED:
							unit.allocateAssignment(assignment);
							break;
						case STARTED:
							unit.startAssignment(assignment);
							break;
						case COMPLETED:
							unit.finishAssignment(assignment);
							break;
						}
						// success
						bFlag = true;
					}

				}
				catch(IllegalOperationException e)
				{
					e.printStackTrace();
				}

				// delete all assignment lines?
				if(!bFlag) {
					deleteAssignmentLines(lines);
				}

			}

			// Keep track of which lines are added
			AbstractAssignmentPanel.removeAddedLines(null,false);

			// resume updates
			Application.getInstance().getMsoModel().resumeClientUpdate(true);

		}
	}

	private static IMessageLineIf getLastAssignmentLine(List<IMessageLineIf> lines) {

		// initialize
		IMessageLineIf line = null;

		// get latest assignment line
		for(IMessageLineIf it : lines)
		{

			switch(it.getLineType())
			{
			case ALLOCATED:
			case STARTED:
			case COMPLETED:
				if(line==null || it.getLineType().compareTo(line.getLineType())>0)
				{
					line = it;
				}
				break;
			default:
				continue;
			}
		}

		// finished
		return line;

	}

	private static IMessageLineIf deleteAssignmentLines(List<IMessageLineIf> lines) {

		// initialize
		IMessageLineIf line = null;

		// get latest assignment line
		for(IMessageLineIf it : lines)
		{

			switch(it.getLineType())
			{
			case ALLOCATED:
			case STARTED:
			case COMPLETED:
				line.delete(true);
				break;
			default:
				continue;
			}
		}

		// finished
		return line;

	}

	/**
	 * Hides all panels
	 */
	public static void hideEditPanels(boolean hideAll)
	{

		// hide all
		if(hideAll) {

			for(IEditorIf it : m_editComponents)
			{
				it.hideEditor();
			}
		}
		else {
			for(IEditorIf it : m_shownEditComponents)
			{
				// allowed to hide it?
				if(!(it instanceof MessageLinePanel))
					it.hideEditor();
			}
		}

		// hide map
        MessageLogPanel.hideMap();

	}

	/**
	 * Unselect all field edit buttons
	 */
	public void clearSelection()
	{
		m_buttonGroup.clearSelection();
	}

	public static void showDTGPanel()
	{
		m_changeDTGButton.doClick();
	}

	public static void showFromPanel()
	{
		m_changeFromButton.doClick();
	}

	public static void showToPanel()
	{
		m_changeToButton.doClick();
	}

	public static void showAssignPanel()
	{
		m_assignButton.doClick();
	}

	public static void showListPanel()
	{
		if(m_currentMessage!=null)
			m_listButton.doClick();
		else {
			hideEditPanels(true);
			m_dummyButton.doClick();
		}

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

	public static void setIsDirty()
	{
		// set flag
		m_messageDirty = (m_currentMessage!=null);

		// forward
		setButtonColors();

	}

	public static void cancelAssign()
	{
		m_messageAllocatedPanel.cancelUpdate();
	}

	public static void cancelStarted()
	{
		m_messageStartedPanel.cancelUpdate();
	}

	public static void cancelCompleted()
	{
		m_messageCompletedPanel.cancelUpdate();
	}

	public static boolean isMessageDirty()
	{
		return m_messageDirty || m_wp.getMsoModel().isChanged();
	}

	/**
	 * Remove any changes since last commit. Clear panel contents
	 */
	public static void clearCurrentMessage()
	{
		if(m_newMessage && m_currentMessage != null)
		{
			m_currentMessage.delete(true);
		}

		m_currentMessage = null;
		m_messageDirty = false;
		m_buttonGroup.clearSelection();

		clearPanelContents();

		rollback();

	}

	private static void showEditComponents(IEditorIf[] components) {
		m_shownEditComponents.clear();
		for(int i=0;i<components.length;i++) {
			m_shownEditComponents.add(components[i]);
			components[i].showEditor();
		}
	}

    private static void commit() {
    	((IDiskoWp)m_wp).commit();
    }

    private static void rollback() {
    	((IDiskoWp)m_wp).rollback();
    }

}