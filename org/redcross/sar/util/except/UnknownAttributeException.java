package org.redcross.sar.util.except;

/**
 * Indicates attempt to acces an unknown attribute
 */
public class UnknownAttributeException  extends MsoException
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnknownAttributeException()
    {
        super();
    }

    public UnknownAttributeException(String aMessage)
    {
        super(aMessage);
    }
}
