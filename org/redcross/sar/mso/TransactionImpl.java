package org.redcross.sar.mso;

import org.redcross.sar.mso.IChangeIf.IChangeObjectIf;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for passing change objects and relations to the mso model dispatcher
 */
public class TransactionImpl implements ITransactionIf
{
	
    private final List<IChangeIf.IChangeObjectIf> m_changeObjects = new ArrayList<IChangeIf.IChangeObjectIf>();
    private final List<IChangeIf.IChangeReferenceIf> m_changeObjectReferences = new ArrayList<IChangeIf.IChangeReferenceIf>();
    private final List<IChangeIf.IChangeReferenceIf> m_changeListReferences = new ArrayList<IChangeIf.IChangeReferenceIf>();

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

    public List<IChangeIf.IChangeReferenceIf> getObjectReferences()
    {
        return m_changeObjectReferences;
    }

    public List<IChangeIf.IChangeReferenceIf> getListReferences()
    {
        return m_changeListReferences;
    }
    	
    /**
     * Add an object to the transaction.
     *
     * @param IChangeRecordIf aRecord - The change record 
     */
    protected void add(IChangeRecordIf aRecord)
    {    	
    	// get change object
    	IChangeObjectIf changeObj = aRecord.getChangedObject();
    	
    	// has change?
    	if(changeObj!=null) 
    	{
    		// add changes
    		m_changeObjects.add(changeObj);
    		m_changeObjectReferences.addAll(aRecord.getChangedObjectReferences());
    		m_changeListReferences.addAll(aRecord.getChangedListReferences());
    		
    	}
    	
    	/*
    	IMsoObjectIf anObject = aRecord.getMsoObject();
    	
    	// is partial commit?
    	if(aRecord.isFiltered()) 
    	{
    		// get partial update list
    		List<IChangeIf> partial = aRecord.getChanges();
    		// add modified object
    		m_changeObjects.add(new ChangeImpl.ChangeObject(anObject,ChangeType.MODIFIED,partial));
    		// add sub-lists of changed object references
    		m_changeObjectReferences.addAll(anObject.getChangedObjectReferences(partial));
    		m_changeListReferences.addAll(anObject.getChangedListReferences(partial));
    	}
    	else 
    	{    	
    		
	    	// full commit, get flags
	        boolean createdObject = aRecord.isCreated();
	        boolean deletedObject = aRecord.isDeleted();
	        boolean modifiedObject = aRecord.isModified();
	        boolean modifiedReference = aRecord.isReferenceChanged();
	
	        // both a create AND delete action on a objects equals no change
	        if (createdObject && deletedObject)
	        {
	            return;
	        }
	        // is object created?
	        if (createdObject)
	        {
	            m_changeObjects.add(new ChangeImpl.ChangeObject(anObject, ChangeType.CREATED,null));
	            m_changeObjectReferences.addAll(anObject.getChangedObjectReferences());
	            m_changeListReferences.addAll(anObject.getChangedListReferences());
	            return;
	        }
	        if (deletedObject)
	        {
	            m_changeObjects.add(new ChangeImpl.ChangeObject(anObject, ChangeType.DELETED,null));
	            return;
	        }
	        if (modifiedObject && !modifiedReference)
	        {
	            m_changeObjects.add(new ChangeImpl.ChangeObject(anObject, ChangeType.MODIFIED,null));
	        }
	        if (modifiedReference)
	        {
	            m_changeObjects.add(new ChangeImpl.ChangeObject(anObject, ChangeType.MODIFIED,null));
	            m_changeObjectReferences.addAll(anObject.getChangedObjectReferences());
	            m_changeListReferences.addAll(anObject.getChangedListReferences());
	        }
    	}	       
    	*/
    }
    
}
