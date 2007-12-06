package org.redcross.sar.mso.event;

import org.redcross.sar.util.except.CommitException;

/**
 *
 */
public interface IMsoCommitListenerIf extends java.util.EventListener
{
    /**
     * Handle a commit event.
     * <p/>
     * Listener method(s) that handle MSO Commit events.
     * @param e The {@link MsoEvent event} that shall be handled.
     * @throws org.redcross.sar.util.except.CommitException when the commit fails.
     */

    public void handleMsoCommitEvent(MsoEvent.Commit e) throws CommitException;
}
