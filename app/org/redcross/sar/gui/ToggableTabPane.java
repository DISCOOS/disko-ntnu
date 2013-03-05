package org.redcross.sar.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ToggableTabPane extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private Map<Integer, ToggableTab> ids = null;
	private Map<String, ToggableTab> tabs = null;
	private List<ActionListener> listeners = null;
	private Dimension preferredTabSize = null;
	
	private JLabel caption = null;
	
	public ToggableTabPane() {
		// prepare
		listeners = new ArrayList<ActionListener>();
		ids = new HashMap<Integer, ToggableTab>();
		tabs = new HashMap<String, ToggableTab>();
		// initialize GUI
		initialize();
	}
	
	private void initialize() {
		FlowLayout fl = new FlowLayout();
		fl.setAlignment(FlowLayout.LEFT);
		fl.setHgap(5);
		fl.setVgap(1);
		this.setLayout(fl);
		this.setBackground(Color.WHITE);
		this.setBorder(null);
		this.add(getCaption());				
	}
	
	private JLabel getCaption() {
		if(caption==null) {
			caption = new JLabel();
			caption.setOpaque(false);
			caption.setVisible(false);
		}
		return caption;
	}
	
	public void setCaption(String text) {
		getCaption().setText(text);
		getCaption().setVisible(text!=null && !text.isEmpty());			
	}
	
	public ToggableTab addTab(String caption, String command, int id) {
		// does not exist?
		if(!tabs.containsKey(command)) {
			ToggableTab tab = new ToggableTab(caption,id);
			tab.setActionCommand(command);
			tab.setPreferredSize(preferredTabSize);
			this.add(tab);
			tab.addActionListener(this);
			ids.put(id,tab);
			tabs.put(command, tab);
			return tab;
		}
		return null;
	}
	
	public ToggableTab getTab(String command) {
		// does exist?
		if(tabs.containsKey(command)) {
			return tabs.get(command);
		}
		return null;		
	}
	
	public ToggableTab getTab(int id) {
		// does exist?
		if(ids.containsKey(id)) {
			return ids.get(id);
		}
		return null;		
	}
	
	public ToggableTab removeTab(String command) {
		// does exist?
		if(tabs.containsKey(command)) {
			ToggableTab tab = tabs.get(command);
			this.remove(tab);
			tabs.remove(command);
			return tab;
		}
		return null;
	}
	
	public ToggableTab removeTab(int id) {
		// does exist?
		if(ids.containsKey(id)) {
			ToggableTab tab = ids.get(id);
			this.remove(tab);
			ids.remove(id);
			tabs.values().remove(tab);
			return tab;
		}
		return null;
	}
	
	public boolean selectTab(String command, boolean isChecked) {
		if (tabs.containsKey(command)) {
			tabs.get(command).doClick();
		}
		return false;		
	}
	
	public boolean selectTab(int id, boolean isChecked) {
		if (ids.containsKey(id)) {
			ids.get(id).doClick();
		}
		return false;		
	}
	
	public boolean isTabSelected(String command) {
		if (tabs.containsKey(command)) {
			return tabs.get(command).isSelected();
		}
		return false;
	}
	
	public boolean isTabSelected(int id) {
		if (ids.containsKey(id)) {
			return ids.get(id).isSelected();
		}
		return false;
	}
	
	public boolean addActionListener(ActionListener listener) {
		return listeners.add(listener);
	}
	
	public boolean removeActionListener(ActionListener listener) {
		return listeners.remove(listener);
	}
	
	public void actionPerformed(ActionEvent e) {
		// forward
		for(ActionListener it : listeners)
			it.actionPerformed(e);
	}
	
	public void setPreferredTabSize(Dimension d) {
		preferredTabSize = d;
		for(ToggableTab it : tabs.values())
			it.setPreferredSize(d);
		
	}
	
}
