package org.redcross.sar.util.except;

/**
 * Indicates attempt to acces an unknown attribute
 */
public class UnknownAttributeException  extends MsoException
{

    public UnknownAttributeException()
    {
        super();
    }

    public UnknownAttributeException(String aMessage)
    {
        super(aMessage);
    }
}
