package org.redcross.sar.mso.event;

import org.redcross.sar.mso.ITransactionIf;
import org.redcross.sar.mso.TransactionImpl;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.except.TransactionException;

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
     * @param anEventTypeMask Type of event (see {@link org.redcross.sar.mso.event.MsoEvent.MsoEventType})
     */
    public void notifyClientUpdate(IMsoObjectIf aSource, UpdateMode mode, boolean isLoopback, int anEventTypeMask);

    /**
     * Notify a client clear all update.
     *
     */
    public void notifyClearAll(IMsoObjectIf root);

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
     * @param anEventTypeMask Type of event (see {@link org.redcross.sar.mso.event.MsoEvent.MsoEventType})
     */
    public void notifyServerUpdate(IMsoObjectIf aSource, UpdateMode mode, int anEventTypeMask);

    /**
     * Add a listener in the Commit Listeners queue.
     *
     * @param aListener The listener
     */
    public void addCommitListener(IMsoTransactionListenerIf aListener);

    /**
     * Remove a listener in the Commit Listeners queue.
     *
     * @param aListener The listener
     */
    public void removeCommitListener(IMsoTransactionListenerIf aListener);

    /**
     * Notify a commit.
     *
     * @param aSource The {@link TransactionImpl} that contains the committable objects and relations
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails.
     */
    public void notifyCommit(ITransactionIf aSource) throws TransactionException;

    public void addDerivedUpdateListener(IMsoDerivedUpdateListenerIf aListener);

    public void removeDerivedUpdateListener(IMsoDerivedUpdateListenerIf aListener);

    public void notifyDerivedUpdate(IMsoObjectIf aSource, int anEventTypeMask);

    /**
     * Notify that a resume operation is begun.</p>
     *
     * For each invocation of <code>enterResume()</code>, the manager increments
     * a internal resume counter. When the this counter is greater than 0,
     * all update notification is buffered. Likewise, for each invocation of
     * <code>leaveResume()</code> the counter is decreased. When the counter reaches
     * zero, all buffered invocation is forwarded to registered update listeners.</p>
     *
     * <b>IMPORTANT</b>: This method should be called by the IMsoObjectIf implementation just BEFORE
     * the update notification is fired in <code>resumeClientUpdate()</code>. </p>
     */
    public void enterResume();

    /**
     * Notify that a resume operation is ended. </p>
     *
     * For each invocation of <code>enterResume()</code>, the manager increments
     * a internal resume counter. When the this counter is greater than 0,
     * all update notification is buffered. Likewise, for each invocation of
     * <code>leaveResume()</code> the counter is decreased. When the counter reaches
     * zero, all buffered invocation is forwarded to registered update listeners.</p>
     *
     * <b>IMPORTANT</b>: This method should be called by the IMsoObjectIf implementation just AFTER
     * the update notification is fired in <code>resumeClientUpdate()</code>. </p>
     */
    public void leaveResume();

}
