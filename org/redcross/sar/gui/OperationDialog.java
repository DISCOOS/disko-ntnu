package org.redcross.sar.gui;

import javax.swing.WindowConstants;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoStringFactory;

public class OperationDialog extends DiskoDialog {

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
            this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(275,160));
            this.pack();
				
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}

	public void setVisible(boolean isVisible) {
		// Only use showLogin() og showChangeRole()
	}
	
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private OperationPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new OperationPanel();
			contentPanel.setPreferredBodySize(new Dimension(275,300));
			contentPanel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if("finish".equalsIgnoreCase(cmd))
						finish();
					else if("cancel".equalsIgnoreCase(cmd))
						cancel();
										
				}
				
			});
		}
		return contentPanel;
	}

	public void setFixedSize() {
		getContentPanel().setFixedSize();
	}	
	
	private void finish() {
		// initialize flag
		boolean auth = false;
		// get values
		String opId = getContentPanel().getSelectedOperation();
		// was selected?
		// forward
		auth = Utils.getApp().activeOperation(opId);
		// is authorized?
		if(auth) 
			super.setVisible(false);
		else
			Utils.showWarning(DiskoStringFactory.getText("WARNING_SELECT_OPERATION_FAILED"));
	}

	private void cancel() {
		// hide this
		super.setVisible(false);
		// exit system?
		if (exitAppOnCancel) System.exit(0);		
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
		super.setVisible(true);
		// finished
		return isCancel;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
