/**
 * 
 */
package org.redcross.sar.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * @author kennetgu
 *
 */
public class TransferableMsoObject<M extends IMsoObjectIf> implements Transferable
{
	private static DataFlavor m_flavor = null;
	
	private M m_anObject;

    public TransferableMsoObject(M anObject)
    {
    	try {
	        // save assignment
	    	m_anObject = anObject;    	
	    	// create flavor
	    	m_flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class="+anObject.getClass().getSimpleName());
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
