package org.redcross.sar.mso.event;

import org.redcross.sar.util.except.TransactionException;

/**
 *
 */
public interface IMsoTransactionListenerIf extends java.util.EventListener
{
    /**
     * Handle a commit event. <p/>
     * Listener method(s) that handle MSO Commit events.
     * @param e The {@link MsoEvent event} that shall be handled.
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails.
     */
    public void handleMsoCommitEvent(MsoEvent.Commit e) throws TransactionException;
    
}
