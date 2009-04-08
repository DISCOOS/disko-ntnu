package org.redcross.sar.gui.panel;

import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;

import org.disco.io.IBroker;
import org.disco.io.IOManager;
import org.disco.io.ISession;
import org.disco.io.aprs.APRSBroker;
import org.disco.io.event.EntityEvent;
import org.disco.io.event.IManagerListener;
import org.disco.io.event.ProtocolEvent;
import org.disco.io.event.SessionEvent;
import org.disco.io.serial.TNCSession;
import org.redcross.sar.gui.dialog.ListSelectorDialog;
import org.redcross.sar.gui.dialog.TNCDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.renderer.DiskoTableCellRenderer;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.util.Utils;

public class ServiceManagerPanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;
	
	private DiskoTable serviceTable;
	
	private JButton addButton;
	private JButton removeButton;
	
	private TNCDialog tncDialog;
	private ListSelectorDialog selectorDialog;
	
	/* ===========================================
	 * Constructors
	 * =========================================== */
	
	public ServiceManagerPanel() {
		// forward
		super("Tjenester",false,true);
	}

	/* ===========================================
	 * Protected methods
	 * =========================================== */
	
	@Override
	protected void initialize() {
		
		// forward
		super.initialize();
		
		// set container 
		setContainer(getServiceTable());
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
			
			// add user interaction options
			/*
			serviceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					// consume?
					if(!isChangeable()) return;
					// set dirty flag
					setDirty(e.getFirstIndex()>=0,false);					
				}
				
			});
			*/
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
	 * This method initializes selectorDialog	
	 * 	
	 * @return org.redcross.sar.gui.dialog.ListSelectorDialog
	 */	
	private ListSelectorDialog getSelectorDialog() {
		if(selectorDialog == null) {
			selectorDialog = new ListSelectorDialog(Utils.getApp().getFrame());
			selectorDialog.prepare("Velg tjeneste", new String[]{"APRS Sanntidssporing"});
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
			tncDialog = new TNCDialog(Utils.getApp().getFrame());
		}
		return tncDialog;
	}		

	private void add() {
		
		// prompt user
		String service = (String)getSelectorDialog().select();
		
		// add service?
		if("APRS Sanntidssporing".equals(service)) {
			// create broker
			APRSBroker broker = new APRSBroker(service,"resources/io");
			// manage service
			if (getTNCDialog().manageSession(broker)) {
				// add to manager
				IOManager.getInstance().addSession(broker);
			}
		}
	}
	
	private void edit() {
		int selRow = getServiceTable().getSelectedRow();
		if(selRow>-1) {
			Object selected = getServiceTable().getValueAt(selRow, 0);
			if(selected instanceof TNCSession) {
				getTNCDialog().manageSession((TNCSession)selected);
			}
		}
	}
	
	private void remove() {
		
	}
	
	/* ===========================================
	 * Private classes
	 * =========================================== */
	
	private static class ServiceTableModel extends AbstractTableModel implements IManagerListener {
		
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
		
		public void onConnect(SessionEvent e) {
			fireTableDataChanged();			
		}

		public void onDisconnect(SessionEvent e) {
			fireTableDataChanged();			
		}
		
		public void onReceive(ISession session, ProtocolEvent e) { /* NOP */ }
		public void onTransmit(ISession session, ProtocolEvent e) { /* NOP */ }
		public void onEntityDetected(IBroker broker,EntityEvent e) { /* NOP */ }

		
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
					if(session.isConnected()) {
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
	        renderer.setForeground(f);
	        renderer.setBackground(b);	        
	    	setBorder(m_border);

	        // finished
	        return renderer;

	    }

	}
	
}
