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
public interface IDiskoDropTarget {

	public Component getComponent();

	public boolean transfer(Transferable data);
	
	public boolean canTransfer(Transferable data);
	
}
