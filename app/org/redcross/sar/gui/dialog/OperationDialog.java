package org.redcross.sar.gui.dialog;

import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.redcross.sar.AppProps;
import org.redcross.sar.Application;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.gui.panel.OperationPanel;
import org.redcross.sar.util.Utils;

public class OperationDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private boolean isCancel = false;
	private boolean exitAppOnCancel = false;
	
	private OperationPanel contentPanel = null;

	/**
	 * @param owner
	 */
	public OperationDialog(Frame owner) {
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
            this.setPreferredSize(new Dimension(375,296));
            this.setContentPane(getContentPanel());
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
	private OperationPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new OperationPanel() {

				private static final long serialVersionUID = 1L;

				@Override
				protected boolean beforeFinish() {
					// initialize flag
					boolean auth = false;
					// get values
					String opId = getContentPanel().getSelectedOperationID();
					// forward
					auth = Application.getInstance().activateOperation(opId);
					// is not authorized?
					if(!auth) 
						Utils.showWarning(DiskoStringFactory.getText("WARNING_SELECT_OPERATION_FAILED"));
					// finished
					return auth;
					
				}
				
				@Override
				protected boolean beforeCancel() {
					if(exitAppOnCancel) {
						int ans = Utils.showConfirm("Bekreftelse", 
								"Dette vil avslutte DISKO. Vil du fortsette?",JOptionPane.YES_NO_OPTION);
						return (ans == JOptionPane.YES_OPTION); 
					}
					return true;
				}
				
			};
			contentPanel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if("create".equalsIgnoreCase(cmd)) {
						// forward
						if(Application.getInstance().createOperation(true))
							setVisible(false);						
					}					
					else if("cancel".equalsIgnoreCase(cmd)) {
						// exit system?
						if (exitAppOnCancel) {
							// yes, shutdown safely
							Application.getInstance().shutdown();		
						}
					}										
				}
				
			});
		}
		return contentPanel;
	}

	/*
	public void setFixedSize() {
		getContentPanel().onResize();
	}	
	*/

	public void load() {
		// forward
		getContentPanel().update();
		// get active operation id
		String id = Application.getInstance().getDispatcher().getCurrentOperationID();
		// select last operation instead?
		if(id==null)
		{
			id = AppProps.getText("STARTUP.LAST.OPRID");
		}
		// select operation
		getContentPanel().setSelectedOperationID(id);
	}
	
	public boolean selectOperation(boolean exitAppOnCancel) {
		// set flags
		this.isCancel = false;
		this.exitAppOnCancel = exitAppOnCancel;
		// show dialog
		setVisible(true);
		// finished
		return isCancel;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
