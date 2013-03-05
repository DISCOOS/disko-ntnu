package org.disco.tools.vsde;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class JAddReply extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel contentPanel = null;
	private JPanel replyPanel = null;
	private JPanel controlPanel = null;

	private JButton addButton = null;
	private JButton cancelButton = null;


	private JLabel messageLabel = null;
	private JLabel replyLabel = null;

	private JTextField messageTextField = null;
	private JTextField replyTextField = null;
	
	String[] autoReply;

	public JAddReply(Frame frame) {
		super(frame);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(280, 140);
		this.setTitle("Add auto-reply");
		this.setModal(true);
		this.setContentPane(getContentPanel());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new JPanel();
			contentPanel.setLayout(new BorderLayout());
			contentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			contentPanel.add(getControlPanel(), BorderLayout.SOUTH);
			contentPanel.add(getReplyPanel(), BorderLayout.CENTER);
		}
		return contentPanel;
	}

	/**
	 * This method initializes controlPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new JPanel();
			controlPanel.setPreferredSize(new Dimension(300,35));
			controlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			controlPanel.add(getCancelButton(), null);
			controlPanel.add(getAddButton(), null);
		}
		return controlPanel;
	}

	/**
	 * This method initializes addButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddButton() {
		if (addButton == null) {
			addButton = new JButton();
			addButton.setText("Add");
			addButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String message = getMessageTextField().getText();
					String reply = getReplyTextField().getText();
					if(message.isEmpty() || reply.isEmpty()) {
						JOptionPane.showMessageDialog(JAddReply.this, "Both a message and an reply must be supplied");
					}
					else {
						autoReply = new String[]{message,reply};
						setVisible(false);
					}
				}
				
			});
		}
		return addButton;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setVisible(false);					
				}
				
			});
		}
		return cancelButton;
	}

	/**
	 * This method initializes replyPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getReplyPanel() {
		if (replyPanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.ipadx = 5;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;		
			replyLabel = new JLabel();
			replyLabel.setText("Reply:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.ipadx = 5;
			gridBagConstraints.anchor = GridBagConstraints.WEST;		
			messageLabel = new JLabel();
			messageLabel.setText("Message:");
			replyPanel = new JPanel();
			replyPanel.setPreferredSize(new Dimension(300,60));
			replyPanel.setLayout(new GridBagLayout());
			replyPanel.add(messageLabel, gridBagConstraints);
			replyPanel.add(replyLabel, gridBagConstraints1);
			replyPanel.add(getMessageTextField(), gridBagConstraints2);
			replyPanel.add(getReplyTextField(), gridBagConstraints3);
		}
		return replyPanel;
	}

	/**
	 * This method initializes messageTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMessageTextField() {
		if (messageTextField == null) {
			messageTextField = new JTextField();
			messageTextField.setPreferredSize(new Dimension(200,30));

		}
		return messageTextField;
	}

	/**
	 * This method initializes replyTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextField getReplyTextField() {
		if (replyTextField == null) {
			replyTextField = new JTextField();
			replyTextField.setPreferredSize(new Dimension(200,30));
		}
		return replyTextField;
	}

	public String[] prompt() {
		autoReply = null;
		this.setVisible(true);
		return autoReply;
	}
	

}
