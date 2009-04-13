package org.redcross.sar.mso;

import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 *  Specification of the Model Driver API.
 */
public interface IDispatcherIf
{
    /**
     * Make an assumably unique Object Id
     * @return  The Object ID
     */
    public IMsoObjectIf.IObjectIdIf makeObjectId();

   /**
    * This method checks to see if a SaraOperation (Hendelse) is running and initiates a mapping
    * between SaraOperation (Hendelse) and a running MsoModel
    */
   public void initiate();

   /**
    *
    * @return true if integration is loaded otherwise false
    */
   public boolean isInitiated();

   /**
    * Fetch a list of currently active operations with name and id
    * @return a List of the currently active operations as String[2]:{Name,Id} pairs
    */
   public java.util.List<String[]> getActiveOperations();

   /**
    *   Sets an operation as active using the id of the operation
    * @param operationid id of the operation to use
    * @return value indicating if the operation is set as active
    */
   public boolean setActiveOperation(String operationid);

   /**
    *   Gets active operation id
    * @return operation id as string
    */
   public String getActiveOperationID();

   /**
    * Gets active operation name
    * @return operation id as string
    */
   public String getActiveOperationName();

   /**
    * Finish the currently active operation
    */
   public void finishActiveOperation();

   /**
    * Initiate a new operation, and set it as active
    */
   public void createNewOperation();

   /**
    * Merge a other operation with the currently active one and finishes the other when merge is completed
    */
   public void merge();

   /**
    * Shut down model driver
    */
   public void shutDown();

   public boolean addDispatcherListener(IDispatcherListenerIf listener);

   public boolean removeDispatcherListener(IDispatcherListenerIf listener);

}