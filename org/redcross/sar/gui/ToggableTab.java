package org.redcross.sar.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class ToggableTab extends JLabel {

	private static final long serialVersionUID = 1L;

	private int id = 0;
	private String command = null;
	private boolean isSelected = false; 
	private List<ActionListener> listeners = null;
	
	public ToggableTab(String caption, int id) {
		// prepare
		this.id = id;
		this.listeners = new ArrayList<ActionListener>();
		super.setText(caption);
		// initialize GUI
		initialize();
	}
	
	private void initialize() {
		super.setBorder(new LineBorder(Color.GRAY, 2, false));
		super.setBackground(Color.LIGHT_GRAY);
		super.setOpaque(false);
		super.setHorizontalAlignment(SwingConstants.CENTER);
		super.setVerticalTextPosition(SwingConstants.CENTER);
		super.addMouseListener(new MouseAdapter() {

			
			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				doClick();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// forward
				//doClick();
			}
			
		});
	}
	
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		super.setOpaque(isSelected);
		super.setForeground(isSelected ? Color.WHITE : Color.BLACK);
		super.repaint();
	}
	
	public boolean isSelected() {
		return this.isSelected;
	}
	
	public void setActionCommand(String command) {
		this.command = command;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public boolean addActionListener(ActionListener listener) {
		return listeners.add(listener);
	}
	
	public boolean removeActionListener(ActionListener listener) {
		return listeners.remove(listener);
	}
	
	private void fireActionEvent(ActionEvent e) {
		// forward
		for(ActionListener it : listeners)
			it.actionPerformed(e);
	}
	
	public void doClick() {
		setSelected(!isSelected());
		fireActionEvent(new ActionEvent(this,id,command));
	}
	
	
	
}
