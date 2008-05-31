package org.redcross.sar.gui.dialog;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.attribute.ComboAttribute;
import org.redcross.sar.gui.attribute.TextFieldAttribute;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.gui.panel.DefaultPanel;

public class LoginDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private boolean isLogin = false;
	private boolean isCancel = false;
	private boolean exitAppOnCancel = false;
	
	private DefaultPanel contentPanel = null;

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
            this.setModal(true);
            this.setUndecorated(true);
            this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(275,160));
            this.pack();
				
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private DefaultPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new DefaultPanel("Innlogging");
			contentPanel.setScrollBarPolicies(
					DefaultPanel.VERTICAL_SCROLLBAR_NEVER,
					DefaultPanel.HORIZONTAL_SCROLLBAR_NEVER);
			contentPanel.setPreferredBodySize(new Dimension(275,100));
			JPanel panel = (JPanel)contentPanel.getBodyComponent();
			panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			panel.add(getRoles());
			panel.add(Box.createVerticalStrut(5));
			panel.add(getUserName());
			panel.add(Box.createVerticalStrut(5));
			panel.add(getPassword());
			contentPanel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if("finish".equalsIgnoreCase(cmd))
						authorize();
					else if("cancel".equalsIgnoreCase(cmd)) {
						// exit system?
						if (exitAppOnCancel) System.exit(0);								
					}
										
				}
				
			});
		}
		return contentPanel;
	}

	public void setFixedSize() {
		int offset = 0;
		getContentPanel().setFixedSize();
		Utils.setFixedSize(getRoles(),getWidth()-offset, 25);	
		Utils.setFixedSize(getUserName(),getWidth()-offset, 25);	
		Utils.setFixedSize(getPassword(),getWidth()-offset, 25);	
	}	
	
	private void authorize() {
		// get values
		String role = (String)getRoles().getValue();
		String user = (String)getUserName().getValue();
		char[] pwd = String.valueOf(getPassword().getValue()).toCharArray();
		// initialize authorization flag
		boolean auth = true;
		// login or change role?
		if(isLogin) {
			// forward
			auth = Utils.getApp().login(role, user, pwd);
			// is not authorized?
			if(!auth) Utils.showWarning(DiskoStringFactory.getText("WARNING_LOGIN_FAILED"));
		}
		else {
			// forward
			auth = Utils.getApp().swapTo(role,user,pwd);
			// is not authorized?
			if(!auth) Utils.showWarning(DiskoStringFactory.getText("WARNING_LOGIN_FAILED"));
		}
		// hide me?
		if(auth) super.setVisible(false);
	}

	/**
	 * This method initializes UserName attribute
	 * 	
	 * @return {@link TextFieldAttribute}
	 */
	public TextFieldAttribute getUserName() {
		if (attrUserName == null) {
			try {
				attrUserName = new TextFieldAttribute("username","Brukernavn",100,"",true);
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
				attrPassword = new TextFieldAttribute("password","Passord",100,"",true);
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
				attrRoles = new ComboAttribute("roles","Roller",100,"",false);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return attrRoles;
	}

	public void load() {
		String[] rolleNames = null;
		IDiskoApplication app = Utils.getApp();
		try {
			rolleNames = app.getModuleManager().getRoleTitles(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (int i = 0; i < rolleNames.length; i++) {
			IDiskoRole currentRolle = app.getCurrentRole();
			if (currentRolle != null && 
					currentRolle.getTitle().equals(rolleNames[i])) {
				// skip current rolle
				continue;
			}
			model.addElement(rolleNames[i]);
		}
		getRoles().fill(model);
	}
	
	public boolean showLogin(boolean exitAppOnCancel) {
		// set flags
		this.isCancel = false;
		this.isLogin = true;
		this.exitAppOnCancel = exitAppOnCancel;
		// show dialog
		super.setVisible(true);
		// finished
		return isCancel;
	}
	
	public boolean showSwapTo() {
		// set flags
		this.isCancel = false;
		this.isLogin = false;
		this.exitAppOnCancel = false;
		// show dialog
		super.setVisible(true);
		// finished
		return isCancel;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
