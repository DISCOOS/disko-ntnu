package org.redcross.sar.util.except;

/**
 * Exception for illegal parameters.
 */
public class IllegalMsoArgumentException extends MsoException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalMsoArgumentException(String aMessage)
    {
        super(aMessage);
    }
}
