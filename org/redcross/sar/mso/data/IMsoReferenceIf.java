package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf.ModificationState;

import java.util.Vector;

/**
 * /**
 */
public interface IMsoReferenceIf<T extends IMsoObjectIf>
{
    public String getName();

    public T getReference();

    public void setCanDelete(boolean canDelete);

    public boolean canDelete();

    public ModificationState getState();

    public boolean isState(ModificationState state);

    public Vector<T> getConflictingValues();

    public void rollback();

    public boolean acceptLocal();

    public boolean acceptServer();

    public boolean isUncommitted();

    /**
     * 
     * @param aReference - the object reference or <code>null</code>
     * @return <code>false</code> if object reference is <code>null</code> 
     * and cardinality is greater than <code>0</code>, <code>true</code> otherwise . 
     */
    public boolean setReference(T aReference);

    public int getChangeCount();


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


}
