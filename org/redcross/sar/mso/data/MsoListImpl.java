package org.redcross.sar.mso.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IChangeIf.IChangeReferenceIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.IMsoModelIf.ModificationState;
import org.redcross.sar.mso.data.IMsoObjectIf.IObjectIdIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.MsoRuntimeException;

public class MsoListImpl<M extends IMsoObjectIf> implements IMsoListIf<M>, IMsoObjectHolderIf<M>
{
    protected String m_name;

    /**
     * The list owner
     */
    protected final AbstractMsoObject m_owner;
    
    /**
     * The references existing remotely 
     */
    protected final HashMap<String, IMsoReferenceIf<M>> m_exits;

    /**
     * The references added locally
     */
    protected final HashMap<String, IMsoReferenceIf<M>> m_added;

    /**
     * The references deleted locally
     */
    protected final HashMap<String, IMsoReferenceIf<M>> m_deleted;
    
    /**
     * The references pending deletion locally. This list store deleted 
     * references until resumeClientUpdate() is called.
     */
    protected final HashMap<String, IMsoReferenceIf<M>> m_deleting;
    
    /**
     * The owner to objects reference cardinality (0,1,..,n,...*)
     */
    protected final int m_cardinality;
    
    /**
     * If <code>true</code>, the list is the owner of referenced objects. 
     * When references to objects in main lists is deleted, the objects are
     * also deleted.
     */
    protected final boolean m_isMain;
    
    /**
     * The object class. Used to collect items based on object class.
     */
    protected final Class<M> m_objectClass;
    
    /**
     * The MSO model owning the objects.
     */
    protected final IMsoModelIf m_msoModel;

    /**
     * The change count since initialization
     */
    protected int m_changeCount;

    /* =========================================================
     * Constructors
     * ========================================================= */
    
    public MsoListImpl(Class<M> theObjectClass, IMsoObjectIf anOwner)
    {
        this(theObjectClass,anOwner, "");
    }

    public MsoListImpl(Class<M> theObjectClass, IMsoObjectIf anOwner, String theName)
    {
        this(theObjectClass,anOwner, theName, false);
    }

    public MsoListImpl(Class<M> theObjectClass, IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        this(theObjectClass, anOwner, theName, isMain, 0, 50);
    }

    public MsoListImpl(Class<M> theObjectClass, IMsoObjectIf anOwner, String theName, boolean isMain, int cardinality, int aSize)
    {
    	if(!(anOwner instanceof AbstractMsoObject))
    	{
    		throw new IllegalArgumentException("MsoListImpl must have a AbstractMsoObject owner");
    	}
        m_owner = (AbstractMsoObject)anOwner;
        m_name = theName;
        m_msoModel = m_owner.getModel();
        m_isMain = isMain;
        m_cardinality = cardinality;
        m_exits = new LinkedHashMap<String, IMsoReferenceIf<M>>(aSize);
        m_added = new LinkedHashMap<String, IMsoReferenceIf<M>>(aSize);
        m_deleted = new LinkedHashMap<String, IMsoReferenceIf<M>>(aSize);
        m_deleting = new LinkedHashMap<String, IMsoReferenceIf<M>>(aSize);
        m_objectClass = theObjectClass;
    }
    
    /* =========================================================
     * IMsoListIf implementation
     * ========================================================= */

    public String getName()
    {
        return m_name;
    }

    @Override
	public boolean isLocalState() {
        for (M it : getAllItems())
        {
            if(!isLocal(it)) return false;
        }
        return true;
	}

	@Override
	public boolean isRemoteState() {
        for (M it : getAllItems())
        {
            if(!isRemote(it)) return false;
        }
        return true;
	}

	@Override
	public boolean isConflictState() {
		return false;
	}
	
	@Override
	public boolean isMixedState() {
		int count = 0;
		count = isLocalState()?count+1:count;
		count = isRemoteState()?count+1:count;
		return count>1;
	}
	
	@Override
	public boolean isLoopbackMode() {
        for (IMsoReferenceIf<M> reference : getReferences())
        {
            if(!reference.isLoopbackMode()) return false;
        }
        return true;
	}	
	
	@Override
	public boolean isRollbackMode() {
        for (IMsoReferenceIf<M> reference : getReferences())
        {
            if(!reference.isRollbackMode()) return false;
        }
        return true;
	}	
	
	public boolean isChanged() {
    	return m_added.size()>0 || m_deleted.size()>0 || m_deleting.size()>0;
    }

    public boolean isChangedSince(int changeCount)
    {
        return (m_changeCount>=changeCount);
    }
    
    public int getChangeCount()
    {
        return m_changeCount;
    }

    public IMsoObjectIf getOwner()
    {
        return m_owner;
    }

    public boolean isMain()
    {
        return m_isMain;
    }

    public int size()
    {
        return m_exits.size() + m_added.size();
    }
    
    public M getHeadObject()
    {
    	IMsoReferenceIf<M> refObj = getHeadReference();
        return refObj!=null?refObj.getReference():null;
    }

    public M getObject(IObjectIdIf anObjectId)
    {
        return getObject(anObjectId.getId());
    }

    public M getObject(String anObjectId)
    {
    	IMsoReferenceIf<M> refObj = getReference(anObjectId);
        return refObj!=null?refObj.getReference():null;
    }

    public Collection<M> getObjects()
    {
        HashSet<M> retVal = new HashSet<M>(size());
        retVal.addAll(getItems(m_exits.values()));
        retVal.addAll(getItems(m_added.values()));
        return retVal;
    }

    
    
    @Override
	public IMsoReferenceIf<M> getHeadReference() {
        Iterator<IMsoReferenceIf<M>> iterator = getReferences().iterator();
        if (iterator.hasNext())
        {
            return iterator.next();
        }
        return null;
	}

	@Override
	public IMsoReferenceIf<M> getReference(M anObject) {
		return getReference(anObject.getObjectId());
	}
	
	@Override
	public IMsoReferenceIf<M> getReference(IObjectIdIf anObjectId) {
		return getReference(anObjectId.getId());
	}

	@Override
	public IMsoReferenceIf<M> getReference(String anObjectId) {
		IMsoReferenceIf<M> refObj = m_exits.get(anObjectId);
        if (refObj == null)
        {
            refObj = m_added.get(anObjectId);
        }
        return refObj;
	}

	@Override
	public Collection<IMsoReferenceIf<M>> getReferences() {
        HashSet<IMsoReferenceIf<M>> retVal = new HashSet<IMsoReferenceIf<M>>(size());
        retVal.addAll(m_exits.values());
        retVal.addAll(m_added.values());
        return retVal;
	}

	public int getCardinality()
    {
        return m_cardinality;
    }

    public Object validate() {
    	if(m_cardinality>0) {
    		return (size()<m_cardinality);
    	}
    	for(IMsoObjectIf it : getObjects()) {
    		Object retVal = it.validate();
    		if(!isTrue(retVal)) return retVal;
    	}
    	return true;
    }
    
    public boolean isDeleted(IMsoObjectIf anObject) {
    	if(anObject!=null) {
    		return m_deleted.containsKey(anObject.getObjectId()) || isDeleting(anObject);
    	}
    	return false;
    }

    public boolean isDeleting(IMsoObjectIf anObject) {
    	if(anObject!=null) {
    		return m_deleting.containsKey(anObject.getObjectId());
    	}
    	return false;
    }

    public boolean isLocal(IMsoObjectIf anObject) {
    	if(anObject!=null) {
    		return m_added.containsKey(anObject.getObjectId()) || isDeleted(anObject);
    	}
    	return false;
    }

    public boolean isRemote(IMsoObjectIf anObject) {
    	if(anObject!=null) {
    		return m_exits.containsKey(anObject.getObjectId());
    	}
    	return false;
    }

    public boolean exists(M anObject)
    {
        return (m_exits.containsKey(anObject.getObjectId()) || m_added.containsKey(anObject.getObjectId()));
    }

    public boolean contains(M anObject)
    {
        return exists(anObject) || isDeleted(anObject);
    }

    public ModificationState getState(M anObject)
    {
        if (isRemote(anObject))
        {
            return ModificationState.STATE_REMOTE;
        }
        if (isLocal(anObject))
        {
            return ModificationState.STATE_LOCAL;
        }
        return ModificationState.STATE_UNDEFINED;
    }

    public boolean add(M anObject)
    {

    	// initialize flags
    	boolean isDirty = false;
    	
    	// valid?
        if (isSetup(anObject))
        {

	    	/* ========================================================
	    	 * Successfully adding a new object is dependent on the
	    	 * update mode of the MSO model.
	    	 *
	    	 * If the model is in REMOTE_UPDATE_MODE, any change from
	    	 * the server will be a new remote object that by definition
	    	 * can not exist locally in this list. Hence, ADD operations
	    	 * on list can not produce a conflict. 
	    	 *
	    	 * If a reference is added locally (isChanged() is true), 
	    	 * and the model is in REMOTE_UPDATE_MODE, this update may 
	    	 * be a loopback. Since loopback updates are just a ACK
	    	 * from the server, no references are are changed. Hence,
	    	 * IMsoClientUpdateListener listeners are not required to fetch
	    	 * added references. However, loopback updates may be used to
	    	 * indicate to the user that the commit was successful. 			    	  
        	 *
	    	 * If the model is in LOCAL_UPDATE_MODE, a object is
	    	 * created locally and should be added as such.
	    	 *
	    	 * A commit() or rollback() will remove all locally
	    	 * added and deleted objects.
	    	 *
	    	 * ======================================================== */

	        // update internal lists
	        switch (m_msoModel.getUpdateMode())
	        {
	            case REMOTE_UPDATE_MODE:
	            {

	            	/* ===========================================================
	            	 * Update to SERVER state
	            	 *
			    	 * If the model is in REMOTE_UPDATE_MODE,
			    	 * any change from the server will be a new remote object.
			    	 * Consequently, this object can not exist locally. Hence, ADD
			    	 * operations can not produce a conflict.
			    	 * 
	            	 * ================================================================
	            	 * IMPORTANT 1
	            	 * ================================================================
	            	 *
	            	 * If the object exists locally, this directly yields that a
	            	 * proper LOOPBACK must have occurred. Any local existence should
	            	 * be removed from the local ADDED list. A new remote object can
	            	 * not by definition be deleted locally because any locally
	            	 * deleted object is already added remotely and can thus not be
	            	 * added once again.
	            	 * 
	            	 *	            	
	            	 * =========================================================== */

	            	// valid operation?
	                if (!isRemote(anObject))
	                {
		            	// get key
		            	String id = anObject.getObjectId();

		            	// initialize 
		            	IMsoReferenceIf<M> aRefObj = null;

		                // remove local existence?
		                if(isLocal(anObject))
		                {
		                	// remove local delete if exists (no action required)
		                	m_deleting.remove(id);
		                	m_deleted.remove(id);
		                	
		                	// remove local existence from lists if exists (loopback)
		                	aRefObj = m_added.remove(id);
		                	
		                	/* If a added reference was found, this is a 
		                	 * loopback (Server ACK).
		                	 * If nothing was found, a new reference is created*/
		                	isDirty = (aRefObj = updateReference(id, anObject, aRefObj, false))!=null;
		                	
		                }
		                else {
		                	/* The If nothing was found, a new reference is created*/
		                	isDirty = (aRefObj = updateReference(id, anObject, aRefObj, true))!=null;		                			                	
		                }

		                // add reference to REMOTE state list?
		                if(isDirty) 
		                {
		                	m_exits.put(id,aRefObj);
		                }

	                }

	                break;
	            }
	            default: // LOCAL_UPDATE_MODE
	            {

	            	/* ===========================================================
	            	 * Update reference to the appropriate state
	            	 *
	            	 * The default update mode is LOCAL_UPDATE_MODE. This mode
	            	 * indicates that the change originates from a GUI (user)
	            	 * or Service (application) invocation. Local existence is
	            	 * registered by adding to the ADDED list
	            	 *
	            	 * ================================================================
	            	 * IMPORTANT
	            	 * ================================================================
	            	 *
	            	 * The new object can not exist neither remotely nor locally.
	            	 *
	            	 * =========================================================== */

	            	// valid operation?
	                if (!exists(anObject))
	                {
		            	// get key
		            	String id = anObject.getObjectId();

		                // add to LOCAL state list
		                m_added.put(id,createReference(id, anObject));

		                // is changed
		                isDirty = true;

	                }
	            }
	        }
	    }

    	// changed?
        if(isDirty)
        {
        	incrementChangeCount();
        }

        // finished
        return isDirty;
        
    }

    public boolean remove(M anObject)
    {

        if (anObject == null)
        {
            throw new MsoRuntimeException(getName() + ": Cannot remove null object");
        }
        
        if(!exists(anObject)) 
        {
            throw new MsoRuntimeException(getName() + ": Cannot remove object the do not exist in list");
        }

    	/* ========================================================
    	 * Successfully removing a object is dependent on the
    	 * update mode of the MSO model.
    	 *
    	 * If the model is in REMOTE_UPDATE_MODE, the removed object
    	 * does not exist remotely and should be removed from the
    	 * list completely. Hence, REMOVE operations can not 
    	 * produce a conflict.
    	 *
    	 * A commit() or rollback() will remove all locally
    	 * added and deleted objects.
    	 *
    	 * ======================================================== */

        // initialize
    	boolean bFlag = false;

    	// remove from list
        if (m_isMain)
        {
        	/* 
        	 * Since this list is a main list, reference objects 
        	 * must be deleted by definition (in general, all 
        	 * IMsoObjectIf instances must be owned by a 
        	 * IMsoListIf instance). Since each referenced object 
        	 * is owned by this list, doDeleteReference is invoked
        	 * from the destroy method of the referenced object. 
        	 */
            bFlag = anObject.delete();
        } 
        else
        {	
        	// only delete reference
            bFlag = deleteReference(anObject);
        }

        // increment change count?
        if(bFlag) incrementChangeCount();

        // finished
        return bFlag;

    }


    /**
     * Delete all objects </p>
     * Can be optimized, but has probably very little effect, as the list normally will be quite short.
     */
    public void removeAll()
    {
        M refObj = getHeadObject();
        while (refObj != null)
        {
            remove(refObj);
            refObj = getHeadObject();
        }
    }

    @Override
    public void rollback()
    {
        // rollback all added references
        rollbackAddedReferences(m_added);
        	
        // rollback all removed references
        rollbackRemovedReferences(m_deleted);
        
        // forward to remote items?
        if (m_isMain)
        {
            for (IMsoReferenceIf<M> it : m_exits.values())
            {
            	M msoObj = it.getReference();
            	if(msoObj!=null) 
            	{
            		((AbstractMsoObject) msoObj).rollback();
            	}
            }
        }
    }

    @Override
    public void rollback(List<IChangeReferenceIf> items) {
            	
    	// rollback added references found among given items
        rollbackAddedReferences(selectReferences(m_added,items));
        	
        // rollback removed references found among given items
        rollbackRemovedReferences(selectReferences(m_deleted,items));
        
        // forward to remote items?
        if (m_isMain)
        {
        	// loop over existing reference among given items
            for (IMsoReferenceIf<M> it : selectReferences(m_exits,items).values())
            {
            	M msoObj = it.getReference();
            	if(msoObj!=null) 
            	{
            		((AbstractMsoObject) msoObj).rollback();
            	}
            }
        }
    	
    }
    
    /**
     * Check if reference to given object can be deleted
     *
     * @param anObject - the object to remove a reference from
     * @return this method only return <code>true</code> if a reference to the object exists or is deleted locally
     */
    public boolean isReferenceDeletable(M anObject)
    {
    	return exists(anObject) || isDeleted(anObject);
    }

    public boolean deleteReference(M anObject)
    {
    	// invalid object?
    	if(!isReferenceDeletable(anObject)) return false;
    	
        /* ================================================================
         * 
    	 * If a reference is deleted locally (isChanged() is true), 
    	 * and the model is in REMOTE_UPDATE_MODE, this update may 
    	 * be a loopback. Since loopback updates are just a ACK
    	 * from the server, no references are are changed. Hence,
    	 * IMsoClientUpdateListener listeners are not required to fetch
    	 * added or deleted references. However, loopback updates may 
    	 * be used to indicate to the user that the commit was successful.
    	 *  			    	  
    	 * If the model is in LOCAL_UPDATE_MODE, a object is
    	 * deleted locally and should be removed as such.
    	 *
         * If client updates are suspended, deleted references must
         * be kept until resumeClientUpdate() is called. If not,
         * the client will never be notified because deleted references
         * are not present in m_added or m_items any more. m_deleting
         * is used to store deleted references until resumeClientUpdate()
         * is called.
         * 
         * ================================================================ */
    	
        // initialize flags
        boolean isLocalUpdate = m_msoModel.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);
        boolean isUpdateSuspended = m_msoModel.isUpdateSuspended();
        
        // get object id
        String id = anObject.getObjectId();

        // remove reference from items 
        IMsoReferenceIf<M> refObj = m_exits.remove(id);
        
        // exists remotely?
        if (refObj != null)
        {
        	// add to locally deleted items? 
            if (isLocalUpdate)
            {
                m_deleted.put(id, refObj);
            }
            
        	// add to pending deletions?
            if(isUpdateSuspended)
            {
            	m_deleting.put(id, refObj);
            }
            
        	/* remove this list as holder of 
        	 * the object and reset the reference */ 
        	destroyReference(refObj);
        	
        } 
        else
        {
        	// remove locally added reference (rollback)
            refObj = m_added.remove(id);
            
        	/* remove this list as holder of 
        	 * the object and reset the reference */ 
        	destroyReference(refObj);
        	
            // remove locally deleted reference (loopback) 
        	refObj =  m_deleted.remove(id);
        	
        	/* add this list as holder of 
        	 * the object and rollback the old reference */ 
        	rollbackReference(refObj);
        	
        }
        // finished
        return (refObj != null);
    }

    public Set<M> selectItems(Selector<M> aSelector)
    {
        return selectItemsInCollection(aSelector, getObjects());
    }

    public List<M> selectItems(Selector<M> aSelector, Comparator<M> aComparator)
    {
        return selectItemsInCollection(aSelector, aComparator, getObjects());
    }

    public M selectSingleItem(Selector<M> aSelector)
    {
        return selectSingleItem(aSelector, getObjects());
    }
    
    /* =========================================================
     * public MsoListImpl methods
     * ========================================================= */
    
    public void checkCreateOp()
    {
        verifyMainOperation("Cannot create object in a non-main list");
    }

    public void verifyMainOperation(String aMessage)
    {
        if (!m_isMain)
        {
            throw new MsoRuntimeException(aMessage);
        }
    }

    public void resumeClientUpdate(boolean all)
    {
    	// only notify existing items once in main list
        if (m_isMain)
        {
        	// loop over all remote and local (added) items
            for (M object : getObjects())
            {
                object.resumeClientUpdate(all);
            }
        }
        // notify deleted and pending items
        for(IMsoReferenceIf<M> it : m_deleted.values()) {
        	M msoObj = it.getReference();
        	if(msoObj!=null) {
        		msoObj.resumeClientUpdate(all);
        	}
        }
        for(IMsoReferenceIf<M> it : m_deleting.values()) {
        	M msoObj = it.getReference();
        	if(msoObj!=null) {
        		msoObj.resumeClientUpdate(all);
        	}
        }
        // reset pending deletions
        m_deleting.clear();
    }
    
    /* =========================================================
     * private helper methods
     * ========================================================= */
    
    /**
     * Remove all added references properly
     */
    private void rollbackAddedReferences(Map<String, IMsoReferenceIf<M>> added)
    {

    	// has no objects?
        if (added.size() == 0)
        {
            return;
        }

        /* Copy the list and clear the original before any
         * events are sent around, since the events are checking
         * the original list */
        Collection<IMsoReferenceIf<M>> items = new Vector<IMsoReferenceIf<M>>(added.size());
        items.addAll(added.values());

        
        // clear from the original list
        if(added==m_added) 
        {
        	
        	// clear all added items
        	m_added.clear();
        	
        } else 
        {
        	// delete individual added items
	        for(String it : added.keySet()) {
	        	m_added.remove(it);
	        }
        }
        
        // loop over list copy
        for(IMsoReferenceIf<M> it : items)
        {
        	// get IMsoObjectIf object
        	M msoObj = it.getReference();
        	// has object?
        	if(msoObj!=null) {
            	// same algorithm as for remove()
                if (m_isMain)
                {
                	msoObj.delete();
                } 
                else
                {
                	deleteReference(msoObj);
                }        		
        	}
        }
    }

    private boolean isTrue(Object value) {
    	if(value instanceof Boolean)
    		return (Boolean)value;
    	return false;
    }
    
    private Collection<M> getAllItems() {
    	Collection<M> allList = new Vector<M>();
    	allList.addAll(getObjects());
    	allList.addAll(getItems(m_deleted.values()));
    	allList.addAll(getItems(m_deleting.values()));
    	return allList;
    }

    /* =========================================================
     * Protected methods
     * ========================================================= */

    /**
     * This method ensures that a LOCAL added object is moved to
     * REMOTE state when it is created after a commit.
     *
     * @param IObjectIdIf anObjectId - the item to check for loopback
     * @return The found loopback object
     */

    protected M getLoopback(IMsoObjectIf.IObjectIdIf anObjectId)
    {
    	// check if exists
    	M retVal = getObject(anObjectId.getId());
        // move to REMOTE state?
        if(isLocal(retVal)) add(retVal);
    	// succeeded
        return getObject(anObjectId.getId());
    }
    
    /**
     * Update list name.
     * 
     * @param aName - the name
     * 
     */
    protected void setName(String aName)
    {
        m_name = aName.toLowerCase();
    }

    protected void incrementChangeCount() {
    	m_changeCount++;
    }
    
    protected M createdItem(M anObject)
    {
    	((AbstractMsoObject) anObject).setup(false);
        ((AbstractMsoObject) anObject).setOwningMainList(this);
        add(anObject);
        anObject.resumeClientUpdate(false);
        return anObject;
    }

    protected M createdUniqueItem(M anObject)
    {
        try
        {
            return createdItem(anObject);
        }
        catch (DuplicateIdException e)
        {
            //throw new MsoRuntimeException("Duplicate object id, should be unique: " + anObject.getObjectId());
        }
        return null;
    }

    protected IObjectIdIf makeUniqueId()
    {
        IObjectIdIf retVal;
        do
        {
            retVal = m_msoModel.getDispatcher().makeObjectId();
        }
        while (m_exits.get(retVal.getId()) != null || m_added.get(retVal.getId()) != null || m_deleted.get(retVal.getId()) != null);
        return retVal;
    }

    public Collection<IChangeIf.IChangeReferenceIf> getChangedReferences()
    {
    	// initialize collection
        Vector<IChangeIf.IChangeReferenceIf> list = new Vector<IChangeIf.IChangeReferenceIf>();
        
        // add changes
        for (IMsoReferenceIf<M> it : m_added.values())
        {
            list.addAll(it.getChangedReferences());
        }
        for (IMsoReferenceIf<M> it : m_deleted.values())
        {
            list.addAll(it.getChangedReferences());
        }
        
        // finished
        return list;
    }
    
    public Collection<IChangeIf.IChangeReferenceIf> getChangedReferences(Collection<IChangeIf> partial)
    {
    	// initialize collection
        Vector<IChangeIf.IChangeReferenceIf> list = new Vector<IChangeIf.IChangeReferenceIf>();
        
        // add changes
        for (IMsoReferenceIf<M> it : m_added.values())
        {
            list.addAll(it.getChangedReferences(partial));
        }
        for (IMsoReferenceIf<M> it : m_deleted.values())
        {
            list.addAll(it.getChangedReferences(partial));
        }
        
        // finished
        return list;
    }
    
    
    

    protected int makeSerialNumber()
    {
        int max = 0;
        for (M item : getObjects())
        {
            try
            {
                ISerialNumberedIf serialItem = (ISerialNumberedIf) item;
                max = Math.max(max, serialItem.getNumber());
            }
            catch (ClassCastException e)
            {
                //throw new MsoRuntimeException("Object " + item + " is not implementing ISerialNumberedIf");
            }
        }
        return max + 1;
    }

    protected int makeSerialNumber(Enum<?> type)
    {
        int max = 0;
        for (M item : getObjects())
        {
            try
            {
            	if(MsoUtils.getType(item, true)==type) {
	                ISerialNumberedIf serialItem = (ISerialNumberedIf) item;
	                max = Math.max(max, serialItem.getNumber());
            	}
            }
            catch (ClassCastException e)
            {
                //throw new MsoRuntimeException("Object " + item + " is not implementing ISerialNumberedIf");
            }
        }
        return max + 1;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MsoListImpl<M> list;
        try
        {
            list = (MsoListImpl<M>)o;
        }
        catch (Exception e)
        {
            return false;
        }

        if (m_owner != null ? !m_owner.equals(list.m_owner) : list.m_owner != null)
        {
            return false;
        }
        if (m_name != null ? !m_name.equals(list.m_name) : list.m_name != null)
        {
            return false;
        }
        if (m_isMain != list.m_isMain)
        {
            return false;
        }
        if (m_exits != null ? !m_exits.equals(list.m_exits) : list.m_exits != null)
        {
            return false;
        }
        if (m_added != null ? !m_added.equals(list.m_added) : list.m_added != null)
        {
            return false;
        }
        if (m_deleted != null ? !m_deleted.equals(list.m_deleted) : list.m_deleted != null)
        {
            return false;
        }

        return true;
    }

    /*
    public IMsoListIf<M> getClone()
    {
    	// create list
        MsoListImpl<M> list = new MsoListImpl<M>(getObjectClass(), getOwner(), getName(), isMain(), getCardinality(), size());
        // suspend updates
        list.resumeClientUpdate(all)
        for (M item : getItems(m_items.values()))
        {
            list.add(item);
        }
        for (M item : getItems(m_added.values()))
        {
            list.m_added.put(item.getObjectId(), item);
        }
        for (M item : getItems(m_deleted.values()))
        {
            list.m_deleted.put(item.getObjectId(), item);
        }
        for (M item : getItems(m_deleting.values()))
        {
            list.m_deleting.put(item.getObjectId(), item);
        }
        return list;
    }
	*/
    
    /**
     * Get the item class
     */
    public Class<M> getObjectClass() {
    	return m_objectClass;
    }

    protected List<M> renumberDuplicateNumbers(M anItem)
    {
    	List<M> updates = new ArrayList<M>(size());
    	if(isNumberDuplicate(anItem))
    		return renumberCandidates(selectCandidates(getRenumberSelector(anItem)));
    	else
    		return updates;
    }

    private boolean isNumberDuplicate(M anItem) {
    	if(anItem instanceof ISerialNumberedIf) {
	    	ISerialNumberedIf s0 = (ISerialNumberedIf)anItem;
	    	for(IMsoObjectIf it: getItems(m_exits.values())) {
	    		if(it instanceof ISerialNumberedIf) {
	    			if(((ISerialNumberedIf)it).getNumber()==s0.getNumber())
	    				return true;
	    		}
	    	}
    	}
    	return false;
    }
    
    /*
    private M getItem(IMsoReferenceIf<M> aReference) {
    	return aReference!=null?aReference.getReference():null;
    }
    */
    
    protected Collection<M> getItems(Collection<IMsoReferenceIf<M>> list) {
    	Collection<M> items = new Vector<M>();
    	for(IMsoReferenceIf<M> it : list) {
    		items.add(it.getReference());
    	}
    	return items;
    }
    
    protected Map<String, IMsoReferenceIf<M>> selectReferences(Map<String, IMsoReferenceIf<M>> references, Collection<IChangeReferenceIf> objects) {
    	Map<String, IMsoReferenceIf<M>> map = new HashMap<String, IMsoReferenceIf<M>>();
    	for(IChangeReferenceIf it : objects) {
    		IMsoObjectIf msoObj = it.getReferredObject();
    		String id = msoObj.getObjectId();
    		IMsoReferenceIf<M> refObj = references.get(id);
    		if(refObj!=null) map.put(id,refObj);
    	}
    	return map;
    }
    
    private boolean isSetup(M msoObj) {
    	if(msoObj == null) return false;
    	if(msoObj instanceof AbstractMsoObject) {
    		return ((AbstractMsoObject) msoObj).isSetup();	
    	}
    	return true;
    }
    
    /**
     * Create a reference.
     * @param id - the object id
     * @param msoObj - the object to create a reference to
     * 
     * @return Create IMsoReferenceIf instance
     */
    private IMsoReferenceIf<M> createReference(String id, M msoObj) {
    	IMsoReferenceIf<M> aRefObj = null;
    	if(msoObj!=null) {
	    	aRefObj = new MsoReferenceImpl<M>(m_owner,id,0,true);
	    	((AbstractMsoObject)msoObj).addMsoObjectHolder(this);
	    	aRefObj.setReference(msoObj);
	        String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
	        System.out.println("Added reference from " + s + " to " + msoObj);
    	}
	    return aRefObj;
    }
    
    /**
     * Update given reference
     * @param aRefObj - the reference to update
     */
    private IMsoReferenceIf<M> updateReference(
    		String id, M msoObj, 
    		IMsoReferenceIf<M> aRefObj, boolean isRemote) {

        // is a loopback?
        if(aRefObj!=null) 
        {
        	// update reference
        	aRefObj.setReference(msoObj);
        } 
        else if(isRemote)
        {
        	// create a new reference
        	aRefObj = createReference(id, msoObj);		                	
        }
        
        // finished
        return aRefObj;
    }
    
    /**
     * Destroy given reference
     * @param refObj - the reference to destroy
     */
    private void destroyReference(IMsoReferenceIf<M> refObj) {
    	if(refObj!=null) {
	    	IMsoObjectIf msoObj = refObj.getReference();
	    	if(msoObj!=null) {
		    	((AbstractMsoObject)msoObj).removeMsoObjectHolder(this);
		    	refObj.setReference(null);
		        String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
		        System.out.println("Deleted reference from " + s + " to " + msoObj);
	    	}
    	}
    }
    
    /**
     * Rollback given reference.
     * 
     * @param refObj - the reference to rollback
     */
    private void rollbackReference(IMsoReferenceIf<M> refObj) {
    	if(refObj!=null) {
    		// undo changes
    		refObj.rollback();
    		// get msoObj
	    	IMsoObjectIf msoObj = refObj.getReference();
	    	// add this list as object holder?    		
	    	if(msoObj!=null) {
	        	((AbstractMsoObject)msoObj).addMsoObjectHolder(this);
		        String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
		        System.out.println("Rollback (added) reference from " + s + " to " + msoObj);
	    	}
    	}
    }    
    
    /**
     * Undelete all deleted object references
     */
    private void rollbackRemovedReferences(Map<String, IMsoReferenceIf<M>> deleted)
    {

    	// has no objects?
        if (deleted.size() == 0)
        {
            return;
        }
        
    	/*clear pending deletes*/
    	clearDeleting(deleted);
    	
        /* Copy the list and clear the original before any
         * events are sent around, since the events are checking
         * the original list */
        Collection<IMsoReferenceIf<M>> items = new Vector<IMsoReferenceIf<M>>(deleted.size());
        items.addAll(deleted.values());

        // clear from the original list
        if(deleted==m_deleted) 
        {
        	
        	// clear all deleted items
        	m_deleted.clear();
        	
        } else 
        {
        	// delete individual items
	        for(String it : deleted.keySet()) {
	        	m_deleted.remove(it);
	        }
        }
        
    	/* undelete all deleted objects */
    	for (IMsoReferenceIf<M> it : items)
        {
    		rollbackRemovedReference(it);
        }
    }

    /**
     * Clear pending deletions properly
     * 
     * @param aList
     */
    private void clearDeleting(Map<String, IMsoReferenceIf<M>> aList) {
    	// only clear if client update is resumed (active)
    	if(!m_msoModel.isUpdateSuspended()) {
	        for(IMsoReferenceIf<M> it : aList.values()) {
	        	M msoObj = it.getReference();
	        	if(msoObj!=null) {
	        		m_deleting.remove(msoObj.getObjectId());
	        	}
	        }
    	}
    }

    /**
     * Re-insert an object in items.
     *
     * @param anObject The reference to undelete.
     */
    private void rollbackRemovedReference(IMsoReferenceIf<M> aRefObj)
    {
        if (aRefObj != null)
        {
        	// add to items
            m_exits.put(aRefObj.getName(),aRefObj);
            // rollback change
            rollbackReference(aRefObj);            
        }
    }        

    protected Selector<M> getRenumberSelector(M anItem)
    {
        renumberSelector.setSelectionItem(anItem);
        return renumberSelector;
    }

    private List<M> selectCandidates(Selector<M> aSelector)
    {
        return selectItems(aSelector, descendingNumberComparator);
    }

    // Loop through all items with a number higher than
    private List<M> renumberCandidates(List<M> candidates)
    {
    	// initialize renumbered list
    	List<M> renumbered = new ArrayList<M>(candidates.size());

    	// loop over all candidates
        if (candidates.size() != 0)
        {

	        m_msoModel.setLocalUpdateMode();
	        int nextNumber = -1;
	        for (M item : candidates)
	        {
	            if (item instanceof ISerialNumberedIf)
	            {
	                ISerialNumberedIf numberedItem = (ISerialNumberedIf) item;
	                if (numberedItem.getNumberState() != ModificationState.STATE_REMOTE)
	                {
	                    if (nextNumber < 0)
	                    {
	                        nextNumber = numberedItem.getNumber() + 1;
	                    }
	                    int tmpNumber = numberedItem.getNumber();
	                    numberedItem.setNumber(nextNumber);
	                    nextNumber = tmpNumber;
	                    // add to list
	                    renumbered.add(item);
	                }
	            }
	        }
	        m_msoModel.restoreUpdateMode();
        }

        // finished
        return renumbered;

    }

    /* =========================================================
     * anonymous classes
     * ========================================================= */
    
    private final Comparator<M> descendingNumberComparator = new Comparator<M>()
    {
        public int compare(M o1, M o2)
        {
            if (o1 instanceof ISerialNumberedIf && o2 instanceof ISerialNumberedIf)
            {
                return -(((ISerialNumberedIf) o1).getNumber() - ((ISerialNumberedIf) o2).getNumber()); // sort descending
            }
            return 0;
        }
    };

    /* =========================================================
     * inner classes
     * ========================================================= */
    
    private final RenumberSelector<M> renumberSelector = new RenumberSelector<M>();

    protected class RenumberSelector<T extends M> implements Selector<T>
    {
        protected M m_selectionItem;

        void setSelectionItem(M anItem)
        {
            m_selectionItem = anItem;
        }

        public boolean select(T anObject)
        {
            if (anObject == m_selectionItem)
            {
                return false;
            }
            if (anObject instanceof ISerialNumberedIf)
            {
                return ((ISerialNumberedIf) anObject).getNumber() >= ((ISerialNumberedIf) m_selectionItem).getNumber();
            }
            return false;
        }
    }
    
    /* =========================================================
     * Static methods
     * ========================================================= */
    
    /**
     * Insert an item into a list.
     *
     * @param aList       The list to insert into
     * @param anItem      The item to add
     * @param aComparator A comparator. If null, the item is appended to the list, if not null, used as a comparator to sort the list.
     */
    private static <T extends IMsoObjectIf> void addSorted(ArrayList<T> aList, T anItem, Comparator<? super T> aComparator)
    {
        if (aComparator == null)
        {
            aList.add(anItem);
        } else
        {
            int size = aList.size();
            int location = Collections.binarySearch(aList, anItem, aComparator);
            if (location < 0)
            {
                location = -location - 1;
            } else
            {
                while (location < size && aComparator.compare(anItem, aList.get(location)) <= 0)
                {
                    location++;
                }
            }
            aList.add(location, anItem);
        }
    }

    public static <T extends IMsoObjectIf> Set<T> selectItemsInCollection(Selector<? super T> aSelector, Collection<T> theItems)
    {
        Set<T> retVal = new LinkedHashSet<T>();
        for (T item : theItems)
        {
            if (aSelector.select(item))
            {
                retVal.add(item);
            }
        }
        return retVal;
    }

    public static <T extends IMsoObjectIf> List<T> selectItemsInCollection(Selector<? super T> aSelector, Comparator<? super T> aComparator, Collection<T> theItems)
    {
        ArrayList<T> retVal = new ArrayList<T>();
        for (T item : theItems)
        {
            if (aSelector.select(item))
            {
                addSorted(retVal,item,aComparator);
            }
        }
        return retVal;
    }

    public static <T extends IMsoObjectIf> T selectSingleItem(Selector<? super T> aSelector, Collection<T> theItems)
    {
        for (T item : theItems)
        {
            if (aSelector.select(item))
            {
                return item;
            }
        }
        return null;
    }
    
}