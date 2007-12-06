package org.redcross.sar.util.except;

/**
 * Indicates that a null pointer was given as parameter when a non-null-pointer was expected
 */
public class MsoNullPointerException extends MsoException
{
    public MsoNullPointerException()
    {
        super();
    }

    public MsoNullPointerException(String aMessage)
    {
        super(aMessage);
    }
}
