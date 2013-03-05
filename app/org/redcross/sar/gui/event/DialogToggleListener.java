package org.redcross.sar.gui.event;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.redcross.sar.gui.dialog.IDialog;

public class DialogToggleListener implements MouseListener {

	private IDialog dialog;

	public DialogToggleListener(IDialog dialog) {
		this.dialog = dialog;
	}
	
	public void mouseClicked(MouseEvent e) {
		// double click?
		if(e.getClickCount() > 1) {
			if(dialog!=null) dialog.setVisible(!dialog.isVisible());
		}
	}

	public void mousePressed(MouseEvent e) {
		// start show/hide
		if(dialog!=null) dialog.delayedSetVisible(!dialog.isVisible(), 250);
	}

	public void mouseReleased(MouseEvent e) {
		// stop show if not shown already
		if(dialog!=null) dialog.cancelSetVisible();
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

}	