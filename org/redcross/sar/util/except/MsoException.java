package org.redcross.sar.util.except;

/**
 * Common MSO API exception base class
 */
public class MsoException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Create exception with empty message.
     */
    public MsoException()
    {
        super();
    }

    /**
     * Create exception with a message.
     */
    public MsoException(String aMessage)
    {
        super(aMessage);
    }
}
