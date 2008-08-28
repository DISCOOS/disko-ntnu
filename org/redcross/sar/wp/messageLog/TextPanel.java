package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
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

	private BasePanel m_textPanel;
	
	private JTextArea m_textArea;
	
	private JPanel m_actionsPanel;
	private AbstractButton m_cancelButton;
	private AbstractButton m_okButton;

	/**
	 * @param wp Message log work process
	 */
	public TextPanel(IDiskoWpMessageLog wp)
	{
		// forward
		super(ButtonSize.SMALL);
		
		// hide header and border
		setHeaderVisible(false);
		setBorderVisible(false);
				
		// add empty border
		setBodyBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		// disable scrolling
		setNotScrollBars();
		
		// set layout
		setBodyLayout(new BoxLayout((JComponent)getBodyComponent(),BoxLayout.X_AXIS));
		
		// add components (BorderLayout is default)
		addBodyChild(getTextPanel());
		addBodyChild(Box.createHorizontalStrut(5));
		addBodyChild(getActionsPanel());		
		
	}

	private BasePanel getTextPanel()
	{
		if(m_textPanel == null) {
			m_textPanel = new BasePanel("Meldingstekst",ButtonSize.SMALL);
			m_textPanel.setBodyComponent(getTextArea());
		}
		return m_textPanel;
	}
	
	private JTextArea getTextArea()
	{
		if(m_textArea ==null) {
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
		}
		return m_textArea;
	}

	private JPanel getActionsPanel()
	{
		if(m_actionsPanel == null) {
			
			// create panel
			m_actionsPanel = new JPanel();
			
			// set layout manager on y-axis
			m_actionsPanel.setLayout(new BoxLayout(m_actionsPanel,BoxLayout.Y_AXIS));
			
			// get buttons
			m_cancelButton = getButton("cancel");
			m_okButton = getButton("finish");
			
			// add to actions
			m_actionsPanel.add(m_cancelButton);			
			m_actionsPanel.add(m_okButton);	
			m_actionsPanel.add(Box.createVerticalGlue());
			
		}
		return m_actionsPanel;
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
		setDirty(false);
		m_textArea.requestFocusInWindow();
	}

	/**
	 *
	 */
	public void clearContents()
	{
		m_textArea.setText("");
	}
}
