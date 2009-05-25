package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IMsoModelIf.ModificationState;

import java.util.Collection;
import java.util.Vector;

/**
 * /**
 */
public interface IMsoReferenceIf<T extends IMsoObjectIf> extends IMsoDataStateIf
{
	/**
	 * Get the unique reference name.
	 *  
	 * @return Returns the unique reference name
	 */
    public String getName();
    
    /**
     * Get the reference owner.
     * 
     * @return Returns the reference owner
     */
    public IMsoObjectIf getOwner();

    /**
     * Set the deleteable state of this reference.
     * 
     * @param isDeleteable - the new deleteable state
     */
    public void setDeletable(boolean isDeleteable);

    /**
     * Get the deleteable state of this reference.
     * 
     * @return Returns the deleteable state of this reference
     */
    public boolean isDeletable();

    /**
     * Get the modification state of this object.
     * 
     * @return Returns the modification state of this reference
     */
    public ModificationState getState();

    /**
     * Check if the modification equals the given state.
     * 
     * @param state - the state to check for
     * @return Returns <code>true</code> is the modification state equals
     * the given state.
     */
    public boolean isState(ModificationState state);

    /**
     * Get remote and local reference values if modification
     * state is CONFLICT.
     * @return A vector of the remote and local reference values.
     */
    public Vector<T> getConflictingValues();

    /**
     * Perform a rollback on the reference
     *
     * @return True if something has been done.
     */
    public boolean rollback();
    
    /**
     * Keep the local reference value of a conflict. This clear the CONFLICT state
     * to LOCAL. 
     * @return Returns <code>true</code> if conflict is resolved.
     */
    public boolean acceptLocal();
    
    /**
     * Overwrite local reference value with remote reference value. This
     * clear the CONFLICT state to REMOTE.
     * @return Returns <code>true</code> if conflict is resolved.
     */
    public boolean acceptRemote();

    /**
     * Get the referenced object.
     * 
     * @return Returns the referenced object.
     */
    public T getReference();

    /**
     * Get the object referenced locally.
     * 
     * @return Returns the object referenced locally.
     */
    public T getLocalReference();
    
    /**
     * Get the object referenced remotely.
     * 
     * @return Returns the object referenced remotely.
     */
    public T getRemoteReference();
    
    /**
     * @param aReference - the object reference or <code>null</code>
     * @return <code>false</code> if object reference is <code>null</code> 
     * and cardinality is greater than <code>0</code>, <code>true</code> otherwise . 
     */
    public boolean setReference(T aReference);

    /**
     * Get value cardinality
     *
     *@return cardinality, if >0, then getReference can not be null.
     */
    public int getCardinality();

    /**
     * Validates getAttrValue against the value cardinality
     * <p/>
     * @return  <code>true<code> if getReference is not null, <code>false<code> otherwise.
     */
    public boolean validate();
    
    /**
     * Get list of changed references.
     * 
     * @return Returns list of changed references.
     */
    public Collection<IChangeIf.IChangeReferenceIf> getChangedReferences();
    
    /**
     * Get sub-list of changed references given the partial list.
     * 
     * @return Returns sub-list of changed references.
     */
    public Collection<IChangeIf.IChangeReferenceIf> getChangedReferences(Collection<IChangeIf> partial);

}
