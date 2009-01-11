/**
 *
 */
package org.redcross.sar.gui.dialog;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.redcross.sar.gui.panel.SnapPanel;

/**
 * @author kennetgu
 *
 */
public class SnapDialog extends DefaultDialog  {

	private static final long serialVersionUID = 1L;

	private SnapPanel m_snapPanel;

	/**
	 * Constructor
	 *
	 * @param owner
	 */
	public SnapDialog(Frame owner) {

		// forward
		super(owner);

		// initialize GUI
		initialize();

	}

	private void initialize() {
		try {
			// prepare dialog
	        this.setContentPane(getSnapPanel());
			// set translucency behavior
			setTrancluentOn(DefaultDialog.TRANSLUCENT_ONMOUSE);
			// pack to content size
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method initializes m_snapPanel
	 *
	 * @return org.redcross.sar.gui.SnapPanel
	 */
	public SnapPanel getSnapPanel() {
		if (m_snapPanel == null) {
			try {
				// create panels
				m_snapPanel = new SnapPanel();
				m_snapPanel.addActionListener(new ActionListener() {

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
		return m_snapPanel;
	}

}  //  @jve:decl-index=0:visual-constraint="23,0"
