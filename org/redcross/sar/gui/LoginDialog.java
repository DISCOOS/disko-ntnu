package org.redcross.sar.gui;

import javax.swing.JPanel;
import javax.swing.WindowConstants;

import java.awt.Frame;
import java.awt.FlowLayout;
import javax.swing.JButton;

import javax.swing.BoxLayout;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.attribute.ComboAttribute;
import org.redcross.sar.gui.attribute.TextFieldAttribute;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

public class LoginDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;

	private boolean allowExit = true;

	private JPanel mainPanel = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JPanel buttonPanel = null;

	private TextFieldAttribute attrUserName = null;
	private TextFieldAttribute attrPassword = null;
	private ComboAttribute attrRoles = null;
	
	/**
	 * @param owner
	 */
	public LoginDialog(Frame owner) {
		// forward
		super(owner);
		// initialize GUI
		initialize();
	}
	
	private void initialize() {
		try {
			this.setTitle("DISKO Innlogging");
            this.setContentPane(getMainPanel());
            this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.setUndecorated(false);
            this.setModal(true);
            this.pack();
				
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}

	public boolean getAllowExit() {
		return allowExit;
	}
	
	public void setVisible(boolean isVisible, boolean exit) {
		allowExit = exit;
		super.setVisible(isVisible);
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
			mainPanel.add(createRigidArea());
			mainPanel.add(getRoles());
			mainPanel.add(createRigidArea());
			mainPanel.add(getUserName());
			mainPanel.add(createRigidArea());
			mainPanel.add(getPassword());
			mainPanel.add(createRigidArea());
			mainPanel.add(getButtonPanel());
			mainPanel.add(createRigidArea());
		}
		return mainPanel;
	}

	public void setFixedSize() {
		int offset = 0;
		Utils.setFixedSize(getRoles(),getWidth()-offset, 25);	
		Utils.setFixedSize(getUserName(),getWidth()-offset, 25);	
		Utils.setFixedSize(getPassword(),getWidth()-offset, 25);	
		Utils.setFixedSize(getButtonPanel(),getWidth()-offset, 60);	
	}	
	
	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOKButton() {
		if (okButton == null) {
			try {
				okButton = DiskoButtonFactory.createButton("GENERAL.OK", ButtonSize.NORMAL);
				//okButton.setPreferredSize(new Dimension(100, 50));
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
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL", ButtonSize.NORMAL);
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
	 * This method initializes UserName attribute
	 * 	
	 * @return {@link TextFieldAttribute}
	 */
	public TextFieldAttribute getUserName() {
		if (attrUserName == null) {
			try {
				attrUserName = new TextFieldAttribute("username","Brukernavn",70,"",true);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return attrUserName;
	}
	
	/**
	 * This method initializes Password attribute
	 * 	
	 * @return {@link TextFieldAttribute}
	 */
	public TextFieldAttribute getPassword() {
		if (attrPassword == null) {
			try {
				attrPassword = new TextFieldAttribute("password","Passord",70,"",true);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return attrPassword;
	}
	
	/**
	 * This method initializes Roles attribute
	 * 	
	 * @return {@link ComboAttribute}
	 */
	public ComboAttribute getRoles() {
		if (attrRoles == null) {
			try {
				attrRoles = new ComboAttribute("roles","Roller",70,"",false);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return attrRoles;
	}

	/**
	 * This method initializes buttonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			try {
				FlowLayout fl = new FlowLayout();
				fl.setAlignment(FlowLayout.CENTER);
				buttonPanel = new JPanel();
				buttonPanel.setLayout(fl);
				buttonPanel.add(getOKButton(), null);
				buttonPanel.add(getCancelButton(), null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
