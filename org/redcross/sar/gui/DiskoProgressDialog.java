/**
 * 
 */
package org.redcross.sar.gui;

import javax.swing.JFrame;

import org.redcross.sar.app.Utils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author kennetgu
 *
 */
public class DiskoProgressDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;

	private DiskoProgressPanel m_progressPanel = null;

	/**
	 * 
	 */
	public DiskoProgressDialog(JFrame frame, boolean cancel) {
		// forward
		super(frame);		
		// initialize GUI
		initialize(frame,cancel);
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(JFrame frame, boolean cancel) {
		// prepare
		this.setTitle("");
		this.setUndecorated(true);
		this.setResizable(false);
		this.setContentPane(getProgressPanel());
		this.setPreferredSize(new Dimension(100,45));
		this.setLocationRelativeTo(frame.getLayeredPane(), DiskoDialog.POS_CENTER, false, true);
		// set cancel button status
		getProgressPanel().setButtonVisible(cancel);
		// apply layout
		this.pack();		
	}

	/**
	 * This method initializes DiskoProgressPanel
	 * 
	 * @return DiskoProgressPanel
	 */
	public DiskoProgressPanel getProgressPanel() {
		if (m_progressPanel == null) {
			m_progressPanel = new DiskoProgressPanel();
			m_progressPanel.setOpaque(false);
			m_progressPanel.getCancelButton().addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// forward
					setVisible(false);
				}
				
			});
		}
		return m_progressPanel;
	}
	
	

}
