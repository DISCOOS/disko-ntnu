package org.redcross.sar.mso;

import java.util.List;

import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * The IChangeSourceIf interface is used by the IMsoTransactionManagerIf object 
 * to collect information about the changes made in a IMsoObjectIf object
 * 
 * @author vinjar, kenneth
 *
 */

@SuppressWarnings("unchecked")
public interface IChangeSourceIf {

	public IMsoObjectIf getMsoObject();
	
	public int getMask();
	
    public boolean isPartial();
    
	public List<IMsoAttributeIf<?>> getPartial();
    
    public boolean setPartial(String attribute);   
    public int setPartial(List<String> attributes);
    
    public boolean addPartial(String attribute);    
    public boolean removePartial(String attribute);
    public void clearPartial();
    
    public boolean isDeleted();

    public boolean isCreated();

    public boolean isModified();

    public boolean isReferenceChanged();
    
}
