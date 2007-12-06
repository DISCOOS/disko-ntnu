package org.redcross.sar.util.except;

/**
 * Exception for illegal parameters.
 */
public class IllegalMsoArgumentException extends MsoException
{
    public IllegalMsoArgumentException(String aMessage)
    {
        super(aMessage);
    }
}
