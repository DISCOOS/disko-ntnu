/**
 * 
 */
package org.redcross.sar.gui.dnd;

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

/**
 * @author kennetgu
 *
 */
public abstract class DiskoDropTargetAdapter implements DropTargetListener {

	public abstract void dragEnter(DropTargetDragEvent e);
	
	public abstract void drop(DropTargetDropEvent e);

	public void dragExit(DropTargetEvent e) {}
	
	public void dragOver(DropTargetDragEvent e) {}

	public void dropActionChanged(DropTargetDragEvent e) {}
	
}
