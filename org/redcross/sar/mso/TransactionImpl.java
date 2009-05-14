package org.redcross.sar.mso;

import org.redcross.sar.mso.IChangeIf.ChangeType;
import org.redcross.sar.mso.data.IMsoObjectIf;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for passing change objects and relations to the mso model dispatcher
 */
public class TransactionImpl implements ITransactionIf
{
	
    private final ArrayList<IChangeIf.IChangeObjectIf> m_changeObjects = new ArrayList<IChangeIf.IChangeObjectIf>();
    private final ArrayList<IChangeIf.IChangeReferenceIf> m_changeAttributeReferences = new ArrayList<IChangeIf.IChangeReferenceIf>();
    private final ArrayList<IChangeIf.IChangeReferenceIf> m_changeListReferences = new ArrayList<IChangeIf.IChangeReferenceIf>();

    private final TransactionType m_type;
    
    protected TransactionImpl(TransactionType type) {
    	m_type = type;
    }
    
    public TransactionType getType() {
    	return m_type;
    }
    
    public List<IChangeIf.IChangeObjectIf> getObjects()
    {
        return m_changeObjects;
    }

    public List<IChangeIf.IChangeReferenceIf> getAttributeReferences()
    {
        return m_changeAttributeReferences;
    }

    public List<IChangeIf.IChangeReferenceIf> getListReferences()
    {
        return m_changeListReferences;
    }
    	
    /**
     * Add an object to the transaction.
     *
     * @param IChangeSourceIf holder - The holder of the update information
     */
    protected void add(IChangeSourceIf anHolder)
    {
    	
    	// get information
    	IMsoObjectIf anObject = anHolder.getMsoObject();
    	
    	// is partial commit?
    	if(anHolder.isPartial()) {
    		// only schedule modified data
    		m_changeObjects.add(
    				new ChangeImpl.ChangeObject(anObject,
    						ChangeType.MODIFIED,anHolder.getPartial()));
    	}
    	else {    	
    		
	    	// full commit, get flags
	        boolean createdObject = anHolder.isCreated();
	        boolean deletedObject = anHolder.isDeleted();
	        boolean modifiedObject = anHolder.isModified();
	        boolean modifiedReference = anHolder.isReferenceChanged();
	
	        // both a create AND delete action on a objects equals no change
	        if (createdObject && deletedObject)
	        {
	            return;
	        }
	        // is object created?
	        if (createdObject)
	        {
	            m_changeObjects.add(new ChangeImpl.ChangeObject(anObject, ChangeType.CREATED,null));
	            m_changeAttributeReferences.addAll(anObject.getChangedAttributeReferences());
	            m_changeListReferences.addAll(anObject.getChangedListReferences());
	            return;
	        }
	        if (deletedObject)
	        {
	            m_changeObjects.add(new ChangeImpl.ChangeObject(anObject, ChangeType.DELETED,null));
	            return;
	        }
	        if (modifiedObject)
	        {
	            m_changeObjects.add(new ChangeImpl.ChangeObject(anObject, ChangeType.MODIFIED,null));
	        }
	        if (modifiedReference)
	        {
	            m_changeAttributeReferences.addAll(anObject.getChangedAttributeReferences());
	            m_changeListReferences.addAll(anObject.getChangedListReferences());
	        }
    	}	       
    }
    
}
