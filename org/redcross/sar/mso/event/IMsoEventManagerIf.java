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
     * Add a listener in the (slave) change listeners queue.
     *
     * @param aListener The listener
     */
    public void addChangeListener(IMsoChangeListenerIf aListener);

    /**
     * Remove a listener in the (slave) change listeners queue.
     *
     * @param aListener The listener
     */
    public void removeChangeListener(IMsoChangeListenerIf aListener);

    /**
     * Notify a that a (slave) change has occurred.
     *
     * @param aChange - the change record
     */
    public void notifyChange(IChangeRecordIf aChange);

    /**
     * Notify that a clear all change has occurred on 
     * given MSO object. 
     *
     */
    public void notifyClearAll(IMsoObjectIf root);

    /**
     * Add a listener to the (master) update listeners queue.
     *
     * @param aListener The listener
     */
    public void addUpdateListener(IMsoUpdateListenerIf aListener);

    /**
     * Remove a listener from the (master) update listeners queue.
     *
     * @param aListener The listener
     */
    public void removeUpdateListener(IMsoUpdateListenerIf aListener);

    /**
     * Notify that a (master) update is required.
     *
     * @param aChange - the change record
     */
    public void notifyUpdate(IChangeRecordIf aChange);

    /**
     * Add a listener in the transaction listeners queue.
     *
     * @param aListener The listener
     */
    public void addTransactionListener(IMsoTransactionListenerIf aListener);

    /**
     * Remove a listener in the transaction listeners queue.
     *
     * @param aListener The listener
     */
    public void removeTransactionListener(IMsoTransactionListenerIf aListener);

    /**
     * Notify a transaction.
     *
     * @param aSource The {@link TransactionImpl} that contains the changes
     * @throws {@link org.redcross.sar.util.except.TransactionException} when the transaction fails.
     */
    public void notifyCommit(ITransactionIf aSource) throws TransactionException;

    public void addCoChangeListener(IMsoCoChangeListenerIf aListener);

    public void removeCoUpdateListener(IMsoCoChangeListenerIf aListener);

    public void notifyCoChange(IChangeRecordIf aChange);

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
