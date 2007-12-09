package org.redcross.sar.wp.messageLog;

import org.apache.derby.iapi.sql.dictionary.KeyConstraintDescriptor;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoButtonFactory;
import org.redcross.sar.gui.DiskoButtonFactory.ButtonType;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Displays the message text in the top panel in the message log
 *
 * @author thomasl
 */
public class TextPanel extends JPanel implements IEditMessageComponentIf
{
	private static final long serialVersionUID = 1L;

	private JScrollPane m_textScroll;
	private JTextArea m_textArea;
	private JButton m_cancelButton;
	private JButton m_okButton;

	/**
	 * @param wp Message log work process
	 */
	public TextPanel(IDiskoWpMessageLog wp)
	{
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		initTextArea(gbc);
		initButtons(gbc);
	}

	private void initTextArea(GridBagConstraints gbc)
	{
		gbc.gridheight = 2;
		gbc.weightx = 1.0;
		m_textArea = new JTextArea();
		m_textArea.setLineWrap(true);
		m_textArea.setWrapStyleWord(true);
		m_textArea.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				// is enter hit without ctrl down?
				if(e.getKeyCode()==KeyEvent.VK_ENTER && !e.isControlDown()) {
					// forward
					apply();
				}
			}			
		});
		m_textArea.setText("");
		m_textScroll = new JScrollPane(m_textArea);
		this.add(m_textScroll, gbc);
	}

	private void initButtons(GridBagConstraints gbc)
	{
		gbc.gridheight = 1;
		gbc.gridx++;
		gbc.weightx = 0.0;

		m_cancelButton = DiskoButtonFactory.createNormalButton(ButtonType.CancelButton);

		m_cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// forward
				cancel();
			}
		});
		this.add(m_cancelButton, gbc);

		gbc.gridy = 1;
		m_okButton = DiskoButtonFactory.createNormalButton(ButtonType.OkButton);
		m_okButton.setDefaultCapable(true);
		m_okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// forward
				apply();

			}
		});
		this.add(m_okButton, gbc);
	}

	private void cancel() {
		// Reset components to what is currently stored in MSO
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		if(message != null)
		{
			IMessageLineIf line = message.findMessageLine(MessageLineType.TEXT, false);
			if(line != null)
			{
				m_textArea.setText(line.getLineText());
			}
			else
			{
				m_textArea.setText("");
			}
		}
		else
		{
			m_textArea.setText("");
		}
		MessageLogBottomPanel.showListPanel();						
	}
	
	private void apply() {
		// has no text?
		if(m_textArea.getText().isEmpty()) {
			Utils.showWarning("Du må skrive inn tekst");
		}
		else {
			// Store text in current message
			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
			IMessageLineIf textLine = message.findMessageLine(MessageLineType.TEXT, true);
			// changed?
			if(m_textArea.getText()!=null && !m_textArea.getText().equals(textLine.getLineText())) {
				textLine.setLineText(m_textArea.getText());
				MessageLogBottomPanel.setIsDirty();
				
			}
			// forward
			MessageLogBottomPanel.showListPanel();
		}
	}
	
	/**
	 *
	 */
	public void hideComponent()
	{
		this.setVisible(false);
	}

	/**
	 * If message has text message line, set contents in text area to text in that line
	 */
	public void newMessageSelected(IMessageIf message)
	{
		IMessageLineIf textMessageLine = message.findMessageLine(MessageLineType.TEXT, false);
		if(textMessageLine != null)
		{
			String text = textMessageLine.getLineText();
			if(!text.equals(""))
			{
				m_textArea.setText(textMessageLine.getLineText());
			}
		}
	}

	/**
	 *
	 */
	public void showComponent()
	{
		this.setVisible(true);
		m_textArea.requestFocus();
	}

	/**
	 *
	 */
	public void clearContents()
	{
		m_textArea.setText("");
	}
}
