/**
 * 
 */
package org.redcross.sar.gui.map;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.map.IDiskoMap;

/**
 * @author kennetgu
 *
 */
public class TocDialog extends DiskoDialog  {

	private static final long serialVersionUID = 1L;
	
	private TocPanel m_tocPanel = null;
	
	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public TocDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// initialize GUI
		initialize();
		
	}

	private void initialize() {
		try {
			// prepare dialog
			Utils.setFixedSize(this, 250, 550);
	        this.setContentPane(getTocPanel());
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onLoad(IDiskoMap map) {
		getTocPanel().load(map);
	}
	
	public void reload() {
		getTocPanel().reload();
	}
	
	/**
	 * This method initializes m_tocPanel	
	 * 	
	 * @return org.redcross.sar.gui.TocPanel
	 */
	public TocPanel getTocPanel() {
		if (m_tocPanel == null) {
			try {
				// create panels
				m_tocPanel = new TocPanel();
				m_tocPanel.addActionListener(new ActionListener() {

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
		return m_tocPanel;
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
