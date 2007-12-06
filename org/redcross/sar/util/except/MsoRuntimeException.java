package org.redcross.sar.util.except;

/**
 *
 */
public class MsoRuntimeException extends RuntimeException
{
    /**
     * Create exception with a message.
     * @param aMessage The error message
     */
    public MsoRuntimeException(String aMessage)
    {
        super("MSO runtime exception: " + aMessage);
    }
}
