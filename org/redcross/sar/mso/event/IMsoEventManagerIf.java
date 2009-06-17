package org.redcross.sar.mso.event;

import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.ITransactionIf;
import org.redcross.sar.mso.TransactionImpl;
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
     * Add a listener in the local update listeners queue.
     *
     * @param aListener The listener
     */
    public void addLocalUpdateListener(IMsoUpdateListenerIf aListener);

    /**
     * Remove a listener in the local update listeners queue.
     *
     * @param aListener The listener
     */
    public void removeLocalUpdateListener(IMsoUpdateListenerIf aListener);

    /**
     * Notify a local update.
     *
     * @param aChange - the change record
     */
    public void notifyLocalUpdate(IChangeRecordIf aChange);

    /**
     * Notify a client clear all update.
     *
     */
    public void notifyClearAll(IMsoObjectIf root);

    /**
     * Add a listener in the remote update listeners queue.
     *
     * @param aListener The listener
     */
    public void addRemoteUpdateListener(IMsoUpdateListenerIf aListener);

    /**
     * Remove a listener in the remote update listeners queue.
     *
     * @param aListener The listener
     */
    public void removeRemoteUpdateListener(IMsoUpdateListenerIf aListener);

    /**
     * Notify a server update.
     *
     * @param aChange - the change record
     */
    public void notifyRemoteUpdate(IChangeRecordIf aChange);

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

    public void addCoUpdateListener(IMsoCoUpdateListenerIf aListener);

    public void removeCoUpdateListener(IMsoCoUpdateListenerIf aListener);

    public void notifyCoUpdate(IChangeRecordIf aChange);

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
