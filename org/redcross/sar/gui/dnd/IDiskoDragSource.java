/**
 * 
 */
package org.redcross.sar.gui.dnd;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;

import javax.swing.Icon;

/**
 * @author kennetgu
 *
 */
public interface IDiskoDragSource {

	public Component getComponent();

	public Transferable getTransferable();
	
}
