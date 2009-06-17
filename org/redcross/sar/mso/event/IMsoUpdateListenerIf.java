package org.redcross.sar.mso.event;

import java.util.EnumSet;

import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;



/**
 * Interface for MsoUpdateListener method
 */
public interface IMsoUpdateListenerIf extends java.util.EventListener
{
    /**
     * Handle an event.
     * <p/>
     * Listener method(s) that handle MSO events.
     *
     * @param e The {@link ChangeList events} that shall be handled.
     */
    public void handleMsoChangeEvent(MsoEvent.ChangeList e);

    /**
     * This method indicates which MSO classes that the listener are interested in. Only
     * changes in IMsoObjectIf objects that implements the given MSO classes are passed to the
     * listener using the <code>handleMsoUpdateEvent</code> method. </p>
     *
     * <b>IMPORTANT</b>: This method is only invoked by the MsoEventManager once when added. If
     * the interest are changed, the listener must be added to the MsoEventManager again. </p>.
     *
     * @return EnumSet of MsoClassCode
     */
    public EnumSet<MsoClassCode> getInterests();

}
