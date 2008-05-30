package org.redcross.sar.wp.messageLog;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.ICmdPostIf.CmdPostStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.util.mso.Selector;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

/**
 * Dialog containing a list of all units  in command post communicator list
 *
 * @author thomasl
 */
public class SingleUnitListSelectionDialog extends DefaultDialog implements IEditMessageComponentIf, IMsoUpdateListenerIf, ActionListener
{
	private static final long serialVersionUID = 1L;

	protected JPanel m_contentsPanel = null;
	protected JScrollPane m_scrollPane = null;
	protected IDiskoWpMessageLog m_wpMessageLog;
	protected UnitType m_unitTypeFilter = null;
	protected AbstractDerivedList<ICommunicatorIf> m_communicatorList;
	protected boolean m_dirtyList;
	protected ButtonGroup m_buttonGroup = null;
	protected boolean m_senderList = true;
	protected HashMap<JToggleButton, ICommunicatorIf> m_buttonCommunicatorMap = null;
	protected HashMap<ICommunicatorIf, JToggleButton> m_communicatorButtonMap = null;
	protected JToggleButton m_currentButton = null;

	final public static int PANEL_WIDTH = DiskoButtonFactory.getButtonSize(ButtonSize.LONG).width * 5;
	private final int NUMBER_OF_ROWS = 6;

	/**
	 * @param wp Message log work process
	 * @param senderList Whether dialog is changing the sender field of a message or not
	 */
	public SingleUnitListSelectionDialog(IDiskoWpMessageLog wp, boolean senderList)
	{
		super(wp.getApplication().getFrame());
		m_wpMessageLog = wp;
		m_wpMessageLog.getMsoEventManager().addClientUpdateListener(this);
		m_communicatorList = wp.getMsoManager().getCmdPost().getCommunicatorList();

		m_senderList = senderList;

		m_buttonCommunicatorMap = new HashMap<JToggleButton, ICommunicatorIf>();
		m_communicatorButtonMap = new HashMap<ICommunicatorIf, JToggleButton>();

		m_buttonGroup = new ButtonGroup();

		initContentsPanel();
		
		m_dirtyList = true;
		buildList();
		
		this.pack();
	}

	/**
	 * Sorts units based on  number
	 */
	private Comparator<ICommunicatorIf> m_communicatorComparator = new Comparator<ICommunicatorIf>()
	{
		
		public int compare(ICommunicatorIf arg0, ICommunicatorIf arg1)
		{
			if(arg0.getCommunicatorNumberPrefix() == arg1.getCommunicatorNumberPrefix())
			{
				return arg0.getCommunicatorNumber() - arg1.getCommunicatorNumber();
			}
			else
			{
				return arg0.getCommunicatorNumberPrefix() - arg1.getCommunicatorNumberPrefix();
			}
		}
	};

	/**
	 * Selects active units based on the unit type filter
	 */
	private final EnumSet<UnitStatus> m_activeUnitStatusSet = EnumSet.range(UnitStatus.READY, UnitStatus.PENDING);
	private final EnumSet<CmdPostStatus> m_activeCmdPostStatusSet = EnumSet.of(CmdPostStatus.IDLE, CmdPostStatus.OPERATING, CmdPostStatus.PAUSED);
	private Selector<ICommunicatorIf> m_communicatorSelector = new Selector<ICommunicatorIf>()
	{
		public boolean select(ICommunicatorIf communicator)
		{
			if(communicator instanceof ICmdPostIf )
			{
				// Command post should only be selected if the type filter is set to cp and the cp is active
				ICmdPostIf cmdPost = (ICmdPostIf)communicator;
				return m_activeCmdPostStatusSet.contains(cmdPost.getStatus()) &&
						(m_unitTypeFilter == UnitType.CP || m_unitTypeFilter == null);

			}
			else if(communicator instanceof IUnitIf)
			{
				// Unit should be selected if it is active, and the unit type filter match
				IUnitIf unit = (IUnitIf)communicator;
				return m_activeUnitStatusSet.contains(unit.getStatus()) &&
						(m_unitTypeFilter == unit.getType() || m_unitTypeFilter == null);
			}
			else
			{
				return false;
			}
		}
	};

	private void initContentsPanel()
	{
		m_contentsPanel = new JPanel();
		m_contentsPanel.setLayout(new BoxLayout(m_contentsPanel, BoxLayout.LINE_AXIS));

		m_scrollPane = new JScrollPane(m_contentsPanel);
		m_scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		m_scrollPane.setPreferredSize(new Dimension(PANEL_WIDTH,
				DiskoButtonFactory.getButtonSize(ButtonSize.LONG).height*NUMBER_OF_ROWS + 20));

		this.add(m_scrollPane);
		
		buildList();
		
		this.pack();
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
	 * Search communicator list and mark unit as selected
	 */
	public void newMessageSelected(IMessageIf message)
	{
		m_unitTypeFilter = null;
		if(this.isVisible())
		{
			buildList();
			updateButtonSelection();
		}
	}

	/**
	 * 
	 */
	public void showComponent()
	{
		buildList();
		updateButtonSelection();
		this.setVisible(true);
	}
	
	private void updateButtonSelection()
	{
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		JToggleButton button = null;
		if(m_senderList)
		{
			// get current sender
			ICommunicatorIf sender = (message!=null ? message.getSender() : (ICommunicatorIf)m_wpMessageLog.getCmdPost());
			// has sender?
			if(sender != null)
			{
				button = m_communicatorButtonMap.get(sender);
			}
		}
		else
		{
			// get flag
			boolean isBroadcast = (message!=null ? message.isBroadcast() : false); 
			
			// setup
			if(!isBroadcast) {

				// get current receiver
				ICommunicatorIf receiver = (message!=null ? message.getSingleReceiver() : (ICommunicatorIf)m_wpMessageLog.getCmdPost());
				
				// has receiver
				if(receiver != null)
				{
					button = m_communicatorButtonMap.get(receiver);
				}
			}
		}
		
		if(button != null)
		{
			m_buttonGroup.setSelected(button.getModel(), true);
			m_currentButton = button;
		}
	}

	private final EnumSet<IMsoManagerIf.MsoClassCode> myInterests =
		EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_CMDPOST,
				IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT);
	/**
	 * Interested in message and message line updates
	 */
	public boolean hasInterestIn(IMsoObjectIf msoObject)
	{
		return myInterests.contains(msoObject.getMsoClassCode());
	}
	
	/**
	 * Updates unit list based on MSO communicator events
	 * 
	 * @see org.redcross.sar.mso.event.IMsoUpdateListenerIf#handleMsoUpdateEvent(org.redcross.sar.mso.event.MsoEvent.Update)
	 */
	public void handleMsoUpdateEvent(Update e)
	{
		IMsoManagerIf mng = m_wpMessageLog.getMsoManager();
		if(mng!=null) {
			ICmdPostIf cmd = mng.getCmdPost();
			if(cmd!=null) {
				AbstractDerivedList<ICommunicatorIf> lst = cmd.getCommunicatorList();
				if (lst!=null) {
					m_dirtyList = m_communicatorList.size() != m_wpMessageLog.getMsoManager().getCmdPost().getCommunicatorList().size();
					if(this.isVisible())
					{
						buildList();
					}
				}
			}
		}
	}

	/**
	 * Gets which unit type the filter is set to
	 * @return Type
	 */
	public UnitType getUnitTypeFilter()
	{
		return m_unitTypeFilter;
	}

	/**
	 * Update unit selection list with type filter
	 */
	public void setUnitTypeFilter(UnitType typeFilter)
	{
		m_unitTypeFilter = typeFilter;
		m_dirtyList = true;
		buildList();
		updateButtonSelection();
	}

	/**
	 * Updates the unit list using the unit type as filter
	 */
	public void buildList()
	{
		if(m_dirtyList)
		{
			// Clear previous list, brute force maintenance
			m_contentsPanel.removeAll();
			m_buttonGroup = new ButtonGroup();

			m_buttonCommunicatorMap.clear();
			m_communicatorButtonMap.clear();

			JPanel panel = null;
			int i = 0;
			for(ICommunicatorIf commnicator: m_communicatorList.selectItems(m_communicatorSelector , m_communicatorComparator))
			{
				if(i%NUMBER_OF_ROWS == 0)
				{
				panel = new JPanel();
				panel.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.setAlignmentY(Component.TOP_ALIGNMENT);
				panel.setPreferredSize(new Dimension(DiskoButtonFactory.getButtonSize(ButtonSize.LONG).width,
						DiskoButtonFactory.getButtonSize(ButtonSize.LONG).height*NUMBER_OF_ROWS));
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				m_contentsPanel.add(panel);
				}
				addUnitButton(commnicator, panel);
				i++;
			}
			
			m_dirtyList = false;
		}
	}

	private void addUnitButton(final ICommunicatorIf communicator, JPanel buttonPanel)
	{
		JToggleButton button = DiskoButtonFactory.createToggleButton(communicator,ButtonSize.LONG);

		m_buttonCommunicatorMap.put(button, communicator);
		m_communicatorButtonMap.put(communicator, button);

		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
				JToggleButton sourceButton  = (JToggleButton)arg0.getSource();
				if(m_currentButton == sourceButton)
				{
					// Deselecting a button should result in standard sender value being used
					m_currentButton = null;
					ICommunicatorIf commandPost = m_wpMessageLog.getMsoManager().getCmdPostCommunicator();
					if(m_senderList)
					{
						message.setSender(commandPost);
					}
					else
					{
						if(!message.isBroadcast())
						{
							message.setSingleReceiver(commandPost);
						}
					}
					updateButtonSelection();
				}
				else
				{
					// Select communicator
					m_currentButton = sourceButton;
					
					if(m_senderList)
					{
						message.setSender(communicator);
					}
					else
					{
						message.setSingleReceiver(communicator);
					}
				}				
				
				fireOnWorkFinish(this,message);
			}
		});

		button.setActionCommand(communicator.getCommunicatorNumberPrefix()
				+ " " + communicator.getCommunicatorNumber());

		buttonPanel.add(button);
		m_buttonGroup.add(button);
	}


	/**
	 * Updates the type filter based on which buttons are pressed in the unit type selection pad
	 */
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		UnitType type = null;
		try
		{
			if(command!=null && command.length()!=0)
				type = UnitType.valueOf(command);
		}
		catch(Exception exc)
		{}
		setUnitTypeFilter(type);
		this.validate();
		this.repaint();
	}

	public Enumeration<AbstractButton> getButtons()
	{
		return m_buttonGroup.getElements();
	}

	/**
	 * Deselect buttons
	 */
	public void clearSelection()
	{
		m_buttonGroup.clearSelection();
	}
}
