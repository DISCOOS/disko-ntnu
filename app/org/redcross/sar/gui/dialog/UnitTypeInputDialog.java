package org.redcross.sar.gui.dialog;

import java.awt.Frame;

import org.redcross.sar.gui.panel.UnitTypeInputPanel;
import org.redcross.sar.mso.data.IUnitIf.UnitType;

/**
 * @author kennetgu
 *
 */
public class UnitTypeInputDialog extends DefaultDialog  {

	private static final long serialVersionUID = 1L;
	
	private UnitTypeInputPanel contentPanel;
	
	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public UnitTypeInputDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// initialize GUI
		initialize();
		
	}

	/* ========================================================
	 * Public methods
	 * ======================================================== */
	
	public UnitType showInput() {
		// finished
		return showInput(null);
	}
	
	public UnitType showInput(UnitType type) {
		// make dialog modal         
		setModal(true);
		// reset input
		getContentPanel().setType(type);
		// show
		setVisible(true);
		// finished
		return getContentPanel().getType();
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
	private UnitTypeInputPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new UnitTypeInputPanel();
			contentPanel.setRequestHideOnFinish(false);

		}
		return contentPanel;
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
