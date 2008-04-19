/**
 * 
 */
package org.redcross.sar.gui.map;

import java.awt.Frame;
import java.io.IOException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.map.IDiskoMap;

/**
 * @author kennetgu
 *
 */
public class GotoDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	
	private GotoPanel m_gotoPanel = null;
	
	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public GotoDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// initialize GUI
		initialize();
		
	}

	private void initialize() {
		try {
			// prepare dialog
			Utils.setFixedSize(this, 325, 125);
	        this.setContentPane(getGotoPanel());
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onLoad(IDiskoMap map) throws IOException {
		getGotoPanel().setMap(map);
	}	
	
	/**
	 * This method initializes m_contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private GotoPanel getGotoPanel() {
		if (m_gotoPanel == null) {
			m_gotoPanel = new GotoPanel();
		}
		return m_gotoPanel;
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
