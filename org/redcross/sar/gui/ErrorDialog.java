package org.redcross.sar.gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Frame;
import javax.swing.JDialog;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

public class ErrorDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JPanel southPanel = null;

	private JButton okButton = null;

	private JLabel msgLabel = null;

	private JPanel labelPanel = null;

	private JScrollPane testAreaScrollPane = null;

	private JTextArea detailsTextArea = null;

	private JToggleButton detailsToggleButton = null;
	
	//  @jve:decl-index=0:

	public ErrorDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setTitle("Feilmelding");
		this.setContentPane(getJContentPane());
	}
	
	public void showError(String msg, String details) {
		msgLabel.setText(msg);
		this.pack();
		detailsTextArea.setText(details);
		this.setVisible(true);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			msgLabel = new JLabel();
			msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
			msgLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
			msgLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getSouthPanel(), BorderLayout.SOUTH);
			jContentPane.add(getLabelPanel(), BorderLayout.NORTH);
			jContentPane.add(getTestAreaScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes southPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSouthPanel() {
		if (southPanel == null) {
			try {
				southPanel = new JPanel();
				southPanel.setLayout(new FlowLayout());
				southPanel.add(getOkButton(), null);
				southPanel.add(getDetailsToggleButton(), null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return southPanel;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			try {
				okButton = new JButton();
				okButton.setText("OK");
				okButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						setVisible(false);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return okButton;
	}

	/**
	 * This method initializes labelPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getLabelPanel() {
		if (labelPanel == null) {
			try {
				FlowLayout flowLayout = new FlowLayout();
				flowLayout.setHgap(10);
				flowLayout.setVgap(10);
				labelPanel = new JPanel();
				labelPanel.setLayout(flowLayout);
				labelPanel.add(msgLabel, null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return labelPanel;
	}

	/**
	 * This method initializes testAreaScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getTestAreaScrollPane() {
		if (testAreaScrollPane == null) {
			try {
				testAreaScrollPane = new JScrollPane();
				testAreaScrollPane.setVisible(false);
				testAreaScrollPane.setViewportView(getDetailsTextArea());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return testAreaScrollPane;
	}

	/**
	 * This method initializes detailsTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getDetailsTextArea() {
		if (detailsTextArea == null) {
			try {
				detailsTextArea = new JTextArea();
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return detailsTextArea;
	}

	/**
	 * This method initializes detailsToggleButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getDetailsToggleButton() {
		if (detailsToggleButton == null) {
			try {
				detailsToggleButton = new JToggleButton();
				detailsToggleButton.setText("Vis detaljer");
				detailsToggleButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						if (detailsToggleButton.isSelected()) {
							detailsToggleButton.setText("Gjem detaljer");
							testAreaScrollPane.setVisible(true);
							pack();
						}
						else {
							detailsToggleButton.setText("Vis detaljer");
							testAreaScrollPane.setVisible(false);
							pack();
						}
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return detailsToggleButton;
	}

}
