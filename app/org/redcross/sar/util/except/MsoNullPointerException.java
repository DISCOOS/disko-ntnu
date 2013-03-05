package org.redcross.sar.util.except;

/**
 * Indicates that a null pointer was given as parameter when a non-null-pointer was expected
 */
public class MsoNullPointerException extends MsoException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MsoNullPointerException()
    {
        super();
    }

    public MsoNullPointerException(String aMessage)
    {
        super(aMessage);
    }
}
