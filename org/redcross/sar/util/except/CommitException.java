package org.redcross.sar.util.except;

/**
 * Indicates that an error during commit.
 */
public class CommitException  extends MsoException
{
    public CommitException(String aMessage)
    {
        super(aMessage);
    }
}
