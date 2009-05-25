package org.redcross.sar.mso;

import org.redcross.sar.mso.IChangeIf.ChangeType;
import org.redcross.sar.mso.IChangeIf.IChangeAttributeIf;
import org.redcross.sar.mso.IChangeIf.IChangeObjectIf;
import org.redcross.sar.mso.IChangeIf.IChangeReferenceIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.ITransactionIf.TransactionType;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoReferenceIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEventManagerImpl;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;
import org.redcross.sar.util.except.TransactionException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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
     * Reference to the owning MsoModel
     */
    private final MsoModelImpl m_msoModel;
    
    /**
     * Reference to the MSO data manager
     */
    private final MsoManagerImpl m_msoManager;

    /**
     * Reference to the MSO event manager
     */
    private final MsoEventManagerImpl m_msoEventManager;
    
    /**
     * Vector for accumulating {@link ChangeSource} objects that is updated.
     */
    private final Vector<IChangeSourceIf> m_changes = new Vector<IChangeSourceIf>(50);

    /**
     * @param theModel Reference to the singleton MSO model object holding the MsoModel object.
     */
    protected TransactionManagerImpl(MsoModelImpl theModel)
    {
        m_msoModel = theModel;
        m_msoManager = (MsoManagerImpl)theModel.getMsoManager();
        m_msoEventManager = (MsoEventManagerImpl)m_msoModel.getEventManager();
        m_msoEventManager.addServerUpdateListener(new IMsoUpdateListenerIf()
        {

			public EnumSet<MsoClassCode> getInterests()
			{
				return EnumSet.allOf(MsoClassCode.class);
			}

			public void handleMsoUpdateEvent(UpdateList events)
            {
				for(MsoEvent.Update e : events.getEvents())
				{
					registerUpdate(e);
				}
            }

        });
    }

    private void registerUpdate(MsoEvent.Update e)
    {
    	// get an update event data
    	int aMask = e.getEventTypeMask();
    	IMsoObjectIf msoObj = e.getSource();
    	boolean isRollback = e.isRollbackMode();

    	// initialize remove action
    	IChangeSourceIf remove = null;
    	
    	// append mask if object already exists
        for (IChangeSourceIf it : m_changes)
        {
            if (it.getMsoObject().getObjectId().equals(msoObj.getObjectId()))
            {
            	if(it instanceof ChangeSource) 
            	{
            		if(aMask!=0 && !isRollback) 
            		{
            			((ChangeSource)it).applyMask(aMask);
                        return;
            		}
            		else if (isRollback)
            		{
            			remove = it;
            			break;
            		}
            	}
            }
        }
        if(remove==null && !isRollback)
        {
        	m_changes.add(new ChangeSource(msoObj, aMask));
        }
        else if(remove!=null)
        {
        	m_changes.remove(remove);        	
        }
    }

    /**
     * Returns pending updates
     * <p/>
     */
    public List<IChangeSourceIf> getChanges()
    {
    	List<IChangeSourceIf> changes = new ArrayList<IChangeSourceIf>(m_changes.size());
    	for(IChangeSourceIf it : m_changes) {
    		changes.add(new ChangeSource(it));
    	}
    	return changes;
    }

    /**
     * Returns pending updates of specific class
     * <p/>
     */
    public List<IChangeSourceIf> getChanges(MsoClassCode of) {
    	return getChanges(EnumSet.of(of));
    }

    /**
     * Returns pending updates of specific classes
     * <p/>
     */
    public List<IChangeSourceIf> getChanges(Set<MsoClassCode> of) {
    	List<IChangeSourceIf> updates = new ArrayList<IChangeSourceIf>(m_changes.size());
    	for (IChangeSourceIf it : m_changes)
        {
    		// add to updates?
    		if(of.contains(it.getMsoObject().getMsoClassCode())) {
    			updates.add(new ChangeSource(it));
    		}
        }
        // finished
        return updates;
    }

    /**
     * Returns pending updates of specific object
     * <p/>
     */
    public IChangeSourceIf getChanges(IMsoObjectIf of) {
    	List<IMsoObjectIf> list = new ArrayList<IMsoObjectIf>(1);
    	list.add(of);
    	List<IChangeSourceIf> updates = getChanges(list);
    	return updates.size()>0 ? updates.get(0) : null;
    }

    /**
     * Returns pending updates of specific objects
     * <p/>
     */
    public List<IChangeSourceIf> getChanges(List<IMsoObjectIf> of)
    {
    	List<IChangeSourceIf> updates = new ArrayList<IChangeSourceIf>(m_changes.size());
    	for (IChangeSourceIf it : m_changes)
        {

    		// add to updates?
    		if(of.contains(it.getMsoObject())) {
    			updates.add(new ChangeSource(it));
    		}

        }
        // finished
        return updates;
    }

    private ITransactionIf createCommit(List<IChangeSourceIf> changes)
    {
    	List<IMsoObjectIf> objects = new ArrayList<IMsoObjectIf>(changes.size());
    	List<IChangeSourceIf> deletable = new ArrayList<IChangeSourceIf>(changes.size());
        TransactionImpl wrapper = new TransactionImpl(TransactionType.COMMIT);
        for (IChangeSourceIf it : changes)
        {
        	// get IMsoObjectIf
        	IMsoObjectIf msoObj = it.getMsoObject();
        	// only add one change object per MSO object
        	if(!objects.contains(msoObj)) {
	        	// add to wrapper
	    		wrapper.add(it);
	    		// add to objects
	    		objects.add(msoObj);
	        	// can be deleted from buffer?
	        	if(!it.isPartial()) deletable.add(it);
        	}
        }
        // only remove full commit updates
        m_changes.removeAll(deletable);
        // ready to commit
        return wrapper;
    }

    /**
     * Perform a commit of all changes. <p/>
     * 
     * Generates a {@link org.redcross.sar.mso.event.MsoEvent.Commit} event.
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void commit() throws TransactionException
    {
        m_msoEventManager.notifyCommit(createCommit(m_changes));
    }

    /**
      * Perform a commit on a subset of all changes<p/>
     * 
     * Note that partial commits is only possible to perform on objects 
     * that exists remotely (modified). 
	 *
     * @param ChangeSource updates - holder for updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     * @see {@link org.redcross.sar.mso.IChangeSourceIf} for more information.
     */
    public void commit(IChangeSourceIf changes) throws TransactionException
    {
    	if(changes!=null) {
	    	List<IChangeSourceIf> list = new Vector<IChangeSourceIf>(1);
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
    public void commit(List<IChangeSourceIf> changes) throws TransactionException
    {
        m_msoEventManager.notifyCommit(createCommit(changes));
    }

    private ITransactionIf createRollback(List<IChangeSourceIf> changes)
    {
    	List<IMsoObjectIf> objects = new ArrayList<IMsoObjectIf>(changes.size());
    	List<IChangeSourceIf> deletable = new ArrayList<IChangeSourceIf>(changes.size());
        TransactionImpl transaction = new TransactionImpl(TransactionType.ROLLBACK);
        for (IChangeSourceIf it : changes)
        {
        	// get IMsoObjectIf
        	IMsoObjectIf msoObj = it.getMsoObject();
        	// only add one change object per MSO object
        	if(!objects.contains(msoObj)) {
	        	// add to wrapper
	    		transaction.add(it);
	        	// can be deleted from buffer?
	        	if(!it.isPartial()) deletable.add(it);
        	}
        }
        // only remove full commit updates
        m_changes.removeAll(deletable);
        // ready to commit
        return transaction;
    }
    
    /**
     * Performs a rollback of all changes. <p/>
     * 
     * Clears all accumulated information.
     */
    public void rollback() throws TransactionException
    {
        m_msoModel.suspendClientUpdate();
        m_msoModel.setLocalUpdateMode();
        m_changes.clear();
        m_msoManager.rollback();
        m_msoModel.restoreUpdateMode();
        m_msoModel.resumeClientUpdate(true);
    }

    /**
     * Perform a rollback on a subset of all changes<p/>
     * 
     * @param UpdateHolder updates - holder for updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void rollback(IChangeSourceIf changes) throws TransactionException {
    	if(changes!=null) {
	    	List<IChangeSourceIf> list = new Vector<IChangeSourceIf>(1);
	    	list.add(changes);
	    	rollback(changes);
    	}
    }

    /**
     * Perform a rollback on a subset of all changes<p/>
     * 
     * @param List<UpdateHolder> updates - list of holders of updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void rollback(List<IChangeSourceIf> changes) throws TransactionException {
    	ITransactionIf transaction = createRollback(changes);
        m_msoModel.suspendClientUpdate();
        m_msoModel.setLocalUpdateMode();
        // loop over all references first
        for(IChangeObjectIf it : transaction.getObjects()) {
        	if(it.isPartial()) {
        		it.getMsoObject().rollback(it.getPartial()); 
        	} else {
        		it.getMsoObject().rollback();
        	}        	
        }
        m_msoModel.restoreUpdateMode();
        m_msoModel.resumeClientUpdate(true);
    }
    
    /**
     * Tell if some uncommitted changes exist
     *
     * @return true if uncommitted changes exist
     */
    public boolean hasUncommitedChanges()
    {
        return m_changes.size() > 0;
    }

    public boolean hasUncommitedChanges(MsoClassCode code) {
    	return getChanges(code).size()>0;
    }

    public boolean hasUncommitedChanges(IMsoObjectIf msoObj) {
    	return getChanges(msoObj)!=null;
    }

    class ChangeSource implements IChangeSourceIf
    {

    	private final IMsoObjectIf m_object;
    	private final List<IChangeIf> m_partial = new ArrayList<IChangeIf>(1);

        private int m_mask;

	    /* ===============================================
	     * Constructors
	     * =============================================== */
        
        ChangeSource(IMsoObjectIf anObject, int aMask)
        {
        	// prepare
            m_object = anObject;
            m_mask = aMask;
        }
        
        ChangeSource(IChangeSourceIf changeSource)
        {
        	m_object = changeSource.getMsoObject();
        	m_partial.addAll(changeSource.getPartial());
        }
        
	    /* ===============================================
	     * Public methods
	     * =============================================== */
        
        @Override
		public int getMask() {
			return m_mask;
		}

        @Override
		public IMsoObjectIf getMsoObject() {
			return m_object;
		}

        @Override
        public boolean isPartial() {
        	return m_partial!=null && m_partial.size()>0;
        }

        @Override
        public List<IChangeIf> getPartial() {
        	return m_partial;
        }

        @Override
        public boolean addPartial(String attribute) {

        	String name = attribute.toLowerCase();
        	if(m_object.getAttributes().containsKey(name)) {
        		return addPartial(m_object.getAttributes().get(name));
        	}
        	return false;
        }
        
        @Override
        public boolean addPartial(IMsoAttributeIf<?> attribute) {

        	/* =======================================
        	 * Only allowed for an object that is
        	 * 	A) already created
        	 * 	B) is modified 
        	 *  C) attribute is changed (locally)
        	 *  D) attribute not already added as partial update
        	 *  E) attribute exists in object
        	 * ======================================= */

        	if(!isCreated() && isModified() 
        			&& m_object.getAttributes().containsValue(attribute)) 
        	{
        		IChangeAttributeIf it = getChange(attribute);
        		if(attribute.isChanged() && it==null) 
        		{
	        		// add attribute change to partial updates
	    			return m_partial.add(new ChangeImpl.ChangeAttribute(attribute,ChangeType.MODIFIED));	    			
        		} 
        		else if(attribute.isRollbackMode() && it!=null) 
        		{
        			// attribute is not changed anymore, remove change 
        			m_partial.remove(it);        			
        		}
        	}
        	// failure
        	return false;
        }        
        
        @Override
        public boolean addPartial(IMsoObjectIf referenced) {
        	
        	/* =======================================
        	 * Only allowed for an object that is
        	 * 	A) already created
        	 * 	B) is modified 
        	 *  C) the reference is changed (locally)
        	 *  D) the reference not already added 
        	 *  as partial update
        	 * ======================================= */

        	if(!isCreated() && isModified()) 
        	{
        		// get object holder
        		IMsoReferenceIf<?> refObj = getMsoObject().getReference(referenced);
        		
        		// found object holder?
        		if(refObj!=null) 
        		{
        			List<IChangeReferenceIf> list = getChange(referenced);
        			if(refObj.isChanged() && list.size()==0) { 
	        			// get remote object
	        			IMsoObjectIf msoObj = refObj.getRemoteReference();        			        			
	        			
	                	// notify that current (remote) reference should be deleted?
	                    if (msoObj != null && !msoObj.isDeleted())
	                    {
	                    	m_partial.add(new ChangeImpl.ChangeReference(refObj, ChangeType.DELETED));
	                    }
	        			
	                    // get local object
	        			msoObj = refObj.getRemoteReference();
	        			
	                    // notify that a new (remote) reference should created?
	                    if (msoObj != null)
	                    {
	                    	m_partial.add(new ChangeImpl.ChangeReference(refObj, ChangeType.CREATED));
	                    }
        			}
        			else if(refObj.isRollbackMode() && list.size()>0) 
        			{
        				m_partial.removeAll(list);
        			}

        		}
        	}
        	// failure
        	return false;
        	
        }        
        
        @Override
        public boolean removePartial(String attribute) {
        	String name = attribute.toLowerCase();
        	IChangeAttributeIf found = null;
        	for(IChangeIf it : m_partial) {
        		if(it instanceof IChangeAttributeIf) {
        			
        			IChangeAttributeIf attr = (IChangeAttributeIf)it;
	        		if(attr.getName().equals(name)) {
	        			found = attr;
	        			break;
	        		}
        		}
        	}
        	if(found!=null) {
        		return m_partial.remove(found);
        	}
        	return false;
        }
        
        @Override
        public boolean removePartial(IMsoAttributeIf<?> attribute) {
        	return removePartial(attribute.getName().toLowerCase());
        }
        
		@Override
		public boolean removePartial(IMsoObjectIf referenced) {
			List<IChangeIf> removeList = new Vector<IChangeIf>(2);
        	for(IChangeIf it : m_partial) {
        		if(it instanceof IChangeReferenceIf) {        			
        			IChangeReferenceIf refObj = (IChangeReferenceIf)it;
	        		if(refObj.getReferredObject()==referenced) {
	        			removeList.add(refObj);
	        		}
        		}
        	}
    		return m_partial.removeAll(removeList);
		}
        
        @Override
        public void clearPartial() {
        	m_partial.clear();
        }

        @Override
        public boolean setPartial(String attribute)
        {
        	clearPartial();
        	return addPartial(attribute);
        }
        
		@Override
		public boolean setPartial(IMsoAttributeIf<?> attribute) {
        	clearPartial();
        	return addPartial(attribute);
		}

		@Override
		public boolean setPartial(IMsoObjectIf referenced) {
        	clearPartial();
        	return addPartial(referenced);
		}

        @Override
	    public boolean isDeleted()
	    {
	    	return (m_mask & MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
	    }

        @Override
	    public boolean isCreated()
	    {
	    	return (m_mask & MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
	    }

        @Override
	    public boolean isModified()
	    {
	    	return (m_mask & MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
	    }

        @Override
	    public boolean isReferenceChanged()
	    {
	    	return (m_mask &
	                (MsoEventType.MODIFIED_REFERENCE_EVENT.maskValue()) |
	                MsoEventType.ADDED_REFERENCE_EVENT.maskValue() |
	                MsoEventType.REMOVED_REFERENCE_EVENT.maskValue())  != 0;
	    }

	    /* ===============================================
	     * Private methods
	     * =============================================== */
        
        private void applyMask(int aMask)
        {
            m_mask |= aMask;
        }
        

        private IChangeAttributeIf getChange(IMsoAttributeIf<?> attr) {
        	for(IChangeIf it : m_partial) {
        		if(it instanceof IChangeAttributeIf) {
        			if(((IChangeAttributeIf)it).getMsoAttribute() == attr) return (IChangeAttributeIf)it;
        		}
        	}
        	return null;
        }

        private List<IChangeReferenceIf> getChange(IMsoObjectIf referenced) {
        	List<IChangeReferenceIf> list = new Vector<IChangeReferenceIf>(2);
        	for(IChangeIf it : m_partial) {
        		if(it instanceof IChangeReferenceIf) {
        			if(((IChangeReferenceIf)it).getReferredObject() == referenced) list.add((IChangeReferenceIf)it);
        		}
        	}
        	return list;
        }
        
    }

}
