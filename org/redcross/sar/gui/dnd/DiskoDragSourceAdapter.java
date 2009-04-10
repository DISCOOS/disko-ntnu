/**
 * 
 */
package org.redcross.sar.gui.dnd;

import java.awt.Component;
import java.awt.datatransfer.Transferable;

import javax.swing.Icon;

/**
 * @author kennetgu
 *
 */
public abstract class DiskoDragSourceAdapter implements IDiskoDragSource, IIconDragSource {

	public abstract Component getComponent();

	public abstract Transferable getTransferable();

	public Icon getIcon() {
		return null;
	}	
}
