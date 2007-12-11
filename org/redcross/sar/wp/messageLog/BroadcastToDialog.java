package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoButtonFactory;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitType;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * Dialog shown when setting the to field of a message in broadcast mode
 * @author thomasl
 */
public class BroadcastToDialog extends DiskoDialog implements IEditMessageComponentIf
{

	private static final long serialVersionUID = 1L;

	protected final int NUM_ROWS_COMMUNICATOR_LIST = 6;

	protected IDiskoWpMessageLog m_wpMessageLog = null;

	protected JPanel m_contentsPanel = null;
	protected JPanel m_buttonRowPanel = null;

	protected JToggleButton m_selectionButton = null;
	protected JToggleButton m_confirmButton = null;
	protected ButtonGroup m_buttonGroup = null;

	protected JButton m_allButton = null;
	protected JButton m_noneButton = null;

	protected boolean m_selectionMode = true;

	protected JPanel m_unitTypePanel = null;
	protected JToggleButton m_teamButton = null;
	protected JToggleButton m_dogButton = null;
	protected JToggleButton m_vehicleButton = null;
	protected JToggleButton m_aircraftButton = null;
	protected JToggleButton m_boatButton = null;
	protected JToggleButton m_commandPostButton = null;

	protected JLabel m_confirmationStatusLabel = null;

	protected JScrollPane m_scrollPane = null;
	protected JPanel m_listArea = null;

	protected List<ICommunicatorIf> m_selectedCommuicators;
	protected List<ICommunicatorIf> m_confirmedCommunicators;
	protected HashMap<JToggleButton, ICommunicatorIf> m_buttonCommunicatorMap = null;
	protected HashMap<ICommunicatorIf, JToggleButton> m_communicatorButtonMap = null;

	/**
	 * @param wp Reference to message log work process
	 */
	public BroadcastToDialog(IDiskoWpMessageLog wp)
	{
		super(wp.getApplication().getFrame());

		m_wpMessageLog = wp;

		m_selectedCommuicators = new LinkedList<ICommunicatorIf>();
		m_confirmedCommunicators = new LinkedList<ICommunicatorIf>();

		m_buttonCommunicatorMap = new HashMap<JToggleButton, ICommunicatorIf>();
		m_communicatorButtonMap = new HashMap<ICommunicatorIf, JToggleButton>();

		initContentsPanel();
		initActionButtons();

		m_contentsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

		initUnitListArea();

		this.pack();
	}

	private void initUnitListArea()
	{
		m_listArea = new JPanel();
		m_listArea.setLayout(new BoxLayout(m_listArea, BoxLayout.LINE_AXIS));
		m_listArea.setAlignmentX(Component.LEFT_ALIGNMENT);
		m_listArea.setAlignmentY(Component.TOP_ALIGNMENT);

		m_scrollPane = new JScrollPane(m_listArea);
		m_scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		m_scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		m_scrollPane.setPreferredSize(new Dimension(SingleUnitListSelectionDialog.PANEL_WIDTH+250,
				DiskoButtonFactory.LONG_BUTTON_SIZE.height*(NUM_ROWS_COMMUNICATOR_LIST)+20));
		m_contentsPanel.add(m_scrollPane);
	}

	/**
	 * Builds buttons based on which communicators are present in the command post. Also stores mapping between these buttons and
	 * the communicator in a hash map
	 */
	@SuppressWarnings("null")
	private void updateCommunicatorList()
	{
		m_listArea.removeAll();
		m_buttonCommunicatorMap.clear();
		m_communicatorButtonMap.clear();

		int i = 0;
		JPanel buttonPanel = null;
		for(final ICommunicatorIf communicator : m_wpMessageLog.getMsoManager().getCmdPost().getActiveCommunicators())
		{
			// Necessary for laying buttons out correctly
			if(i%NUM_ROWS_COMMUNICATOR_LIST == 0)
			{
				buttonPanel = new JPanel();
				buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
				buttonPanel.setAlignmentY(Component.TOP_ALIGNMENT);
				buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
				m_listArea.add(buttonPanel);
			}

			JToggleButton button = DiskoButtonFactory.createLongToggleButton(communicator);
			button.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Update message receiver lists. Toggle buttons gets updated through newMessageSelected, triggered by mso update
					IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
					JToggleButton toggleButton = (JToggleButton)e.getSource();
					if(!toggleButton.isSelected())
					{
						// Button has been unselected
						if(m_selectionMode)
						{
							message.getUnconfirmedReceivers().removeReference(communicator);
							message.getConfirmedReceivers().removeReference(communicator);
						}
						else
						{
							// Move from confirmed to selected
							message.getConfirmedReceivers().removeReference(communicator);
							message.getUnconfirmedReceivers().add(communicator);
						}
					}
					else
					{
						// Button has been selected
						if(m_selectionMode)
						{

							message.addUnconfirmedReceiver(communicator);
						}
						else
						{
							// Communicator should be in unconfirmed list, transfer to confirmed
							message.confirmReceiver(communicator);
						}
					}
					updateStatusLabel();
				}
			});

			// Store mapping between button and communicator
			m_buttonCommunicatorMap.put(button, communicator);
			m_communicatorButtonMap.put(communicator, button);

			buttonPanel.add(button);
			i++;
		}
	}

	/**
	 * Updates label giving information about current state of selection, i.e. number of confirmed/unconfirmed receivers
	 */
	private void updateStatusLabel()
	{
		int numReceivers = m_selectedCommuicators.size();
		int numConfirmedReceivers = m_confirmedCommunicators.size();
		String messageText = m_wpMessageLog.getText("BroadcastStatusLabel.text");
		m_confirmationStatusLabel.setText(String.format(messageText, numConfirmedReceivers, numReceivers));
	}

	/**
	 *  Updates button selection based on selection mode
	 */
	private void updateButtonSelection()
	{
		JToggleButton button = null;
		if(m_selectionMode)
		{
			for(ICommunicatorIf communicator : m_wpMessageLog.getMsoManager().getCmdPost().getActiveCommunicators())
			{
				button = m_communicatorButtonMap.get(communicator);
				if(button != null)
				{
					button.setVisible(true);
					if(m_selectedCommuicators.contains(communicator))
					{
						button.setSelected(true);
					}
					else
					{
						button.setSelected(false);
					}
				}
			}
		}
		else
		{
			for(ICommunicatorIf communicator : m_wpMessageLog.getMsoManager().getCmdPost().getCommunicatorList().getItems())
			{
				button = m_communicatorButtonMap.get(communicator);
				if(button != null)
				{
					if(m_selectedCommuicators.contains(communicator))
					{
						button.setVisible(true);
						if(m_confirmedCommunicators.contains(communicator))
						{
							button.setSelected(true);
						}
						else
						{
							button.setSelected(false);
						}
					}
					else
					{
						button.setVisible(false);
					}
				}
			}
		}
	}

	private void initActionButtons()
	{
		m_buttonRowPanel = new JPanel();
		m_buttonRowPanel.setLayout(new BoxLayout(m_buttonRowPanel, BoxLayout.LINE_AXIS));
		m_buttonRowPanel.setPreferredSize(new Dimension(SingleUnitListSelectionDialog.PANEL_WIDTH,
				DiskoButtonFactory.LONG_BUTTON_SIZE.height));
		m_buttonRowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		m_buttonGroup = new ButtonGroup();

		m_selectionButton = DiskoButtonFactory.createLongToggleButton(m_wpMessageLog.getText("SelectionButton.text"));
		m_selectionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		m_selectionButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if(!m_selectionMode)
				{
					setSelectionMode();
					updateButtonSelection();
				}
			}
		});
		m_selectionButton.setSelected(true);
		m_buttonGroup.add(m_selectionButton);
		m_buttonRowPanel.add(m_selectionButton);

		m_confirmButton = DiskoButtonFactory.createLongToggleButton(m_wpMessageLog.getText("ConfirmButton.text"));
		m_confirmButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		m_confirmButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(m_selectionMode)
				{
					setConfirmationMode();
					updateButtonSelection();
				}
			}
		});
		m_buttonGroup.add(m_confirmButton);
		m_buttonRowPanel.add(m_confirmButton);

		Dimension emptyAreaDimension = new Dimension(30, DiskoButtonFactory.LONG_BUTTON_SIZE.height);
		m_buttonRowPanel.add(Box.createRigidArea(emptyAreaDimension));

		m_allButton = DiskoButtonFactory.createNormalButton(m_wpMessageLog.getText("AllButton.text"));
		m_allButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		m_allButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
				if(m_selectionMode)
				{
					for(ICommunicatorIf communicator : m_wpMessageLog.getMsoManager().getCmdPost().getActiveCommunicators())
					{
						if(!message.getUnconfirmedReceivers().contains(communicator))
						{
							message.addUnconfirmedReceiver(communicator);
						}
					}

					// Set unit type filter buttons
					m_teamButton.setSelected(true);
					m_aircraftButton.setSelected(true);
					m_boatButton.setSelected(true);
					m_commandPostButton.setSelected(true);
					m_dogButton.setSelected(true);
					m_vehicleButton.setSelected(true);
				}
				else
				{
					for(ICommunicatorIf communicator : m_wpMessageLog.getMsoManager().getCmdPost().getActiveCommunicators())
					{
						message.confirmReceiver(communicator);
					}
				}
				updateStatusLabel();
			}
		});
		m_buttonRowPanel.add(m_allButton);

		m_noneButton = DiskoButtonFactory.createNormalButton(m_wpMessageLog.getText("NoneButton.text"));
		m_noneButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		// None button should remove all selected items from the current selection mode (selection/confirmation)
		m_noneButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
				if(m_selectionMode)
				{
					// Need to iterate over all active communicators to avoid concurrency issues, otherwise just selected communicators
					for(ICommunicatorIf communicator : m_wpMessageLog.getMsoManager().getCmdPost().getActiveCommunicators())
					{
						message.getUnconfirmedReceivers().removeReference(communicator);
					}

					// Set unit type filter buttons
					m_teamButton.setSelected(false);
					m_aircraftButton.setSelected(false);
					m_boatButton.setSelected(false);
					m_commandPostButton.setSelected(false);
					m_dogButton.setSelected(false);
					m_vehicleButton.setSelected(false);
				}
				else
				{
					for(ICommunicatorIf communicator : m_wpMessageLog.getMsoManager().getCmdPost().getActiveCommunicators())
					{
						if(message.getConfirmedReceivers().removeReference(communicator))
						{
							message.getUnconfirmedReceivers().add(communicator);
						}
					}
				}
				updateStatusLabel();
			}
		});
		m_buttonRowPanel.add(m_noneButton);

		m_buttonRowPanel.add(Box.createRigidArea(emptyAreaDimension));

		m_unitTypePanel = new JPanel();
		m_unitTypePanel.setLayout(new BoxLayout(m_unitTypePanel, BoxLayout.LINE_AXIS));

		m_teamButton = createUnitButton(UnitType.TEAM);
		m_dogButton = createUnitButton(UnitType.DOG);
		m_vehicleButton = createUnitButton(UnitType.VEHICLE);
		m_aircraftButton = createUnitButton(UnitType.AIRCRAFT);
		m_boatButton = createUnitButton(UnitType.BOAT);
		m_commandPostButton = createUnitButton(UnitType.CP);

		m_buttonRowPanel.add(m_unitTypePanel);

		m_confirmationStatusLabel = new JLabel();
		updateStatusLabel();
		m_confirmationStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		m_buttonRowPanel.add(m_confirmationStatusLabel);

		m_contentsPanel.add(m_buttonRowPanel, BorderLayout.NORTH);
	}

	private JToggleButton createUnitButton(final UnitType type)
	{
		JToggleButton button = DiskoButtonFactory.createNormalToggleButton();
		button.setIcon(Utils.getIcon(type));

		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				JToggleButton sourceButton = (JToggleButton)arg0.getSource();
				if(sourceButton.isSelected())
				{
					addSelectedUnits(type);
				}
				else
				{
					removeSelectedUnits(type);
				}
				updateStatusLabel();
			}
		});

		m_unitTypePanel.add(button);
		return button;
	}

	private void initContentsPanel()
	{
		m_contentsPanel = new JPanel();
		m_contentsPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		m_contentsPanel.setLayout(new BoxLayout(m_contentsPanel, BoxLayout.PAGE_AXIS));
		this.add(m_contentsPanel);
	}

	/**
	 * Resets button selection
	 */
	public void clearContents()
	{
		m_selectionMode = true;
		m_confirmedCommunicators.clear();
		m_selectedCommuicators.clear();
		updateButtonSelection();
	}

	/**
	 *
	 */
	public void hideComponent()
	{
		this.setVisible(false);
	}

	/**
	 * Removes all units of the given type from the selection list
	 */
	private void removeSelectedUnits(UnitType type)
	{
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
		for(ICommunicatorIf communicator : m_wpMessageLog.getMsoManager().getCmdPost().getActiveCommunicators())
		{
			if(communicator instanceof ICmdPostIf && type == UnitType.CP)
			{
				message.getUnconfirmedReceivers().removeReference(communicator);
			}
			else if(communicator instanceof IUnitIf)
			{
				IUnitIf unit = (IUnitIf)communicator;
				if(unit.getType() == type)
				{
					message.getUnconfirmedReceivers().removeReference(communicator);
				}
			}
		}
	}

	/**
	 * Adds all units of a given type to the selection list
	 */
	private void addSelectedUnits(UnitType type)
	{
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
		for(ICommunicatorIf communicator : m_wpMessageLog.getMsoManager().getCmdPost().getActiveCommunicators())
		{
			if(communicator instanceof ICmdPostIf && type == UnitType.CP)
			{
				if(!message.getUnconfirmedReceivers().contains(communicator))
				{
					message.getUnconfirmedReceivers().add(communicator);
				}
			}
			else if(communicator instanceof IUnitIf)
			{
				IUnitIf unit = (IUnitIf)communicator;
				if((unit.getType() == type) && (!message.getUnconfirmedReceivers().contains(communicator)))
				{
					message.getUnconfirmedReceivers().add(communicator);
				}
			}
		}
	}

	/**
	 * Makes button selection based on which receivers are confirmed/unconfirmed
	 * @param message Message in question
	 */
	public void newMessageSelected(IMessageIf message)
	{
		if(!message.isBroadcast())
		{
			return;
		}

		m_confirmedCommunicators.clear();
		m_selectedCommuicators.clear();

		IMsoListIf<ICommunicatorIf> unconfirmedReceivers = message.getBroadcastUnconfirmed();
		IMsoListIf<ICommunicatorIf> confirmedReceivers = message.getConfirmedReceivers();

		// Select buttons and update maps based on message receiver list
		ICommunicatorIf communicator = null;
		for(JToggleButton button : m_buttonCommunicatorMap.keySet())
		{
			communicator = m_buttonCommunicatorMap.get(button);
			if(unconfirmedReceivers.contains(communicator))
			{
				m_selectedCommuicators.add(communicator);
			}
			else if(confirmedReceivers.contains(communicator))
			{
				m_selectedCommuicators.add(communicator);
				m_confirmedCommunicators.add(communicator);
			}
		}

		updateButtonSelection();
		updateStatusLabel();
	}

	/**
	 * Displays the part of the dialog where receivers are chosen
	 */
	public void setSelectionMode()
	{
		m_confirmButton.setSelected(false);
		m_selectionButton.setSelected(true);

		m_selectionMode = true;
		m_unitTypePanel.setVisible(true);
		m_confirmationStatusLabel.setVisible(false);
	}

	/**
	 * Displays the part of the dialog where receivers are confirmed
	 */
	public void setConfirmationMode()
	{
		m_selectionButton.setSelected(false);
		m_confirmButton.setSelected(true);

		m_selectionMode = false;
		m_unitTypePanel.setVisible(false);
		m_confirmationStatusLabel.setVisible(true);
	}

	/**
	 * Displays the dialog. If there are unconfirmed receivers go to confirmation mode,
	 * else display in selection mode (select receivers)
	 */
	public void showComponent()
	{
		// If there exists unconfirmed receivers in the message, go to confirmation mode, most efficient work flow
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		if(message != null)
		{
			if(message.getBroadcastUnconfirmed().size() != 0)
			{
				m_selectionMode = false;
			}
			else
			{
				m_selectionMode = true;
			}

			this.setVisible(true);
			m_listArea.revalidate();

			// Refresh list of communicators
			updateCommunicatorList();

			if(m_selectionMode)
			{
				setSelectionMode();
			}
			else
			{
				setConfirmationMode();
			}

			updateButtonSelection();
		}
	}

	/**
	 * Deselect buttons
	 */
	public void clearSelection()
	{
		m_confirmedCommunicators.clear();
		m_selectedCommuicators.clear();
		updateButtonSelection();
	}
}
