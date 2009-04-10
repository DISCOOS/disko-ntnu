package org.redcross.sar.gui.dialog;

import java.awt.Frame;

import org.redcross.sar.gui.panel.NumPadPanel;
 
public class NumPadDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;
	
	private NumPadPanel contentPanel;

	/* ========================================================
	 * Constructors
	 * ======================================================== */
	
	public NumPadDialog(Frame owner) {
		// forward
		super(owner);
		// initialize gui
		initialize();
	}
	
	/* ========================================================
	 * Public methods
	 * ======================================================== */
	
	public int showInput() {
		// finished
		return showInput(-1);
	}
	
	public int showInput(int number) {
		// make dialog modal         
		setModal(true);
		// reset input
		getContentPanel().setNumber(number);
		// show
		setVisible(true);
		// finished
		return getContentPanel().getNumber();
	}
	
	/* ========================================================
	 * Helper methods
	 * ======================================================== */
	
	private void initialize(){
		// prepare
		this.setUndecorated(true);		
        this.setContentPane(getContentPanel());
        this.setResizable(false);
        this.pack();
	}
	
	/**
	 * This method initializes contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private NumPadPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new NumPadPanel();
			contentPanel.setRequestHideOnFinish(false);

		}
		return contentPanel;
	}

}  //  @jve:decl-index=0:visual-constraint="43,48"
