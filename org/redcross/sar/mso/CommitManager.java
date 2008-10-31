package org.redcross.sar.mso;

import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.committer.CommitWrapper;
import org.redcross.sar.mso.committer.ICommitWrapperIf;
import org.redcross.sar.mso.committer.IUpdateHolderIf;
import org.redcross.sar.mso.data.AbstractMsoObject;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;
import org.redcross.sar.util.except.CommitException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * The purpose of the commit manager is to catch Server update events, accumulate them, and when a commit is executed,
 * fire {@link org.redcross.sar.mso.event.MsoEvent.Commit} events.
 * The event provides access to MSO data structures that shall be committed  by passing a {@link org.redcross.sar.mso.committer.ICommitWrapperIf} object
 * to the listeners.
 */
public class CommitManager implements ICommitManagerIf
{
    /**
     * Types of commit depending on what has happened to the object/relation.
     */
    public enum CommitType
    {
        COMMIT_CREATED,
        COMMIT_MODIFIED,
        COMMIT_DELETED
    }

    /**
     * Reference to the owning MsoModel
     */
    private final IMsoModelIf m_msoModel;

    /**
     * Vector for accumulating {@link UpdateHolder} objects that is updated.
     */
    private final Vector<IUpdateHolderIf> m_updates = new Vector<IUpdateHolderIf>(50);

    /**
     * @param theModel Reference to the singleton MSO model object holding the MsoModel object.
     */
    public CommitManager(IMsoModelIf theModel)
    {
        m_msoModel = theModel;
        m_msoModel.getEventManager().addServerUpdateListener(new IMsoUpdateListenerIf()
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
        for (IUpdateHolderIf it : m_updates)
        {
            if (it.getMsoObject().getObjectId().equals(anObject.getObjectId()))
            {
            	if(it instanceof UpdateHolder) {
            		((UpdateHolder)it).applyMask(aMask);
                    return;
            	}
            }
        }
        m_updates.add(new UpdateHolder(anObject, aMask));
    }

    /**
     * Perform commit.
     * <p/>
     * Generates a {@link org.redcross.sar.mso.event.MsoEvent.Commit} event.
     * @throws org.redcross.sar.util.except.CommitException when the commit fails
     */
    public void commit() throws CommitException
    {
        m_msoModel.getEventManager().notifyCommit(createCommit(m_updates));
    }

    /**
     * Perform a partial commit
     * <p/>
     * @param List<UpdateHolder> updates - holder for updates
     * @throws org.redcross.sar.util.except.CommitException when the commit fails
     */
    public void commit(List<IUpdateHolderIf> updates) throws CommitException
    {
        m_msoModel.getEventManager().notifyCommit(createCommit(updates));
    }

    /**
     * Returns pending updates
     * <p/>
     */
    public List<IUpdateHolderIf> getUpdates()
    {
    	return new ArrayList<IUpdateHolderIf>(m_updates);
    }

    /**
     * Returns pending updates of specific class
     * <p/>
     */
    public List<IUpdateHolderIf> getUpdates(MsoClassCode of) {
    	return getUpdates(EnumSet.of(of));
    }

    /**
     * Returns pending updates of specific classes
     * <p/>
     */
    public List<IUpdateHolderIf> getUpdates(Set<MsoClassCode> of) {
    	List<IUpdateHolderIf> updates = new ArrayList<IUpdateHolderIf>(m_updates.size());
    	for (IUpdateHolderIf it : m_updates)
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
    public IUpdateHolderIf getUpdates(IMsoObjectIf of) {
    	List<IMsoObjectIf> list = new ArrayList<IMsoObjectIf>(1);
    	list.add(of);
    	List<IUpdateHolderIf> updates = getUpdates(list);
    	return updates.size()>0 ? updates.get(0) : null;
    }

    /**
     * Returns pending updates of specific objects
     * <p/>
     */
    public List<IUpdateHolderIf> getUpdates(List<IMsoObjectIf> of)
    {
    	List<IUpdateHolderIf> updates = new ArrayList<IUpdateHolderIf>(m_updates.size());
    	for (IUpdateHolderIf it : m_updates)
        {

    		// add to updates?
    		if(of.contains(it.getMsoObject())) {
    			updates.add(it);
    		}

        }
        // finished
        return updates;
    }

    private ICommitWrapperIf createCommit(List<IUpdateHolderIf> updates)
    {
    	List<IUpdateHolderIf> buffer = new ArrayList<IUpdateHolderIf>(updates.size());
        CommitWrapper wrapper = new CommitWrapper();
        for (IUpdateHolderIf it : updates)
        {
        	// add to wrapper
    		wrapper.add(it);
        	// add to remove buffer?
        	if(!it.isPartial()) buffer.add(it);
        }
        // only remove full commit updates
        m_updates.removeAll(buffer);
        // ready to commit
        return wrapper;
    }

    /**
     * Perform rollback.
     * <p/>
     * Clears all accumulated information.
     */
    public void rollback()
    {
        m_updates.clear();
    }

    /**
     * Tell if some uncommited changes exist
     *
     * @return true if uncommited changes exist
     */
    public boolean hasUncommitedChanges()
    {
        return m_updates.size() > 0;
    }

    public boolean hasUncommitedChanges(MsoClassCode code) {
    	return getUpdates(code).size()>0;
    }

    public boolean hasUncommitedChanges(IMsoObjectIf msoObj) {
    	return getUpdates(msoObj)!=null;
    }

    class UpdateHolder implements IUpdateHolderIf
    {

    	private final IMsoObjectIf m_object;
    	private final List<IAttributeIf> m_partial =  new ArrayList<IAttributeIf>(1);

        private int m_mask;

        UpdateHolder(IMsoObjectIf anObject, int aMask)
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

        public List<IAttributeIf> getPartial() {
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
