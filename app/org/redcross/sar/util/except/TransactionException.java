package org.redcross.sar.util.except;

import java.util.List;

import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * Indicates that an error during commits and rollbacks.
 */
public class TransactionException  extends MsoException
{
	
	private static final long serialVersionUID = 1L;
	
	private final List<IMsoObjectIf> residue;
	
    public TransactionException(String aMessage, List<IMsoObjectIf> residue)
    {
        super(aMessage);
        this.residue = residue;
    }
    
    public List<IMsoObjectIf> getResidue() {
    	return residue;
    }
}
