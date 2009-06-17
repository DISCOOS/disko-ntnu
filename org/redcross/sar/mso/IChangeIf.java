package org.redcross.sar.mso;

import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoDataIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoRelationIf;
import org.redcross.sar.mso.event.MsoEvent;

/**
 * The IChangeIf interface define methods that used by the transaction handler when 
 * committing and rolling back objects and references.
 */
@SuppressWarnings("unchecked")
public interface IChangeIf
{
    
	/**
	 * Get the update mask for this object.
	 * @return Returns the update mask for this object
	 * @see MsoEvent for more information about individual mask values.
	 */
	public int getMask();
	
	/**
	 * Check if a specific flag is set
	 * @param mask - the mask to check for
	 * @return Returns {@code true} is mask is set.
	 */
	public boolean isFlagSet(int flag);
	
	/**
	 * Get the changed flag.
	 *  
	 * @return Returns {@code true} if {@code getMask()} 
	 * is greater than zero. 
	 */
	public boolean isChanged();
	
	/**
	 * Check if mask {@code MsoEventType.CREATED_OBJECT_EVENT} is set. 
	 * @return Returns {@code true} if mask 
	 * {@code MsoEventType.CREATED_OBJECT_EVENT} is set.
	 */
    public boolean isObjectCreated();

	/**
	 * Check if mask {@code MsoEventType.DELETED_OBJECT_EVENT} is set. 
	 * @return Returns {@code true} if mask 
	 * {@code MsoEventType.DELETED_OBJECT_EVENT} is set.
	 */
    public boolean isObjectDeleted();

	/**
	 * Check if mask {@code MsoEventType.MODIFIED_OBJECT_EVENT} is set. 
	 * @return Returns {@code true} if mask 
	 * {@code MsoEventType.MODIFIED_OBJECT_EVENT} is set.
	 */
    public boolean isObjectModified();

	/**
	 * Check if mask {@code MsoEventType.ADDED_RELATION_EVENT} is set. 
	 * @return Returns {@code true} if mask 
	 * {@code MsoEventType.ADDED_RELATION_EVENT} is set.
	 */
    public boolean isRelationAdded();

	/**
	 * Check if mask {@code MsoEventType.REMOVED_RELATION_EVENT} is set. 
	 * @return Returns {@code true} if mask 
	 * {@code MsoEventType.REMOVED_RELATION_EVENT} is set.
	 */
    public boolean isRelationRemoved();

	/**
	 * Check if mask {@code MsoEventType.ADDED_RELATION_EVENT} or
	 * {@code MsoEventType.REMOVED_RELATION_EVENT} is set. 
	 * @return Returns {@code true} if mask mask 
	 * {@code MsoEventType.ADDED_RELATION_EVENT} or
	 * {@code MsoEventType.REMOVED_RELATION_EVENT} is set.
	 */
    public boolean isRelationModified();

	/**
	 * Check if mask {@code MsoEventType.CLEAR_ALL_EVENT} is set. 
	 * @return Returns {@code true} if mask mask 
	 * {@code MsoEventType.CLEAR_ALL_EVENT} is set.
	 */
    public boolean isAllDataCleared();
	
    
    /**
     * Get loopback mode </p> 
     * 
     * </p>If the change is a loopback, no data is changed, 
     * only the <i>data state</i> is changed from LOCAL to REMOTE. 
     * The method can therefore be used to decide if data 
     * state rendering should be updated or not.</p>
     *   
     * <i>This flag is only true if all changes are loopbacks</i>.</p>
     * 
     * <b>NOTE</b>: <code>IChangeIf::isLoopbackMode</code> and 
     * <code>IMsoObjectIf::isLoopbackMode</code> may return different 
     * values. The reason is that <code>IMsoObjectIf::isLoopbackMode</code> 
     * checks if all it's data objects (attributes and references) are in 
     * loopback mode. If one or more data objects are not in 
     * loopback mode, the IMsoObjectIf object instance is not in loopback 
     * mode. The same rationale applies to the calculation of 
     * <code>IMsoObjectIf::isLoopbackMode</code>. However, since only
     * changed data objects is included in this calculation, a difference
     * may occur. </p>
     * @return Returns <code>true</code> if all all changes are loopbacks. 
     * @see See <code>IMsoUpdateStateIf</code> for more information. 
     */
    public boolean isLoopbackMode();
    
    /**
     * Get rollback mode </p> 
     * 
     * </p>If the change is a rollback, all data is a rollback 
     * from LOCAL to REMOTE. The method can be used to 
     * decide if data state rendering should be updated or not.</p>
     *   
     * <i>This flag is only true if all changes are rollbacks</i>.</p>
     * 
     * <b>NOTE</b>: <code>IChangeIf::isRollbackMode</code> and 
     * <code>IMsoObjectIf::isRollbackMode</code> may return different 
     * values. The reason is that <code>IMsoObjectIf::isRollbackMode</code> 
     * checks if all it's data objects (attributes and references) are in 
     * rollback mode. If one or more data objects are not in 
     * rollback mode, the IMsoObjectIf object instance is not in rollback 
     * mode. The same rationale applies to the calculation of 
     * <code>IMsoObjectIf::isRollbackMode</code>. However, since only
     * changed data objects is included in this calculation, a difference
     * may occur. </p>
     * @return Returns <code>true</code> if all all changes are rollbacks. 
     * @see See <code>IMsoUpdateStateIf</code> for more information. 	     */
    public boolean isRollbackMode();
    
    /**
     * Get the update mode of the model when the change occurred.</p>
     * 
     * The returned update mode is a union of the update 
     * mode of each individual change. If one of the changes
     * occurred during a local update, this mode overrides any 
     * previous found remote update modes (LOCAL_UPDATE_MODE is 
     * dominant)
     * 
     * @return Returns the update mode of the model when the change occurred.
     */    
    public UpdateMode getUpdateMode();
    
    /**
     * Get data object
     * 
     * @return Returns data object.
     */
    public IMsoDataIf getObject();
        
	/**
	 * Methods that used by the transaction handler when committing objects.
	 */
    public interface IChangeObjectIf extends IChangeIf
    {
        /**
        * Get the object to commit.
        */
        public IMsoObjectIf getMsoObject();
        
        
    }
    
    public interface IChangeAttributeIf extends IChangeIf {
        
    	/**
         * Get the object that owns the attribute.
         */
         public IMsoObjectIf getOwnerObject();
         
         /**
          * Get the attribute name
          */
         public String getName();
         
         /**
          * Get the value after the change
          * 
          * @return Returns the value after the change
          */
         public Object getValue();
         
         /**
          * Get a reference to the changed MSO attribute
          *  
          * @return Returns a reference to the changed MSO attribute
          */
         public IMsoAttributeIf<?> getMsoAttribute();
    	
    }

	/**
	 * Methods that used by the commit handler when committing references.
	 * Note that a relation is defined by a relation name and two MSO objects.
	 * The name is needed since some classes may have several types of relations 
	 * to another class.
	 * 
	 */
    public interface IChangeRelationIf extends IChangeIf
    {
        /**
        * Get name of reference.
        */
        public String getName();
        
        
        /**
         * Check if this list reference change (part of an 
         * one-to-many relation).
         * @return Returns {@code true} if change occurred in a MSO list.
         */
        public boolean isInList();

        /**
        * Get referring object (owner).
        */
        public IMsoObjectIf getReferringObject();

        /**
        * Get referred object (referenced object).
        */
        public IMsoObjectIf getReferredObject();            
        
        /**
         * Get reference object
         * 
         * @return Returns the object representing the reference between referring and referred objects
         */
        public IMsoRelationIf<?> getMsoRelation();
        
    }

}
