package org.redcross.sar.gui;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

/**
 *  Generic handler for popup events
 */
public class PopupHandler extends AbstractPopupHandler {
	
	final private JPopupMenu popup = new JPopupMenu();

	protected JPopupMenu getMenu(MouseEvent e) {
		return popup;
	}
	
	public JPopupMenu getMenu() {
		return popup;
	}
	
}	

