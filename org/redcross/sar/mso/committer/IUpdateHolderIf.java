package org.redcross.sar.mso.committer;

import java.util.List;

import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IUpdateHolderIf {

	public IMsoObjectIf getMsoObject();
	
	public int getMask();
	
    public boolean isPartial();
    
    public List<IAttributeIf> getPartial();
    
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
