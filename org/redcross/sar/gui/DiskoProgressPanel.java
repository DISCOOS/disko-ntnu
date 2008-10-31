/**
 *
 */
package org.redcross.sar.gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.JButton;

import org.redcross.sar.gui.dialog.DefaultDialog;

/**
 * @author kennetgu
 *
 */
public class DiskoProgressPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JLabel m_labelMessage = null;
	private JProgressBar m_progressBar = null;
	private JPanel m_buttonPanel = null;
	private JButton m_cancelButton = null;

	public DiskoProgressPanel() {
		// initialize GUI
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		// create the label
		m_labelMessage = new JLabel();
		m_labelMessage.setOpaque(false);
		m_labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
		m_labelMessage.setVerticalAlignment(SwingConstants.CENTER);
		m_labelMessage.setFocusable(false);
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		this.add(m_labelMessage, BorderLayout.NORTH);
		this.add(getProgressBar(), BorderLayout.CENTER);
		this.add(getButtonPanel(), BorderLayout.SOUTH);
		this.setFocusable(false);

	}

	/**
	 * This method initializes m_progressBar
	 *
	 * @return javax.swing.JProgressBar
	 */
	private JProgressBar getProgressBar() {
		if (m_progressBar == null) {
			m_progressBar = new JProgressBar();
			m_progressBar.setPreferredSize(new Dimension(100,30));
			m_progressBar.setFocusable(false);
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
			m_buttonPanel.add(getCancelButton(), BorderLayout.CENTER);
			m_buttonPanel.setVisible(false);
			m_buttonPanel.setFocusable(false);
		}
		return m_buttonPanel;
	}

	public void setButtonVisible(boolean isVisible) {
		getCancelButton().setVisible(isVisible);
	}

	/**
	 * This method initializes m_cancelButton
	 *
	 * @return javax.swing.JButton
	 */
	public JButton getCancelButton() {
		if (m_cancelButton == null) {
			m_cancelButton = new JButton();
		}
		return m_cancelButton;
	}

	public void setLimits(int min, int max, boolean intermediate) {
		if(intermediate) {
			getProgressBar().setMinimum(min);
			getProgressBar().setMaximum(max);
		}
		getProgressBar().setIndeterminate(intermediate);
	}

	public void setProgress(int step,String text,String message) {
		// update progress
		getProgressBar().setValue(step);
		// update progress bar text
		if(text!=null) {
			getProgressBar().setString(text);
		}
		// update progressbar message
		m_labelMessage.setText(message);
		Graphics g = m_labelMessage.getGraphics();
		Component c = SwingUtilities.getRoot(this);
		if(g!=null && message!=null) {
			int w = Math.max(g.getFontMetrics().stringWidth(message),100);
			c.setSize(w+10,SwingUtilities.getRoot(this).getHeight());
		}
		else {
			c.setSize(75,SwingUtilities.getRoot(this).getHeight());

		}
		// update position
		if(c instanceof DefaultDialog)
			((DefaultDialog)c).snapTo();

	}

}
