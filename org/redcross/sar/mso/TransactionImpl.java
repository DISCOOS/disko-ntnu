package org.redcross.sar.mso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Wrapper class for passing change objects and relations to the mso model dispatcher
 */
public class TransactionImpl implements ITransactionIf
{
	
    private final List<IChangeRecordIf> m_changes = new ArrayList<IChangeRecordIf>();
    private final List<IChangeIf.IChangeObjectIf> m_changeObjects = new ArrayList<IChangeIf.IChangeObjectIf>();
    private final List<IChangeIf.IChangeAttributeIf> m_changeAttributes = new ArrayList<IChangeIf.IChangeAttributeIf>();
    private final List<IChangeIf.IChangeRelationIf> m_changeObjectReferences = new ArrayList<IChangeIf.IChangeRelationIf>();
    private final List<IChangeIf.IChangeRelationIf> m_changeListReferences = new ArrayList<IChangeIf.IChangeRelationIf>();

    private final TransactionType m_type;
    
    protected TransactionImpl(TransactionType type) {
    	m_type = type;
    }
    
    public TransactionType getType() {
    	return m_type;
    }
    
    public List<IChangeRecordIf> getRecords()
    {
        return sort(m_changes);
    }
    
    public List<IChangeIf.IChangeObjectIf> getObjectChanges()
    {
    	return sort(m_changeObjects);
    }
    
    public List<IChangeIf.IChangeAttributeIf> getAttributeChanges()
    {
    	return sort(m_changeAttributes);
    }

    public List<IChangeIf.IChangeRelationIf> getObjectRelationChanges()
    {
        return sort(m_changeObjectReferences);
    }

    public List<IChangeIf.IChangeRelationIf> getListRelationChanges()
    {
        return sort(m_changeListReferences);
    }
    	
    /**
     * Add an object to the transaction.
     *
     * @param IChangeRecordIf aRecord - The change record 
     */
    protected void add(IChangeRecordIf aRecord)
    {    	
    	// add to list?
    	if(!m_changes.contains(aRecord))
    	{
    		
    		// add to changes
    		m_changes.add(aRecord);
    		
    		// add changed references 
    		m_changeAttributes.addAll(aRecord.getAttributeChanges());
    		m_changeObjectReferences.addAll(aRecord.getObjectReferenceChanges());
    		m_changeListReferences.addAll(aRecord.getListReferenceChanges());
    		
    	}
    }
    
    /**
     * Create copy of list, sort, and return it.
     * @param <T> - the list data type
     * @param list - the list to create a copy of and sort
     * @return Returns a sorted copy of given list.
     */
    private static <T extends Comparable<? super T>> List<T> sort(List<T> list) 
    {
    	list = clone(list);
    	Collections.sort(list);
    	return list;
    }

    /**
     * Clone given list
     * @param <T> - the list data type
     * @param list - the list to clone
     * @return Returns a copy of the given list
     */
    private static <T extends Comparable<? super T>> List<T> clone(List<T> list) 
    {
    	return new Vector<T>(list);
    }
    
}
