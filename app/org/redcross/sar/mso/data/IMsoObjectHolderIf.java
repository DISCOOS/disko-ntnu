package org.redcross.sar.mso.data;

/**
 * Interface for IMsoObjectIf holders.</p>
 * 
 * The purpose of this interface is to define functionality for managing
 * global deletion (remove all references) of MsoObject. 
 * 
 * @author vinjar, kenneth
 */
public interface IMsoObjectHolderIf
{
    /**
     * Check if a reference to object can be deleted.
     *
     * @param anObject The object to delete a reference from
     * @return <code>true</code> if referenced object exists and can be deleted, <code>false</code> otherwise
     */
    public boolean isRelationDeletable(IMsoObjectIf anObject);

    /**
     * Deletes a reference to the given IMsoObjectIf object if possible.
     *
     * @param anObject The object to delete
     * @return <code>true</code> if the reference has been deleted, <code>false</code> otherwise.
     */
    public boolean deleteRelation(IMsoObjectIf anObject);
}
