package org.redcross.sar.util.except;

import java.util.List;

import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * Indicates that an error during commit.
 */
public class CommitException  extends MsoException
{
	
	private final List<IMsoObjectIf> residue;
	
    public CommitException(String aMessage, List<IMsoObjectIf> residue)
    {
        super(aMessage);
        this.residue = residue;
    }
    
    public List<IMsoObjectIf> getResidue() {
    	return residue;
    }
}
