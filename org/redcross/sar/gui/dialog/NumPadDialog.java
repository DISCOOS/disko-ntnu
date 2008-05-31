package org.redcross.sar.gui.dialog;

import java.awt.Frame;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.text.BadLocationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;

public class NumPadDialog extends JDialog {

	private BasePanel buttonPanel = null;  //  @jve:decl-index=0:visual-constraint="328,118"
	private static final long serialVersionUID = 1L;
	private JButton oneButton = null;
	private JButton twoButton = null;
	private JButton threeButton = null;
	private JButton fourButton = null;
	private JButton fiveButton = null;
	private JButton sixButton = null;
	private JButton sevenjButton = null;
	private JButton eightButton = null;
	private JButton nineButton = null;
	private JButton zeroButton = null;
	private JButton delButton = null;
	private JButton okButton = null;	
	private JFormattedTextField jtf = null;
	private Dimension size = new Dimension(32,32);

	public NumPadDialog(Frame owner) {
		// forward
		super(owner);
		// initialize gui
		initialize();
	}
	
	private void initialize(){
		this.setUndecorated(true);		
        this.setContentPane(getButtonPanel());
		Utils.setFixedSize(this,180, 250);
        this.pack();
	}
	
	public void setTextField(JFormattedTextField jtf){
		this.jtf = jtf;
	}
	
	private void deleteTextFieldValue(){
		if (jtf != null && jtf.getText().length() > 0){	
			try {
				jtf.getDocument().remove(jtf.getCaretPosition()-1, 1);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	private void addTextFieldValue(String s){
		if (jtf != null) {
			try {
				jtf.getDocument().insertString(jtf.getCaretPosition(), s, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This method initializes componentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(4);
			gridLayout.setColumns(3);
			buttonPanel = new BasePanel("Numerisk tastatur");
			JPanel panel = (JPanel)buttonPanel.getBodyComponent();
			panel.setLayout(gridLayout);		
			panel.setSize(new Dimension(215, 214));
			panel.add(getOneButton(), null);
			panel.add(getTwoButton(), null);
			panel.add(getThreeButton(), null);
			panel.add(getFourButton(), null);
			panel.add(getFiveButton(), null);
			panel.add(getSixButton(), null);
			panel.add(getSevenButton(), null);
			panel.add(getEightButton(), null);
			panel.add(getNineButton(), null);
			panel.add(getZeroButton(), null);
			panel.add(getDelButton(), null);
			panel.add(getOkButton(), null);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes oneButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOneButton() {
		if (oneButton == null) {
			oneButton = new JButton();
			oneButton.setPreferredSize(size);
			oneButton.setText("1");
			oneButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					addTextFieldValue("1");
				}
			});
			
			oneButton.doClick();
			
		}
		return oneButton;
	}

	/**
	 * This method initializes twoButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getTwoButton() {
		if (twoButton == null) {
			twoButton = new JButton();
			twoButton.setPreferredSize(size);
			twoButton.setText("2");
			twoButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					addTextFieldValue("2");
				}
			});
		}
		return twoButton;
	}

	/**
	 * This method initializes threeButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getThreeButton() {
		if (threeButton == null) {
			threeButton = new JButton();
			threeButton.setPreferredSize(size);
			threeButton.setText("3");
			threeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					addTextFieldValue("3");
				}
			});
		}
		return threeButton;
	}

	/**
	 * This method initializes fourButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getFourButton() {
		if (fourButton == null) {
			fourButton = new JButton();
			fourButton.setPreferredSize(size);
			fourButton.setText("4");
			fourButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					addTextFieldValue("4");
				}
			});
		}
		return fourButton;
	}

	/**
	 * This method initializes fiveButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getFiveButton() {
		if (fiveButton == null) {
			fiveButton = new JButton();
			fiveButton.setPreferredSize(size);
			twoButton.setToolTipText("5");
			fiveButton.setText("5");
			fiveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					addTextFieldValue("5");
				}
			});
		}
		return fiveButton;
	}

	/**
	 * This method initializes sixButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSixButton() {
		if (sixButton == null) {
			sixButton = new JButton();
			sixButton.setPreferredSize(size);
			sixButton.setText("6");
			sixButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					addTextFieldValue("6");
				}
			});
		}
		return sixButton;
	}

	/**
	 * This method initializes sevenjButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSevenButton() {
		if (sevenjButton == null) {
			sevenjButton = new JButton();
			sevenjButton.setPreferredSize(size);
			sevenjButton.setToolTipText("");
			sevenjButton.setText("7");
			sevenjButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					addTextFieldValue("7");
				}
			});
		}
		return sevenjButton;
	}

	/**
	 * This method initializes eightButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getEightButton() {
		if (eightButton == null) {
			eightButton = new JButton();
			eightButton.setPreferredSize(size);
			eightButton.setText("8");
			eightButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					addTextFieldValue("8");
				}
			});
		}
		return eightButton;
	}

	/**
	 * This method initializes nineButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getNineButton() {
		if (nineButton == null) {
			nineButton = new JButton();
			nineButton.setPreferredSize(size);
			nineButton.setText("9");
			nineButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					addTextFieldValue("9");
				}
			});
		}
		return nineButton;
	}

	/**
	 * This method initializes zeroButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getZeroButton() {
		if (zeroButton == null) {
			zeroButton = new JButton();
			zeroButton.setPreferredSize(size);
			zeroButton.setText("0");
			zeroButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					addTextFieldValue("0");
				}
			});
		}
		return zeroButton;
	}

	/**
	 * This method initializes delButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDelButton() {
		if (delButton == null) {
			delButton = new JButton();
			delButton.setPreferredSize(size);
			delButton.setText("Del");
			delButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					deleteTextFieldValue();
				}
			});
		}
		return delButton;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	public JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setPreferredSize(size);
			okButton.setText("OK");
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return okButton;
	}
	


}  //  @jve:decl-index=0:visual-constraint="43,48"
