
package org.redcross.sar.gui.panel;
 
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class MessagePanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;

	private JLabel m_msgLabel = null;
	
	public MessagePanel() {
		// forward
		super();
		// initialize GUI
		initialize();
	}
	
	/**
	 * Initialize this
	 */
	private void initialize() {
		// set table
		setBodyComponent(getMessageLabel());
	}
	
	/**
	 * Initialize the message label 
	 */
	private JLabel getMessageLabel() {
		if(m_msgLabel == null) {
			m_msgLabel = new JLabel();
			m_msgLabel.setOpaque(false);
			m_msgLabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			m_msgLabel.setHorizontalAlignment(SwingConstants.LEFT);
			m_msgLabel.setVerticalAlignment(SwingConstants.CENTER);
		}
		return m_msgLabel;
	}
	
	public String getMessage() {
		return getMessageLabel().getText();
	}
	
	public void setMessage(String msg) {
		getMessageLabel().setText("<html>"+msg+"</html>");
	}
	
}
