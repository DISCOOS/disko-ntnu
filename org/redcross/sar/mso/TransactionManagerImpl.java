package org.redcross.sar.mso;

import org.redcross.sar.mso.IChangeIf.IChangeObjectIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.ITransactionIf.TransactionType;
import org.redcross.sar.mso.data.AbstractMsoObject;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
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
 * The purpose of the commit manager is to catch Server update events, accumulate them, and when a commit is executed,
 * fire {@link org.redcross.sar.mso.event.MsoEvent.Commit} events.
 * The event provides access to MSO data structures that shall be committed  by passing a {@link org.redcross.sar.mso.ITransactionIf} object
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
					registerUpdate((AbstractMsoObject) e.getSource(), e.getEventTypeMask());
				}
            }

        });
    }

    private void registerUpdate(AbstractMsoObject anObject, int aMask)
    {
        for (IChangeSourceIf it : m_changes)
        {
            if (it.getMsoObject().getObjectId().equals(anObject.getObjectId()))
            {
            	if(it instanceof ChangeSource) {
            		((ChangeSource)it).applyMask(aMask);
                    return;
            	}
            }
        }
        m_changes.add(new ChangeSource(anObject, aMask));
    }

    /**
     * Returns pending updates
     * <p/>
     */
    public List<IChangeSourceIf> getChanges()
    {
    	return new ArrayList<IChangeSourceIf>(m_changes);
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
    			updates.add(it);
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
    			updates.add(it);
    		}

        }
        // finished
        return updates;
    }

    private ITransactionIf createCommit(List<IChangeSourceIf> changes)
    {
    	List<IChangeSourceIf> deletable = new ArrayList<IChangeSourceIf>(changes.size());
        TransactionImpl wrapper = new TransactionImpl(TransactionType.COMMIT);
        for (IChangeSourceIf it : changes)
        {
        	// add to wrapper
    		wrapper.add(it);
        	// can be deleted from buffer?
        	if(!it.isPartial()) deletable.add(it);
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
     * Note that partial commits (attributes only) is only possible to perform on objects 
     * that exists remotely (modified). If a IChangeSourceIf is marked for partial commit, object references 
     * and list references are not affected, only the marked attributes. See 
     * {@link org.redcross.sar.mso.IChangeSourceIf} for more information.
	 *
     * @param ChangeSource updates - holder for updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void commit(IChangeSourceIf changes) throws TransactionException
    {
    	List<IChangeSourceIf> list = new Vector<IChangeSourceIf>(1);
    	list.add(changes);
    	commit(changes);
    }
        
    /**
     * Perform a commit on a subset of all changes<p/>
     * 
     * Note that partial commits (attributes only) is only possible to perform on objects 
     * that exists remotely (modified). If a IChangeSourceIf is marked for partial commit, object references 
     * and list references are not affected, only the marked attributes. See 
     * {@link org.redcross.sar.mso.IChangeSourceIf} for more information.
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
    	List<IChangeSourceIf> deletable = new ArrayList<IChangeSourceIf>(changes.size());
        TransactionImpl wrapper = new TransactionImpl(TransactionType.COMMIT);
        for (IChangeSourceIf it : changes)
        {
        	// add to wrapper
    		wrapper.add(it);
        	// can be deleted from buffer?
        	if(!it.isPartial()) deletable.add(it);
        }
        // only remove full commit updates
        m_changes.removeAll(deletable);
        // ready to commit
        return wrapper;
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
    	List<IChangeSourceIf> list = new Vector<IChangeSourceIf>(1);
    	list.add(changes);
    	rollback(changes);
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
        // loop over all objects
        for(IChangeObjectIf it : transaction.getObjects()) {
        	if(it.isPartial()) {
        		it.getObject().rollback(it.getPartial()); 
        	} else {
        		it.getObject().rollback();
        	}        	
        }
        m_msoModel.restoreUpdateMode();
        m_msoModel.resumeClientUpdate(true);    	
    }
    
    /**
     * Tell if some uncommited changes exist
     *
     * @return true if uncommited changes exist
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
    	private final List<IAttributeIf<?>> m_partial =  new ArrayList<IAttributeIf<?>>(1);

        private int m_mask;

        ChangeSource(IMsoObjectIf anObject, int aMask)
        {
        	// prepare
            m_object = anObject;
            m_mask = aMask;
        }

		public int getMask() {
			return m_mask;
		}

		public IMsoObjectIf getMsoObject() {
			return m_object;
		}

        void applyMask(int aMask)
        {
            m_mask |= aMask;
        }

        public boolean isPartial() {
        	return m_partial!=null && m_partial.size()>0;
        }

        public List<IAttributeIf<?>> getPartial() {
        	return m_partial;
        }

        public boolean addPartial(String attribute) {

        	// only allowed for an object that is
        	// A) Already created
        	// B) Is modified

        	if(!isCreated() && isModified()) {
	        	String name = attribute.toLowerCase();
	        	if(m_object.getAttributes().containsKey(name)) {
	        		IAttributeIf item = m_object.getAttributes().get(name);
	        		if(!m_partial.contains(item)) {
	        			return m_partial.add(item);
	        		}
	        	}
        	}
        	return false;
        }

        public boolean removePartial(String attribute) {
        	String name = attribute.toLowerCase();
        	IAttributeIf found = null;
        	for(IAttributeIf it : m_partial) {
        		if(it.getName().equals(name)) {
        			found = it;
        			break;
        		}
        	}
        	if(found!=null) {
        		return m_partial.remove(found);
        	}
        	return false;
        }

        public void clearPartial() {
        	m_partial.clear();
        }

        public boolean setPartial(String attribute)
        {
        	clearPartial();
        	return addPartial(attribute);
        }

        public int setPartial(List<String> attributes)
        {
        	int count = 0;

            // forward
        	for(String name : attributes) {
        		if(addPartial(name)) count++;
        	}
            return count;
        }

	    public boolean isDeleted()
	    {
	    	return (m_mask & MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
	    }

	    public boolean isCreated()
	    {
	    	return (m_mask & MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
	    }

	    public boolean isModified()
	    {
	    	return (m_mask & MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
	    }

	    public boolean isReferenceChanged()
	    {
	    	return (m_mask &
	                (MsoEventType.MODIFIED_REFERENCE_EVENT.maskValue()) |
	                MsoEventType.ADDED_REFERENCE_EVENT.maskValue() |
	                MsoEventType.REMOVED_REFERENCE_EVENT.maskValue())  != 0;
	    }

    }

}
