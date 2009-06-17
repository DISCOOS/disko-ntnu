package org.redcross.sar.util.except;

/**
 * Indicates that an object id (that is supposed to be unique) is used over again. 
 */
public class DuplicateIdException extends MsoRuntimeException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateIdException(String aMessage)
    {
        super(aMessage);
    }

}
