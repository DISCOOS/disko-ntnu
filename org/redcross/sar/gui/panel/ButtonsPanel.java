package org.redcross.sar.gui.panel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

public class ButtonsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public static final int LEFT = SwingUtilities.LEFT;
	public static final int RIGHT = SwingUtilities.RIGHT;
	
	private int m_alignment = LEFT;
	private Component m_glue = Box.createHorizontalGlue();
	
	private ButtonSize m_buttonSize = ButtonSize.SMALL;
	
	private List<JComponent> m_items = new ArrayList<JComponent>();
	
	final private Map<String, AbstractButton> m_commands = new HashMap<String, AbstractButton>();
	final private EventListenerList m_listeners = new EventListenerList();;
		
	/* =======================================================
	 * Constructors
	 * ======================================================= */
	
	public ButtonsPanel() {
		// initialize GUI
		initialize();
	}
	
	public ButtonsPanel(int alignment, ButtonSize buttonSize) {
		// prepare
		m_alignment = alignment;
		m_buttonSize = buttonSize;
		// initialize GUI
		initialize();
	}
	
	/* =======================================================
	 * Private methods
	 * ======================================================= */
	
	/**
	 * This method initializes the panel
	 * 	
	 */
	private void initialize() {
		this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		this.setBorder(null);
		this.setOpaque(true);
	}	
	
	private JComponent insert(JComponent prefix, JComponent item) {
		if(item!=null) {
			// clone
			List<JComponent> list = new ArrayList<JComponent>(m_items.size());
			// remove all buttons
			this.removeAll();
			// add glue?
			if(m_alignment == SwingConstants.RIGHT) 
				this.add(m_glue);
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
			// add glue?
			if(m_alignment == SwingConstants.LEFT) 
				this.add(m_glue);
			
			// replace
			m_items = list;
			// finished
			return item;
		}
		return null;
	}

	/* =======================================================
	 * Public methods
	 * ======================================================= */

	/**
	 * Button size that should be used when adding and inserting buttons
	 */
	public ButtonSize getButtonSize() {
		return m_buttonSize;
	}
	
	public AbstractButton insertButton(String before, AbstractButton button, String command) {
		AbstractButton prefix = m_commands.get(before);
		return (AbstractButton)insert(prefix,addButton(button, command));
	}
	
	public AbstractButton insertButton(String before, String command, String caption) {
		AbstractButton prefix = m_commands.get(before);
		return (AbstractButton)insert(prefix,addButton(command, caption));
	}
	

	public AbstractButton addButton(AbstractButton button, String command) {
		// exists?
		if(!m_commands.containsKey(command)) {
			// set name and action command
			button.setName(command);
			button.setActionCommand(command);
			// align center in parent container
			button.setAlignmentX(AbstractButton.CENTER_ALIGNMENT);
			button.setAlignmentY(AbstractButton.CENTER_ALIGNMENT);			
			// remove glue?
			if(m_alignment == SwingConstants.LEFT) 
				this.remove(m_glue);
			// add to component
			this.add(button);
			// add glue?
			if(m_alignment == SwingConstants.LEFT) 
				this.add(m_glue);
			// add to lists
			m_items.add(button);
			m_commands.put(command, button);
			button.addActionListener(m_actionHandler);
			return button;
		}
		return null;
	}
	
	public AbstractButton addButton(String command, String caption) {
		// exists?
		if(!m_commands.containsKey(command)) {
			JButton button = DiskoButtonFactory.createButton(
					caption,caption,null,m_buttonSize);
			// forward
			return addButton(button,command);
		}
		return null;
	}
	
	public void removeButton(String command) {
		AbstractButton button = m_commands.get(command);
		this.remove(button);
		m_items.remove(button);
		m_commands.remove(command);
		button.removeActionListener(m_actionHandler);
	}
	
	
	public boolean addItem(JComponent item) {
		if(!m_items.contains(item)) {
			// align center in parent container
			item.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			item.setAlignmentY(JComponent.CENTER_ALIGNMENT);
			// remove glue?
			if(m_alignment == SwingConstants.LEFT) 
				this.remove(m_glue);
			// add to component
			this.add(item);
			// add glue?
			if(m_alignment == SwingConstants.LEFT) 
				this.add(m_glue);
			// add to list
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
	
	public AbstractButton[] getButtons() {
		List<AbstractButton> buttons = new ArrayList<AbstractButton>();
		for(JComponent it : m_items) {
			if(it instanceof AbstractButton)
				buttons.add((AbstractButton)it);
		}
		AbstractButton[] items = new AbstractButton[buttons.size()];
		buttons.toArray(items);
		return items;
	}
	
	public JComponent[] getItems() {
		JComponent[] items = new JComponent[m_items.size()];
		m_items.toArray(items);
		return items;
	}
	
	public boolean containsButton(String command) {
		return m_commands.containsKey(command);
	}			
	
	public AbstractButton getButton(String command) {
		return m_commands.get(command);
	}
	
	public boolean isButtonVisible(String command) {
		return m_commands.containsKey(command) ? m_commands.get(command).isVisible() : false;
	}
	
	public void setButtonVisible(String command, boolean isVisible) {
		if(m_commands.containsKey(command))
			m_commands.get(command).setVisible(isVisible);
	}
	
	public boolean isButtonEnabled(String command) {
		return m_commands.containsKey(command) ? m_commands.get(command).isEnabled() : false;
	}
	
	public void setButtonEnabled(String command, boolean isEnabled) {
		if(m_commands.containsKey(command))
			m_commands.get(command).setEnabled(isEnabled);
	}

	public void addActionListener(ActionListener listener) {
		m_listeners.add(ActionListener.class, listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		m_listeners.remove(ActionListener.class, listener);
	}
	
	public boolean doAction(String command) {
		if(m_commands.containsKey(command)) {
			m_commands.get(command).doClick();
			return true;
		}
		return false;
	}
	
	public int getTotalItemWidth() {
		return getTotalItemWidth(false);			
	}
	
	public int getTotalItemWidth(boolean onlyButtons) {
		int w = 0;
		for(JComponent it: m_items) {
			if(it.isVisible() && (!onlyButtons || it instanceof AbstractButton))
				w += (it.getWidth()>0 ? it.getWidth() : it.getPreferredSize().width);
		}
		return w;					
	}
	
	public int getMaxItemHeigth() {
		return getMaxItemHeigth(false);			
	}
	
	public int getMaxItemHeigth(boolean onlyButtons) {
		int max = 0;
		for(JComponent it: m_items) {
			if(it.isVisible() && (!onlyButtons || it instanceof AbstractButton))
				max = Math.max(max, (it.getHeight()>0 ? it.getHeight() : it.getPreferredSize().width));
		}
		return max;			
	}
	
  	@Override
  	public void setEnabled(boolean isEnabled) {
  		for(int i=0;i<getComponentCount();i++) {
  			getComponent(i).setEnabled(isEnabled);
  		}
  	}
  	
  	/* ========================================================
  	 * Because AbstractButton do not respect the sequence
  	 * of which ActionListeners are added, a global handler
  	 * is added together with local ActionListener list for
  	 * each abstract button. When a button is pressed, this
  	 * handler forward the action to the listeners in the
  	 * sequence that these listeners where added. 
  	 * ======================================================== */
  	  	
  	final ActionHandler m_actionHandler = new ActionHandler();
  		  		
  	class ActionHandler implements ActionListener {
  		
  		@Override
  		public void actionPerformed(ActionEvent e) {
  			// get list of listeners
  			ActionListener[] list = m_listeners.getListeners(ActionListener.class);
			for(int i=0; i<list.length ; i++) {
				list[i].actionPerformed(e);
			}
  		}
  		
  	};

  	
}  //  @jve:decl-index=0:visual-constraint="10,10"
