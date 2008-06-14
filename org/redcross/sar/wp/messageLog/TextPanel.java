package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Displays the message text in the top panel in the message log
 *
 * @author thomasl
 */
public class TextPanel extends DefaultPanel implements IEditMessageComponentIf
{
	private static final long serialVersionUID = 1L;

	private JTextArea m_textArea;
	private JPanel m_actionsPanel = null;
	private JButton m_cancelButton;
	private JButton m_okButton;

	/**
	 * @param wp Message log work process
	 */
	public TextPanel(IDiskoWpMessageLog wp)
	{
		setHeaderVisible(false);
		setScrollBarPolicies(
				BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED, 
				BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel panel = (JPanel)getBodyComponent();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		initTextArea(panel,gbc);
		initButtons(panel,gbc);
	}

	private void initTextArea(JPanel panel,GridBagConstraints gbc)
	{
		gbc.gridheight = 2;
		gbc.weightx = 1.0;
		
		m_textArea = new JTextArea();
		m_textArea.setLineWrap(true);
		m_textArea.setWrapStyleWord(true);
		m_textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// is enter hit without ctrl down?
				if(e.getKeyCode()==KeyEvent.VK_ENTER && !e.isControlDown()) {
					// consume event
					e.consume();
					// forward
					finish();
				}
			}
		});
		m_textArea.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) { change(); }
			@Override
			public void insertUpdate(DocumentEvent e) { change(); }
			@Override
			public void removeUpdate(DocumentEvent e) { change(); }
			
			private void change() {
				if(!isChangeable()) return;
				setDirty(true);
			}
			
		});
		
		m_textArea.setText("");
		panel.add(m_textArea,gbc);
	}

	private void initButtons(JPanel panel,GridBagConstraints gbc)
	{
		gbc.gridheight = 1;
		gbc.gridx++;
		gbc.weightx = 0.0;
		
		// create panel
		m_actionsPanel = new JPanel();
		
		// set layout manager on y-axis
		m_actionsPanel.setLayout(new BoxLayout(m_actionsPanel,BoxLayout.Y_AXIS));
		
		// add buttons
		m_cancelButton = (JButton)getButton("cancel");
		m_actionsPanel.add(m_cancelButton);
		
		m_okButton = (JButton)getButton("finish");
		m_actionsPanel.add(m_okButton);

		m_actionsPanel.add(Box.createVerticalGlue());
		
		panel.add(m_actionsPanel,gbc);
				
	}

	public boolean cancel() {
		// forward
		boolean bFlag = super.cancel();
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
		return bFlag;
	}
	
	public boolean finish() {
		// has no text?
		if(m_textArea.getText().isEmpty()) {
			Utils.showWarning("Du må skrive inn tekst");
		}
		else {
			// forward
			boolean bFlag = super.finish();
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
			return bFlag;			
		}
		return false;
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
		setDirty(false);
	}

	/**
	 *
	 */
	public void clearContents()
	{
		m_textArea.setText("");
	}
}
