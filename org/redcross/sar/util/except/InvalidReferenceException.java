package org.redcross.sar.util.except;

/**
 * Indicates attempt to establish a reference between objects that is not recognized in the model
 */
public class InvalidReferenceException extends MsoException
{
    public InvalidReferenceException()
    {
        super();
    }

    public InvalidReferenceException(String aMessage)
    {
        super(aMessage);
    }

}
