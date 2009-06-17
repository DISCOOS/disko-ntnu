package org.redcross.sar.mso;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Wrapper class for passing change objects and relations to the mso model dispatcher
 */
public class TransactionImpl implements ITransactionIf
{
	
    private final List<IChangeRecordIf> m_changes = new ArrayList<IChangeRecordIf>();
    private final List<IChangeIf.IChangeRelationIf> m_changeObjectReferences = new ArrayList<IChangeIf.IChangeRelationIf>();
    private final List<IChangeIf.IChangeRelationIf> m_changeListReferences = new ArrayList<IChangeIf.IChangeRelationIf>();

    private final TransactionType m_type;
    
    protected TransactionImpl(TransactionType type) {
    	m_type = type;
    }
    
    public TransactionType getType() {
    	return m_type;
    }
    
    public List<IChangeRecordIf> getChanges()
    {
        return new Vector<IChangeRecordIf>(m_changes);
    }

    public List<IChangeIf.IChangeRelationIf> getObjectRelations()
    {
        return new Vector<IChangeIf.IChangeRelationIf>(m_changeObjectReferences);
    }

    public List<IChangeIf.IChangeRelationIf> getListRelations()
    {
        return new Vector<IChangeIf.IChangeRelationIf>(m_changeListReferences);
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
    		m_changeObjectReferences.addAll(aRecord.getObjectReferenceChanges());
    		m_changeListReferences.addAll(aRecord.getListReferenceChanges());
    		
    	}
    }
    
}
