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
import java.util.Collection;
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
     * Vector for accumulating {@link ChangeRecord} objects that is updated.
     */
    private final Vector<IChangeRecordIf> m_changes = new Vector<IChangeRecordIf>(50);
    
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
        m_msoManager = (MsoManagerImpl)theModel.getMsoManager();
        m_msoEventManager = (MsoEventManagerImpl)m_msoModel.getEventManager();
        m_msoEventManager.addServerUpdateListener(new IMsoUpdateListenerIf()
        {

			public EnumSet<MsoClassCode> getInterests()
			{
				return m_interests;
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
    	IChangeRecordIf remove = null;
    	
    	// append mask if object already exists
        for (IChangeRecordIf it : m_changes)
        {
            if (it.getMsoObject().getObjectId().equals(msoObj.getObjectId()))
            {
            	if(it instanceof ChangeRecord) 
            	{
            		if(aMask!=0 && !isRollback) 
            		{
            			((ChangeRecord)it).applyMask(aMask);
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
        	m_changes.add(new ChangeRecord(msoObj, aMask));
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
    public List<IChangeRecordIf> getChanges()
    {
    	List<IChangeRecordIf> changes = new ArrayList<IChangeRecordIf>(m_changes.size());
    	for(IChangeRecordIf it : m_changes) {
    		changes.add(new ChangeRecord(it));
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
    	for (IChangeRecordIf it : m_changes)
        {
    		// add to updates?
    		if(of.contains(it.getMsoObject().getMsoClassCode())) {
    			updates.add(new ChangeRecord(it));
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
    	for (IChangeRecordIf it : m_changes)
        {

    		// add to updates?
    		if(of.contains(it.getMsoObject())) {
    			updates.add(new ChangeRecord(it));
    		}

        }
        // finished
        return updates;
    }

    private ITransactionIf createCommit(List<IChangeRecordIf> changes)
    {
    	List<IMsoObjectIf> objects = new ArrayList<IMsoObjectIf>(changes.size());
    	List<IChangeRecordIf> deletable = new ArrayList<IChangeRecordIf>(changes.size());
        TransactionImpl transaction = new TransactionImpl(TransactionType.COMMIT);
        for (IChangeRecordIf it : changes)
        {
        	// get IMsoObjectIf
        	IMsoObjectIf msoObj = it.getMsoObject();
        	// only add one change object per MSO object
        	if(!objects.contains(msoObj)) {
	        	// add to transaction
	    		transaction.add(it);
	    		// add to objects
	    		objects.add(msoObj);
	        	// can be deleted from buffer?
	        	if(!it.isFiltered()) deletable.add(it);
        	}
        }
        // only remove full commit updates
        m_changes.removeAll(deletable);
        // ready to commit
        return transaction;
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
        m_msoEventManager.notifyCommit(createCommit(changes));
    }

    private ITransactionIf createRollback(List<IChangeRecordIf> changes)
    {
    	List<IMsoObjectIf> objects = new ArrayList<IMsoObjectIf>(changes.size());
    	List<IChangeRecordIf> deletable = new ArrayList<IChangeRecordIf>(changes.size());
        TransactionImpl transaction = new TransactionImpl(TransactionType.ROLLBACK);
        for (IChangeRecordIf it : changes)
        {
        	// get IMsoObjectIf
        	IMsoObjectIf msoObj = it.getMsoObject();
        	// only add one change object per MSO object
        	if(!objects.contains(msoObj)) {
	        	// add to wrapper
	    		transaction.add(it);
	        	// can be deleted from buffer?
	        	if(!it.isFiltered()) deletable.add(it);
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
    public void rollback(IChangeRecordIf changes) throws TransactionException {
    	if(changes!=null) {
	    	List<IChangeRecordIf> list = new Vector<IChangeRecordIf>(1);
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
    public void rollback(List<IChangeRecordIf> changes) throws TransactionException {
    	ITransactionIf transaction = createRollback(changes);
        m_msoModel.suspendClientUpdate();
        m_msoModel.setLocalUpdateMode();
        // loop over all references first
        for(IChangeObjectIf it : transaction.getObjects()) {
        	if(it.isFiltered()) {
        		it.getMsoObject().rollback(it.getChanges()); 
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

    class ChangeRecord implements IChangeRecordIf
    {

    	private final IMsoObjectIf m_object;
    	private final List<IChangeIf> m_partial = new ArrayList<IChangeIf>(1);

        private int m_mask;

	    /* ===============================================
	     * Constructors
	     * =============================================== */
        
        ChangeRecord(IMsoObjectIf anObject, int aMask)
        {
        	// prepare
            m_object = anObject;
            m_mask = aMask;
        }
        
        ChangeRecord(IChangeRecordIf changeSource)
        {
        	m_object = changeSource.getMsoObject();
        	m_partial.addAll(changeSource.getChanges());
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
        
        @Override
        public boolean isFiltered() {
        	return m_partial!=null && m_partial.size()>0;
        }

        @Override
        public boolean addFilter(String attribute) {

        	String name = attribute.toLowerCase();
        	if(m_object.getAttributes().containsKey(name)) {
        		return addFilter(m_object.getAttributes().get(name));
        	}
        	return false;
        }
        
        @Override
        public boolean addFilter(IMsoAttributeIf<?> attribute) {

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
	    			return m_partial.add(attribute.getChange());
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
        public boolean addFilter(IMsoObjectIf referenced) {
    		return addFilter(getMsoObject().getReference(referenced));        	
        }
        	
        @Override
        public boolean addFilter(IMsoReferenceIf<?> refObj) {
        	
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
        		// has reference?
        		if(refObj!=null) 
        		{
        			List<IChangeReferenceIf> list = getChange(refObj);
        			if(refObj.isChanged() && list.size()==0) {
        				
	        			// get remote object
        				m_partial.addAll(refObj.getChangedReferences());

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
        public boolean removeFilter(String attribute) {
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
        public boolean removeFilter(IMsoAttributeIf<?> attribute) {
        	return removeFilter(attribute.getName().toLowerCase());
        }
        
		@Override
		public boolean removeFilter(IMsoObjectIf referenced) {
    		return removeFilter(getMsoObject().getReference(referenced));        	
		}
		
		@Override
		public boolean removeFilter(IMsoReferenceIf<?> refObj) {
			List<IChangeIf> removeList = new Vector<IChangeIf>(2);
        	for(IChangeIf it : m_partial) {
        		if(it instanceof IChangeReferenceIf) {        			
	        		if(((IChangeReferenceIf)it).equals(refObj)) {
	        			removeList.add((IChangeReferenceIf)it);
	        		}
        		}
        	}
    		return m_partial.removeAll(removeList);
		}
        
        @Override
        public void clearFilters() {
        	m_partial.clear();
        }

        @Override
        public boolean setFilter(String attribute)
        {
        	clearFilters();
        	return addFilter(attribute);
        }
        
		@Override
		public boolean setFilter(IMsoAttributeIf<?> attribute) {
        	clearFilters();
        	return addFilter(attribute);
		}

		@Override
		public boolean setFilter(IMsoObjectIf referenced) {
        	clearFilters();
        	return addFilter(referenced);
		}

		@Override
		public boolean setFilter(IMsoReferenceIf<?> reference) {
        	clearFilters();
        	return addFilter(reference);
		}

        @Override
        public List<IChangeIf> getChanges() {
        	
        	// initialize 
        	List<IChangeIf> changes = new Vector<IChangeIf>();
        	
	        // both a create AND delete action on a objects equals no change
	        if (isCreated() && isDeleted())
	        {
	            return changes;
	        }
	        
        	// add changes
        	changes.add(getChangedObject());
        	changes.addAll(getChangedObjectReferences());
        	changes.addAll(getChangedListReferences());
        	
        	// finished
        	return changes;
        }

        @Override
    	public IChangeObjectIf getChangedObject() 
    	{
	    	
        	// get flags
	        boolean createdObject = isCreated();
	        boolean deletedObject = isDeleted();
	        boolean modifiedObject = isModified();
	        boolean modifiedReference = isReferenceChanged();
	
	        // both a create AND delete action on a objects equals no change
	        if (createdObject && deletedObject)
	        {
	            return null;
	        }
	        
        	if(createdObject && isFiltered())
        	{
        		// add modified object
        		return new ChangeImpl.ChangeObject(m_object,ChangeType.MODIFIED,m_partial);
        	}
        	else 
        	{
    	        
    	        // is object created?
    	        if (createdObject)
    	        {
    	        	return new ChangeImpl.ChangeObject(m_object, ChangeType.CREATED,null);
    	        }
    	        if (deletedObject)
    	        {
    	        	return new ChangeImpl.ChangeObject(m_object, ChangeType.DELETED,null);
    	        }
    	        if (modifiedObject && !modifiedReference)
    	        {
    	        	return new ChangeImpl.ChangeObject(m_object, ChangeType.MODIFIED,null);
    	        }
    	        if (modifiedReference)
    	        {
    	        	return new ChangeImpl.ChangeObject(m_object, ChangeType.MODIFIED,null);
    	        }
        	}
        	return null;
    	}
    	
        @Override
		public Collection<IChangeAttributeIf> getChangedAttributes() {

        	// initialize 
        	List<IChangeAttributeIf> changes = new Vector<IChangeAttributeIf>();
        	
	    	// get flags
	        boolean createdObject = isCreated();
	        boolean deletedObject = isDeleted();
	        boolean modifiedObject = isModified();
	        
	        // both a create AND delete action on a objects equals no change
	        if (createdObject && deletedObject)
	        {
	            return changes;
	        }
	        
        	if(createdObject && isFiltered())
        	{
        		// add sub-list of modified attributes
        		changes.addAll(m_object.getChangedAttributes(m_partial));
        	}
        	else 
        	{    	
    	        if ((createdObject || modifiedObject) && !deletedObject)
    	        {
            		// add modified attributes
            		changes.addAll(m_object.getChangedAttributes());
    	        }
        	}
        	// finished
        	return changes;
        }

		@Override
		public Collection<IChangeReferenceIf> getChangedListReferences() {

        	// initialize 
        	List<IChangeReferenceIf> changes = new Vector<IChangeReferenceIf>();
        	
	    	// get flags
	        boolean createdObject = isCreated();
	        boolean deletedObject = isDeleted();
	        boolean modifiedReference = isReferenceChanged();
	
	        // both a create AND delete action on a objects equals no change
	        if (createdObject && deletedObject)
	        {
	            return changes;
	        }
	        
        	if(createdObject && isFiltered())
        	{
        		// get changes references from object (m_partial may contain old changes) 
        		changes.addAll(m_object.getChangedListReferences(m_partial));
        	}
        	else 
        	{
    	        
    	        // is object created?
    	        if ((createdObject || modifiedReference) && !deletedObject)
    	        {
    	        	changes.addAll(m_object.getChangedListReferences());
    	        }
    	        
        	}
        	// finished
        	return changes;		
        }

		@Override
		public Collection<IChangeReferenceIf> getChangedObjectReferences() {

        	// initialize 
        	List<IChangeReferenceIf> changes = new Vector<IChangeReferenceIf>();
        	
	    	// get flags
	        boolean createdObject = isCreated();
	        boolean deletedObject = isDeleted();
	        boolean modifiedReference = isReferenceChanged();
	
	        // both a create AND delete action on a objects equals no change
	        if (createdObject && deletedObject)
	        {
	            return changes;
	        }
	        
        	if(createdObject && isFiltered())
        	{
        		// get changes references from object (m_partial may contain old changes) 
        		changes.addAll(m_object.getChangedObjectReferences(m_partial));
        	}
        	else 
        	{
    	        
    	        // is object created?
    	        if ((createdObject || modifiedReference) && !deletedObject)
    	        {
    	        	changes.addAll(m_object.getChangedObjectReferences());
    	        }
    	        
        	}
        	// finished
        	return changes;		
        	
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

        private List<IChangeReferenceIf> getChange(IMsoReferenceIf<?> refObj) {
        	List<IChangeReferenceIf> list = new Vector<IChangeReferenceIf>(2);
        	for(IChangeIf it : m_partial) {
        		if(it instanceof IChangeReferenceIf) {
        			if(((IChangeReferenceIf)it).equals(refObj)) list.add((IChangeReferenceIf)it);
        		}
        	}
        	return list;
        }

        
    }

}
