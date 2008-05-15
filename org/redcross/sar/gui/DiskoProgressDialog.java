/**
 * 
 */
package org.redcross.sar.gui;

import javax.swing.JFrame;
import javax.swing.Timer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author kennetgu
 *
 */
public class DiskoProgressDialog extends DiskoDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private boolean buffer = false;
	private DelayAction delay = new DelayAction();
	

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
	
	
	@Override
	public void setVisible(boolean isVisible) {
		if(isVisible()!=isVisible) {
			super.setVisible(buffer);
			buffer = isVisible;
			delay.start();
		}
		else delay.stop();

	}
	
	public void actionPerformed(ActionEvent e) {
		super.setVisible(buffer);
	}		
	
	class DelayAction extends Timer{
		
		private static final long serialVersionUID = 1L;
		
		DelayAction() {
			
			// forward
			super(0, DiskoProgressDialog.this);
			
			// one shot and start at once
			this.setRepeats(false);
			this.setInitialDelay(0);
			
		}

	}	
	

}
