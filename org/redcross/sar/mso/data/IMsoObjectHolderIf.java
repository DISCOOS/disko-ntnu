package org.redcross.sar.mso.data;

/**
 * Interface for MsoObject holders.
 * The purpose of this interface is to define functionality for managing global deletion (remove all references) of MsoObject.
 */
public interface IMsoObjectHolderIf<M extends IMsoObjectIf>
{
    /**
     * Check if a MsoObject can be deleted.
     *
     * @param anObject The object to deleteObject
     * @return <code>true</code> if reference can be deleted, <code>false</code> otherwise
     */
    public boolean canDeleteReference(M anObject);

    /**
     * Delete a MsoObject.
     *
     * @param anObject The object to deleteObject
     * @return <code>true</code> if the reference has been deleted, <code>false</code> otherwise.
     */
    public boolean doDeleteReference(M anObject);
}
