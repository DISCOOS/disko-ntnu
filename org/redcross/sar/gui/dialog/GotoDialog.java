/**
 * 
 */
package org.redcross.sar.gui.dialog;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.panel.GotoPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;

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
		
		addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowGainedFocus(WindowEvent e) {
				// activate selection tool
				Utils.getApp().invoke(DiskoToolType.SELECT_TOOL,false);
			}

			@Override
			public void windowLostFocus(WindowEvent e) {}
			
		});

	}

	private void initialize() {
		try {
			// prepare dialog
			Utils.setFixedSize(this, 280, 140);
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
			m_gotoPanel.setButtonVisible("cancel", true);
		}
		return m_gotoPanel;
	}
	
	public void getPoint() {
		getGotoPanel().setClickPoint();		
	}
	
	public void getClickPoint() {
		getGotoPanel().setClickPoint();
	}
	
	public void getMovePoint() {
		getGotoPanel().setMovePoint();
	}
	
	public void getCenterPoint() {
		getGotoPanel().setCenterPoint();
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
