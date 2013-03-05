package org.redcross.sar.mso.data;

/**
 *
 */

import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.data.IMsoObjectIf.IObjectIdIf;
import org.redcross.sar.util.except.TransactionException;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public interface IMsoListIf<M extends IMsoObjectIf> extends IMsoDataIf
{
    /**
     * Get name of list
     *
     * @return The name
     */
    public String getName();

    /**
     * The list owner 
     */
    public IMsoObjectIf getOwner();
    
    /**
     * Check the main list flag. </p>
     * If <code>true</code>, the list is the owner of the 
     * referenced objects. When references to objects in main 
     * lists are deleted, the objects are also deleted. </p>
	 *
     * @return <code>true</code> if main list, <code>false</code> otherwise.
     */
    public boolean isMain();

    /**
     * Get a java.util.Collection of the referenced items in the list.
     *
     * @return All referenced items (not marked for deletion in the list)
     */
    public Collection<M> getObjects();

    /**
     * Get first item referenced by the list.
     *
     * @return An the first item referenced by the list.
     */
    public M getHeadObject();

    /**
     * Get an referenced item in the list, with a given object ID object.
     *
     * @param anObjectId The object ID
     * @return The referenced item, if it exists, otherwise <code>null</code>.
     */
    public M getObject(IObjectIdIf anObjectId);
    
    /**
     * Get an referenced item in the list, with a given object ID string.
     *
     * @param anObjectId The object ID
     * @return The referenced item, if it exists, otherwise <code>null</code>.
     */
    public M getObject(String anObjectId);

    /**
     * Get a java.util.Collection of the references in the list.
     *
     * @return All references (not marked for deletion in the list)
     */
    public Collection<IMsoRelationIf<M>> getRelations();

    /**
     * Get first item in the list.
     *
     * @return The first reference in the list.
     */
    public IMsoRelationIf<M> getHeadRelation();

    /**
     * Get an reference in the list, that reference a given object ID object.
     *
     * @param anObjectId The object ID of the referenced object
     * @return The reference, if it exists, otherwise <code>null</code>.
     */
    public IMsoRelationIf<M> getRelation(IObjectIdIf anObjectId);
    
    /**
     * Get an reference in the list, that reference a given object ID string.
     *
     * @param anObjectId The object ID of the referenced object
     * @return The reference, if it exists, otherwise <code>null</code>.
     */
    public IMsoRelationIf<M> getRelation(String anObjectId);

    /**
     * Get an reference in the list, that reference a given object.
     *
     * @param anObject The referenced object
     * @return The reference, if it exists, otherwise <code>null</code>.
     */
    public IMsoRelationIf<M> getRelation(M anObject);
    
    /**
     * Returns the number of objects in the list.
     *
     * @return the number of objects in the list.
     */
    public int size();

    /**
     * Add the given object to the list.
     *
     * @param anObject The object to add
     * @return <code>false</code> if the list already contains an object with 
     * the same object ID, if object is null or not properly initialized 
     * (<code>isSetup()==false</code>). <code>true</code> if added.  
     */
    public boolean add(M anObject);

    /**
     * Remove the given object from the list. </p>
     * 
     * An object in a main list is removed completely, otherwise it 
     * is only removed from the current list.
     *
     * @param anObject The object to remove.
     * @return True if success, otherwise false.
     */
    public boolean remove(M anObject);

    /**
     * Delete all objects in the list</p>
     * 
     * Can be optimized, but has probably very little effect, as the list normally will be quite short.
     */
    public void removeAll();    
    
    /**
     * 
     * Commit changes to remote sources
     *
     * @return Returns <code>true</code> if changes was committed.
     * @throws TransactionException
     */
    public boolean commit() throws TransactionException;
    
    /**
     * Rollback changes in this list.  
     * Generates a update events if changes are rolled back.
     * @return Returns {@code true} is changes was rolled back, 
     * {@code false} otherwise.
     */    
    public boolean rollback();
    
   /** 
    * Generate an List of selected items from the list.
    *
    * @param aSelector A {@link org.redcross.sar.data.Selector} that is used for selecting items.
    * @return The generated list
    */
   public Set<M> selectItems(Selector<M> aSelector);

    /**
     * Generate an List of selected items from the list.
     *
     * @param aSelector A {@link org.redcross.sar.data.Selector} that is used for selecting items.
     * @param aComparator {@link java.util.Comparator} that is used for determining the ordering the items, if <code>null</code>, no ordering will be done.
     * @return The generated list
     */
    public List<M> selectItems(Selector<M> aSelector, Comparator<M> aComparator);

    /**
     * Find an item in the list.
     *
     * @param aSelector A {@link org.redcross.sar.data.Selector} that is used for selecting items.
     * @return The found item, or null if not found;
     */
    public M selectSingleItem(Selector<M> aSelector);

    /**
     * Check if the object is changed locally (added or deleted).
     *
     * @param anObject The object to match
     * @return <code>true</code> if the specific object exists locally
     */
    public boolean isOriginLocal(M anObject);
    
    /**
     * Check if the object exists remotely (created).
     *
     * @param anObject The object to match
     * @return <code>true</code> if the specific object exists remotely
     */
    public boolean isOriginRemote(M anObject);
    
    /**
     * Check if object is added
     * @param anObject
     * @return
     */
    public boolean isAdded(IMsoObjectIf anObject);
    
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
    public boolean exists(IMsoObjectIf anObject);

    /**
     * Check if the list contains a specific object, regardless of state (exists either locally or remotely, or deleted locally).
     *
     * @param anObject The object to match
     * @return <code>true</code> if the specific object is contained in the list. Same as <code>isRemote()||isLocal()||isDeleted()</code>
     */
    public boolean contains(IMsoObjectIf anObject);
    
    /**
     * 
     * @param anObject - the reference to check origin of
     * @param origin - the origin to match
     * @return Returns <code>true</code> if the origin of the reference to the given 
     * object match the given origin.
     */
    public boolean isOrigin(M anObject, IData.DataOrigin origin);
    
    /**
     * Get the {@link org.redcross.sar.data.IData.DataOrigin origin} of the specified object
     *
     * @param anObject The reference.
     * @return Potential return values are </p>
     * <ol>
     * 	<li> <code>NONE</code>: object does not exist, same <code>!exists()</code>
     * 	<li> <code>REMOTE</code>: same as <code>isOriginRemote(anObject)</code>
     * 	<li> <code>LOCAL</code>: same as <code>isOriginLocal(anObject)</code>
     * </ol> </p>
     * 
     * The <code>CONFLICT</code> state is not defined for individual items in a
     * one-to-many relation (list). For lists, conflicts can only occur if the
     * position in the list is relevant. If the position is relevant, adding and
     * removing an reference to a list in a given position, a conflict can occur.
     * The position is undefined in MSO models.  Consequently, a CONFLICT state is 
     * undefined accordingly.
     *  
     */
    public IData.DataOrigin getOrigin(M aObject);    
    
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
    
    /**
     * The number of changes since last commit 
     */
    public int getChangeCount();
    
    /**
     * Get list owner
     *
     * @return Reference to IMsoObjectIf object.
     */
    public IMsoObjectIf getOwnerObject();
    
    /**
     * Get the list object class
     */
    public Class<M> getObjectClass();
    
    /**
     * Get list of changed references.
     * 
     * @return Returns list of changed references
     */
    public Collection<IChangeIf.IChangeRelationIf> getChanges();

}
