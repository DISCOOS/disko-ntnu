/**
 * 
 */
package org.redcross.sar.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.redcross.sar.mso.data.IAssignmentIf;

/**
 * @author kennetgu
 *
 */
public class AssignmentTransferable implements Transferable
{
	private static DataFlavor m_flavor = null;
	
	private IAssignmentIf m_assignment;

    public AssignmentTransferable(IAssignmentIf anAssignment)
    {
    	try {
	        // save assignment
	    	m_assignment = anAssignment;    	
	    	// create flavor
	    	m_flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=org.redcross.sar.mso.data.IAssignmentIf");
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
    {
        if (!isDataFlavorSupported(flavor))
        {
            throw new UnsupportedFlavorException(flavor);
        }
        return m_assignment;
    }

    public DataFlavor[] getTransferDataFlavors()
    {
        return new DataFlavor[]{m_flavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return m_flavor.equals(flavor);
    }        
}
