package org.redcross.sar.mso.data;

/**
 *
 */

import org.redcross.sar.mso.IMsoModelIf;
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
     * @throws DuplicateIdException if the list already contains an object with the same object ID.
     */
    public void add(M anObject);

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
    public boolean removeReference(M anObject);

    /**
     * Get the {@link org.redcross.sar.mso.IMsoModelIf.ModificationState ModificationState} of the reference
     *
     * @param aReference The tested reference.
     * @return The state, possible values are: {@link org.redcross.sar.mso.IMsoModelIf.ModificationState#STATE_SERVER},
     *         {@link org.redcross.sar.mso.IMsoModelIf.ModificationState#STATE_LOCAL} and, if the object doesn't exist in the list, {@link org.redcross.sar.mso.IMsoModelIf.ModificationState#STATE_UNDEFINED}
     */
    public IMsoModelIf.ModificationState getState(M aReference);

    /**
     * Generate an List of selected items from the list.
     *
     * @param aSelector A {@link org.redcross.sar.util.mso.Selector} that is used for selecting items.
     * @param aComparator {@link java.util.Comparator} that is used for determining the ordering the items, if <code>null</code>, no ordering will be done.
     * @return The generated list
     */
    public List<M> selectItems(Selector<M> aSelector, Comparator<M> aComparator);


    public Set<M> selectItems(Selector<M> aSelector);

    /**
     * Find an item in the list.
     *
     * @param aSelector A {@link org.redcross.sar.util.mso.Selector} that is used for selecting items.
     * @return The found item, or null if not found;
     */
    public M selectSingleItem(Selector<M> aSelector);

    /**
     * Check if the list contains a specific object.
     *
     * @param anObject Object whose presence in this list is to be tested
     * @return <code>true</code> if this list contains the specific object's ObjectID
     */
    public boolean contains(M anObject);

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
