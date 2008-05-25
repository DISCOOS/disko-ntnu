package org.redcross.sar.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class DiskoButtons extends JPanel {

	private static final long serialVersionUID = 1L;

	public static final int LEFT = FlowLayout.LEFT;
	public static final int CENTER = FlowLayout.CENTER;
	public static final int RIGHT = FlowLayout.RIGHT;
	public static final int LEADING = FlowLayout.LEADING;
	public static final int TRAILING = FlowLayout.TRAILING;
	
	private int m_alignment = FlowLayout.LEFT;
	
	private List<JComponent> m_items = null;
	private Map<String, AbstractButton> m_commands = null;
		
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
		this.setOpaque(true);
		m_items = new ArrayList<JComponent>();
		m_commands = new HashMap<String, AbstractButton>();
	}	
	
	public AbstractButton insertButton(String before, AbstractButton button, String command) {
		AbstractButton prefix = m_commands.get(before);
		// forward
		return (AbstractButton)insert(prefix,addButton(button, command));
	}
	
	public AbstractButton insertButton(String before, String command, String caption) {
		AbstractButton prefix = m_commands.get(before);
		// forward
		return (AbstractButton)insert(prefix,addButton(command, caption));
	}
	
	private JComponent insert(JComponent prefix, JComponent item) {
		if(item!=null) {
			// clone
			List<JComponent> list = new ArrayList<JComponent>(m_items.size());
			// remove all buttons
			this.removeAll();
			/*/ set layout again
			FlowLayout fl = new FlowLayout();
			fl.setHgap(0);
			fl.setVgap(0);
			fl.setAlignment(m_alignment);
			this.setLayout(fl);*/
			// add before all?
			if(prefix==null)
				this.add(item);
			// loop over all buttons
			for(JComponent it:m_items) {
				if(it==prefix) {
					list.add(item);
					this.add(item);
				}
				if(it!=item) {
					list.add(it);
					this.add(it);
				}
			}
			// replace
			m_items = list;
		}
		return null;
	}
	
	public AbstractButton addButton(AbstractButton button, String command) {
		// exists?
		if(!m_commands.containsKey(command)) {
			button.setActionCommand(command);
			this.add(button);
			m_items.add(button);
			m_commands.put(command, button);
			return button;
		}
		return null;
	}
	
	public AbstractButton addButton(String command, String caption) {
		// exists?
		if(!m_commands.containsKey(command)) {
			JButton button = new JButton(caption);
			button.setActionCommand(command);
			this.add(button);
			m_items.add(button);
			m_commands.put(command, button);
			return button;
		}
		return null;
	}
	
	public void removeButton(String command) {
		AbstractButton button = m_commands.get(command);
		this.remove(button);
		m_items.remove(button);
		m_commands.remove(command);
	}
	
	
	public boolean addItem(JComponent item) {
		if(!m_items.contains(item)) {
			this.add(item);
			return m_items.add(item);		
		}
		return false;
	}
	
	public boolean insertItem(String before, JComponent item) {
		AbstractButton prefix = m_commands.get(before);
		// forward
		return insert(prefix,addItem(item) ? item : null)!=null;
	}
	
	public boolean insertItem(JComponent before, JComponent item) {
		// forward
		return insert(before,addItem(item) ? item : null)!=null;
	}
	
	public boolean removeItem(JComponent item) {
		if(m_items.contains(item)) {
			this.remove(item);
			return m_items.remove(item);			
		}
		return false;		
	}
	
	public boolean containsButton(String command) {
		return m_commands.containsKey(command);
	}			
	
	public AbstractButton getButton(String command) {
		return m_commands.get(command);
	}
	
	public boolean isButtonVisible(String command) {
		return m_commands.get(command).isVisible();
	}
	
	public void setButtonVisible(String command, boolean isVisible) {
		m_commands.get(command).setVisible(isVisible);
	}
	
	public boolean isButtonEnabled(String command) {
		return m_commands.get(command).isEnabled();
	}
	
	public void setButtonEnabled(String command, boolean isEnabled) {
		m_commands.get(command).setEnabled(isEnabled);
	}

	public void addActionListener(ActionListener listener) {
		for(AbstractButton b: m_commands.values()) {
			b.addActionListener(listener);
		}
	}
	
	public void removeActionListener(ActionListener listener) {
		for(AbstractButton b: m_commands.values()) {
			b.removeActionListener(listener);
		}
	}
	
	public boolean doAction(String command) {
		if(m_commands.containsKey(command)) {
			m_commands.get(command).doClick();
			return true;
		}
		return false;
	}
	
	public int getTotalButtonWidth() {
		int w = 0;
		for(JComponent it: m_items) {
			if(it.isVisible())
				w += (it.getWidth()>0 ? it.getWidth() : it.getPreferredSize().width);
		}
		return w;			
	}
	
	public int getMaxButtonHeigth() {
		int max = 0;
		for(JComponent it: m_items) {
			if(it.isVisible())
				max = Math.max(max, (it.getHeight()>0 ? it.getHeight() : it.getPreferredSize().width));
		}
		return max;			
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
