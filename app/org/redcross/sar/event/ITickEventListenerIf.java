package org.redcross.sar.event;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 27.sep.2007
 */

/**
 *  Interface for tick handler with individual interval lengths.
 *
 * The mechanism does not guarantee that the tick events occur exactly at the given intervals.
 */
public interface ITickEventListenerIf extends java.util.EventListener
{
    /**
     * Set interval countdown value.
     *
     * @param aCounter The counter value.
     */
    public void setTimeCounter(long aCounter);

    /**
     * Get interval countdown value.
     *
     * @return The counter value.
     */
    public long getTimeCounter();

    /**
     * Get time interval (in milliseconds).
     *
     * @return The interval.
     */
    public long getInterval();

    /**
     * Handle a tick event
     *
     * @param e The event object
     */
    public void handleTick(TickEvent e);
}
