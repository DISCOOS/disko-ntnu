package org.redcross.sar.mso;

import java.util.Collection;
import java.util.List;

import org.redcross.sar.mso.IChangeIf.IChangeAttributeIf;
import org.redcross.sar.mso.IChangeIf.IChangeObjectIf;
import org.redcross.sar.mso.IChangeIf.IChangeRelationIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoRelationIf;
import org.redcross.sar.mso.event.MsoEvent;

/**
 * The IChangeSourceIf interface is used by the IMsoTransactionManagerIf object 
 * to collect information about the changes made in a IMsoObjectIf object
 * 
 * @author vinjar, kenneth
 *
 */

@SuppressWarnings("unchecked")
public interface IChangeRecordIf extends Comparable<IChangeRecordIf> {

	/**
	 * Get change record set sequence number. </p>
	 * 
	 * This number equals the lowest change sequence number in record.
	 * 
	 * @return Returns lowest change sequence number. If no change is 
	 * recorded, -1 is returned.
	 */
	public long getSeqNo();
	
	/**
	 * Get maximum recorded sequence number. </p>
	 * 
	 * This number equals the lowest change sequence number in record.
	 * 
	 * @return Returns highest recorded change sequence number. If no change is 
	 * recorded, or sequence numbers are disabled, -1 is returned.
	 */
	public long getNextSeqNo();
	
	/**
	 * Get sequence number state.
	 * 
	 * @return Returns {@code true} is sequence numbers are on.
	 */
	public boolean isSeqNoEnabled();
	
	/**
	 * Enable or disable sequence number. If on, the change sequence number
	 * is set for each change.
	 * 
	 * @param isEnabled - the state
	 */
	public void setSeqNoEnabled(boolean isEnabled);
	
	/**
	 * Check if changes are sorted. Default value is {@code false}.
	 * 
	 * @return Returns {@code true} if changes are sorted. 
	 */
	public boolean isSorted();
	
	/**
	 * Set sorting behavior of record set list. </p>
	 * 
	 * If set to {@code true}, all lists of changes are 
	 * sorted before they are returned. </p> 
	 * 
	 * Changes are sorted ascending on the change sequence number
	 * {@code IChangeIf.getSeqNo()} (the order they occurred).
	 */
	public void setSorted(boolean isSorted);
	
	/**
	 * Get owner of the changes.
	 *  
	 * @return Returns the IMsoObjectIf instance that owns the changes 
	 */
	public IMsoObjectIf getMsoObject();
	
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
	 * Get the changes exists flag. Changes exists as long
	 * as {@code getMask()} is greater than zero and the object
	 * is not both created and deleted. Objects that are both
	 * created and deleted is by definition not changed because they
	 * never existed remotely. Hence, {@code isChanged()} returns 
	 * {@code false} if the object is both created and deleted.
	 *  
	 * @return Returns {@code true} if changes exists.
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
     * Get update loopback mode </p> 
     * 
     * If the update is a loopback, no data is changed, 
     * only the <i>data state</i> is changed from LOCAL to REMOTE. 
     * The method can therefore be used to decide if data 
     * state rendering should be updated or not.</p>
     *   
     * The loopback flag is only set in remote update mode (see 
     * {@code getUpdateMode()}).</p>
     * 
     * <i>This flag is only true if all changes are loopbacks</i>.</p>
     * 
     * <b>NOTE</b>: <code>IChangeRecordIf::isLoopbackMode</code> and 
     * <code>IMsoObjectIf::isLoopbackMode</code> may return different 
     * values. The reason is that <code>IMsoObjectIf::isLoopbackMode</code> 
     * checks if all it's data objects (attributes and references) are in 
     * loopback mode. If one or more data objects are not in 
     * loopback mode, the IMsoObjectIf object instance is not in loopback 
     * mode. The same rationale applies to the calculation of 
     * <code>IMsoObjectIf::isLoopbackMode</code>. However, since only
     * changed data objects is included in this calculation, a difference
     * may occur. </p>
     * @return Returns <code>true</code> if all changes are loopbacks. 
     * @see See <code>IMsoUpdateStateIf</code> and
     * {@code getUpdateMode()} for more information. 
     */
    public boolean isLoopbackMode();
    
    /**
     * Get update rollback mode </p> 
     * 
     * If the update is a rollback, all data is a rollback 
     * from LOCAL to REMOTE. The method can be used to 
     * decide if data state rendering should be updated or not. </p>
     * 
     * The rollback flag is only set in local update mode.</p>
     *   
     * <i>This flag is only true if all changes are rollbacks</i>.</p>
     * 
     * <b>NOTE</b>: <code>IChangeRecordIf::isRollbackMode</code> and 
     * <code>IMsoObjectIf::isRollbackMode</code> may return different 
     * values. The reason is that <code>IMsoObjectIf::isRollbackMode</code> 
     * checks if all it's data objects (attributes and references) are in 
     * rollback mode. If one or more data objects are not in 
     * rollback mode, the IMsoObjectIf object instance is not in rollback 
     * mode. The same rationale applies to the calculation of 
     * <code>IMsoObjectIf::isRollbackMode</code>. However, since only
     * changed data objects is included in this calculation, a difference
     * may occur. </p>
     * @return Returns <code>true</code> if all changes are rollbacks. 
     * @see See <code>IMsoUpdateStateIf</code> and 
     * {@code getUpdateMode()} for more information. 	     
     */
    public boolean isRollbackMode();
    
    /**
     * Get the recorded update mode.</p>
     *
     * 
     * @return Returns the recorded update mode.
     */    
    public UpdateMode getUpdateMode();
        
	/**
	 * Get list of changes in given object
	 * @param objectId - the object id
	 * @return Returns list of changes in given object
	 */
	public List<IChangeIf> get(String objectId);
	
	/**
	 * Get changes for given MSO object
	 * @param object - the changed MSO object
	 * @return Returns the change of given MSO object
	 */
	public List<IChangeObjectIf> get(IMsoObjectIf anObject);
	
	/**
	 * Get change for given attribute
	 * @param object - the changed attribute
	 * @return Returns the change of given attribute
	 */
	public IChangeAttributeIf get(IMsoAttributeIf<?> anAttribute);
	
	/**
	 * Get change for given reference
	 * @param object - the changed reference
	 * @return Returns the change of given reference
	 */
	public List<IChangeRelationIf> get(IMsoRelationIf<?> aReference);
	
	/**
	 * Add a new change to the record.
	 * 
	 * @param aChange - the change to add to the record
	 * @param difference - if {@code true}, a loopback or rollback 
	 * change remove previous changes recorded on the same 
	 * data object. If {@code false}, a loopback or rollback change 
	 * are just added to previous changes. </p>
	 * 
	 * Note that the change record references the actual change object, 
	 * and may alter it (sequence number) according to current 
	 * state (setting the sequence number is enabled).
	 * 
	 * @return Returns {@code true} if changes processed.
	 */
	public boolean record(IChangeIf aChange, boolean difference);
	
	/**
	 * Remove a change from the record.
	 * @param aChange - the change to remove from the record
	 * @return Returns {@code true} is change was removed.
	 */
	public boolean remove(IChangeIf aChange);
	
	/**
	 * Clear all changes from this change record.
	 */
	public void clear();
		
    /**
     * Create a union of changes in this with changes 
     * in given change record. 
     * @param aChange - the change record to union with this
     * @param compress TODO
     * 
     * @return Returns {@code true} if union was performed on this.
     */
    public boolean union(IChangeRecordIf aChange, boolean compress);	    
    
	/**
	 * Create a complement of changes in this change record 
	 * with respect to the changes in the given change 
	 * record. This is the same as taking a <i>set difference</i> 
	 * on the changes in this with respect to the changes in 
	 * the given change record. 
	 * @param rs - the given change record.
	 * @return Returns {@code true} if difference was performed on this. 
	 */
	public boolean difference(IChangeRecordIf rs);
	
    /**
     * Get list of recorded changes.
     *  
     * If filters are active, a sub-list is returned. 
     * The list is sorted on ascending change sequence 
     * number {@code getSeqNo()}.
     * 
     * @return Returns a list of recorded changes.
     */
	public List<IChangeIf> getChanges();
    	
    /**
     * Get a copy of the list of recorded object changes. </p>
     * 
     * If filters are active, a sub-list is returned. 
     * The list is sorted on ascending change sequence 
     * number {@code getSeqNo()}.
     *   
     * @return Returns a copy of the list of recorded object changes.
     */
	public List<IChangeObjectIf> getObjectChanges();
	
    /**
     * Get a copy of the list of recorded attribute changes. </p>
     * 
     * If filters are active, a sub-list is returned. 
     * The list is sorted on ascending change sequence 
     * number {@code getSeqNo()}.
     * 
     * @return Returns a copy of the list of recorded attribute changes.
     */    
    public Collection<IChangeIf.IChangeAttributeIf> getAttributeChanges();
    
    /**
     * Get a copy of the list of recorded relation changes.  </p>
     * 
     * If filters are active, a sub-list is returned. 
     * The list is sorted on ascending change sequence 
     * number {@code getSeqNo()}.
     *  
     * @return Returns a copy of the list of recorded relation changes.
     */
    public Collection<IChangeIf.IChangeRelationIf> getRelationChanges();
    
    /**
     * Get a copy of the list of recorded object (one-to-one) 
     * relation changes.  </p>
     * 
     * If filters are active, a sub-list is returned. 
     * The list is sorted on ascending change sequence 
     * number {@code getSeqNo()}.
     *  
     * @return Returns a copy of the list of recorded 
     * object relation changes.
     */
    public Collection<IChangeIf.IChangeRelationIf> getObjectReferenceChanges();

    /**
     * Get a copy of the list of recorded list (one-to-many) 
     * relation changes.  </p>
     * 
     * If filters are active, a sub-list is returned. 
     * The list is sorted on ascending change sequence 
     * number {@code getSeqNo()}.
     *  
     * @return Returns a copy of the list of recorded 
     * list relation changes.
     */
    public Collection<IChangeIf.IChangeRelationIf> getListReferenceChanges();
    
	/**
	 * Check if changes are filtered
	 * @return Returns <code>true</code> if changes are filtered.
	 */
    public boolean isFiltered();
    
    public boolean setFilter(String anAttribute);       
    public boolean setFilter(IMsoObjectIf aReference);   
    public boolean setFilter(IMsoRelationIf<?> aReference);   
    public boolean setFilter(IMsoAttributeIf<?> anAttribute);   
    
    public boolean addFilter(String anAttribute);
    public boolean addFilter(IMsoObjectIf aReference);
    public boolean addFilter(IMsoRelationIf<?> aReference);
    public boolean addFilter(IMsoAttributeIf<?> anAttribute);
    
    public boolean removeFilter(String anAttribute);
    public boolean removeFilter(IMsoObjectIf aReference);
    public boolean removeFilter(IMsoRelationIf<?> aReference);
    public boolean removeFilter(IMsoAttributeIf<?> anAttribute);
    
    public boolean containsFilter(String anAttribute);
    public boolean containsFilter(IMsoObjectIf aReference);
    public boolean containsFilter(IMsoRelationIf<?> aReference);
    public boolean containsFilter(IMsoAttributeIf<?> anAttribute);
    
    /**
     * Get filters.
     */
    public List<IChangeIf> getFilters();
    
    /**
     * Clear filters.
     */
    public void clearFilters();
    
    /**
     * Clear only filters found in given record.
     */
    public void clearFilters(IChangeRecordIf rs);
    
    /**
     * This interface create change sequence numbers. 
     *   
     * @author kenneth
     *
     */
    public interface ISeqNoGenIf {
    	
    	/**
    	 * Create ascending change sequence number 
    	 * @return Returns change sequence number.
    	 */
    	public long createSeqNo();
    	
    	/**
    	 * Get change record set sequence number. </p>
    	 * 
    	 * This number equals the lowest change sequence number in record.
    	 * 
    	 * @return Returns lowest change sequence number. If no change is 
    	 * recorded, -1 is returned.
    	 */
    	public long getNextSeqNo();
    	
    	/**
    	 * Get sequence number state.
    	 * 
    	 * @return Returns {@code true} is sequence numbers are on.
    	 */
    	public boolean isSeqNoEnabled();
    	
    	/**
    	 * Enable or disable sequence number. If on, the change sequence number
    	 * is set for each change.
    	 * 
    	 * @param isEnabled - the state
    	 */
    	public void setSeqNoEnabled(boolean isEnabled);    	
    	
    }
    
}
