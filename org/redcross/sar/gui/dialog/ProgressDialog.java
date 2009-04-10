/**
 *
 */
package org.redcross.sar.gui.dialog;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.redcross.sar.gui.DiskoProgressPanel;
import org.redcross.sar.gui.DiskoProgressPanel.ProgressStyleType;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author kennetgu
 *
 */
public class ProgressDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private DiskoProgressPanel m_progressPanel;

	public ProgressDialog(Window window, boolean cancel, ProgressStyleType style) {
		// forward
		super(window);
		// initialize GUI
		initialize(window,cancel);
		// set style
		getProgressPanel().setStyle(style);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize(Window window, boolean cancel) {
		// prepare
		this.setTitle("");
		this.setUndecorated(true);
		this.setResizable(false);
		this.setFocusable(false);
		this.setFocusableWindowState(false);
		this.setContentPane(getProgressPanel());
		this.setSnapToLocation(getLocationComponent(window), DefaultDialog.POS_CENTER, 0, true, false);

		// set cancel button status
		getProgressPanel().setButtonVisible(cancel);

		// apply layout
		this.pack();
	}

	private Component getLocationComponent(Window window) {
		if(window instanceof JFrame)
			return ((JFrame)window).getLayeredPane();
		else if(window instanceof JDialog)
			return ((JDialog)window).getLayeredPane();
		else
			return window;
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

	public ProgressStyleType getStyle() {
		return getProgressPanel().getStyle();
	}

	public void setStyle(ProgressStyleType style) {
		getProgressPanel().setStyle(style);
	}

}
