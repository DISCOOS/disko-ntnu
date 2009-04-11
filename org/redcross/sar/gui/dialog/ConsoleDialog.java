package org.redcross.sar.gui.dialog;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.disco.io.IBroker;
import org.disco.io.IOManager;
import org.disco.io.IResponse;
import org.disco.io.ISession;
import org.disco.io.event.EntityEvent;
import org.disco.io.event.IManagerListener;
import org.disco.io.event.ProtocolEvent;
import org.disco.io.event.SessionEvent;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.DefaultPanel;

public class ConsoleDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private DefaultPanel contentPanel;
	private JPanel outputPanel;	
	private JPanel commandPanel;
	
	private JScrollPane commandScrollPane;
	private JScrollPane consoleScrollPane;

	private JLabel commandPrefixLabel;
	
	private JTextArea consoleTextArea;
	private JTextArea commandTextArea;
	private JTextField commandTextField;
	
	private JButton executeButton;
	
	private IOManager io = IOManager.getInstance();
	
	private int step = 0;
	private List<String> history = new ArrayList<String>();
	
	private final String RX = "RX: %1$s";
	private final String TX = "TX: %1$s";
	private final String ENTITY = "Entity %1$s detected";
	private final String LINE_TO = "%1$s < %2$s";
	private final String LINE_FROM = "%1$s > %2$s";
	private final String SESSION_SET = "Session set: %1$s";
	private final String SESSION_ADDED = "Session added: %1$s";
	private final String SESSION_REMOVED = "Session removed: %1$s";
	private final String SESSION_CONNECTED = "%1$s CONNECTED to %2$s";
	private final String SESSION_DISCONNECTED = "%1$s DISCONNECTED from %2$s";

	public ConsoleDialog(Frame owner) {
		super(owner);
		initialize();
		io.addManagerListener(listener);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(600, 400);
		this.setResizable(false);
		this.setContentPane(getContentPanel());
	}

	/**
	 * This method initializes contentPanel
	 * 
	 * @return org.redcross.sar.gui.dialog.DefaultPanel
	 */
	private DefaultPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new DefaultPanel();
			contentPanel.setContainerLayout(new BorderLayout(5,5));
			contentPanel.addToContainer(getOutputPanel(), BorderLayout.CENTER);
			contentPanel.addToContainer(getCommandPanel(), BorderLayout.SOUTH);
			contentPanel.setNotScrollBars();
		}
		return contentPanel;
	}

	/**
	 * This method initializes outputPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getOutputPanel() {
		if (outputPanel == null) {
			outputPanel = new JPanel();
			outputPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			outputPanel.setLayout(new BoxLayout(outputPanel,BoxLayout.Y_AXIS));
			outputPanel.add(getConsoleScrollPane());
			outputPanel.add(Box.createVerticalStrut(5));
			outputPanel.add(getCommandScrollPane());
		}
		return outputPanel;
	}	
	
	/**
	 * This method initializes consoleScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getConsoleScrollPane() {
		if (consoleScrollPane == null) {
			consoleScrollPane = new JScrollPane();
			consoleScrollPane.setViewportView(getConsoleTextArea());
			consoleScrollPane.setMinimumSize(new Dimension(0,150));
			consoleScrollPane.setBorder(BorderFactory.createTitledBorder("IO/events"));			
		}
		return consoleScrollPane;
	}

	/**
	 * This method initializes consoleTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getConsoleTextArea() {
		if (consoleTextArea == null) {
			consoleTextArea = new JTextArea();
			consoleTextArea.setEditable(false);
			consoleTextArea.setTabSize(2);
			consoleTextArea.setBackground(getBackground());
		}
		return consoleTextArea;
	}
	
	/**
	 * This method initializes commandPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCommandPanel() {
		if (commandPanel == null) {
			commandPanel = new JPanel(new BorderLayout(5,5));
			commandPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			commandPanel.add(getCommandPrefixLabel(),BorderLayout.WEST);
			commandPanel.add(getCommandTextField(),BorderLayout.CENTER);
			commandPanel.add(getExecuteButton(),BorderLayout.EAST);
		}
		return commandPanel;
	}	
	
	/**
	 * This method initializes replyScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCommandScrollPane() {
		if (commandScrollPane == null) {
			commandScrollPane = new JScrollPane();
			commandScrollPane.setViewportView(getCommandTextArea());
			commandScrollPane.setBorder(BorderFactory.createTitledBorder("Commands"));
			commandScrollPane.setMinimumSize(new Dimension(0,150));
		}
		return commandScrollPane;
	}
	
	/**
	 * This method initializes commandTextArea
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getCommandTextArea() {
		if (commandTextArea == null) {
			commandTextArea = new JTextArea();
			commandTextArea.setEditable(false);
			commandTextArea.setTabSize(2);
			commandTextArea.setBackground(getBackground());

		}
		return commandTextArea;
	}	
	
	/**
	 * This method initializes commandPrefixLabel
	 * 	
	 * @return javax.swing.JLabel
	 */
	private JLabel getCommandPrefixLabel() {
		if (commandPrefixLabel == null) {
			commandPrefixLabel = new JLabel("IOManager <");
		}
		return commandPrefixLabel;
		
	}	
	
	/**
	 * This method initializes commandTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getCommandTextField() {
		if (commandTextField == null) {
			commandTextField = new JTextField();
			commandTextField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ENTER) {
						execute();
					}
					else if(e.getKeyCode() == KeyEvent.VK_UP) {
						if(step>0) step--;
						commandTextField.setText(getHistory(step));
					}
					else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
						if(step<history.size()-1) step++;
						commandTextField.setText(getHistory(step));
					}
				}
				
			});		}
		return commandTextField;
		
	}	
	
	/**
	 * This method initializes executeButton
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getExecuteButton() {
		if (executeButton == null) {
			executeButton = DiskoButtonFactory.createButton(
					"GENERAL.APPLY",ButtonSize.SMALL);			
			executeButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					
					execute();
					
				}
				
			});

		}
		return executeButton;
	}	
	
	private void execute() {
		
		// get command
		String cmd = getCommandTextField().getText();
		
		// TODO: save command
		add(cmd);
		
		// reset command line
		getCommandTextField().setText("");
		
		// notify user
		publish(getCmdSource(io.getCurrentSession()),cmd,"cmd");
		
		// forward
		IResponse r = io.execute(cmd);
		
		// is batch?
		if(r.isBatch()) {
			// serialize out batch to command window
			for(IResponse it : (IResponse[])r.getValue()) {
				publish(getCmdSource(it.getSource()),it.getCommand(),"cmd");
				publish(getCmdSource(it.getSource()),it.translate(),"response");
			}
		}
		else {
			// notify user
			publish(getCmdSource(r.getSource()), r.translate(),"response");
		}
		
	}
	
	private void add(String cmd) {
		history.add(cmd);
		step = history.size();
	}
	
	private String getHistory(int step) {
		return history.size()>0 ?history.get(step) : "IOManager >";
	}
	
	public void open() {
		// forward
		validateSession();
		// add command line
		publish(getCmdSource(io.getCurrentSession()), "", "response");
		// show 
		setVisible(true);
	}
	
	public void open(ISession session) {
		// set as current
		IOManager.getInstance().setCurrentSession(session.getName());
		// forward
		open();
	}
	
	private void validateSession() {
		// get current session
		ISession session = io.getCurrentSession();
		// validate
		if(session!=null) {
			getContentPanel().setCaptionText(session.getName());
		}
		else {
			getContentPanel().setCaptionText("No session set");		
		}
		
	}
	
	private String getCmdSource(Object obj) {
		if(obj instanceof ISession) {
			return ((ISession)obj).getName();
		}
		return "IOManager";
	}
			
	private void publish(String source, String text, String sink) {
		if("stream".equalsIgnoreCase(sink)) {
			getConsoleTextArea().setText(getConsoleTextArea().getText() + insertSource(LINE_FROM,source,text));
			getConsoleTextArea().setCaretPosition(getConsoleTextArea().getText().length() - 1);
		} else if("cmd".equalsIgnoreCase(sink)) {
			getCommandTextArea().setText(getCommandTextArea().getText() + insertSource(LINE_TO,source,text));
			getCommandTextArea().setCaretPosition(getCommandTextArea().getText().length() - 1);
		} else if("response".equalsIgnoreCase(sink)) { 
			getCommandTextArea().setText(getCommandTextArea().getText() + insertSource(LINE_FROM,source,text));
			getCommandTextArea().setCaretPosition(getCommandTextArea().getText().length() - 1);
		}
	}
	
	private String insertSource(String template, String source, String text) {
		String[] lines = text.split("\\n");
		text = "";
		for(String line : lines) {
			text = text.concat(String.format(template,source,line.trim())) + "\n";
		}
		return text;
	}
	
	private IManagerListener listener = new IManagerListener() {

		public void onConnect(SessionEvent e) {
			// get session
			ISession session = e.getSource();
			// forward
			validateSession();
			// notify user
			publish("Event",String.format(SESSION_CONNECTED,session.getName(),session.getLink().getName()),"stream");
		}

		public void onDisconnect(SessionEvent e) {
			// get session
			ISession session = e.getSource();
			// notify user
			publish("Event",String.format(SESSION_DISCONNECTED,session.getName(),session.getLink().getName()),"stream");
		}

		public void onReceive(ISession session, ProtocolEvent e) {
			publish(session.getName(), String.format(RX,e.getPacket().getMessage()),"stream");
		}

		public void onTransmit(ISession session, ProtocolEvent e) {
			publish(session.getName(), String.format(TX,e.getPacket().getMessage()),"stream");			
		}

		public void onEntityDetected(IBroker broker, EntityEvent e) {
			publish(broker.getName(), String.format(ENTITY,e.getEntity().getCue()),"stream");						
		}

		public void onSessionAdded(ISession session) {
			publish("IOManager",String.format(SESSION_ADDED, session.getName()),"stream");
			// ensure correct session
			validateSession();
		}
		
		public void onSessionRemoved(ISession session) {
			publish("IOManager", String.format(SESSION_REMOVED,session.getName()),"stream");			
			// forward
			validateSession();
		}

		public void onCurrentSessionChanged(ISession session) {
			publish("IOManager", String.format(SESSION_SET, session.getName()),"stream");
			String prefix = (session==null ? "IOManager" : session.getName()); 
			getCommandPrefixLabel().setText(prefix + " < ");
			// forward
			validateSession();
		}
		
	};

}  //  @jve:decl-index=0:visual-constraint="10,10"
