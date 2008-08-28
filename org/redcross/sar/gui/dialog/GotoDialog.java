/**
 * 
 */
package org.redcross.sar.gui.dialog;

import java.awt.Frame;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.map.IDiskoMap;

/**
 * @author kennetgu
 *
 */
public class GotoDialog extends DefaultDialog {

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
			Utils.setFixedSize(this, 340, 140);
	        this.setContentPane(getGotoPanel());
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onLoad(IDiskoMap map) {
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
			m_gotoPanel.setAutoUpdate(true);
		}
		return m_gotoPanel;
	}
	
	public void getClickPoint() {
		getGotoPanel().getClickPoint();
	}
	
	public void getMovePoint() {
		getGotoPanel().getMovePoint();
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
