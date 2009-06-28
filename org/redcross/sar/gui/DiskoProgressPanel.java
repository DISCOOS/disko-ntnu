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
import org.redcross.sar.gui.factory.DiskoAnimationFactory;

/**
 * @author kennetgu
 *
 */
public class DiskoProgressPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public enum ProgressStyleType {
		BAR_STYLE,
		ICON_STYLE
	}

	private ProgressStyleType m_style;

	private JLabel m_labelMessage;
	private JLabel m_progressIcon;
	private JProgressBar m_progressBar;
	private JPanel m_buttonPanel;
	private JButton m_cancelButton;

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
		// prepare
		m_labelMessage = new JLabel();
		m_labelMessage.setOpaque(false);
		m_labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
		m_labelMessage.setVerticalAlignment(SwingConstants.CENTER);
		m_labelMessage.setFocusable(false);
		m_progressIcon = new JLabel();
		m_progressIcon.setOpaque(false);
		m_progressIcon.setHorizontalAlignment(SwingConstants.CENTER);
		m_progressIcon.setVerticalAlignment(SwingConstants.CENTER);
		m_progressIcon.setFocusable(false);
		m_progressIcon.setIcon(DiskoAnimationFactory.getIcon("ANIMATE.CLOCK"));
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		this.setFocusable(false);
		setStyle(ProgressStyleType.BAR_STYLE);

	}

	/**
	 * This method initializes m_progressBar
	 *
	 * @return javax.swing.JProgressBar
	 */
	public JProgressBar getProgressBar() {
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

	public void setProgress(int step, String text, String message) {
		// update progress
		getProgressBar().setValue(step);
		// update progress bar text
		if(text!=null) {
			getProgressBar().setString(text);
		}
		// update progressbar message
		m_labelMessage.setText(message);
		Graphics g = m_labelMessage.getGraphics();
		if(g!=null && message!=null) {
			int w = Math.max(g.getFontMetrics().stringWidth(message),100);
			setParentSize(w+10,SwingUtilities.getRoot(this).getHeight());
		}
		else {
			setParentSize(75,SwingUtilities.getRoot(this).getHeight());
		}
	}

	private void setParentSize(int width, int height) {
		Component c = SwingUtilities.getRoot(this);
		if(c!=null) {
			c.setSize(width, height);
			// update position
			if(c instanceof DefaultDialog)
				((DefaultDialog)c).snapTo();
		}
	}

	public ProgressStyleType getStyle() {
		return m_style;
	}

	public void setStyle(ProgressStyleType style) {
		this.removeAll();
		Component c = SwingUtilities.getRoot(this);
		int w = c!=null ? c.getWidth() : 100;
		w = Math.min(100, w);
		if(ProgressStyleType.BAR_STYLE.equals(style)) {
			this.add(m_labelMessage, BorderLayout.NORTH);
			this.add(getProgressBar(), BorderLayout.CENTER);
			this.add(getButtonPanel(), BorderLayout.SOUTH);
			setParentSize(w, 45);
		}
		else {
			this.add(m_labelMessage, BorderLayout.NORTH);
			this.add(m_progressIcon, BorderLayout.CENTER);
			this.add(getButtonPanel(), BorderLayout.SOUTH);
			setParentSize(w, 65);
		}
		m_style = style;
	}

}
