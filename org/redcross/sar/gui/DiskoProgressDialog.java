/**
 * 
 */
package org.redcross.sar.gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.redcross.sar.thread.DiskoProgressEvent;
import org.redcross.sar.thread.DiskoProgressMonitor;
import org.redcross.sar.thread.IDiskoProgress;
import org.redcross.sar.thread.IDiskoProgressListener;
import org.redcross.sar.thread.DiskoProgressEvent.DiskoProgressEventType;

/**
 * @author kennetgu
 *
 */
public class DiskoProgressDialog extends DiskoDialog implements IDiskoProgressListener {

	private static final long serialVersionUID = 1L;

	private JPanel m_contentPanel = null;

	private JLabel m_labelMessage = null;

	private JProgressBar m_progressBar = null;

	private JPanel m_buttonPanel = null;

	private JButton m_button = null;
	
	private IDiskoProgress m_progress = null;
	private boolean m_allow = false;
	private boolean m_cancel = false;	

	/**
	 * 
	 */
	public DiskoProgressDialog(JFrame frame, IDiskoProgress progress, boolean allow, boolean cancel) {
		super(frame);		
		// save
		m_allow = allow;
		m_cancel = cancel;
		m_progress = progress;
		initialize(frame);
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(JFrame frame) {
		setSize(150, 50);
		setTitle("");
		setUndecorated(true);
		setContentPane(getContentPanel());
		//	Set the new frame location
		setLocationRelativeTo(frame);
		// add listener
		m_progress.addListener(this);
	}

	/**
	 * This method initializes m_contentPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getContentPanel() {
		if (m_contentPanel == null) {
			m_labelMessage = new JLabel();
			m_labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
			m_labelMessage.setVerticalAlignment(SwingConstants.CENTER);
			m_contentPanel = new JPanel();
			m_contentPanel.setLayout(new BorderLayout());
			m_contentPanel.add(m_labelMessage, BorderLayout.NORTH);
			m_contentPanel.add(getProgressBar(), BorderLayout.CENTER);
			//m_contentPanel.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return m_contentPanel;
	}

	/**
	 * This method initializes m_progressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getProgressBar() {
		if (m_progressBar == null) {
			m_progressBar = new JProgressBar();
		}
		return m_progressBar;
	}

	/**
	 * This method initializes m_buttonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (m_buttonPanel == null) {
			m_buttonPanel = new JPanel();
			m_buttonPanel.setLayout(new FlowLayout());
			m_buttonPanel.setPreferredSize(new Dimension(150,50));
			m_buttonPanel.add(getButton(), BorderLayout.CENTER);
		}
		return m_buttonPanel;
	}

	/**
	 * This method initializes m_cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getButton() {
		if (m_button == null) {
			m_button = new JButton();
			if(m_cancel)
				m_button.setText("Avbryt");
			else
				m_button.setText("Lukk");
			m_button.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					// allow to hide me?
					if(m_allow) {
						// hide me
						setVisible(false);
					}					
				}
				
			});
		}
		return m_button;
	}

	public void changeProgress(DiskoProgressEvent e) {
		// get progress monitor
		if(e.getSource() instanceof DiskoProgressMonitor) {
			// get monitor
			DiskoProgressMonitor monitor = (DiskoProgressMonitor)e.getSource();
			// get type
			DiskoProgressEventType type = e.getType();
			// dispatch
			if(type == DiskoProgressEventType.EVENT_START || 
					type == DiskoProgressEventType.EVENT_UPDATE) {
				// update progress bar values
				if(monitor.getIntermediate()) {
					getProgressBar().setMinimum(monitor.getMinimum());
					getProgressBar().setMaximum(monitor.getMaximum());					
				}
				getProgressBar().setIndeterminate(monitor.getIntermediate());
				// update message and value
				if(monitor.getNote()!=null) {
					getProgressBar().setString(monitor.getNote());
				}
				getProgressBar().setValue(monitor.getProgress());
				m_labelMessage.setText(monitor.getNote());
				// show me?
				if(!isVisible())
					setVisible(true);
				else
					repaint();
			}
			else if(type == DiskoProgressEventType.EVENT_CANCEL ||
					type == DiskoProgressEventType.EVENT_FINISH ) {
				// hide me
				setVisible(false);
			}		
		}
	}
}
