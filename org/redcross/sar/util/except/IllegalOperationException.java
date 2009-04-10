package org.redcross.sar.util.except;

/**
 * Indicates attempt to establish a reference between objects that is not recognized or accepted in the model
 */
public class IllegalOperationException extends MsoException
{
    public IllegalOperationException()
    {
        super();
    }

    public IllegalOperationException(String aMessage)
    {
        super(aMessage);
    }
}
