package org.redcross.sar.mso;

import java.util.List;

import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;

/**
 * The IChangeSourceIf interface is used by the IMsoTransactionManagerIf object 
 * to collect information about the changes made in a IMsoObjectIf object
 * 
 * @author vinjar, kenneth
 *
 */

@SuppressWarnings("unchecked")
public interface IChangeSourceIf {

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
	 * Check if only a partial commit is required
	 * @return Returns <code>true</code> if only a partial commit is required.
	 */
    public boolean isPartial();
    
    /**
     * Get partial list of changed data
     * @return Returns a partial list of changed data
     */
	public List<IChangeIf> getPartial();
    
    public boolean setPartial(String attribute);       
    public boolean setPartial(IMsoObjectIf referenced);   
    public boolean setPartial(IMsoAttributeIf<?> attribute);   
    
    public boolean addPartial(String attribute);    
    public boolean addPartial(IMsoObjectIf referenced);
    public boolean addPartial(IMsoAttributeIf<?> attribute);
    
    public boolean removePartial(String attribute);
    public boolean removePartial(IMsoObjectIf referenced);
    public boolean removePartial(IMsoAttributeIf<?> attribute);
    
    public void clearPartial();
    
    public boolean isDeleted();

    public boolean isCreated();

    public boolean isModified();

    public boolean isReferenceChanged();
        
}
