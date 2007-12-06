package org.redcross.sar.util.except;

/**
 * This exception is thrown when trying to deleteObject an MsoObject tha cannot be deleted.
 */
public class IllegalDeleteException extends MsoException
{
    public IllegalDeleteException(String aMessage)
    {
        super(aMessage);
    }
}
