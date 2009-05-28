package org.redcross.sar.mso;

import java.util.Collection;
import java.util.List;

import org.redcross.sar.mso.IChangeIf.IChangeObjectIf;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoReferenceIf;
import org.redcross.sar.mso.event.MsoEvent;

/**
 * The IChangeSourceIf interface is used by the IMsoTransactionManagerIf object 
 * to collect information about the changes made in a IMsoObjectIf object
 * 
 * @author vinjar, kenneth
 *
 */

@SuppressWarnings("unchecked")
public interface IChangeRecordIf {

	/**
	 * Get owner of the changes.
	 *  
	 * @return Returns the IMsoObjectIf instance that owns the changes 
	 */
	public IMsoObjectIf getMsoObject();
	
	/**
	 * Get the update mask for this object.
	 * @return Returns the update mask for this object
	 * @see MsoEvent
	 */
	public int getMask();	
    
    /**
     * Check if object is deleted
     * 
     * @return
     */
    public boolean isDeleted();

    /**
     * Check if object exist only locally
     * @return
     */
    public boolean isCreated();

    /**
     * Check if an attribute is changed
     * 
     * @return
     */
    public boolean isModified();

    /**
     * Check if a reference is changed
     * @return
     */
    public boolean isReferenceChanged();
    
	/**
	 * Check if changes are filtered
	 * @return Returns <code>true</code> if changes are filtered.
	 */
    public boolean isFiltered();
    
    public boolean setFilter(String attribute);       
    public boolean setFilter(IMsoObjectIf referenced);   
    public boolean setFilter(IMsoReferenceIf<?> reference);   
    public boolean setFilter(IMsoAttributeIf<?> attribute);   
    
    public boolean addFilter(String attribute);
    public boolean addFilter(IMsoObjectIf referenced);
    public boolean addFilter(IMsoReferenceIf<?> reference);
    public boolean addFilter(IMsoAttributeIf<?> attribute);
    
    public boolean removeFilter(String attribute);
    public boolean removeFilter(IMsoObjectIf referenced);
    public boolean removeFilter(IMsoReferenceIf<?> reference);
    public boolean removeFilter(IMsoAttributeIf<?> attribute);
    
    /**
     * Clear filters.
     */
    public void clearFilters();
    
    /**
     * Get list of changed data. If filters are active, a 
     * sub-set of all changes are returned.
     *   
     * @return Returns a list of changed data
     */
	public List<IChangeIf> getChanges();
    
    /**
     * Get the changed object. 
     *   
     * @return Returns the object that has changed 
     */
	public IChangeObjectIf getChangedObject();
	
    /**
     * Get list of changed attributes.
     * @return Return list of attributes changed locally
     */    
    public Collection<IChangeIf.IChangeAttributeIf> getChangedAttributes();
    
    /**
     * Get the list of changed object (one-to-one) references.
     *  
     * @return Returns a list of changed object references
     */
    public Collection<IChangeIf.IChangeReferenceIf> getChangedObjectReferences();

    /**
     * Get the list of changed list (one-to-many) references.
     *  
     * @return Returns a list of changed list references
     */
    public Collection<IChangeIf.IChangeReferenceIf> getChangedListReferences();

}
