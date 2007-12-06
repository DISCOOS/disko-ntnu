package org.redcross.sar.mso.event;

import org.redcross.sar.mso.committer.CommitWrapper;
import org.redcross.sar.mso.committer.ICommitWrapperIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.except.CommitException;

/**
 * Interface for Update manager
 * <p/>
 * Manages listener (observer) sets and notifications to the listeners.
 */
public interface IMsoEventManagerIf
{
    /**
     * Add a listener in the Client Update Listeners queue.
     *
     * @param aListener The listener
     */
    public void addClientUpdateListener(IMsoUpdateListenerIf aListener);

    /**
     * Remove a listener in the Client Update Listeners queue.
     *
     * @param aListener The listener
     */
    public void removeClientUpdateListener(IMsoUpdateListenerIf aListener);

    /**
     * Notify a client update.
     *
     * @param aSource         The source object
     * @param anEventTypeMask Type of event (see {@link org.redcross.sar.mso.event.MsoEvent.EventType})
     */
    public void notifyClientUpdate(IMsoObjectIf aSource, int anEventTypeMask);

    /**
     * Add a listener in the Server Update Listeners} queue.
     *
     * @param aListener The listener
     */
    public void addServerUpdateListener(IMsoUpdateListenerIf aListener);

    /**
     * Remove a listener in the Server Update Listeners queue.
     *
     * @param aListener The listener
     */
    public void removeServerUpdateListener(IMsoUpdateListenerIf aListener);

    /**
     * Notify a server update.
     *
     * @param aSource         The source object
     * @param anEventTypeMask Type of event (see {@link org.redcross.sar.mso.event.MsoEvent.EventType})
     */
    public void notifyServerUpdate(IMsoObjectIf aSource, int anEventTypeMask);

    /**
     * Add a listener in the Commit Listeners queue.
     *
     * @param aListener The listener
     */
    public void addCommitListener(IMsoCommitListenerIf aListener);

    /**
     * Remove a listener in the Commit Listeners queue.
     *
     * @param aListener The listener
     */
    public void removeCommitListener(IMsoCommitListenerIf aListener);

    /**
     * Notify a commit.
     *
     * @param aSource The {@link CommitWrapper} that contains the committable objects and relations
     * @throws org.redcross.sar.util.except.CommitException when the commit fails.
     */
    public void notifyCommit(ICommitWrapperIf aSource) throws CommitException;

    public void addDerivedUpdateListener(IMsoDerivedUpdateListenerIf aListener);

    public void removeDerivedUpdateListener(IMsoDerivedUpdateListenerIf aListener);

    public void notifyDerivedUpdate(IMsoObjectIf aSource, int anEventTypeMask);


}
