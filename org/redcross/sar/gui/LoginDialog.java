package org.redcross.sar.gui;

import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.Dimension;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class LoginDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private JPanel mainPanel = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JTextField usernameTextField = null;
	private JLabel usernameLabel = null;
	private JLabel passwordLabel = null;
	private JPasswordField passwordField = null;
	private JLabel rolleLabel = null;
	private JComboBox rolleComboBox = null;
	private JPanel buttonPanel = null;
	private boolean allowExit = true;

	/**
	 * @param owner
	 */
	public LoginDialog(Frame owner) {
		super(owner);
		initialize();
	}
	
	private void initialize() {
		try {
			this.setSize(319, 289);
			this.setTitle("DISKO Innlogging");
            this.setContentPane(getMainPanel());
            this.setUndecorated(false);
            this.setModal(true);
            this.pack();
				
		}
		catch (java.lang.Throwable e) {
			//  Do Something
		}
	}

	public boolean getAllowExit() {
		return allowExit;
	}
	
	public void setVisible(boolean value, boolean exit) {
		allowExit = exit;
		super.setVisible(value);
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.gridy = 4;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.insets = new Insets(0, 10, 25, 0);
			gridBagConstraints7.gridy = 2;
			rolleLabel = new JLabel();
			rolleLabel.setText("Rolle:");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.insets = new Insets(0, 10, 25, 0);
			gridBagConstraints6.gridy = 1;
			passwordLabel = new JLabel();
			passwordLabel.setText("Passord:");
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.insets = new Insets(10, 10, 25, 0);
			gridBagConstraints5.gridy = 0;
			usernameLabel = new JLabel();
			usernameLabel.setText("Brukernavn:");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.gridy = 2;
			gridBagConstraints4.ipady = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.gridheight = 1;
			gridBagConstraints4.insets = new Insets(0, 0, 25, 10);
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.ipadx = -189;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridwidth = 2;
			gridBagConstraints2.weighty = 0.0D;
			gridBagConstraints2.insets = new Insets(0, 0, 25, 10);
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.ipadx = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new Insets(10, 0, 25, 10);
			gridBagConstraints.gridwidth = 1;
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridBagLayout());
			mainPanel.add(getUsernameTextField(), gridBagConstraints);
			mainPanel.add(getPasswordField(), gridBagConstraints2);
			mainPanel.add(getRolleComboBox(), gridBagConstraints4);
			mainPanel.add(usernameLabel, gridBagConstraints5);
			mainPanel.add(passwordLabel, gridBagConstraints6);
			mainPanel.add(rolleLabel, gridBagConstraints7);
			mainPanel.add(getButtonPanel(), gridBagConstraints8);
		}
		return mainPanel;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			try {
				okButton = DiskoButtonFactory.createNormalButton("OK");
				okButton.setPreferredSize(new Dimension(100, 50));
				okButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						fireOnWorkFinish();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return okButton;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = DiskoButtonFactory.createNormalButton("Avbryt");
				cancelButton.setPreferredSize(new Dimension(100, 50));
				cancelButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						fireOnWorkCancel();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return cancelButton;
	}


	/**
	 * This method initializes usernameTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getUsernameTextField() {
		if (usernameTextField == null) {
			try {
				usernameTextField = new JTextField();
				usernameTextField.setPreferredSize(new Dimension(200, 24));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return usernameTextField;
	}

	/**
	 * This method initializes passwordField	
	 * 	
	 * @return javax.swing.JPasswordField	
	 */
	public JPasswordField getPasswordField() {
		if (passwordField == null) {
			try {
				passwordField = new JPasswordField();
				passwordField.setPreferredSize(new Dimension(200, 24));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return passwordField;
	}

	/**
	 * This method initializes rolleComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JComboBox getRolleComboBox() {
		if (rolleComboBox == null) {
			try {
				rolleComboBox = new JComboBox();
				rolleComboBox.setPreferredSize(new Dimension(200, 24));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return rolleComboBox;
	}

	/**
	 * This method initializes buttonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			try {
				GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
				gridBagConstraints1.insets = new Insets(0, 111, 0, 0);
				gridBagConstraints1.gridx = 1;
				gridBagConstraints1.gridy = 0;
				gridBagConstraints1.ipady = 0;
				gridBagConstraints1.gridheight = 1;
				GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
				gridBagConstraints3.gridwidth = 1;
				gridBagConstraints3.gridy = -1;
				gridBagConstraints3.ipadx = 0;
				gridBagConstraints3.gridx = -1;
				buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout());
				buttonPanel.add(getOkButton(), null);
				buttonPanel.add(getCancelButton(), null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
