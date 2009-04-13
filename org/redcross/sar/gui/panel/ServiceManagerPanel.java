package org.redcross.sar.gui.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;

import org.disco.io.IBroker;
import org.disco.io.IOManager;
import org.disco.io.ISession;
import org.disco.io.aprs.APRSBroker;
import org.disco.io.aprs.APRSParser;
import org.disco.io.event.EntityEvent;
import org.disco.io.event.IManagerListener;
import org.disco.io.event.ProtocolEvent;
import org.disco.io.event.SessionEvent;
import org.disco.io.net.NetSession;
import org.disco.io.serial.TNCSession;
import org.redcross.sar.app.Application;
import org.redcross.sar.gui.dialog.ConsoleDialog;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.ListSelectorDialog;
import org.redcross.sar.gui.dialog.NetDialog;
import org.redcross.sar.gui.dialog.TNCDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.renderer.DiskoTableCellRenderer;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.util.Utils;

public class ServiceManagerPanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;
	
	private DiskoTable serviceTable;
	
	private JButton addButton;
	private JButton removeButton;
	private JButton consoleButton;
	
	private TNCDialog tncDialog;
	private NetDialog netDialog;
	private LoginDialog loginDialog;
	private ConsoleDialog consoleDialog;
	private ListSelectorDialog selectorDialog;
	
	/* ===========================================
	 * Constructors
	 * =========================================== */
	
	public ServiceManagerPanel() {
		// forward
		super("Tjenester",false,true);
		// initialize GUI
		initialize();
	}

	/* ===========================================
	 * Protected methods
	 * =========================================== */
	
	private void initialize() {
		
		// set container 
		setContainer(getServiceTable());
		insertButton("finish", getConsoleButton(), "console");
		insertButton("finish", getAddButton(), "add");
		insertButton("finish", getRemoveButton(), "remove");
		
		// add action listener
		addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				String cmd = e.getActionCommand();
				if("add".equalsIgnoreCase(cmd)) {
					add();
				}
				else if("remove".equalsIgnoreCase(cmd)) {
					remove();
				}
				else if("console".equalsIgnoreCase(cmd)) {
					console();
				}

			}

		});		

		
	}
	
	/* ===========================================
	 * Private methods
	 * =========================================== */
	
	private DiskoTable getServiceTable() {
		if (serviceTable == null) {
			serviceTable = new DiskoTable(new ServiceTableModel());
			serviceTable.setTableHeader(null);
			serviceTable.setFillsViewportHeight(true);
			serviceTable.setRowHeight(35);
			serviceTable.setShowGrid(false);
			serviceTable.setIntercellSpacing(new Dimension(5,5));
			serviceTable.setDefaultRenderer(Object.class, new ServiceTableRowRenderer());
			
			serviceTable.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					if(KeyEvent.VK_ENTER == e.getKeyCode())
						edit();
					else if(KeyEvent.VK_ESCAPE == e.getKeyCode())
						cancel();					
				}

			});
			serviceTable.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2) {
						edit();
					}
				}

			});			
		}
		return serviceTable;
	}
	
	/**
	 * This method initializes addButton	
	 * 	
	 * @return javax.swing.JButton
	 */	
	private JButton getAddButton() {
		if(addButton == null) {
			addButton = DiskoButtonFactory.createButton("GENERAL.PLUS",ButtonSize.SMALL);
			addButton.setToolTipText("Legg til tjeneste");
		}
		return addButton;
	}	
	
	/**
	 * This method initializes removeButton	
	 * 	
	 * @return javax.swing.JButton
	 */	
	private JButton getRemoveButton() {
		if(removeButton == null) {
			removeButton = DiskoButtonFactory.createButton("GENERAL.MINUS",ButtonSize.SMALL);
			removeButton.setToolTipText("Fjern tjeneste");
		}
		return removeButton;
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
		}
		return consoleButton;
	}
	
	/**
	 * This method initializes consoleDialog	
	 * 	
	 * @return org.redcross.sar.gui.dialog.ConsoleDialog
	 */	
	private ConsoleDialog getConsoleDialog() {
		if(consoleDialog == null) {
			consoleDialog = new ConsoleDialog(Application.getInstance());
			consoleDialog.setLocationRelativeTo(Application.getInstance());
		}
		return consoleDialog;
	}		
		
	/**
	 * This method initializes selectorDialog	
	 * 	
	 * @return org.redcross.sar.gui.dialog.ListSelectorDialog
	 */	
	private ListSelectorDialog getSelectorDialog() {
		if(selectorDialog == null) {
			selectorDialog = new ListSelectorDialog(Application.getInstance());
			selectorDialog.prepare("Velg tjeneste", new String[]{
					"Sanntidssporing (APRS-COM)",
					"Sanntidssporing (APRS-IS)"});
		}
		return selectorDialog;
	}	
	
	/**
	 * This method initializes tncDialog	
	 * 	
	 * @return org.redcross.sar.gui.dialog.TNCDialog
	 */	
	private TNCDialog getTNCDialog() {
		if(tncDialog == null) {
			tncDialog = new TNCDialog(Application.getInstance());
			tncDialog.setLocationRelativeTo(Application.getInstance());			
		}
		return tncDialog;
	}		
	
	/**
	 * This method initializes netDialog	
	 * 	
	 * @return org.redcross.sar.gui.dialog.NetDialog
	 */	
	private NetDialog getNetDialog() {
		if(netDialog == null) {
			netDialog = new NetDialog(Application.getInstance());
			netDialog.setLocationRelativeTo(Application.getInstance());			
		}
		return netDialog;
	}			
	
	/**
	 * This method initializes loginDialog	
	 * 	
	 * @return org.redcross.sar.gui.panel.ServiceManagerPanel.LoginDialog
	 */	
	private LoginDialog getLoginDialog() {
		if(loginDialog == null) {
			loginDialog = new LoginDialog();
			loginDialog.setLocationRelativeTo(Application.getInstance());			
		}
		return loginDialog;
	}			

	private void add() {
		
		// prompt user
		String service = (String)getSelectorDialog().select();
		
		// add service?
		if("Sanntidssporing (APRS-COM)".equals(service)) {
			// create APRS parser
			APRSParser parser = new APRSParser("APRS parser",'\r','\n');			
			// create a TNC session
			TNCSession session = new TNCSession("APRS",parser,"-","resources/io");
			// create broker
			APRSBroker broker = new APRSBroker(session);
			// manage service
			if (getTNCDialog().manage(session)) {
				// add to manager
				IOManager.getInstance().addSession(broker);
			}
		}
		else if("Sanntidssporing (APRS-IS)".equals(service)) {
			// create APRS parser
			APRSParser parser = new APRSParser("APRS parser",'\r','\n');			
			// create a NET session
			NetSession session = new NetSession("APRS-IS",parser,"-",APRSBroker.APRS_IS_LOGIN);
			// create broker
			APRSBroker broker = new APRSBroker(session);
			// manage service
			if (getNetDialog().manage(session)) {
				// add to manager
				IOManager.getInstance().addSession(broker);
			}
		}
	}
	
	private void edit() {
		int selRow = getServiceTable().getSelectedRow();
		if(selRow>-1) {
			Object selected = getServiceTable().getValueAt(selRow, 0);
			if(selected instanceof IBroker<?>) {
				IBroker<?> broker = (IBroker<?>)selected;
				if(broker.getSession() instanceof TNCSession) {
					getTNCDialog().manage((TNCSession)broker.getSession());
				}
				else if(broker.getSession() instanceof NetSession) {
					getNetDialog().manage((NetSession)broker.getSession());
				}
			}
		}
	}
	
	private void remove() {
		
		try {
			// get selection index
			int row = getServiceTable().getSelectedRow();
			
			// get session?
			if(row!=-1) {
				ISession session = (ISession)getServiceTable().getValueAt(row, 0);
				// prompt user
				int ans = JOptionPane.showConfirmDialog(this, 
						"Dette vil fjerne tjenesten " + session.getName() 
						+ ". Vil du fortsette?","Bekreftelse",JOptionPane.YES_NO_OPTION);
				// remove?
				if(ans == JOptionPane.YES_OPTION) {
					session.close();
					IOManager.getInstance().removeSession(session);				
				}
				return;
			}
			Utils.showMessage("Du må velge en tjeneste først");
		} catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void console() {
		ConsoleDialog dlg = new ConsoleDialog(Application.getInstance());
		dlg.setLocationRelativeTo(Application.getInstance());
		getConsoleDialog().open();		
	}
	
	/* ===========================================
	 * Private classes
	 * =========================================== */
	
	private class ServiceTableModel extends AbstractTableModel implements IManagerListener {
		
		private static final long serialVersionUID = 1L;

		List<Object> rows = new ArrayList<Object>();
		
		ServiceTableModel() {
			IOManager.getInstance().addManagerListener(this);
			rows.addAll(IOManager.getInstance().getSessions());
		}

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return rows.size();
		}

		public Object getValueAt(int row, int col) {
			if(row>=0 && row<getRowCount()) {
				return rows.get(row);
			}
			return null;			
		}

		public void onSessionAdded(ISession session) {
			// add to table?
			if(!rows.contains(session)) {
				rows.add(session);
				fireTableDataChanged();			
			}
		}

		public void onSessionRemoved(ISession session) {
			// remove from table?
			if(rows.contains(session)) {
				rows.remove(session);
				fireTableDataChanged();			
			}
		}
		
		public void onOpen(SessionEvent e) {
			if(e.getSource() instanceof NetSession) {
				// prepare
				NetSession session = (NetSession)e.getSource();				
				// prompt user
				String[] login = getLoginDialog().prompt(
						session.getUsername(),
						session.getPassword(),
						"filter r/64/10/25");
				// login supplied?
				if(login!=null) {
					session.login(login[0],login[1],login[2]);
				}
			}
			fireTableDataChanged();
		}

		public void onClose(SessionEvent e) {
			fireTableDataChanged();			
		}
		
		public void onCurrentSessionChanged(ISession session) { /* NOP */ }
		public void onReceive(ISession session, ProtocolEvent e) { /* NOP */ }
		public void onTransmit(ISession session, ProtocolEvent e) { /* NOP */ }
		public void onEntityDetected(IBroker<?> broker,EntityEvent e) { /* NOP */ }
		public void onBufferOverflow(ISession session, ProtocolEvent e) {  /* NOP */ }

		
	}
	
	private class LoginDialog extends DefaultDialog {

		private static final long serialVersionUID = 1L;
		
		FieldsPanel fieldsPanel;
		TextLineField userNameField;
		TextLineField passwordField;
		TextLineField commandField;
		
		boolean isCancel = false;
		
		public String[] prompt(String username, String password, String command) {
			// prepare
			isCancel = false;
			// show med
			setVisible(true);
			// return selection
			return isCancel ? null: 
				new String[]{getUserNameField().getValue().toString(),
				           getPasswordField().getValue().toString(),
				           getCommandField().getValue().toString()}; 
		}
		
		public LoginDialog() {
			// forward
			super(Application.getInstance());
			// initialize gui
			initialize();
		}

		private void initialize() {
			this.setSize(250, 150);
			this.setResizable(false);
			this.setContentPane(getLoginPanel());
			this.setModal(true);
			
		}
		
		private FieldsPanel getLoginPanel() {
			if(fieldsPanel==null) {
				fieldsPanel = new FieldsPanel("Login");
				fieldsPanel.setScrollBarPolicies(
						DefaultPanel.VERTICAL_SCROLLBAR_NEVER,
						DefaultPanel.HORIZONTAL_SCROLLBAR_NEVER);
				fieldsPanel.setCaptionWidth(100);
				fieldsPanel.addField(getUserNameField());
				fieldsPanel.addField(getPasswordField());
				fieldsPanel.addField(getCommandField());
				
				// add action listener
				fieldsPanel.addActionListener(new ActionListener() {
		
					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						if("cancel".equalsIgnoreCase(cmd)) {
							// set flag
							isCancel = true;
						}
					}
		
				});				
				
			}
			return fieldsPanel;
		}
		
		/**
		 * This method initializes userNameField	
		 * 	
		 * @return org.redcross.sar.gui.field.TextLineField
		 */
		private TextLineField getUserNameField() {
			if (userNameField == null) {
				userNameField = new TextLineField("username","Brukernavn",true);			
				userNameField.setValue("DISKO");
			}
			return userNameField;
		}		
		
		/**
		 * This method initializes hostModeField	
		 * 	
		 * @return org.redcross.sar.gui.field.TextLineField	
		 */
		private TextLineField getPasswordField() {
			if (passwordField == null) {
				passwordField = new TextLineField("password","Passord",true);			
				passwordField.setValue("-1");
			}
			return passwordField;
		}				
		
		/**
		 * This method initializes hostModeField	
		 * 	
		 * @return org.redcross.sar.gui.field.TextLineField	
		 */
		private TextLineField getCommandField() {
			if (commandField == null) {
				commandField = new TextLineField("command","Kommando",true);			
				commandField.setValue("filter r/64/10/200");
			}
			return commandField;
		}				
		
	}
	
	private static class ServiceTableRowRenderer extends DiskoTableCellRenderer {
	
		private static final long serialVersionUID = 1L;

		private final Icon m_icon;
		private final Border m_border = BorderFactory.createCompoundBorder(
				new LineBorder(Color.LIGHT_GRAY, 2, true),BorderFactory.createEmptyBorder(2,2,2,2));

	    public ServiceTableRowRenderer()
		{
	    	setOpaque(true);
	    	m_icon = DiskoIconFactory.getIcon("GENERAL.CANCEL", "24x24");
		}

	    /**
	     * Get cell component. Message lines with status postponed have pink background
	     */
		public JLabel getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
	    {

			// initialize
			String text = "";
			Color f = table.getForeground();
			Color b = table.getBackground();

			// prepare
			JLabel renderer = super.prepare(table, null, isSelected, hasFocus, row, col, true, false);

			// set
			setVerticalAlignment(SwingConstants.CENTER);

			// has row?
			if(m_rowInModel!=-1) {

		        // get icon and text
				if(value instanceof ISession)
				{
					ISession session = (ISession)value;
					if(session.isOpen()) {
						text = ((ISession)value).getName() + " (tilkoblet::" + session.getLink().getName().replace("//./","") +")";
						b = Color.GREEN;
					}
					else
						text = ((ISession)value).getName() + " (frakoblet)";
					
		        }
			}

	        // update
	        renderer.setText(text);
	        renderer.setIcon(m_icon);
			// update selection state?
			if (isSelected){
				renderer.setBackground(table.getSelectionBackground());
				renderer.setForeground(table.getSelectionForeground());
			}
			else {
		        renderer.setForeground(f);
		        renderer.setBackground(b);	        
			}
	    	setBorder(m_border);

	        // finished
	        return renderer;

	    }

	}
	
}
