package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.util.except.TransactionException;

import java.util.Collection;
import java.util.Vector;

/**
 * /**
 */
public interface IMsoRelationIf<T extends IMsoObjectIf> extends IMsoDataIf
{
	
	/**
	 * Get the unique relation name.
	 *  
	 * @return Returns the unique relation name
	 */
    public String getName();
    
    /**
     * Get the relation owner.
     * 
     * @return Returns the relation owner
     */
    public IMsoObjectIf getOwnerObject();

    /**
     * Set the deleteable state of the relation to the related object.
     * 
     * @param isDeleteable - the new deleteable state
     */
    public void setDeletable(boolean isDeleteable);

    /**
     * Check if the relation to the related object can be deleted.
     * 
     * @return Returns <code>true</code> if relation can be deleted.
     */
    public boolean isDeletable();

    /**
     * Check if this is a list relation (part of an one-to-many relation)
     * @return Returns {@code true} if reference is in a MSO list.
     */
    public boolean isInList();
    
    /**
     * Get current data origin 
     * @return Returns current data origin.
     */    
    public IData.DataOrigin getOrigin();

    /**
     * Check if the modification equals the given state.
     * 
     * @param state - the state to check for
     * @return Returns <code>true</code> is the modification state equals
     * the given state.
     */
    public boolean isOrigin(IData.DataOrigin state);

	/**
     * Check if data is in more than one origin.</p>
     * 
	 * @returns Since relations only have one data object, mixed origin states are not
	 * possible. This method returns therefore always <code>false</code>.
	 */
    public boolean isOriginMixed();
    
    /**
     * Get remote and local relation values if modification
     * state is CONFLICT.
     * @return A vector of the remote and local relation values.
     */
    public Vector<T> getConflictingValues();

    
    /**
     * 
     * Commit changes to remote sources
     *
     * @return Returns <code>true</code> if changes was committed.
     * @throws TransactionException
     */
    public boolean commit() throws TransactionException;
    
    /**
     * Perform a rollback on the relation
     *
     * @return True if something has been done.
     */
    public boolean rollback();
    
    /**
     * Keep the local relation value of a conflict. This clear the CONFLICT state
     * to LOCAL. 
     * @return Returns <code>true</code> if conflict is resolved.
     */
    public boolean acceptLocal();
    
    /**
     * Overwrite relationerence value with remote relation value. This
     * clear the CONFLICT state to REMOTE.
     * @return Returns <code>true</code> if conflict is resolved.
     */
    public boolean acceptRemote();

    /**
     * Get the related object.
     * 
     * @return Returns the related object.
     */
    public T get();

    /**
     * Get the object related locally.
     * 
     * @return Returns the object related locally.
     */
    public T getLocal();
    
    /**
     * Get the object related remotely.
     * 
     * @return Returns the object related remotely.
     */
    public T getRemote();
    
    /**
     * Set related object. A new relation is only allowed if:
     * <ol>
     * 	<li>object is properly initialized</li>
     * 	<li>the new relation is not <code>null</code> when cardinality 
     * 		is greater than <code>0</code>
     *  </li>
     *  <li>the relation is part of a list and the list does
     * 		not allow the change. List relations has always cardinality 
     * 		equal 1. The relation can thus not be null. Furthermore, each
     * 		relation in a list relation must be unique. Hence, if the new 
     * 		object relation already exists in the list, the change will be
     * 		rejected.
     * 	</li>
     * </ol>
     * <p> </p>
     *  
     * 
     * @param aRelation - the object relation or <code>null</code>
     * @return <code>false</code> if object is not properly initialized, if 
     * relation is <code>null</code> and cardinality is greater than 
     * <code>0</code>, or if this relation is part of a list and the list does
     * not allow the change, <code>true</code> otherwise. 
     */
    public boolean set(T aRelation);

    /**
     * Get value cardinality
     *
     *@return cardinality, if >0, then getRelation can not be null.
     */
    public int getCardinality();

    /**
     * Validates related object against the value cardinality
     * <p/>
     * @return  <code>true<code> if getRelation is not null, <code>false<code> otherwise.
     */
    public boolean validate();
    
    /**
     * Get list of changed relations.
     * 
     * @return Returns list of changed relations.
     */
    public Collection<IChangeIf.IChangeRelationIf> getChanges();
    
    /**
     * Not in use
     */
    public int compareTo(IData o);
    
}
