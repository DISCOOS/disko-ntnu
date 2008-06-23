package org.redcross.sar.gui.dialog;

import javax.swing.WindowConstants;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.gui.panel.OperationPanel;

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
            this.setPreferredSize(new Dimension(375,300));
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
					String opId = getContentPanel().getSelectedOperation();
					// forward
					auth = Utils.getApp().activeOperation(opId);
					// is not authorized?
					if(!auth) 
						Utils.showWarning(DiskoStringFactory.getText("WARNING_SELECT_OPERATION_FAILED"));
					// finished
					return auth;
					
				}
			};
			contentPanel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if("create".equalsIgnoreCase(cmd)) {
						// forward
						if(Utils.getApp().createOperation())
							setVisible(false);						
					}					
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
		getContentPanel().onResize();
	}	
	
	public void load() {
		// forward
		getContentPanel().update();
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
