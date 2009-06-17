package org.redcross.sar.util.except;

/**
 * Indicates attempt to establish a relation between objects that is not recognized in the model
 */
public class InvalidRelationException extends MsoException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidRelationException()
    {
        super();
    }

    public InvalidRelationException(String aMessage)
    {
        super(aMessage);
    }

}
