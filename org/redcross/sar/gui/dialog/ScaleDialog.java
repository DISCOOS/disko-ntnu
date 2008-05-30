package org.redcross.sar.gui.dialog;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.panel.ScalePanel;
import org.redcross.sar.map.IDiskoMap;

public class ScaleDialog extends DefaultDialog {
	
	private static final long serialVersionUID = 1L;

	private ScalePanel m_scalePanel = null;
	
	public ScaleDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// initialize GUI
		initialize();
		
	}

	private void initialize() {
		try {
			// prepare dialog
			Utils.setFixedSize(this, 200, 500);
	        this.setContentPane(getScalePanel());
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onLoad(IDiskoMap map) {
		getScalePanel().load(map);
	}
	
	public void reload() {
		getScalePanel().update();
	}
	
	/**
	 * This method initializes m_scalePanel	
	 * 	
	 * @return org.redcross.sar.gui.ScalePanel
	 */
	public ScalePanel getScalePanel() {
		if (m_scalePanel == null) {
			try {
				// create panels
				m_scalePanel = new ScalePanel();
				m_scalePanel.setScrollBarPolicies(
						ScalePanel.VERTICAL_SCROLLBAR_NEVER, 
						ScalePanel.HORIZONTAL_SCROLLBAR_NEVER);
				m_scalePanel.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						// hide?
						if("finish".equalsIgnoreCase(cmd) 
						|| "cancel".equalsIgnoreCase(cmd))
							setVisible(false);						
					}
					
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_scalePanel;
	}

}
