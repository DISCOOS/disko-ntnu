package org.redcross.sar.mso.data;

/**
 *
 */

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoModelIf.ModificationState;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.mso.Selector;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public interface IMsoListIf<M extends IMsoObjectIf>
{
    /**
     * Get name of list
     *
     * @return The name
     */
    public String getName();

    public IMsoObjectIf getOwner();

    /**
     * Get a java.util.Collection of the items in the list.
     *
     * @return All items (not marked for deletion in the list)
     */
    public Collection<M> getItems();

    /**
     * Get any item in the list.
     *
     * @return An item held by the list.
     */
    public M getItem();

    /**
     * Get an item in the list, with a given object ID.
     *
     * @param anObjectId The object ID
     * @return The item, if it exists, otherwise <code>null</code>.
     */
    public M getItem(String anObjectId);


    /**
     * Add an object to the list.
     *
     * @param anObject The object to add
     * @return <code>false</code> if the list already contains an object with 
     * the same object ID, if object is null or not properly initialized 
     * (<code>isSetup()==false</code>). <code>true</code> if added.  
     */
    public boolean add(M anObject);

    /**
     * Returns the number of objects in the list.
     *
     * @return the number of objects in the list.
     */
    public int size();

    /**
     * Remove reference to an object.
     * An object in a main list is removed completely, otherwise it is only removed from the current list.
     *
     * @param anObject The object to remove.
     * @return True if success, otherwise false.
     */
    public boolean remove(M anObject);

   /** 
    * Generate an List of selected items from the list.
    *
    * @param aSelector A {@link org.redcross.sar.util.mso.Selector} that is used for selecting items.
    * @return The generated list
    */
   public Set<M> selectItems(Selector<M> aSelector);

    /**
     * Generate an List of selected items from the list.
     *
     * @param aSelector A {@link org.redcross.sar.util.mso.Selector} that is used for selecting items.
     * @param aComparator {@link java.util.Comparator} that is used for determining the ordering the items, if <code>null</code>, no ordering will be done.
     * @return The generated list
     */
    public List<M> selectItems(Selector<M> aSelector, Comparator<M> aComparator);

    /**
     * Find an item in the list.
     *
     * @param aSelector A {@link org.redcross.sar.util.mso.Selector} that is used for selecting items.
     * @return The found item, or null if not found;
     */
    public M selectSingleItem(Selector<M> aSelector);

    /**
     * Check if the object exists only locally in the list (added locally only, thus not created remotely).
     *
     * @param anObject The object to match
     * @return <code>true</code> if the specific object exists locally only
     */
    public boolean isLocal(M anObject);
    
    /**
     * Check if the object exists only remotely in the list (created remotely, thus not added or deleted locally).
     *
     * @param anObject The object to match
     * @return <code>true</code> if the specific object exists remotely only
     */
    public boolean isRemote(M anObject);
    
    /**
     * Check if the object is deleted locally only (created remotely, deleted locally).
     *
     * @param anObject The object to match
     * @return <code>true</code> if the specific object is deleted locally
     */
    public boolean isDeleted(M anObject);
    
    /**
     * Check if the object is pending to be deleted after a client update.
     *
     * @param anObject The object to match
     * @return <code>true</code> if the specific object is pending to be deleted after a client update.
     */
    public boolean isDeleting(M anObject);
    
    /**
     * Check if the list contains a specific object that exists (created remotely, not deleted locally).
     *
     * @param anObject The object to match
     * @return <code>true</code> if the specific object exists. Same as <code>exists():=isRemote()||isLocal()</code>
     */
    public boolean exists(M anObject);

    /**
     * Check if the list contains a specific object, regardless of state (exists either locally or remotely, or deleted locally).
     *
     * @param anObject The object to match
     * @return <code>true</code> if the specific object is contained in the list. Same as <code>exists():=isRemote()||isLocal()||isDeleted()</code>
     */
    public boolean contains(M anObject);
    
    /**
     * Get the {@link org.redcross.sar.mso.IMsoModelIf.ModificationState ModificationState} of the specified object
     *
     * @param anObject The tested reference.
     * @return Potential return values are </p>
     * <ol>
     * 	<li> <code>STATE_UNDEFINED</code>: object does not exist, same <code>!exists()</code>
     * 	<li> <code>STATE_SERVER</code>: same as <code>isRemote()</code>
     * 	<li> <code>STATE_LOCAL</code>: same as <code>isLocal()</code>
     * </ol> </p>
     * 
     * The state <code>STATE_CONFLICTING</code> is not defined for individual items in the 
     * list. For lists, conflicts occur when the local list and the remote list on the server 
     * differ during an remote update. However, during updates only one change at the time is
     * received from the server. The server state of the list (existing objects at given point 
     * in time) is thus not known and can therefore not be assumed to be synchronized. 
     * Consequently, a STATE_CONFLICTING state is not feasible to
     *  
     */
    public ModificationState getState(M aObject);    
    
    /**
     * Get a clone of the list.
     *
     * The cloned list will be a copy that refers to the same objects
     * @return The cloned list.
     */
    public IMsoListIf<M> getClone();
    
    /**
     * Get list cardinality
     * 
     *@return cardinality, if >0, then list can not be empty. 
     */
    public int getCardinality();

    /**
     * Validates list and object states (cardinality of attributes and relations)
     * <p/>
     * @return  <code>true</code> if the list cardinality and all object states are valid, 
     * <code>false</code> is list cardinality is violated and <code>IMsoObjectIf</code> otherwise.
     */
    public Object validate();
    
    
    public int getChangeCount();    

}
