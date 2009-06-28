/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 14.des.2006
 * To change this template use File | Settings | File Templates.
 */
/**
 *
 */
package org.redcross.sar.mso;

import org.redcross.sar.data.IDataSource;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.MsoEvent;

/**
 * Interface for singleton class for accessing the MSO model
 */
public interface IMsoModelIf extends IDataSource<MsoEvent.ChangeList>, IMsoTransactionManagerIf
{
    /**
     * Update modes, to be used when updating local data in order to inform the GUI how data have been updated.
     */
    public enum UpdateMode
    {
        LOCAL_UPDATE_MODE,
        REMOTE_UPDATE_MODE 
    }

	/**
     * Get the data source id
     *
     * @return The data source id
     */
    @Override
    public String getID();
    
    /**
     * Check if model exists
     * @return Returns <code>true</code> if model exists.
     */
    public boolean exists();
    
    /**
     * Check if model is deleted
     * @return Returns <code>true</code> if model is deleted.
     */
    public boolean isDeleted();
    
    /**
     * Get the {@link IMsoManagerIf MSO manager}
     *
     * @return The MSO manager
     */
    public IMsoManagerIf getMsoManager();

    /**
     * Get the {@link org.redcross.sar.mso.event.IMsoEventManagerIf event manager}
     *
     * @return The event manager.
     */
    public IMsoEventManagerIf getEventManager();

    /**
     * Get the {@link org.redcross.sar.mso.IDispatcherIf Model driver}
     *
     * @return The Model driver.
     */
    public IDispatcherIf getDispatcher();

    /**
     * Set the dispatcher for this model.
     * 
     * @param aDispatcher - the model dispatcher
     */
    public void setDispatcher(IDispatcherIf aDispatcher);

    /**
     * Set update mode to {@link UpdateMode#LOCAL_UPDATE_MODE LOCAL_UPDATE_MODE}.
     */
    public void setLocalUpdateMode();

    /**
     * Set update mode to {@link UpdateMode#REMOTE_UPDATE_MODE REMOTE_UPDATE_MODE}.
     */
    public void setRemoteUpdateMode();

    /**
     * Restore previous update mode.
     */
    public void restoreUpdateMode();

    /**
     * Get current update mode.
     *
     * @return The update mode
     */
    public UpdateMode getUpdateMode();

    /**
     * Query update mode
     *
     * @param mode
     * @return
     */
    public boolean isUpdateMode(UpdateMode mode);
    
    /**
     * Get the transaction manager.
     * 
     * @return Reference to IMsoTransactionManagerIf instance
     */
    public IMsoTransactionManagerIf getMsoTransactionManager();

    /**
     * Check if changes are suspended.
     *  
     * @return Returns <code>true</code> if updates are suspended.
     */
    public boolean isChangeSuspended();

    /**
     * Suspend change notifications to listeners. <p/>
     * 
     * Use this method to group all change notifications into one single 
     * event. This will greatly improve the event handling process when a 
     * large number of changes are pending. The method has memory function, 
     * which ensures that the same number invocations of {@code suspendChange()} and 
     * {@code resumeChange()} is required to return to the same state. 
     * For example, if changes are suspended by calling {@code suspendChange()}
     * four times, resuming changes requires {@code resumeChange()} to be called
     * four times. This make it possible to enable and disable changes in a
     * object hierarchy.
     */
    public void suspendChange();

    /**
     * Resume pending update notification to listeners. <p/>
     *
     * @return Returns <code>true</code> if suspended updates were resumed. 
     * If no suspended updates were resumed and notified to clients, this 
     * method returns <code>false</code>. </p>
     * 
     * @see For more information, see {@code suspendUpdate()}
     */
    public void resumeUpdate();

    /**
     * Get editable state of mode
     * 
     * @return Returns <code>true</code> if model is editable, otherwise <code>false</code>
     */   
    public boolean isEditable();

}
