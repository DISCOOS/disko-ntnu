package org.redcross.sar.wp.messageLog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkEventListener;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;

/**
 * Provides a dialog for selecting broadcast or non-broadcast receiver. This dialog also handles sub-dialogs
 * such as field based unit selection, list unit selection and the broadcast dialogs
 *
 * @author thomasl
 */
public class ChangeToDialog extends DiskoDialog 
	implements IEditMessageComponentIf, IDiskoWorkEventListener
{
	private static final long serialVersionUID = 1L;

	protected JToggleButton m_broadcastButton;
	protected JToggleButton m_nonBroadcastButton;
	protected ButtonGroup m_buttonGroup;
	protected JPanel m_contentsPanel;

	protected UnitFieldSelectionDialog m_nbFieldDialog;
	protected SingleUnitListSelectionDialog m_nbListDialog;

	protected BroadcastToDialog m_broadcastDialog;

	protected boolean m_broadcast = false;

	protected IDiskoWpMessageLog m_wpMessageLog;

	/**
	 * @param wp Message log work process reference
	 */
	public ChangeToDialog(IDiskoWpMessageLog wp)
	{
		super(wp.getApplication().getFrame());

		m_wpMessageLog = wp;

		initialize();
	}

	private void initialize()
	{
		initContentsPanel();
		initButtons();
		initDialogs();
		this.pack();
	}

	private void initDialogs()
	{
		m_nbFieldDialog = new UnitFieldSelectionDialog(m_wpMessageLog, false);
		m_nbFieldDialog.addDiskoWorkEventListener(this);
		m_nbFieldDialog.getOKButton().addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				ICommunicatorIf singleReceiver = m_nbFieldDialog.getCommunicator();
				if(singleReceiver != null)
				{
					IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
					message.setSingleReceiver(singleReceiver);
					fireOnWorkFinish();
				}
			}	
		});
		m_nbListDialog = new SingleUnitListSelectionDialog(m_wpMessageLog, false);
		m_broadcastDialog = new BroadcastToDialog(m_wpMessageLog);
		
		m_nbFieldDialog.addDiskoWorkEventListener(this);
		m_nbListDialog.addDiskoWorkEventListener(this);
		m_broadcastDialog.addDiskoWorkEventListener(this);
		
		m_nbFieldDialog.addActionListener(m_nbListDialog);
	}

	private void initContentsPanel()
	{
		m_contentsPanel = new JPanel();
		m_contentsPanel.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		m_contentsPanel.setLayout(new BoxLayout(m_contentsPanel, BoxLayout.LINE_AXIS));
		Dimension dim = DiskoButtonFactory.getButtonSize(ButtonSize.LONG);
		m_contentsPanel.setPreferredSize(new Dimension(dim.width*2,dim.height));
		this.add(m_contentsPanel);
	}

	private void initButtons()
	{
		m_buttonGroup = new ButtonGroup();
		String text = m_wpMessageLog.getText("NonBroadcastButton.text");
		m_nonBroadcastButton = DiskoButtonFactory.createToggleButton(text,text,null,ButtonSize.LONG);
		//m_nonBroadcastButton.setHorizontalAlignment(SwingConstants.LEFT);
		m_nonBroadcastButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				m_broadcast = false;
				IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
				if(message!=null) message.setBroadcast(false);

				m_broadcastDialog.hideComponent();
				m_broadcastDialog.clearSelection();
				showNonBroadcast();
			}
		});
		m_nonBroadcastButton.setSelected(true);
		m_buttonGroup.add(m_nonBroadcastButton);
		m_contentsPanel.add(m_nonBroadcastButton);
		
		text = m_wpMessageLog.getText("BroadcastButton.text");
		m_broadcastButton = DiskoButtonFactory.createToggleButton(text,text,null,ButtonSize.LONG);
		//m_broadcastButton.setHorizontalAlignment(SwingConstants.LEFT);
		m_broadcastButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				m_broadcast = true;
				IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
				if(message!=null) message.setBroadcast(true);

				m_nbFieldDialog.hideComponent();
				m_nbListDialog.hideComponent();
				m_nbListDialog.clearSelection();
				
				showBroadcast();
			}
		});
		m_buttonGroup.add(m_broadcastButton);
		m_contentsPanel.add(m_broadcastButton);
		
		m_contentsPanel.add(Box.createHorizontalGlue());
	}
	
	/**
	 * Hides dialogs
	 */
	public void hideComponent()
	{
		this.setVisible(false);
		m_nbListDialog.hideComponent();
		m_nbFieldDialog.hideComponent();
		m_broadcastDialog.hideComponent();
	}

	/**
	 * Set broadcast mode if message is broadcast message
	 */
	public void newMessageSelected(IMessageIf message)
	{
		m_broadcast = message.isBroadcast();
		if(m_broadcast)
		{
			m_broadcastButton.setSelected(true);
		}
		else
		{
			m_nonBroadcastButton.setSelected(true);
		}
		
		m_nbListDialog.newMessageSelected(message);
		m_nbFieldDialog.newMessageSelected(message);
		m_broadcastDialog.newMessageSelected(message);
	}
	
	public String getCommunicatorText()
	{
		if(m_broadcast)
		{
			return "FA";
		}
		else
		{
			return m_nbFieldDialog.getCommunicatorText();
		}
	}

	/**
	 * If broadcast message broadcast dialogs are shown, else single receiver mode dialogs are shown
	 */
	public void showComponent()
	{
		// show me
		this.setVisible(true);
		// get current message, do not create if not exist
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		// get flag
		boolean isBroadcast = (message!=null ? message.isBroadcast() : false); 
		// setup
		if(isBroadcast)
		{
			showBroadcast();
		}
		else
		{
			showNonBroadcast();
		}
	}

	private void showNonBroadcast()
	{
		// get current message and command post
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		ICmdPostIf cmdPost = m_wpMessageLog.getCmdPost();
		
		// get current receiver
		ICommunicatorIf receiver = (cmdPost!=null && message!=null? message.getSingleReceiver() : (ICommunicatorIf)cmdPost);
		
		// has receiver?
		if(receiver != null)
		{
			m_nbFieldDialog.setCommunicatorNumber(receiver.getCommunicatorNumber());
			m_nbFieldDialog.setCommunicatorNumberPrefix(receiver.getCommunicatorNumberPrefix());
		}
		
		Point location = m_nonBroadcastButton.getLocationOnScreen();
		location.y -= m_nbFieldDialog.getHeight();
		m_nbFieldDialog.setLocation(location);
		m_nbFieldDialog.showComponent();
		
		location = m_nonBroadcastButton.getLocationOnScreen();
		location.y -= m_nbListDialog.getHeight();
		location.x += m_nbFieldDialog.getWidth();
		m_nbListDialog.setLocation(location);
		m_nbListDialog.showComponent();
	}

	private void showBroadcast()
	{
		Point location = m_nonBroadcastButton.getLocationOnScreen();
		location.y -= m_broadcastDialog.getHeight();
		m_broadcastDialog.setLocation(location);
		m_broadcastDialog.showComponent();
	}
	
	/**
	 * {@link IEditMessageComponentIf#clearContents()}
	 */
	public void clearContents()
	{
		m_nonBroadcastButton.setSelected(true);
		m_broadcastDialog.clearContents();
		m_nbFieldDialog.clearContents();
		m_nbListDialog.clearContents();
	}

	/**
	 * {@link IDiskoWorkEventListener#onDialogCancel(DiskoWorkEvent)}
	 */
	public void onWorkCancel(DiskoWorkEvent e)
	{
		// not in use!
	}

	/**
	 * {@link IDiskoWorkEventListener#onWorkFinish(DiskoWorkEvent)}
	 */
	public void onWorkFinish(DiskoWorkEvent e)
	{
		fireOnWorkFinish(e);
	}

	/**
	 * {@link IDiskoWorkEventListener#onWorkChange(DiskoWorkEvent)}
	 */
	public void onWorkChange(DiskoWorkEvent e)
	{
		// not in use!
	}
	
	/**
	 * Keep track of broadcast or not
	 * @param broadcast
	 */
	public void setBroadcast(boolean broadcast)
	{
		m_broadcast = broadcast;
	}
	
	/**
	 * @return Whether in broadcast mode or not
	 */
	public boolean getBroadcast()
	{
		return m_broadcast;
	}
}
