package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.NumPadDialog;
import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.mso.DTG;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Calendar;

/**
 * Creates the dialog for changing DTG in message log edit mode.
 * @author thomasl
 */
public class ChangeDTGDialog extends DefaultDialog implements KeyListener, IEditMessageComponentIf
{
	private static final long serialVersionUID = 1L;

	private BasePanel m_contentsPanel = null;

	private JPanel m_createdPanel;
	private JLabel m_createdLabel;
	private JTextField m_createdTextField;

	private JPanel m_timePanel;
	private JLabel m_timeLabel;
	private JFormattedTextField m_timeTextField;
	private IDiskoWpMessageLog m_wp;

	private NumPadDialog m_numpad = null;
	private static final boolean NOTEBOOK_MODE = true;

	/**
	 * @param wp Message log work process reference
	 */
	public ChangeDTGDialog(IDiskoWpMessageLog wp)
	{
		super(wp.getApplication().getFrame());
		m_wp = wp;

		initialize();
	}

	private void initialize()
	{
		try
		{
            this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(170,240));
            this.setUndecorated(true);
			this.pack();
			m_timeTextField.requestFocus();
		}
		catch (java.lang.Throwable e){}

		// Initialize numpad
		if(NOTEBOOK_MODE)
		{
			m_numpad = new NumPadDialog(m_wp.getApplication().getFrame());
			JButton okButton = m_numpad.getOkButton();
			okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					saveDTG();
				}
			});
			m_numpad.setTextField(m_timeTextField);
		}
	}

	private JPanel getContentPanel()
	{
		if (m_contentsPanel == null) {
			try
			{
				m_contentsPanel = new BasePanel("Dato-Tid-Gruppe");
				m_contentsPanel.setPreferredSize(new Dimension(180,250));
				m_contentsPanel.setScrollBarPolicies(
						BasePanel.VERTICAL_SCROLLBAR_NEVER, 
						BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
				JPanel panel = (JPanel)m_contentsPanel.getBodyComponent();
				
				panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
			
				m_createdPanel = new JPanel();
				m_createdPanel.setLayout(new BoxLayout(m_createdPanel,BoxLayout.X_AXIS));
				m_createdLabel = new JLabel(m_wp.getBundleText("ChangeDTGDialogCreated.text"));
				m_createdLabel.setHorizontalAlignment(SwingConstants.CENTER);
				m_createdPanel.add(m_createdLabel);
				Utils.setFixedSize(m_createdLabel,70,30);
				m_createdPanel.add(Box.createHorizontalStrut(5));
				m_createdTextField = new JTextField(6);
				m_createdTextField.setEditable(false);
				m_createdPanel.add(m_createdTextField);
				m_createdPanel.add(Box.createHorizontalGlue());
				Utils.setFixedSize(m_createdPanel,160,30);
				

				panel.add(m_createdPanel);				
				panel.add(Box.createVerticalStrut(5));

				m_timePanel = new JPanel();
				m_timePanel.setLayout(new BoxLayout(m_timePanel,BoxLayout.X_AXIS));
				m_timeLabel = new JLabel(m_wp.getBundleText("ChangeDTGDialogTime.text"));
				m_timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
				m_timePanel.add(m_timeLabel);
				Utils.setFixedSize(m_timeLabel,70,30);
				m_createdPanel.add(Box.createHorizontalStrut(5));
				m_timeTextField = new JFormattedTextField();
				m_timeTextField.setDocument(new NumericDocument(6,0,false));
				m_timeTextField.addKeyListener(this);
				m_timePanel.add(m_timeTextField);
				m_timePanel.add(Box.createHorizontalGlue());
				Utils.setFixedSize(m_timePanel,160,30); 

				panel.add(m_timePanel);
				panel.add(Box.createVerticalGlue());

				
				// initialize contents
				clearContents();
			}
			catch (java.lang.Throwable e)
			{
			}
		}
		return m_contentsPanel;
	}

	/**
	 * @return Text in time text field
	 */
	public String getTime()
	{
		return m_timeTextField.getText();
	}

	/**
	 * Saves DTG if key is enter
	 */
	public void keyPressed(KeyEvent ke)
	{
		// Changes should be checked, and if found correct, sent to mso, not commited
		if(ke.getKeyCode() == KeyEvent.VK_ENTER)
		{
			saveDTG();
		}
	}

	private void saveDTG()
	{
		this.setVisible(false);

		try
		{
			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
			message.setTimeStamp(DTG.DTGToCal(this.getTime()));
			fireOnWorkFinish(this,message);
		}
		catch (IllegalMsoArgumentException e1)
		{
			Utils.showError(m_wp.getBundleText("InvalidDTG.header"), m_wp.getBundleText("InvalidDTG.details"));
			fireOnWorkCancel(this,null);
		}
	}

	@Override
	public boolean isFocusable()
	{
		return true;
	}

	public void keyReleased(KeyEvent arg0){}
	public void keyTyped(KeyEvent arg0){}

	/**
	 * Sets the DTG field, get it from the message
	 */
	public void newMessageSelected(IMessageIf message)
	{
    	// forward
		setCreated(message.getCreated());
		setTime(message.getTimeStamp());
	}

	/**
	 * Sets the created text field
	 * @param created
	 */
	public void setCreated(Calendar created)
	{
		m_createdTextField.setText(DTG.CalToDTG(created));
	}

	/**
	 * Sets the time text field
	 * @param calendar
	 */
	public void setTime(Calendar calendar)
	{
		m_timeTextField.setText(DTG.CalToDTG(calendar));
	}

	/**
	 * Displays the numpad if in notebook mode
	 */
	public void showComponent()
	{
		// get new message
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		
		// has no message?
		if(message==null) {		
			// get time
			Calendar time = Calendar.getInstance();
			// update fields
			setCreated(time);
			setTime(time);
		}
		
		// show me
		this.setVisible(true);

		// show num pad?
		if(NOTEBOOK_MODE)
		{
			Point location = m_contentsPanel.getLocationOnScreen();
			location.x += m_contentsPanel.getWidth();
			location.y += m_contentsPanel.getHeight();
			location.y -= m_numpad.getHeight();
			m_numpad.setLocation(location);
			m_numpad.setVisible(true);
		}
	}

	/**
	 * Hides the numpad if in notebook mode
	 */
	public void hideComponent()
	{
		this.setVisible(false);

		if(NOTEBOOK_MODE)
		{
			m_numpad.setVisible(false);
		}
	}

	/**
	 * Reset created and time fields
	 */
	public void clearContents()
	{
		m_createdTextField.setText("");
		m_timeTextField.setText("");
	}
}
