package org.redcross.sar.util.except;

/**
 * Indicates that an object id (that is supposed to be unique) is used over again. 
 */
public class DuplicateIdException extends MsoRuntimeException
{
    public DuplicateIdException(String aMessage)
    {
        super(aMessage);
    }

}
