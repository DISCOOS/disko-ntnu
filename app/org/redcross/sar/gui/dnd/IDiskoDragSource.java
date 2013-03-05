/**
 * 
 */
package org.redcross.sar.gui.dnd;

import java.awt.Component;
import java.awt.datatransfer.Transferable;

/**
 * @author kennetgu
 *
 */
public interface IDiskoDragSource {

	public Component getComponent();

	public Transferable getTransferable();
	
}
