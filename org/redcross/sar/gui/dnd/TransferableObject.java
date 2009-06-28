/**
 * 
 */
package org.redcross.sar.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * @author kennetgu
 *
 */
public class TransferableObject implements Transferable
{
	private static DataFlavor m_flavor = null;
	
	private Object m_anObject;

	/**
	 * Constructs a TransferableObject instance 
	 * @param anObject - the object to transfer
	 * @param canonicalName - the local object canonical class name
	 */
    public TransferableObject(Object anObject, String canonicalName)
    {
    	try {
	        // save assignment
	    	m_anObject = anObject;    	
	    	// create flavor
	    	m_flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class="+canonicalName);
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
        return m_anObject;
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
