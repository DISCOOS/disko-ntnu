package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.util.SpringUtilities;
import org.redcross.sar.util.Utils;
 
public class NumPadPanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;
	
	private JPanel buttonsPanel;
	private JButton oneButton;
	private JButton twoButton;
	private JButton threeButton;
	private JButton fourButton;
	private JButton fiveButton;
	private JButton sixButton;
	private JButton sevenButton;
	private JButton eightButton;
	private JButton nineButton;
	private JButton zeroButton;
	private JButton delButton;
	private JFormattedTextField inputField;
	
	private boolean m_isInputVisible = true;

	/* ========================================================
	 * Constructors
	 * ======================================================== */
		
	public NumPadPanel() {
		this("Tastatur",true,false);
	}
	
	public NumPadPanel(String caption, boolean finish, boolean cancel) {
		// forward
		super(caption,finish,cancel,ButtonSize.SMALL);
		// initialize GUI
		initialize();
	}
	
	/* ========================================================
	 * Public methods
	 * ======================================================== */
	
	public Object getValue() {
		if(getInputField()!=null) {
			return getInputField().getText();
		}
		return null;
	}
	
	public void setValue(Object value) {
		if(getInputField()!=null) {
			getInputField().setText(value!=null ? value.toString() : "");
			String text = getInputField().getText();
			getInputField().setCaretPosition(text!=null ? text.length() : 0);
		}
	}
	
	public int getNumber() {
		if(getInputField()!=null) {
			Object value = getValue();
			if(value!=null) {
				String[] split = value.toString().split("[\\D]+");
				int index = getPrefix().isEmpty() ? 0 : 1;
				return split.length>index && !split[index].equals("") ? Integer.valueOf(split[index]) : -1;
			}
		}
		return -1;
	}
	
	public void setNumber(int value) {
		if(getInputField()!=null) {
			setText(getPrefix(), value, getSuffix());
		}
	}
	
	public String getPrefix() {
		if(getInputField()!=null) {
			Object value = getValue();
			if(value!=null) {
				String[] split = value.toString().split("[\\d]+");
				return split.length>0 ? split[0] : "";
			}
		}
		return "";
	}
	
	public void setPrefix(String value) {
		if(getInputField()!=null) {
			setText(value, getNumber(), getSuffix());
		}
	}
	
	public String getSuffix() {
		if(getInputField()!=null) {
			Object value = getValue();
			if(value!=null) {
				String[] split = value.toString().split("[\\d]+");
				return split.length>0 ? split[split.length-1] : "";
			}
		}
		return "";
	}
	
	public void setSuffix(String value) {
		if(getInputField()!=null) {
			setText(getPrefix(), getNumber(), value);
		}
	}	
	
	public JFormattedTextField getInputField() {
		if(inputField==null) {
			inputField = new JFormattedTextField();
			inputField.setColumns(1);
			inputField.setDocument(new NumericDocument(-1,0,false));
			setInputField(inputField,true);			
			
		}
		return inputField;
	}
	
	public void setInputField(JFormattedTextField field, boolean addToPad) {
		if(field!=null) {
			if(inputField!=null) {
				inputField.removeKeyListener(m_keyListener);
				inputField.getDocument().removeDocumentListener(m_documentListener);
				remove(inputField);
			}
			field.addKeyListener(m_keyListener);
			field.getDocument().addDocumentListener(m_documentListener);			
			if(addToPad) {
				add(field,BorderLayout.NORTH);
				Utils.setFixedHeight(field, 35);
				field.setVisible(m_isInputVisible);
			}
			inputField = field;
		}
	}
	
	public boolean isInputVisible() {
		return m_isInputVisible;
	}
			
	public void setInputVisible(boolean isInputVisible) {
		m_isInputVisible = isInputVisible;
		if(inputField!=null) inputField.setVisible(isInputVisible);
	}
	
	/* ========================================================
	 * Helper methods
	 * ======================================================== */
	
	private void initialize() {
		// prepare
		setNotScrollBars();
		setContainerLayout(new BorderLayout(5,5));
		// set body border
		setContainerBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// add components 
		addToContainer(getInputField(),BorderLayout.NORTH);
		addToContainer(getButtonsPanel(),BorderLayout.CENTER);
	}	
	
	/**
	 * This method initializes Button panel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonsPanel() {
		if(buttonsPanel==null) {
			buttonsPanel = new JPanel(new SpringLayout());		
			buttonsPanel.add(getOneButton());
			buttonsPanel.add(getTwoButton());
			buttonsPanel.add(getThreeButton());
			buttonsPanel.add(getFourButton());
			buttonsPanel.add(getFiveButton());
			buttonsPanel.add(getSixButton());
			buttonsPanel.add(getSevenButton());
			buttonsPanel.add(getEightButton());
			buttonsPanel.add(getNineButton());
			buttonsPanel.add(getZeroButton());
			buttonsPanel.add(getDelButton());
			// capture finish button from header panel
			AbstractButton b = getButton("finish");
			DiskoButtonFactory.setButtonSize(b, ButtonSize.NORMAL);
			b.setIcon(DiskoIconFactory.getIcon("GENERAL.FINISH", 
					DiskoIconFactory.getCatalog(ButtonSize.NORMAL)));
			// layout buttons
			SpringUtilities.makeCompactGrid(buttonsPanel, 4, 3, 0, 0, 0, 0, true, true);
			
		}
		return buttonsPanel;
	}
	
	/**
	 * This method initializes oneButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOneButton() {
		if (oneButton == null) {
			oneButton = DiskoButtonFactory.createButton(buttonSize);
			oneButton.setText("1");
			oneButton.setFont(oneButton.getFont().deriveFont(20));
			oneButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertText("1");
				}
			});
			oneButton.setFocusable(false);			
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
			twoButton = DiskoButtonFactory.createButton(buttonSize);
			twoButton.setText("2");
			twoButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertText("2");
				}
			});
			twoButton.setFocusable(false);			
			
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
			threeButton = DiskoButtonFactory.createButton(buttonSize);
			threeButton.setText("3");
			threeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertText("3");
				}
			});
			threeButton.setFocusable(false);			

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
			fourButton = DiskoButtonFactory.createButton(buttonSize);
			fourButton.setText("4");
			fourButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertText("4");
				}
			});
			fourButton.setFocusable(false);
			
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
			fiveButton = DiskoButtonFactory.createButton(buttonSize);
			twoButton.setToolTipText("5");
			fiveButton.setText("5");
			fiveButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertText("5");
				}
			});
			fiveButton.setFocusable(false);			

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
			sixButton = DiskoButtonFactory.createButton(buttonSize);
			sixButton.setText("6");
			sixButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertText("6");
				}
			});
			sixButton.setFocusable(false);			

		}
		return sixButton;
	}

	/**
	 * This method initializes sevenjButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSevenButton() {
		if (sevenButton == null) {
			sevenButton = DiskoButtonFactory.createButton(buttonSize);
			sevenButton.setToolTipText("");
			sevenButton.setText("7");
			sevenButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertText("7");
				}
			});
			sevenButton.setFocusable(false);			

		}
		return sevenButton;
	}

	/**
	 * This method initializes eightButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getEightButton() {
		if (eightButton == null) {
			eightButton = DiskoButtonFactory.createButton(buttonSize);
			eightButton.setText("8");
			eightButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertText("8");
				}
			});
			eightButton.setFocusable(false);			

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
			nineButton = DiskoButtonFactory.createButton(buttonSize);
			nineButton.setText("9");
			nineButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertText("9");
				}
			});
			nineButton.setFocusable(false);			

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
			zeroButton = DiskoButtonFactory.createButton(buttonSize);
			zeroButton.setText("0");
			zeroButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					insertText("0");
				}
			});
			zeroButton.setFocusable(false);

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
			delButton = DiskoButtonFactory.createButton("GENERAL.DELETE",buttonSize);
			delButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeText();
				}
			});
			delButton.setFocusable(false);
		}
		return delButton;
	}
	
	private void removeText(){
		if (inputField != null && inputField.getText().length() > 0){	
			try {
				if(inputField.getCaretPosition()>0) {
					inputField.getDocument().remove(inputField.getCaretPosition()-1, 1);
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	private void insertText(String s){
		if (getInputField() != null) {
			try {
				getInputField().getDocument().insertString(getInputField().getCaretPosition(), s, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}	
	
	private void setText(String prefix, int number, String suffix) {
		if (getInputField() != null) {
			getInputField().setText(prefix + (number!=-1 ? number : "") + suffix);
			String text = getInputField().getText();
			getInputField().setCaretPosition(text!=null ? text.length() : 0);
		}		
	}
	
	/* ========================================================
	 * Anonymous classes
	 * ======================================================== */
	
	private KeyListener m_keyListener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				finish();
			}
		}				
	};
	
	private DocumentListener m_documentListener = new DocumentListener() {
		public void changedUpdate(DocumentEvent e) { change(); }
		public void insertUpdate(DocumentEvent e) { change(); }
		public void removeUpdate(DocumentEvent e) { change(); }
		
		private void change() {
			if(!isChangeable()) return;
			fireOnWorkChange(getInputField(), getInputField().getText());
		}
	};
}  
