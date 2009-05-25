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
import org.redcross.sar.mso.work.IMsoWork;

/**
 * Interface for singleton class for accessing the MSO model
 */
public interface IMsoModelIf extends IDataSource<MsoEvent.UpdateList>, IMsoTransactionManagerIf
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
     * Modification state, tells the modification state of each attribute or relation.
     */
    public enum ModificationState
    {
        STATE_UNDEFINED,
        STATE_REMOTE,
        STATE_CONFLICT,
        STATE_LOCAL
    }

    /**
     * Get the data source id
     *
     * @return The data source id
     */
    @Override
    public String getID();

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

    public void suspendClientUpdate();

    public void resumeClientUpdate(boolean all);

    public boolean isUpdateSuspended();

    public long schedule(IMsoWork work);
    
    /**
     * Get editable state of mode
     * 
     * @return Returns <code>true</code> if model is editable, otherwise <code>false</code>
     */
    
    public boolean isEditable();
    
    

}
