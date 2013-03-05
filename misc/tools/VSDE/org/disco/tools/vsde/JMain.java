package org.disco.tools.vsde;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JComboBox;

import org.disco.io.CommPortIdentifier;
import org.disco.io.IBroker;
import org.disco.io.IOManager;
import org.disco.io.IParser;
import org.disco.io.ISession;
import org.disco.io.NoSuchPortException;
import org.disco.io.PortInUseException;
import org.disco.io.TokenParser;
import org.disco.io.UnsupportedCommOperationException;
import org.disco.io.event.EntityEvent;
import org.disco.io.event.IManagerListener;
import org.disco.io.event.ProtocolEvent;
import org.disco.io.event.SessionEvent;
import org.disco.io.serial.SerialLink;
import org.disco.io.serial.SerialSession;
import org.disco.io.serial.TNCSession;

import java.util.TooManyListenersException;

public class JMain extends JFrame implements Log {

	private static final long serialVersionUID = 1L;
	private static final int BW = 100;
	private static final int BH = 30;

	private JList replyList = null;
	private JList eventList = null;
	
	private JScrollPane replyListScrollPane = null;
	private JScrollPane eventListScrollPane = null;

	private JPanel contentPane = null;
	private JPanel statusPanel = null;
	private JPanel portPanel = null;
	private JPanel replyPanel = null;
	private JPanel replyControlPanel = null;
	private JPanel outputPanel = null;
	private JPanel optionsPanel = null;
	private JPanel controlPanel = null;
	
	private JButton connectButton = null;
	private JButton diconnectButton = null;
	private JButton refreshButton = null;
	private JButton consoleButton = null;
	private JButton sendButton = null;
	private JButton addReplyButton = null;
	private JButton removeReplyButton = null;	
	
	private JLabel messageLabel = null;
	private JLabel protocolLabel = null;
	private JLabel modeLabel = null;
	private JLabel deviceLabel = null;
	private JLabel portLabel = null;
	private JLabel baudRateLabel = null;
	private JLabel parityLabel = null;
	private JLabel stopBitsLabel = null;
	private JLabel flowControlLabel = null;
	private JLabel dataBitsLabel = null;
	
	private JTextField sendTextField = null;
	
	private JComboBox protocolComboBox = null;
	private JComboBox modeComboBox = null;
	private JComboBox deviceComboBox = null;
	private JComboBox portComboBox = null;
	private JComboBox baudRateComboBox = null;
	private JComboBox dataBitsComboBox = null;
	private JComboBox parityComboBox = null;
	private JComboBox stopBitsComboBox = null;
	private JComboBox flowControlComboBox = null;
	
	private JConsole consoleDialog;

	private IOManager io;
	private IParser anyParser;	
	private IParser tncParser;
	private SerialSession anySession;
	private TNCSession tncSession;
	
	private final JComboBox[] fields;
	private final EventLogger listener = new EventLogger();
	
	public JMain() throws HeadlessException {

		// forward
		super();
		
		// initialize GUI
		initialize();
		
		// create array of comboboxes
		fields = new JComboBox[]{protocolComboBox,
				portComboBox, deviceComboBox,
				baudRateComboBox, dataBitsComboBox, parityComboBox,
				stopBitsComboBox,flowControlComboBox, modeComboBox};
		
		// parse buffer in lines using the /n flag
		io = IOManager.getInstance();
		anyParser = new TokenParser("LineParser","","\r","\n");
		anySession = new SerialSession("SerialSession",anyParser,"-");
		
		// parse buffer in lines using the /n flag
		tncParser = new TokenParser("LineParser","","\r","\n");
		tncSession = new TNCSession("TNCSession",tncParser,"-","/config");
		
		// add sessions to manager
		io.addSession(anySession);
		io.addSession(tncSession);
		
		// manager listener
		io.addManagerListener(listener);
		
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(500, 400);
		this.setResizable(false);
		//this.setMinimumSize(new Dimension(500, 400));
		this.setContentPane(getContentPanel());
		this.setTitle("Virtual Serial Device Emulator - <no connection>");
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);				
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent arg0) {
				// cleanup
				io.closeAll();
				dispose();
			}

		});
		this.pack();
		refresh();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getContentPanel() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
			contentPane.add(getStatusPanel());
			contentPane.add(Box.createVerticalStrut(5));
			contentPane.add(getPortPanel());
			contentPane.add(Box.createVerticalStrut(5));
			contentPane.add(getOutputPanel());
			contentPane.add(Box.createVerticalStrut(5));
			contentPane.add(getReplyPanel());
			contentPane.add(Box.createVerticalStrut(5));
			contentPane.add(getEventListScrollPane());
			contentPane.add(Box.createVerticalGlue());
		}
		return contentPane;
	}

	/**
	 * This method initializes eventList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getEventList() {
		if (eventList == null) {
			eventList = new JList();
			eventList.setModel(new DefaultListModel());
		}
		return eventList;
	}
	
	/**
	 * This method initializes replayPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getReplyPanel() {
		if (replyPanel == null) {
			replyPanel = new JPanel();
			replyPanel.setLayout(new BorderLayout(5, 0));
			replyPanel.add(getReplyListScrollPane(),BorderLayout.CENTER);
			replyPanel.add(getReplyControlPanel(),BorderLayout.EAST);
		}
		return replyPanel;
	}
	
	/**
	 * This method initializes replayControlPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getReplyControlPanel() {
		if (replyControlPanel == null) {
			replyControlPanel = new JPanel();
			replyControlPanel.setLayout(new BoxLayout(replyControlPanel, BoxLayout.Y_AXIS));
			replyControlPanel.add(getAddReplyButton());
			replyControlPanel.add(getRemoveReplyButton());
		}
		return replyControlPanel;
	}	
	
	/**
	 * This method initializes addReplyButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddReplyButton() {
		if (addReplyButton == null) {
			addReplyButton = new JButton();
			addReplyButton.setText("Add...");
			addReplyButton.setMinimumSize(new Dimension(BW,BH));			
			addReplyButton.setPreferredSize(new Dimension(BW,BH));
			addReplyButton.setMaximumSize(new Dimension(BW,BH));			
			
			addReplyButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					add();					
				}
				
			});					
		}
		return addReplyButton;
	}		
	
	/**
	 * This method initializes removeReplyButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRemoveReplyButton() {
		if (removeReplyButton == null) {
			removeReplyButton = new JButton();
			removeReplyButton.setText("Remove");
			removeReplyButton.setMinimumSize(new Dimension(BW,BH));			
			removeReplyButton.setPreferredSize(new Dimension(BW,BH));
			removeReplyButton.setMaximumSize(new Dimension(BW,BH));			
			
			removeReplyButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					remove();					
				}
				
			});					
		}
		return removeReplyButton;
	}		
	
	/**
	 * This method initializes autoReplyListScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getReplyListScrollPane() {
		if (replyListScrollPane == null) {
			replyListScrollPane = new JScrollPane(getReplyList());
			replyListScrollPane.setPreferredSize(new Dimension(390,100));
		}
		return replyListScrollPane;
	}
	
	/**
	 * This method initializes replayList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getReplyList() {
		if (replyList == null) {
			replyList = new JList();
			replyList.setModel(new DefaultListModel());
		}
		return replyList;
	}
	
	
	/**
	 * This method initializes eventListScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getEventListScrollPane() {
		if (eventListScrollPane == null) {
			eventListScrollPane = new JScrollPane(getEventList());
			eventListScrollPane.setPreferredSize(new Dimension(390,100));
		}
		return eventListScrollPane;
	}

	/**
	 * This method initializes statusPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getStatusPanel() {
		if (statusPanel == null) {
			statusPanel = new JPanel();
			statusPanel.setLayout(new BorderLayout());
			messageLabel = new JLabel();
			messageLabel.setText("Status: Uninitialized (execute refresh)");
			messageLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			messageLabel.setBorder(BorderFactory.createLineBorder(Color.RED));
			statusPanel.add(messageLabel,BorderLayout.CENTER);
			statusPanel.setVisible(false);
		}
		return statusPanel;
	}	
	
	/**
	 * This method initializes outputPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOutputPanel() {
		if (outputPanel == null) {
			outputPanel = new JPanel();
			outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.X_AXIS));
			outputPanel.add(getSendTextField());
			outputPanel.add(Box.createHorizontalStrut(5));
			outputPanel.add(getSendButton());
		}
		return outputPanel;
	}	
	
	/**
	 * This method initializes portPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPortPanel() {
		if (portPanel == null) {
			portPanel = new JPanel();
			portPanel.setLayout(new BorderLayout(5,0));
			portPanel.add(getOptionsPanel(),BorderLayout.CENTER);
			portPanel.add(getControlPanel(),BorderLayout.EAST);
		}
		return portPanel;
	}		
	
	/**
	 * This method initializes optionsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOptionsPanel() {
		if (optionsPanel == null) {
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			gridBagConstraints61.gridx = 0;
			gridBagConstraints61.gridy = 2;
			gridBagConstraints61.ipadx = 5;
			gridBagConstraints61.anchor = GridBagConstraints.WEST;		
			modeLabel = new JLabel();
			modeLabel.setText("Host mode:");
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints51.gridy = 2;
			gridBagConstraints51.weightx = 1.0;
			gridBagConstraints51.gridx = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.ipadx = 5;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;		
			protocolLabel = new JLabel();
			protocolLabel.setText("Protocol:");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.gridx = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.ipadx = 5;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;		
			deviceLabel = new JLabel();
			deviceLabel.setText("Device:");
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints14.gridy = 1;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.gridx = 1;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.gridy = 5;
			gridBagConstraints13.ipadx = 5;
			gridBagConstraints13.anchor = GridBagConstraints.WEST;		
			dataBitsLabel = new JLabel();
			dataBitsLabel.setText("Data bits:");
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints12.gridy = 8;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.gridx = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints11.gridy = 7;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.gridx = 1;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints10.gridy = 6;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.gridx = 1;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints9.gridy = 5;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints.gridy = 4;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.gridx = 1;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints8.gridy = 3;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.gridx = 1;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 8;
			gridBagConstraints7.ipadx = 5;
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			flowControlLabel = new JLabel();
			flowControlLabel.setText("Flow control:");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 7;
			gridBagConstraints6.ipadx = 5;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;		
			stopBitsLabel = new JLabel();
			stopBitsLabel.setText("Stop bits:");
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 6;
			gridBagConstraints5.ipadx = 5;
			gridBagConstraints5.anchor = GridBagConstraints.WEST;		
			parityLabel = new JLabel();
			parityLabel.setText("Parity:");
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.gridy = 4;
			gridBagConstraints21.ipadx = 5;
			gridBagConstraints21.anchor = GridBagConstraints.WEST;		
			baudRateLabel = new JLabel();
			baudRateLabel.setText("Bits per second:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 3;
			gridBagConstraints1.ipadx = 5;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;		
			portLabel = new JLabel();
			portLabel.setText("Port:");
			optionsPanel = new JPanel();
			optionsPanel.setLayout(new GridBagLayout());
			optionsPanel.add(portLabel, gridBagConstraints1);
			optionsPanel.add(baudRateLabel, gridBagConstraints21);
			optionsPanel.add(dataBitsLabel, gridBagConstraints13);
			optionsPanel.add(parityLabel, gridBagConstraints5);
			optionsPanel.add(stopBitsLabel, gridBagConstraints6);
			optionsPanel.add(flowControlLabel, gridBagConstraints7);
			optionsPanel.add(getPortComboBox(), gridBagConstraints8);
			optionsPanel.add(getBaudRateComboBox(), gridBagConstraints);
			optionsPanel.add(getDataBitsComboBox(), gridBagConstraints9);
			optionsPanel.add(getParityComboBox(), gridBagConstraints10);
			optionsPanel.add(getStopBitsComboBox(), gridBagConstraints11);
			optionsPanel.add(getFlowControlComboBox(), gridBagConstraints12);
			optionsPanel.add(getDeviceComboBox(), gridBagConstraints14);
			optionsPanel.add(deviceLabel, gridBagConstraints2);
			optionsPanel.add(getProtocolComboBox(), gridBagConstraints3);
			optionsPanel.add(protocolLabel, gridBagConstraints4);
			optionsPanel.add(getModeComboBox(), gridBagConstraints51);
			optionsPanel.add(modeLabel, gridBagConstraints61);
		}
		return optionsPanel;
	}

	/**
	 * This method initializes controlPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getControlPanel() {
		if (controlPanel == null) {
			controlPanel = new JPanel();
			controlPanel.setLayout(new BoxLayout(getControlPanel(), BoxLayout.Y_AXIS));
			controlPanel.add(getRefreshButton());
			controlPanel.add(getConnetButton());
			controlPanel.add(getDisconnectButton());
			controlPanel.add(getConsoleButton(), null);
		}
		return controlPanel;
	}

	/**
	 * This method initializes startButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getConnetButton() {
		if (connectButton == null) {
			connectButton = new JButton();
			connectButton.setText("Connect");
			connectButton.setMinimumSize(new Dimension(BW,BH));			
			connectButton.setPreferredSize(new Dimension(BW,BH));
			connectButton.setMaximumSize(new Dimension(BW,BH));			
			
			connectButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					connect();					
				}
				
			});			
		}
		return connectButton;
	}

	/**
	 * This method initializes stopButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDisconnectButton() {
		if (diconnectButton == null) {
			diconnectButton = new JButton();
			diconnectButton.setText("Disconnect");
			diconnectButton.setMinimumSize(new Dimension(BW,BH));			
			diconnectButton.setPreferredSize(new Dimension(BW,BH));
			diconnectButton.setMaximumSize(new Dimension(BW,BH));			
			
			diconnectButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					close();					
				}
				
			});					
		}
		return diconnectButton;
	}

	/**
	 * This method initializes refreshButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRefreshButton() {
		if (refreshButton == null) {
			refreshButton = new JButton();
			refreshButton.setText("Refresh");
			refreshButton.setMinimumSize(new Dimension(BW,BH));			
			refreshButton.setPreferredSize(new Dimension(BW,BH));
			refreshButton.setMaximumSize(new Dimension(BW,BH));
			refreshButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					refresh();					
				}
				
			});
		}
		return refreshButton;
	}
	
	/**
	 * This method initializes sendButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSendButton() {
		if (sendButton == null) {
			sendButton = new JButton();
			sendButton.setText("Send");
			sendButton.setMinimumSize(new Dimension(BW,BH));			
			sendButton.setPreferredSize(new Dimension(BW,BH));
			sendButton.setMaximumSize(new Dimension(BW,BH));			
			
			sendButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					transmit();					
				}
				
			});					
		}
		return sendButton;
	}		
	
	/**
	 * This method initializes portComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getPortComboBox() {
		if (portComboBox == null) {
			portComboBox = new JComboBox();
			portComboBox.setPreferredSize(new Dimension(250,BH));
		}
		return portComboBox;
	}

	/**
	 * This method initializes baudRateComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getBaudRateComboBox() {
		if (baudRateComboBox == null) {
			baudRateComboBox = new JComboBox(new String[]{
					"110","300","2400","2400",
					"4800","9600","19200","38400","57600",
					"115200","230400","460800","921600"});
			baudRateComboBox.setPreferredSize(new Dimension(250,BH));
			baudRateComboBox.setSelectedItem("9600");
		}
		return baudRateComboBox;
	}

	/**
	 * This method initializes databitsComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDataBitsComboBox() {
		if (dataBitsComboBox == null) {
			dataBitsComboBox = new JComboBox(new String[]{"5","6","7","8"});
			dataBitsComboBox.setPreferredSize(new Dimension(250,BH));
			dataBitsComboBox.setSelectedItem("8");
		}
		return dataBitsComboBox;
	}

	/**
	 * This method initializes parityComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getParityComboBox() {
		if (parityComboBox == null) {
			parityComboBox = new JComboBox(new String[]{"None","Even","Odd","Mark","Space"});
			parityComboBox.setPreferredSize(new Dimension(250,BH));
		}
		return parityComboBox;
	}

	/**
	 * This method initializes stopBitsComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getStopBitsComboBox() {
		if (stopBitsComboBox == null) {
			stopBitsComboBox = new JComboBox(new String[]{"1","1.5","2"});
			stopBitsComboBox.setPreferredSize(new Dimension(250,BH));
		}
		return stopBitsComboBox;
	}

	/**
	 * This method initializes flowControlComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getFlowControlComboBox() {
		if (flowControlComboBox == null) {
			flowControlComboBox = new JComboBox(new String[]{"None","XON/XOFF","Hardware"});
			flowControlComboBox.setPreferredSize(new Dimension(250,BH));
			flowControlComboBox.setSelectedItem("Hardware");
		}
		return flowControlComboBox;
	}
	
	/**
	 * This method initializes sendTextBox	
	 * 	
	 * @return javax.swing.JTextField
	 */
	private JTextField getSendTextField() {
		if (sendTextField == null) {
			sendTextField = new JTextField();
			sendTextField.setPreferredSize(new Dimension(250,BH));
			sendTextField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyTyped(KeyEvent e) {
					// translate
					if(e.getID()==KeyEvent.VK_ENTER) {
						getSendButton().doClick();
					}
						
				}
				
			});
		}
		return sendTextField;
	}
	

	/**
	 * This method initializes deviceComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDeviceComboBox() {
		if (deviceComboBox == null) {
			deviceComboBox = new JComboBox(new String[]{"ANY","TNC"});
			deviceComboBox.setPreferredSize(new Dimension(250,BH));
			deviceComboBox.setSelectedItem("ANY");
			deviceComboBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if("ANY".equals(e.getItem())) 
						getModeComboBox().setEnabled(false);
					else
						getModeComboBox().setEnabled(true);				
				}
				
			});
		}
		return deviceComboBox;
	}

	/**
	 * This method initializes consoleButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getConsoleButton() {
		if (consoleButton == null) {
			consoleButton = new JButton();
			consoleButton.setText("Console...");
			consoleButton.setMinimumSize(new Dimension(BW,BH));			
			consoleButton.setPreferredSize(new Dimension(BW,BH));
			consoleButton.setMaximumSize(new Dimension(BW,BH));			
			
			consoleButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					console();
				}
				
			});							
		}
		return consoleButton;
	}	
	
	/**
	 * This method initializes protocolComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getProtocolComboBox() {
		if (protocolComboBox == null) {
			protocolComboBox = new JComboBox(new String[]{"ANY","APRS"});
			protocolComboBox.setPreferredSize(new Dimension(250,BH));
			protocolComboBox.setSelectedItem("ANY");
			protocolComboBox.setEnabled(false);
		}
		return protocolComboBox;
	}

	/**
	 * This method initializes modeComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getModeComboBox() {
		if (modeComboBox == null) {
			modeComboBox = new JComboBox(new String[]{"NONE","KISS"});
			modeComboBox.setPreferredSize(new Dimension(250,BH));
			modeComboBox.setSelectedItem("NONE");
			modeComboBox.setEnabled(false);
		}
		return modeComboBox;
	}
	
	/**
	 * This method initializes consoleDialog	
	 * 	
	 * @return javax.swing.JDialog
	 */
	private JConsole getConsoleDialog() {
		if (consoleDialog == null) {
			consoleDialog = new JConsole(this);
		}
		return consoleDialog;
	}
	
	
	private void console() {
		getConsoleDialog().open();
			
	}
	
	private void refresh() {

		try {
			// get selected port
			Object port = getPortComboBox().getSelectedItem();
			
			// create model
			DefaultComboBoxModel model = new DefaultComboBoxModel();
			
			// fill ports 
			for(CommPortIdentifier it : IOManager.getSerialPortIdentifiers(true)) {
				model.addElement(it.getName());
			}
			getPortComboBox().setModel(model);
			
			// reselect port
			if(port!=null)
				getPortComboBox().setSelectedItem(port);
			else
				getPortComboBox().setSelectedItem(0);
		} catch (NoSuchPortException e) {
			logEvent("NoSuchPort:" + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	private void connect() {

		String port = String.valueOf(getPortComboBox().getSelectedItem());
		
		if(port.isEmpty() || port.length()==0) {
			logEvent("Select a COM port!");  
		}
		else {
			try {
				CommPortIdentifier portIdentifier = IOManager.getPortIdentifier(port);  			   
				if (portIdentifier.isCurrentlyOwned()) {  
				    logEvent("Port in use!");  
				} else {
					Setup setup = new Setup(fields);
					String device = (String)getDeviceComboBox().getSelectedItem();
					if("Any".equalsIgnoreCase(device)) {
						// forward
						anySession.open(port, setup.baudRate, 
								setup.dataBits, setup.stopBits, 
								setup.parity, setup.flowCtrl);
					}
					else {
						tncSession.open(setup.device, port, 
								setup.baudRate, setup.dataBits, 
								setup.stopBits, setup.parity, 
								setup.flowCtrl, setup.hostMode, false);						
					}
					// finished
					return;
				}
			} catch (IOException e) {
				logEvent("IOException:" + e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TooManyListenersException e) {
				logEvent("TooManyListenersException:" + e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPortException e) {
				logEvent("NoSuchPort:" + e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PortInUseException e) {
				logEvent("PortInUse:" + e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedCommOperationException e) {
				logEvent("UnsupportedCommOperation:" + e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		    // update title
		    this.setTitle("Virtual Serial Device Emulator - <error>");
		    
		    // secure user input
			getPortComboBox().setEnabled(true);
			getDeviceComboBox().setEnabled(true);
		    
		    
		}
	}
	
	private ISession getSession() {
		String device = (String)getDeviceComboBox().getSelectedItem();
		return ("Any".equalsIgnoreCase(device) ? anySession : tncSession);
	}
	
	private void close() {

		// get session
		ISession session = getSession();
			
		// try to close session
		try {
			session.close();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void transmit() {

		// get session
		ISession session = getSession();
		
		// get data
		String data = getSendTextField().getText();
		
		// has data?
		if(data.isEmpty() || data.length()==0) {
			logEvent("send failed: empty data");
		}
		else {
		    if(!session.transmit(data)) {
		    	logEvent("send failed: " + data);
		    }
		}
		
	}
	
	private void add() {
		JAddReply dlg = new JAddReply(this);
		String[] auto = dlg.prompt();
		if(auto!=null) {
			ISession session = getSession();
			session.getProtocol().getParser().addAutoReplay(auto[0], auto[1]);
			DefaultListModel model = (DefaultListModel)getReplyList().getModel();			
			model.addElement(auto[0] + "-->" + auto[1]);
			getReplyList().ensureIndexIsVisible(model.getSize()-1);
		}
	}
	
	private void remove() {
		String auto = String.valueOf(getReplyList().getSelectedValue());
		if(auto.isEmpty()) {
			JOptionPane.showMessageDialog(this,"Select an auto-reply first");			
		}
		else {
			int ans = JOptionPane.showConfirmDialog(this, 
					"Do you want to remove autoreplay " + auto + "?",
					"Confirmation", JOptionPane.YES_NO_OPTION);
			if(ans==JOptionPane.YES_OPTION) {
				String[] split = auto.split("-->"); 
				ISession session = getSession();
				session.getProtocol().getParser().removeAutoReplay(split[0]);
				DefaultListModel model = (DefaultListModel)getReplyList().getModel();			
				model.removeElement(auto);
			}
			
		}
		
	}
	                    
	
	public void logEvent(String name) {
		DefaultListModel model = (DefaultListModel)getEventList().getModel();
		model.addElement(name);
		getEventList().ensureIndexIsVisible(model.getSize()-1);
	}

	private static class Setup {
		
		public String protocol;
		public String port;
		public String device;
		public int baudRate;
		public int dataBits;
		public int stopBits;
		public int parity;
		public int flowCtrl;
		public String hostMode;
		
		public Setup(String protocol, String port, String device, String hostMode) {
			this.protocol = protocol;
			this.port = port;
			this.device = device;
			baudRate = 9600; 
			dataBits = SerialLink.DATABITS_8; 
			stopBits = SerialLink.STOPBITS_1;
			parity = SerialLink.PARITY_NONE; 
			flowCtrl = SerialLink.FLOWCONTROL_XONXOFF_IN + SerialLink.FLOWCONTROL_XONXOFF_OUT;
			this.hostMode = hostMode;			
		}
		
		public Setup(JComboBox[] fields) {
			protocol = getProtocol(fields[0].getSelectedItem());
			port = getPort(fields[1].getSelectedItem());
			device = getTNC(fields[2].getSelectedItem());
			baudRate = getBaudRate(fields[3].getSelectedItem().toString()); 
			dataBits = getDataBits(fields[4].getSelectedItem().toString()); 
			stopBits = getStopBits(fields[5].getSelectedItem().toString());
			parity = getParity(fields[6].getSelectedItem().toString()); 
			flowCtrl = getFlowCtrl(fields[7].getSelectedItem().toString());
			hostMode = getHostMode(fields[8].getSelectedItem().toString());			
		}
		
		public Setup(TNCSession session) {
			if(session.isOpen()) {
				protocol = session.getProtocol().getName();
				port = session.getLink().getName().replace("//./", "");
				device = session.getType();
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
				device = setup.device;
				baudRate = setup.baudRate; 
				dataBits = setup.dataBits; 
				stopBits = setup.stopBits;
				parity = setup.parity; 
				flowCtrl = setup.flowCtrl;
				hostMode = setup.hostMode;
								
			}
		}
				
		public void setValues(JComboBox[] fields) {
			fields[0].setSelectedItem(protocol);
			fields[1].setSelectedItem(port);
			fields[2].setSelectedItem(device);
			fields[3].setSelectedItem(getBaudRate(baudRate)); 
			fields[4].setSelectedItem(getDataBits(dataBits)); 
			fields[5].setSelectedItem(getStopBits(stopBits));
			fields[6].setSelectedItem(getParity(parity)); 
			fields[7].setSelectedItem(getFlowCtrl(flowCtrl));
			fields[8].setSelectedItem(hostMode);			
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
	
	private class EventLogger implements IManagerListener {

		public void onOpen(SessionEvent e) {

			// log event
			logEvent("CONNECTED: " + e.getSource().getLink().getName());

			// update title
		    setTitle("Virtual Serial Device Emulator - " + e.getSource().getName());
		    
		    // prevent user input
			getPortComboBox().setEnabled(false);
			getDeviceComboBox().setEnabled(false);
			
		}

		public void onClose(SessionEvent e) {

		    // notify
		    logEvent("DISCONNECTED: " + e.getSource().getLink().getName());
		    
		    // update title
		    setTitle("Virtual Serial Device Emulator - <no connection>");
		    
			// enable user input
			getPortComboBox().setEnabled(true);
			getDeviceComboBox().setEnabled(true);
			
		}

		public void onEntityDetected(IBroker<?> broker, EntityEvent e) {
			logEvent("ENTITY DETECTED: " + e.getEntity().getCue());
		}

		public void onReceive(ISession session, ProtocolEvent e) {
			logEvent("RX: " + e.getPacket().getMessage());
		}

		public void onTransmit(ISession session, ProtocolEvent e) {
			logEvent("TX: " + e.getPacket().getMessage());			
		}
		
		public void onSessionAdded(ISession session) {
			logEvent("SESSION ADDED: " + session.getName());
		}

		public void onSessionRemoved(ISession session) {
			logEvent("SESSION REMOVED: " + session.getName());			
		}

		public void onCurrentSessionChanged(ISession session) {
			logEvent("SESSION SET: " + session.getName());						
		}

		public void onBufferOverflow(ISession session, ProtocolEvent e) {
			logEvent("BUFFER OVERFLOW: " + session.getName());
		}

	}
	
	/**
	 * The main method.
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		// initialize GUI on new thread
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JMain frame = new JMain();
				frame.setVisible(true);
			}
		});
	}	
	

}  //  @jve:decl-index=0:visual-constraint="11,0"

