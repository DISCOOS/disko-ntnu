package org.redcross.sar.util.except;

/**
 * This exception is thrown when trying to deleteObject an MsoObject tha cannot be deleted.
 */
public class IllegalDeleteException extends MsoException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalDeleteException(String aMessage)
    {
        super(aMessage);
    }
}
