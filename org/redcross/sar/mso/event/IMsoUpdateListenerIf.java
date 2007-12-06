package org.redcross.sar.mso.event;

import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * Interface for MsoUpdateListener method
 */
public interface IMsoUpdateListenerIf extends java.util.EventListener
{
    /**
     * Handle an event.
     * <p/>
     * Listener method(s) that handle MSO events.
     * @param e The {@link MsoEvent event} that shall be handled.
     */
    public void handleMsoUpdateEvent(MsoEvent.Update e);

    public boolean hasInterestIn(IMsoObjectIf aMsoObject);
}
