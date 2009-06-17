package org.redcross.sar.event;

import java.util.EventObject;
/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 27.sep.2007
  */

/**
 *
 */
public class TickEvent extends EventObject
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TickEvent(Object source)
    {
        super(source);
    }
}
