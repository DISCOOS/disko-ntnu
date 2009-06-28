package org.redcross.sar.mso;

import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoTransactionListenerIf;
import org.redcross.sar.work.IWorkPool;

/**
 *  Specification of the Model Driver API.
 */
public interface IDispatcherIf extends IMsoTransactionListenerIf
{
    /**
     * Make an unique Object Id.
     * 
     * @return  The Object ID
     */
    public IMsoObjectIf.IObjectIdIf createObjectId();

    /**
     * This method initiates the dispatcher. </p> 
     * 
     * The method should only be called once. Subsequent calls to 
     * does not succeed if the startup procedure is already initiated.</p>
     *  
     * Since the startup procedure may take some time, the initiation 
     * is executed in another thread. Hence, this method does not block.</p>
     * 
     * The method {@link IDispatcherIf#isReady()} returns {@code true} when
     * the startup procedure has finished.
     *   
     * @param pool The work pool to dispatch work on.
     * 
     * @return Returns {@code true} if startup procedure initiated successfully.
     */
    public boolean initiate(IWorkPool pool);
    
    /**
     * Check if dispatcher is initiated.
     * 
     * @return Returns {@code true} if dispatcher is initiated.
     */
    
    public boolean isInitiated();
    
   /**
    * Check if the dispatcher is ready. </p> This flag is only if 
    * the dispatcher startup procedure is completed successfully.
    * 
    * @see For more information, see {@link IDispatcherIf#initiate(IWorkPool)}
    * @return Returns {@code true} if dispatcher is ready. </p>
    */
   public boolean isReady();

   /**
    * This method initiates the given model.</p>
    * 
    * <b>NOTE</b>: If the dispatcher is not initiated, 
    * the method fails. </p>
    * 
    * @param IMsoModelIf - the MSO model to initiate
    * @return Returns {@code true} if model initiation succeeds.
    */
   public boolean initiate(IMsoModelIf aModel);

   /**
    * Check if model is initiated.
    *
    * @param aModel - the model to check for
    * 
    * @return Returns {@code true} if given model is initiated.
    */   
   public boolean isInitiated(IMsoModelIf aModel);
   
   /**
    * Fetch a list of currently active operations with name and id
    * @return a List of the currently active operations as String[2]:{Name,Id} pairs
    */
   public java.util.List<String[]> getActiveOperations();

   /**
    *   Sets an operation as active using the id of the operation
    * @param oprID - id of the operation to use
    * @return value indicating if the operation is set as active
    */
   public boolean setCurrentOperation(String oprID);

   /**
    *   Gets active operation id
    * @return operation id as string
    */
   public String getCurrentOperationID();

   /**
    * Gets active operation name
    * @return operation id as string
    */
   public String getCurrentOperationName();

   /**
    * Initiate a new operation creation process.</p> The creation of
    * an operation requires an acknowledgment from the master. This may
    * take some time, depending on the distribution topology (no network,
    * type of network, bandwidth), since the call is made asynchronously. 
    * When the acknowledgment is received,  {@link IDispatcherListenerIf} 
    * listeners are notified by invoking the method 
    * {@link IDispatcherListenerIf#onOperationCreated(OprID, true)}. The flag
    * {@link IDispatcherIf#isCreationInProgress()} returns {@code true} until
    * the acknowledgment is received. </p>
    * 
    * If a creation process is already started, this method returns 
    * {@code false} until an acknowledgment is received or the 
    * creation process timeout is passed.    
    * 
    * @param timeOutMillis The creation process timeout in milliseconds.
    * 
    * @return Returns {@code true} if a new operation creation process was 
    * initiated. 
    */
   public boolean createNewOperation(long timeOutMillis);

   /**
    * Check if a operation creation is in progress.
    * 
    * @return Returns {@code true} if a operation creation is in progress.
    */
   public boolean isCreationInProgress();
   
   /**
    * Finish the currently active operation
    * 
    * @return Returns {@code true} if currently active operation was finished.
    */
   public boolean finishCurrentOperation();

   /**
    * Merge a other operation with the currently active one and finishes the other when merge is completed
    */
   public void merge();

   /**
    * Shut down the dispatcher
    */
   public void shutdown();

   /**
    * Add a IDispatcherListenerIf instance to the listener queue
    * @param listener - the listener
    * @return Returns {@code true} if successfully added.
    */
   public boolean addDispatcherListener(IDispatcherListenerIf listener);

   /**
    * Remove a IDispatcherListenerIf instance from the listener queue
    * @param listener - the listener
    * @return Returns {@code true} if successfully removed.
    */
   public boolean removeDispatcherListener(IDispatcherListenerIf listener);

}
