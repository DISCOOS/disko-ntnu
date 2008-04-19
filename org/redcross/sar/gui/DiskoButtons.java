package org.redcross.sar.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;

public class DiskoButtons extends JPanel {

	private static final long serialVersionUID = 1L;

	public static final int LEFT = FlowLayout.LEFT;
	public static final int CENTER = FlowLayout.CENTER;
	public static final int RIGHT = FlowLayout.RIGHT;
	public static final int LEADING = FlowLayout.LEADING;
	public static final int TRAILING = FlowLayout.TRAILING;
	
	private int m_alignment = FlowLayout.LEFT;
	
	private Map<String, AbstractButton> m_buttons = null;
	
	public DiskoButtons() {
		// initialize GUI
		initialize();
	}
	
	public DiskoButtons(int alignment) {
		// prepare
		m_alignment = alignment;
		// initialize GUI
		initialize();
	}
	
	/**
	 * This method initializes the panel
	 * 	
	 */
	private void initialize() {
		FlowLayout fl = new FlowLayout();
		fl.setHgap(0);
		fl.setVgap(0);
		fl.setAlignment(m_alignment);
		this.setLayout(fl);
		this.setBorder(null);
		m_buttons = new HashMap<String, AbstractButton>();
	}	
	
	public AbstractButton addButton(AbstractButton button, String command) {
		// exists?
		if(!m_buttons.containsKey(command)) {
			button.setActionCommand(command);
			this.add(button);
			m_buttons.put(command, button);
			return button;
		}
		return null;
	}
	
	public AbstractButton addButton(String command, String caption) {
		// exists?
		if(!m_buttons.containsKey(command)) {
			JButton button = new JButton(caption);
			button.setActionCommand(command);
			this.add(button);
			m_buttons.put(command, button);
			return button;
		}
		return null;
	}
	
	public void removeButton(String command) {
		AbstractButton button = m_buttons.get(command);
		this.remove(button);
		m_buttons.remove(command);
	}
	
	public boolean containsButton(String command) {
		return m_buttons.containsKey(command);
	}			
	
	public AbstractButton getButton(String command) {
		return m_buttons.get(command);
	}
	
	public boolean isButtonVisible(String command) {
		return m_buttons.get(command).isVisible();
	}
	
	public void setButtonVisible(String command, boolean isVisible) {
		m_buttons.get(command).setVisible(isVisible);
	}
	
	public boolean isButtonEnabled(String command) {
		return m_buttons.get(command).isEnabled();
	}
	
	public void setButtonEnabled(String command, boolean isEnabled) {
		m_buttons.get(command).setEnabled(isEnabled);
	}
	
	public void addActionListener(ActionListener listener) {
		for(AbstractButton b: m_buttons.values()) {
			b.addActionListener(listener);
		}
	}
	
	public void removeActionListener(ActionListener listener) {
		for(AbstractButton b: m_buttons.values()) {
			b.removeActionListener(listener);
		}
	}
	
	public boolean doAction(String command) {
		if(m_buttons.containsKey(command)) {
			m_buttons.get(command).doClick();
			return true;
		}
		return false;
	}
		
}  //  @jve:decl-index=0:visual-constraint="10,10"
