package org.redcross.sar.gui.dialog;

import java.awt.Frame;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.disco.io.net.NetSession;
import org.redcross.sar.Application;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.field.ComboBoxField;
import org.redcross.sar.gui.field.IField;
import org.redcross.sar.gui.field.TextField;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.FieldPane;
import org.redcross.sar.util.Utils;

import java.io.IOException;
import java.util.List;
   
public class NetDialog extends DefaultDialog  {

	private static final long serialVersionUID = 1L;
	private static final String[] fieldNames = new String[] {
												"protocol",
												"host",
												"port",
												"username",
												"password"};

	private FieldPane sessionPanel;
	
	private ComboBoxField protocolField;
	private ComboBoxField hostField;	
	private ComboBoxField portField;
	private TextField userNameField;
	private TextField passwordField;
	
	private JButton consoleButton;
	private JButton connectButton;
	private JButton disconnectButton;
	
	private ConsoleDialog consoleDialog;
	
	private boolean isCancel;
	private NetSession session;
	
	/**
	 * Only constructor
	 * 
	 * @return void
	 */
	public NetDialog(Frame owner) {
		// forward
		super(owner);
		// initialize GUI
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(350, 200);
		this.setResizable(false);
		this.setContentPane(getSessionPanel());
		refresh();
	}

	/**
	 * This method initializes sessionPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private FieldPane getSessionPanel() {
		if (sessionPanel == null) {
			sessionPanel = new FieldPane("Tilkobling");
			sessionPanel.setScrollBarPolicies(
					DefaultPanel.VERTICAL_SCROLLBAR_NEVER,
					DefaultPanel.HORIZONTAL_SCROLLBAR_NEVER);
			sessionPanel.setCaptionWidth(100);
			sessionPanel.addField(getProtocolField());
			sessionPanel.addField(getHostField());
			sessionPanel.addField(getPortField());
			sessionPanel.addField(getUserNameField());
			sessionPanel.addField(getPasswordField());
			sessionPanel.insertButton("finish", getConsoleButton(), "console");
			sessionPanel.insertButton("finish", getDisconnectButton(), "disconnect");
			sessionPanel.insertButton("finish", getConnectButton(), "connect");
			
			// add action listener
			sessionPanel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if("console".equalsIgnoreCase(cmd)) {
						console();
					}
					else if("connect".equalsIgnoreCase(cmd)) {
						connect();
					}
					else if("disconnect".equalsIgnoreCase(cmd)) {
						close();
					}
					else if("cancel".equalsIgnoreCase(cmd)) {
						// set flag
						isCancel = true;
					}
					else if("finish".equalsIgnoreCase(cmd)) {
						// forward
						connect();
					}
				}

			});
		}
		return sessionPanel;
	}
	
	/**
	 * This method initializes protocolField	
	 * 	
	 * @return org.redcross.sar.gui.field.ComboBoxField	
	 */
	private ComboBoxField getProtocolField() {
		if (protocolField == null) {
			protocolField = new ComboBoxField("protocol","Protokoll",true);
			protocolField.fill(new String[]{"APRS Sanntidssporing (APRS-IS)"});
		}
		return protocolField;
	}	
	
	/**
	 * This method initializes portField	
	 * 	
	 * @return org.redcross.sar.gui.field.ComboBoxField	
	 */
	private ComboBoxField getPortField() {
		if (portField == null) {
			portField = new ComboBoxField("port","Port",true);
			portField.fill(new String[]{"14579"});
		}
		return portField;
	}	

	/**
	 * This method initializes hostField	
	 * 	
	 * @return org.redcross.sar.gui.field.ComboBoxField	
	 */
	private ComboBoxField getHostField() {
		if (hostField == null) {
			hostField = new ComboBoxField("host","Server",true);
			hostField.fill(new String[]{"ahubswe.net"});
		}
		return hostField;
	}	
	
	/**
	 * This method initializes userNameField	
	 * 	
	 * @return org.redcross.sar.gui.field.TextLineField
	 */
	private TextField getUserNameField() {
		if (userNameField == null) {
			userNameField = new TextField("username","Brukernavn",true);			
			userNameField.setValue("DISKO");
		}
		return userNameField;
	}		
	
	/**
	 * This method initializes hostModeField	
	 * 	
	 * @return org.redcross.sar.gui.field.TextLineField	
	 */
	private TextField getPasswordField() {
		if (passwordField == null) {
			passwordField = new TextField("password","Passord",true);			
			passwordField.setValue("-");
		}
		return passwordField;
	}		

	/**
	 * This method initializes consoleButton	
	 * 	
	 * @return javax.swing.JButton
	 */	
	private JButton getConsoleButton() {
		if(consoleButton == null) {
			consoleButton = DiskoButtonFactory.createButton("GENERAL.CONSOLE",ButtonSize.SMALL);
			consoleButton.setToolTipText("Åpne konsoll for å sende kommandoer direkte til TNC");
			consoleButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					ConsoleDialog dlg = new ConsoleDialog(Application.getFrameInstance());
					dlg.setLocationRelativeTo(Application.getFrameInstance());
					getConsoleDialog().open(session);					
				}
				
			});
		}
		return consoleButton;
	}
	
	/**
	 * This method initializes connectButton	
	 * 	
	 * @return javax.swing.JButton
	 */	
	private JButton getConnectButton() {
		if(connectButton == null) {
			connectButton = DiskoButtonFactory.createButton("GENERAL.CONNECT",ButtonSize.SMALL);
			connectButton.setToolTipText("Start tjeneste");
		}
		return connectButton;
	}
	
	/**
	 * This method initializes disconnectButton	
	 * 	
	 * @return javax.swing.JButton
	 */	
	private JButton getDisconnectButton() {
		if(disconnectButton == null) {
			disconnectButton = DiskoButtonFactory.createButton("GENERAL.DISCONNECT",ButtonSize.SMALL);
			disconnectButton.setToolTipText("Stopp tjeneste");
		}
		return disconnectButton;
	}	
	
	/**
	 * This method initializes consoleDialog	
	 * 	
	 * @return org.redcross.sar.gui.dialog.ConsoleDialog
	 */	
	private ConsoleDialog getConsoleDialog() {
		if(consoleDialog == null) {
			consoleDialog = new ConsoleDialog(Application.getFrameInstance());
			consoleDialog.setLocationRelativeTo(Application.getFrameInstance());
		}
		return consoleDialog;
	}	
	
	/* =======================================================
	 * Helper functions
	 * ======================================================= */
	
	private void refresh() {
        
        // load session?
        if(session!=null) {
        	Setup setup = new Setup(session);
        	setup.setValues(getSessionPanel().getFields(fieldNames));
        }
        
        // update caption
        setTitle();
        
	}
	
	private void setTitle() {
        getSessionPanel().setCaptionText("Tjeneste (" + (session!=null && session.isOpen() ? "tilkoblet)" : "frakoblet)"));
		
	}

	private void console() {
		
	}
	
	private void connect() {
		
		// set solution tip
		String tip = "Sjekk applikasjonsloggen (uventet feil)";

		try {

			// disconnect current
			session.close();
			
			// get setup parameters
			Setup setup = getSetup();
			
			if(setup.host.isEmpty() || setup.host.length()==0) {
				Utils.showMessage("Du må først velge en server");  
			}
			else if(setup.port<=0) {
				Utils.showMessage("Du må først velge en port");  
			}
			else {
					
				// try to connect session to TNC device
				session.open(setup.host, setup.port); 
					
				// success
				return;
			}
		} catch (NumberFormatException e) {			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// set solution tip
			tip = "Sjekk at utstyr er på og kablet til";
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		// notify
		Utils.showError("Oppsett av kommunikasjon feilet. " + tip);  

		// connection process failed
		session = null;
			
	}
	
	private void close() {

		// forward
		try {
			session.close();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	public boolean manage(NetSession session) {
		// prepare
		this.isCancel = false;
		this.session = session;
		// load session
		refresh();
		// comboboxes are not editable
		getSessionPanel().setEditable(false);
		// dialog is modal, the call blocks on this 
		setVisible(true);
		// finished
		return !isCancel;
	}
	
	private Setup getSetup() {
		return new Setup(getSessionPanel().getFields(fieldNames));
	}
	
	private static class Setup {
		
		public String protocol;
		public String host;
		public int port;
		public String username;
		public String password;
		
		public Setup(String protocol, String host, int port, String username, String password) {
			this.protocol = protocol;
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
		}
		
		public Setup(List<IField<?>> fields) {
			protocol = getProtocol(fields.get(0).getValue());
			host = getHost(fields.get(1).getValue());
			port = getPort(fields.get(2).getValue());
			username = getUserName(fields.get(3).getValue());
			password = getPassWord(fields.get(4).getValue());
		}
		
		public Setup(NetSession session) {
			if(session.isOpen()) {
				protocol = session.getProtocol().getName();
				String[] url = session.getLink().getName().split(":");
				host = url[0];
				port = getPort(url[1]);
				username = session.getUsername();
				password = session.getPassword(); 
			}
			else {
				Setup setup = new Setup("APRS Sanntidssporing (APRS-IS)", 
						"ahubswe.net", 14579, "DISKO", "-1");
				protocol = setup.protocol;
				host = setup.host;
				port = setup.port;
				username = setup.username; 
				password = setup.password; 								
			}
		}
				
		public void setValues(List<IField<?>> fields) {
			fields.get(0).setValue(protocol);
			fields.get(1).setValue(host);
			fields.get(2).setValue(port);
			fields.get(3).setValue(username); 
			fields.get(4).setValue(password); 
		}
		
		private String getProtocol(Object protocol) {
			return protocol.toString();
		}
		
		private String getHost(Object port) {
			return port.toString();
		}
		
		private Integer getPort(Object port) {
			return Integer.valueOf(port.toString());
		}
		
		private String getUserName(Object port) {
			return port.toString();
		}

		private String getPassWord(Object port) {
			return port.toString();
		}
		
	}



}  //  @jve:decl-index=0:visual-constraint="11,0"

