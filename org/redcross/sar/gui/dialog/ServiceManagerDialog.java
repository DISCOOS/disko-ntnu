/**
 *
 */
package org.redcross.sar.gui.dialog;

import java.awt.Frame;

import org.redcross.sar.gui.panel.ServiceManagerPanel;


/**
 * @author kennetgu
 *
 */
public class ServiceManagerDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private ServiceManagerPanel m_serviceManagerPanel = null;

	/**
	 * Constructor
	 *
	 * @param owner
	 */
	public ServiceManagerDialog(Frame owner) {

		// forward
		super(owner);

		// initialize GUI
		initialize();

	}

	private void initialize() {
		try {
			// prepare dialog			
			this.setSize(350, 315);
			this.setResizable(false);
	        this.setContentPane(getServiceManagerPanel());
	        this.setModal(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method initializes m_gotoPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private ServiceManagerPanel getServiceManagerPanel() {
		if (m_serviceManagerPanel == null) {
			m_serviceManagerPanel = new ServiceManagerPanel();

		}
		return m_serviceManagerPanel;
	}

	public void manage() {
		// this will block because dialog is modal
		setVisible(true);
	}
	

}  //  @jve:decl-index=0:visual-constraint="23,0"
