package org.redcross.sar.mso.event;

import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * Adapter class for IMsoUpdateListenerIf for server updates.
 * <p/>
 * Provides empty methods, to be implemented in subclasses.
 */
public class MsoServerUpdateAdapter implements IMsoUpdateListenerIf
{
    /**
     * Handle an update event sent to server
     */
    public void handleMsoUpdateEvent(MsoEvent.Update e)
    {
        int mask = e.getEventTypeMask();
        Object source = e.getSource();

        if ((mask & MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0)
        {
            // Possible handling of ADDED_REFERENCE_EVENT
        }
        if ((mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0)
        {
            // Possible handling of MODIFIED_DATA_EVENT
        }
        // etc..
    }

	public boolean hasInterestIn(IMsoObjectIf msoObject, UpdateMode mode) 
	{
		// check against interests
        return true;
    }
    
}
