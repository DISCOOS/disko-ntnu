package org.redcross.sar.gui.dialog;

import java.awt.Frame;

import javax.swing.DefaultComboBoxModel;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.disco.io.CommPortIdentifier;
import org.disco.io.IOManager;
import org.disco.io.NoSuchPortException;
import org.disco.io.PortInUseException;
import org.disco.io.UnsupportedCommOperationException;
import org.disco.io.serial.SerialLink;
import org.disco.io.serial.TNCSession;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.ComboBoxField;
import org.redcross.sar.gui.field.IDiskoField;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.TooManyListenersException;
   
public class TNCDialog extends DefaultDialog  {

	private static final long serialVersionUID = 1L;
	private static final String[] fieldNames = new String[] {
												"protocol",
												"port",
												"tnc",
												"baudRate",
												"dataBits",
												"stopBits",
												"parity",
												"flowCtrl",
												"hostMode"};

	private FieldsPanel sessionPanel;
	
	private ComboBoxField protocolField;
	private ComboBoxField portField;
	private ComboBoxField tncField;	
	private ComboBoxField baudRateField;
	private ComboBoxField dataBitsField;
	private ComboBoxField stopBitsField;
	private ComboBoxField parityField;
	private ComboBoxField flowCtrlField;
	private ComboBoxField hostModeField;
	
	private JButton consoleButton;
	private JButton connectButton;
	private JButton disconnectButton;
	
	private ConsoleDialog consoleDialog;
	
	private boolean isCancel;
	private TNCSession session;
	
	/**
	 * Only constructor
	 * 
	 * @return void
	 */
	public TNCDialog(Frame owner) {
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
		this.setSize(350, 315);
		this.setResizable(false);
		this.setContentPane(getSessionPanel());
		refresh();
	}

	/**
	 * This method initializes sessionPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private FieldsPanel getSessionPanel() {
		if (sessionPanel == null) {
			sessionPanel = new FieldsPanel("Tilkobling");
			sessionPanel.setScrollBarPolicies(
					DefaultPanel.VERTICAL_SCROLLBAR_NEVER,
					DefaultPanel.HORIZONTAL_SCROLLBAR_NEVER);
			sessionPanel.setCaptionWidth(100);
			sessionPanel.addField(getProtocolField());
			sessionPanel.addField(getPortField());
			sessionPanel.addField(getTNCField());
			sessionPanel.addField(getBaudRateField());
			sessionPanel.addField(getDataBitsField());
			sessionPanel.addField(getStopBitsField());
			sessionPanel.addField(getParityField());
			sessionPanel.addField(getFlowCtrlField());
			sessionPanel.addField(getHostModeField());
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
						disconnect();
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
			protocolField.fill(new String[]{"APRS Sanntidssporing"});
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
			portField = new ComboBoxField("port","Seriell port",true);
			portField.fill(new String[]{"Ingen funnet"});
		}
		return portField;
	}	

	/**
	 * This method initializes tncField	
	 * 	
	 * @return org.redcross.sar.gui.field.ComboBoxField	
	 */
	private ComboBoxField getTNCField() {
		if (tncField == null) {
			tncField = new ComboBoxField("tnc","Protokoll",true);
			tncField.fill(new String[]{"APRS Sanntidssporing"});
		}
		return tncField;
	}	
	
	/**
	 * This method initializes bitRateField	
	 * 	
	 * @return org.redcross.sar.gui.field.ComboBoxField	
	 */
	private ComboBoxField getBaudRateField() {
		if (baudRateField == null) {
			baudRateField = new ComboBoxField("baudRate","Datahastighet",true);			
			baudRateField.fill(new String[]{
					"110","300","2400","2400",
					"4800","9600","19200","38400","57600",
					"115200","230400","460800","921600"});
		}
		return baudRateField;
	}	
	
	/**
	 * This method initializes dataBitsField	
	 * 	
	 * @return org.redcross.sar.gui.field.ComboBoxField	
	 */
	private ComboBoxField getDataBitsField() {
		if (dataBitsField == null) {
			dataBitsField = new ComboBoxField("dataBits","Data bits",true);			
			dataBitsField.fill(new String[]{"5","6","7","8"});
		}
		return dataBitsField;
	}	
	
	/**
	 * This method initializes stopBitsField	
	 * 	
	 * @return org.redcross.sar.gui.field.ComboBoxField	
	 */
	private ComboBoxField getStopBitsField() {
		if (stopBitsField == null) {
			stopBitsField = new ComboBoxField("stopBits","Stopp bits",true);			
			stopBitsField.fill(new String[]{"Ingen","Like","Odde","Merke","Mellomrom"});
		}
		return stopBitsField;
	}	

	/**
	 * This method initializes parityField	
	 * 	
	 * @return org.redcross.sar.gui.field.ComboBoxField	
	 */
	private ComboBoxField getParityField() {
		if (parityField == null) {
			parityField = new ComboBoxField("parity","Paritet",true);			
			parityField.fill(new String[]{"1","1.5","2"});
		}
		return parityField;
	}	
	
	/**
	 * This method initializes flowCtrlField	
	 * 	
	 * @return org.redcross.sar.gui.field.ComboBoxField	
	 */
	private ComboBoxField getFlowCtrlField() {
		if (flowCtrlField == null) {
			flowCtrlField = new ComboBoxField("flowCtrl","Flytkontroll",true);			
			flowCtrlField.fill(new String[]{"Ingen","XON/XOFF","Maskinvare"});
		}
		return flowCtrlField;
	}		
	
	/**
	 * This method initializes hostModeField	
	 * 	
	 * @return org.redcross.sar.gui.field.ComboBoxField	
	 */
	private ComboBoxField getHostModeField() {
		if (hostModeField == null) {
			hostModeField = new ComboBoxField("hostMode","TNC modus",true);			
			hostModeField.fill(new String[]{"Ingen","KISS"});
		}
		return hostModeField;
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
					ConsoleDialog dlg = new ConsoleDialog(Utils.getApp().getFrame());
					dlg.setLocationRelativeTo(Utils.getApp().getFrame());
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
			connectButton = DiskoButtonFactory.createButton("GENERAL.PLAY",ButtonSize.SMALL);
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
			disconnectButton = DiskoButtonFactory.createButton("GENERAL.STOP",ButtonSize.SMALL);
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
			consoleDialog = new ConsoleDialog(Utils.getApp().getFrame());
			consoleDialog.setLocationRelativeTo(Utils.getApp().getFrame());
		}
		return consoleDialog;
	}	
	
	/* =======================================================
	 * Helper functions
	 * ======================================================= */
	
	private void refresh() {

		// get ports
		DefaultComboBoxModel model = new DefaultComboBoxModel();                
        try {
			for(CommPortIdentifier it : IOManager.getSerialPortIdentifiers(true)) {
				String name = it.getName();
				model.addElement(name);      
			}
		} catch (NoSuchPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        getPortField().fill(model);
        
        // load session?
        if(session!=null) {
        	Setup setup = new Setup(session);
        	setup.setValues(getSessionPanel().getFields(fieldNames));
        }
        
        // update caption
        setTitle();
        
	}
	
	private void setTitle() {
        getSessionPanel().setCaptionText("Tjeneste (" + (session!=null && session.isConnected() ? "tilkoblet)" : "frakoblet)"));
		
	}

	private void console() {
		
	}
	
	private void connect() {
		
		// set solution tip
		String tip = "Sjekk applikasjonsloggen (uventet feil)";

		try {

			// disconnect current
			session.disconnect();
			
			// get setup parameters
			Setup setup = getSetup();
			
			if(setup.port.isEmpty() || setup.port.length()==0) {
				Utils.showMessage("Du må først velge en COM port");  
			}
			else {
					
				// get information about port
				CommPortIdentifier identifier = IOManager.getPortIdentifier(setup.port);;
				// check is available
				if (identifier.isCurrentlyOwned()) {  
					Utils.showMessage(setup.port + " er i bruk (" + identifier.getCurrentOwner() + ")");
				} else {
					
					// try to connect session to TNC device
					session.connect(setup.tnc, setup.port, 
							setup.baudRate, setup.dataBits, 
							setup.stopBits, setup.parity, 
							setup.flowCtrl, setup.hostMode, false); 
					
					// success
					return;
						
				}
			}
		} catch (NumberFormatException e) {			
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// set solution tip
			tip = "Sjekk at utstyr er på og kablet til";
		} catch (TooManyListenersException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// set solution tip
			tip = "Sjekk applikasjonsloggen (uventet feil)";
		} catch (NoSuchPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// set solution tip
			tip = "Oppgitt port eksiserer ikke (hardware feil)";
		} catch (PortInUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// set solution tip
			tip = "Oppgitt port er allerede i bruk (bytt port)";
		} catch (UnsupportedCommOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		// notify
		Utils.showError("Oppsett av kommunikasjon feilet. " + tip);  

		// connection process failed
		session = null;
			
	}
	
	private void disconnect() {

		// forward
		session.disconnect(true);
				
	}
	
	/*
	private void setup() {
		
		try {
			// initialize tnc changed flag
			boolean isChanged = false;
			
			// get current link
			TNCLink link = session.getLink();
			
			// get current setup
			Setup setup = new Setup(getSessionPanel().getFields(fieldNames));
			
			// set TNC flag?
			if(session.isConnected()) {
				// is TNC type or hostmode is changed, an init is required
				isChanged = setup.hostMode!= link.getHostMode() || setup.tnc != link.getType();
			}
			// update link
			session.setType(setup.tnc);
			session.setHostMode(setup.hostMode);
			link.setSerialPortParams(setup.baudRate, setup.dataBits, setup.stopBits, setup.flowCtrl);
			// initialize TNC?
			if(isChanged) session.initTNC();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	*/

	
	public boolean manageSession(TNCSession session) {
		// prepare
		this.isCancel = false;
		this.session = session;
		// load session
		refresh();
		// comboboxes are not editable
		getSessionPanel().setEditable(false);
		// dialog is modal, the call it blocks on this 
		setVisible(true);
		// finished
		return !isCancel;
	}
	
	private Setup getSetup() {
		return new Setup(getSessionPanel().getFields(fieldNames));
	}
	
	private static class Setup {
		
		public String protocol;
		public String port;
		public String tnc;
		public int baudRate;
		public int dataBits;
		public int stopBits;
		public int parity;
		public int flowCtrl;
		public String hostMode;
		
		public Setup(String protocol, String port, String tnc, String hostMode) {
			this.protocol = protocol;
			this.port = port;
			this.tnc = tnc;
			baudRate = 9600; 
			dataBits = SerialLink.DATABITS_8; 
			stopBits = SerialLink.STOPBITS_1;
			parity = SerialLink.PARITY_NONE; 
			flowCtrl = SerialLink.FLOWCONTROL_XONXOFF_IN + SerialLink.FLOWCONTROL_XONXOFF_OUT;
			this.hostMode = hostMode;			
		}
		
		public Setup(List<IDiskoField> fields) {
			protocol = getProtocol(fields.get(0).getValue());
			port = getPort(fields.get(1).getValue());
			tnc = getTNC(fields.get(2).getValue());
			baudRate = getBaudRate(fields.get(3).getValue().toString()); 
			dataBits = getDataBits(fields.get(4).getValue().toString()); 
			stopBits = getStopBits(fields.get(5).getValue().toString());
			parity = getParity(fields.get(6).getValue().toString()); 
			flowCtrl = getFlowCtrl(fields.get(7).getValue().toString());
			hostMode = getHostMode(fields.get(8).getValue().toString());			
		}
		
		public Setup(TNCSession session) {
			if(session.isConnected()) {
				protocol = session.getProtocol().getName();
				port = session.getLink().getName().replace("//./", "");
				tnc = session.getType();
				baudRate = session.getLink().getBaudRate(); 
				dataBits = session.getLink().getDataBits(); 
				stopBits = session.getLink().getStopBits();
				parity = session.getLink().getParity(); 
				flowCtrl = session.getLink().getFlowControlMode();
				hostMode = session.getLink().getHostMode();
			}
			else {
				Setup setup = new Setup("APRS Sanntidssporing", "COM1", "TNC", "Ingen");
				protocol = setup.protocol;
				port = setup.port;
				tnc = setup.tnc;
				baudRate = setup.baudRate; 
				dataBits = setup.dataBits; 
				stopBits = setup.stopBits;
				parity = setup.parity; 
				flowCtrl = setup.flowCtrl;
				hostMode = setup.hostMode;
								
			}
		}
				
		public void setValues(List<IDiskoField> fields) {
			fields.get(0).setValue(protocol);
			fields.get(1).setValue(port);
			fields.get(2).setValue(tnc);
			fields.get(3).setValue(getBaudRate(baudRate)); 
			fields.get(4).setValue(getDataBits(dataBits)); 
			fields.get(5).setValue(getStopBits(stopBits));
			fields.get(6).setValue(getParity(parity)); 
			fields.get(7).setValue(getFlowCtrl(flowCtrl));
			fields.get(8).setValue(hostMode);			
		}
		
		private String getProtocol(Object protocol) {
			return protocol.toString();
		}
		
		private String getPort(Object port) {
			return port.toString();
		}
		
		private String getTNC(Object tnc) {
			return tnc.toString();
		}
		
		private int getBaudRate(String baudRate) {
			return Integer.valueOf(baudRate).intValue();
		}				
		
		private String getBaudRate(int baudRate) {
			return String.valueOf(baudRate);
		}				
		
		private int getDataBits(String dataBits) {
			if("5".equalsIgnoreCase(dataBits))
				return SerialLink.DATABITS_5;
			else if("6".equalsIgnoreCase(dataBits))
				return SerialLink.DATABITS_6;
			else if("7".equalsIgnoreCase(dataBits))
				return SerialLink.DATABITS_7;
			else if("8".equalsIgnoreCase(dataBits))
				return SerialLink.DATABITS_8;
			return SerialLink.DATABITS_8;
		}			
		
		private String getDataBits(int dataBits) {
			if(SerialLink.STOPBITS_1 == dataBits)
				return "5";				
			else if(SerialLink.STOPBITS_1_5 == dataBits)
				return "6";							
			else if(SerialLink.STOPBITS_1_5 == dataBits)
				return "7";							
			else if(SerialLink.STOPBITS_1_5 == dataBits)
				return "8";						
			return "8";
		}		
		
		private int getStopBits(String stopBits) {
			if("1".equalsIgnoreCase(stopBits))
				return SerialLink.STOPBITS_1;
			else if("1.5".equalsIgnoreCase(stopBits))
				return SerialLink.STOPBITS_1_5;
			else if("2".equalsIgnoreCase(stopBits))
				return SerialLink.STOPBITS_2;
			return SerialLink.STOPBITS_1;
		}
		
		private String getStopBits(int stopBits) {
			if(SerialLink.STOPBITS_1 == stopBits)
				return "1";				
			else if(SerialLink.STOPBITS_1_5 == stopBits)
				return "1.5";							
			else if(SerialLink.STOPBITS_2 == stopBits)
				return "2";							
			return "1";
		}					
		
		private int getParity(String parity) {
			if("Ingen".equalsIgnoreCase(parity))
				return SerialLink.PARITY_NONE;
			else if("Like".equalsIgnoreCase(parity))
				return SerialLink.PARITY_EVEN;
			else if("Odde".equalsIgnoreCase(parity))
				return SerialLink.PARITY_ODD;
			else if("Merke".equalsIgnoreCase(parity))
				return SerialLink.PARITY_MARK;
			else if("Mellomrom".equalsIgnoreCase(parity))
				return SerialLink.PARITY_SPACE;
			return SerialLink.PARITY_NONE;
		}
		
		private String getParity(int parity) {
			if(SerialLink.PARITY_NONE == parity)
				return "Ingen";
			else if(SerialLink.PARITY_EVEN == parity)
				return "Like";
			else if(SerialLink.PARITY_ODD == parity)
				return "Odde";
			else if(SerialLink.PARITY_MARK == parity)
				return "Merke";
			else if(SerialLink.PARITY_SPACE == parity)
				return "Mellomrom";
			return "Ingen";
		}		
		
		private int getFlowCtrl(String flowCtrl) {
			if("Ingen".equalsIgnoreCase(flowCtrl))
				return SerialLink.FLOWCONTROL_NONE;
			else if("XON/XOFF".equalsIgnoreCase(flowCtrl))
				return SerialLink.FLOWCONTROL_RTSCTS_IN + SerialLink.FLOWCONTROL_RTSCTS_OUT;
			else if("Maskinvare".equalsIgnoreCase(flowCtrl))
				return SerialLink.FLOWCONTROL_XONXOFF_IN + SerialLink.FLOWCONTROL_XONXOFF_OUT;
			return SerialLink.FLOWCONTROL_NONE;
		}	
		
		private String getFlowCtrl(int flowCtrl) {
			if(SerialLink.FLOWCONTROL_NONE == flowCtrl)
				return "Ingen";
			else if(SerialLink.FLOWCONTROL_RTSCTS_IN + SerialLink.FLOWCONTROL_RTSCTS_OUT == flowCtrl)
				return "XON/XOFF";
			else if(SerialLink.FLOWCONTROL_XONXOFF_IN + SerialLink.FLOWCONTROL_XONXOFF_OUT == flowCtrl)
				return "Maskinvare";
			return "Ukjent";
		}	
		
		private String getHostMode(Object hostMode) {
			return hostMode.toString();
		}
		
		
	}



}  //  @jve:decl-index=0:visual-constraint="11,0"

