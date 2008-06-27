package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

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

    public IMsoModelIf.ModificationState getState();

    public Vector<T> getConflictingValues();

    public void rollback();

    public boolean acceptLocal();

    public boolean acceptServer();

    public boolean isUncommitted();

    public void setReference(T aReference);
    
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
