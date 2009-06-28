package org.redcross.sar.mso.event;

/**
 * This interface implements change notification to listeners 
 * that implements parallel book holding of items (a derived or 
 * "co-list") by listening to create, delete and modify events. 
 * Listeners implementing this interface is always notified before
 * listeners implementing the {@link IMsoChangeListenerIf} 
 * interface.
 *   
 */
public interface IMsoCoChangeListenerIf
{
    public void handleMsoCoChangeEvent(MsoEvent.CoChange event);
}
