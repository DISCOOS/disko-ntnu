package org.redcross.sar.mso;

import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.ITransactionIf.TransactionType;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoDataIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoRelationIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEventManagerImpl;
import org.redcross.sar.mso.event.MsoEvent.ChangeList;
import org.redcross.sar.util.except.TransactionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * The purpose of the transaction manager is to catch server update events, 
 * accumulate them, and when a commit is executed,
 * fire {@link org.redcross.sar.mso.event.MsoEvent.Commit} events.
 * The event provides access to MSO data structures that is 
 * committed by passing a {@link org.redcross.sar.mso.ITransactionIf} object
 * to the listeners.
 */
@SuppressWarnings("unchecked")
public class TransactionManagerImpl implements IMsoTransactionManagerIf
{

    /**
     * Reference to the owning MSO Model
     */
    private final MsoModelImpl m_msoModel;
    
    /**
     * Reference to the MSO event manager
     */
    private final MsoEventManagerImpl m_msoEventManager;
    
    /**
     * Vector for accumulating {@link ChangeRecord} objects that is updated.
     */
    private final Map<IMsoObjectIf,IChangeRecordIf> m_changes = new HashMap<IMsoObjectIf,IChangeRecordIf>();
    
    /**
     * Set of all MSO objects types. Is used to indicate interests in all change notifications
     */
    private final static EnumSet<MsoClassCode> m_interests = EnumSet.allOf(MsoClassCode.class);

    /**
     * @param theModel Reference to the singleton MSO model object holding the MsoModel object.
     */
    protected TransactionManagerImpl(MsoModelImpl theModel)
    {
        m_msoModel = theModel;
        m_msoEventManager = (MsoEventManagerImpl)m_msoModel.getEventManager();
        m_msoEventManager.addRemoteUpdateListener(new IMsoUpdateListenerIf()
        {

			public EnumSet<MsoClassCode> getInterests()
			{
				return m_interests;
			}

			public void handleMsoChangeEvent(ChangeList events)
            {
				for(MsoEvent.Change e : events.getEvents())
				{
					register(e);
				}
            }

        });
    }

    /**
     * Returns pending updates
     * <p/>
     */
    public List<IChangeRecordIf> getChanges()
    {
    	List<IChangeRecordIf> changes = new ArrayList<IChangeRecordIf>(m_changes.size());
    	for(IChangeRecordIf it : m_changes.values()) {
    		changes.add(new ChangeRecordImpl((ChangeRecordImpl)it));
    	}
    	return changes;
    }

    /**
     * Returns pending updates of specific class
     * <p/>
     */
    public List<IChangeRecordIf> getChanges(MsoClassCode of) {
    	return getChanges(EnumSet.of(of));
    }

    /**
     * Returns pending updates of specific classes
     * <p/>
     */
    public List<IChangeRecordIf> getChanges(Set<MsoClassCode> of) {
    	List<IChangeRecordIf> updates = new ArrayList<IChangeRecordIf>(m_changes.size());
    	for (IChangeRecordIf it : m_changes.values())
        {
    		// add to updates?
    		if(of.contains(it.getMsoObject().getClassCode())) {
    			updates.add(new ChangeRecordImpl((ChangeRecordImpl)it));
    		}
        }
        // finished
        return updates;
    }

    /**
     * Returns pending updates of specific object
     * <p/>
     */
    public IChangeRecordIf getChanges(IMsoObjectIf of) {
    	List<IMsoObjectIf> list = new ArrayList<IMsoObjectIf>(1);
    	list.add(of);
    	List<IChangeRecordIf> updates = getChanges(list);
    	return updates.size()>0 ? updates.get(0) : null;
    }

    /**
     * Returns pending updates of specific objects
     * <p/>
     */
    public List<IChangeRecordIf> getChanges(List<IMsoObjectIf> of)
    {
    	List<IChangeRecordIf> updates = new ArrayList<IChangeRecordIf>(m_changes.size());
    	for (IMsoObjectIf msoObj : of)
        {
    		// try to get change record
    		IChangeRecordIf it = m_changes.get(msoObj);
    		// add to updates?
    		if(it!=null) 
    		{
    			updates.add(new ChangeRecordImpl((ChangeRecordImpl)it));
    		}

        }
        // finished
        return updates;
    }

    /**
     * Perform a commit of all changes. <p/>
     * 
     * Generates a {@link org.redcross.sar.mso.event.MsoEvent.Commit} event.
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void commit() throws TransactionException
    {
        m_msoEventManager.notifyCommit(create(TransactionType.COMMIT,m_changes.values()));
    }

    /**
      * Perform a commit on a subset of all changes<p/>
     * 
     * Note that partial commits is only possible to perform on objects 
     * that exists remotely (modified). 
	 *
     * @param ChangeRecord updates - holder for updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     * @see {@link org.redcross.sar.mso.IChangeRecordIf} for more information.
     */
    public void commit(IChangeRecordIf changes) throws TransactionException
    {
    	if(changes!=null) {
	    	List<IChangeRecordIf> list = new Vector<IChangeRecordIf>(1);
	    	list.add(changes);
	    	commit(list);
    	}
    }
        
    /**
     * Perform a commit on a subset of all changes<p/>
     * 
     * Note that partial commits is only possible to perform on objects 
     * that exists remotely (modified). 
     *  
     * @param List<UpdateHolder> updates - list of holders of updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void commit(List<IChangeRecordIf> changes) throws TransactionException
    {
        m_msoEventManager.notifyCommit(create(TransactionType.COMMIT,changes));
    }

    /**
     * Performs a rollback of all changes. <p/>
     * 
     * Clears all accumulated information.
     */
    public void rollback() throws TransactionException
    {
        rollback(new Vector<IChangeRecordIf>(m_changes.values()));
    }

    /**
     * Perform a rollback on a subset of all changes<p/>
     * 
     * @param UpdateHolder updates - holder for updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void rollback(IChangeRecordIf changes) throws TransactionException {
    	if(changes!=null) {
	    	List<IChangeRecordIf> list = new Vector<IChangeRecordIf>(1);
	    	list.add(changes);
	    	rollback(list);
    	}
    }

    /**
     * Perform a rollback on a subset of all changes<p/>
     * 
     * @param List<UpdateHolder> updates - list of holders of updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void rollback(List<IChangeRecordIf> changes) throws TransactionException {
    	ITransactionIf transaction = create(TransactionType.ROLLBACK,changes);
        // loop over all references first
        for(IChangeRecordIf rs : transaction.getChanges()) 
        {
        	if(rs.isFiltered()) 
        	{
        		// prepare to rollback
                m_msoModel.suspendUpdate();
                m_msoModel.setLocalUpdateMode();
        		// only roll back changed attributes and references
        		for(IChangeIf it : rs.getFilters())
        		{
        			IMsoDataIf data = it.getObject();
        			if(data instanceof IMsoAttributeIf)
        			{
        				((IMsoAttributeIf<?>)data).rollback();        				
        			}
        			else if(data instanceof IMsoRelationIf)
        			{
        				((IMsoRelationIf<?>)data).rollback();        				
        			}
        			// finalize rollback
        	        m_msoModel.restoreUpdateMode();
        	        m_msoModel.resumeUpdate();
        		}
        	} else 
        	{
        		// forward to object
        		rs.getMsoObject().rollback();
        	}        	
        }
    }
    
    /**
     * Tell if some uncommitted changes exist
     *
     * @return true if uncommitted changes exist
     */
    public boolean isChanged()
    {
        return m_changes.size() > 0;
    }

    public boolean isChanged(MsoClassCode code) {
    	return getChanges(code).size()>0;
    }

    public boolean isChanged(IMsoObjectIf msoObj) {
    	return getChanges(msoObj)!=null;
    }
    
    /* ===========================================================
     * Private methods
     * =========================================================== */
    
    private void register(MsoEvent.Change e)
    {
    	// get an change event data
    	IMsoObjectIf msoObj = e.getSource();

    	// get change record associated with object 
    	IChangeRecordIf it = m_changes.get(msoObj);
    	
    	// record does not exists?
    	if(it == null)
    	{
    		it = new ChangeRecordImpl(msoObj,UpdateMode.LOCAL_UPDATE_MODE);
    		m_changes.put(msoObj,it);
    	}

    	// register change
    	it.union(e.getChange());
    	
    	// remove record?
    	if(!it.isChanged())
    	{
    		m_changes.remove(msoObj);
    	}
    	
    }

    private ITransactionIf create(TransactionType type, Collection<IChangeRecordIf> records)
    {
    	// initialize
        TransactionImpl transaction = new TransactionImpl(type);
    	
    	// loop over all records
        for (IChangeRecordIf it : records)
        {
        	
        	// get IMsoObjectIf
        	IMsoObjectIf msoObj = it.getMsoObject();
        	
        	// get changes recorded by transaction manager
        	IChangeRecordIf rs = m_changes.get(msoObj);
        	
        	// check if change is recorded by transaction manager
        	if(rs!=null)
        	{
	        	// add record to transaction
	    		transaction.add(it);
        	}
        }
        
        // finished
        return transaction;
        
    }

}
