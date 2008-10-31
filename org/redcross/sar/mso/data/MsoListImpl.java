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
import java.util.Set;
import java.util.Vector;

import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.CommitManager;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.IMsoModelIf.ModificationState;
import org.redcross.sar.mso.committer.CommittableImpl;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.MsoRuntimeException;

/**
 *
 */
public class MsoListImpl<M extends IMsoObjectIf> implements IMsoListIf<M>, IMsoObjectHolderIf<M>
{
    protected final IMsoObjectIf m_owner;
    protected String m_name;
    protected final HashMap<String, M> m_items;
    protected final HashMap<String, M> m_added;
    protected final HashMap<String, M> m_deleted;
    protected final HashMap<String, M> m_pending;
    protected final HashMap<M, Integer> m_conflicts;
    protected final int m_cardinality;
    protected final boolean m_isMain;
    protected final Class<M> m_itemClass;

    protected int m_changeCount;

    public MsoListImpl(Class<M> theItemClass, IMsoObjectIf anOwner)
    {
        this(theItemClass,anOwner, "");
    }

    public MsoListImpl(Class<M> theItemClass, IMsoObjectIf anOwner, String theName)
    {
        this(theItemClass,anOwner, theName, false);
    }

    public MsoListImpl(Class<M> theItemClass, IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        this(theItemClass, anOwner, theName, isMain, 0, 50);
    }

    public MsoListImpl(Class<M> theItemClass, IMsoObjectIf anOwner, String theName, boolean isMain, int cardinality, int aSize)
    {
        m_owner = anOwner;
        m_name = theName;
        m_isMain = isMain;
        m_cardinality = cardinality;
        m_items = new LinkedHashMap<String, M>(aSize);
        m_added = new LinkedHashMap<String, M>();
        m_deleted = new LinkedHashMap<String, M>();
        m_pending = new LinkedHashMap<String, M>();
        m_conflicts = new LinkedHashMap<M, Integer>();
        m_itemClass = theItemClass;
    }

    protected void setName(String aName)
    {
        m_name = aName.toLowerCase();
    }

    public String getName()
    {
        return m_name;
    }

    public int getChangeCount()
    {
        return m_changeCount;
    }

    protected void incrementChangeCount() {
    	m_changeCount++;
    }

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

    public IMsoObjectIf getOwner()
    {
        return m_owner;
    }

    public boolean isMain()
    {
        return m_isMain;
    }

    public Collection<M> getItems()
    {
        HashSet<M> retVal = new HashSet<M>(size());
        retVal.addAll(m_items.values());
        retVal.addAll(m_added.values());
        return retVal;
    }

    public M getItem()
    {
        Iterator<M> iterator = getItems().iterator();
        if (iterator.hasNext())
        {
            return iterator.next();
        }
        return null;
    }

    public M getItem(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        return getItem(anObjectId.getId());
    }

    public M getItem(String anObjectId)
    {
        M retVal = m_items.get(anObjectId);
        if (retVal == null)
        {
            retVal = m_added.get(anObjectId);
        }
        return retVal;
    }

    public boolean isDeleted(IMsoObjectIf anObject) {
    	if(anObject!=null) {
    		return m_deleted.containsKey(anObject.getObjectId()) || isDeleting(anObject);
    	}
    	return false;
    }

    public boolean isDeleting(IMsoObjectIf anObject) {
    	if(anObject!=null) {
    		return m_pending.containsKey(anObject.getObjectId());
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
    		return m_items.containsKey(anObject.getObjectId());
    	}
    	return false;
    }


    public boolean exists(M anObject)
    {
        return (m_items.containsKey(anObject.getObjectId()) || m_added.containsKey(anObject.getObjectId()));
    }

    public boolean contains(M anObject)
    {
        return exists(anObject) || isDeleted(anObject);
    }

    public ModificationState getState(M anObject)
    {
        if (isRemote(anObject))
        {
            return ModificationState.STATE_SERVER;
        }
        if (isLocal(anObject))
        {
            return ModificationState.STATE_LOCAL;
        }
        return ModificationState.STATE_UNDEFINED;
    }

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
    	M retVal = getItem(anObjectId.getId());
        // move to REMOTE state?
        if(isLocal(retVal)) add(retVal);
    	// succeeded
        return getItem(anObjectId.getId());
    }


    public boolean add(M anObject)
    {

    	// valid?
        if (anObject != null && ((AbstractMsoObject) anObject).isSetup())
        {

	    	/* ========================================================
	    	 * Successfully adding a new object is dependent on the
	    	 * update mode of the MSO model.
	    	 *
	    	 * If the model is in LOOPBACK_UPDATE_MODE, the added object
	    	 * exist now remotely and should be added as such.
	    	 *
	    	 * If the model is in REMOTE_UPDATE_MODE, any change from
	    	 * the server will be a new remote object that by definition
	    	 * can not exist locally in this list. Hence, ADD operations
	    	 * no list can not produce a conflict. Consequently,
	    	 * LOOPBACK_UPDATE_MODE and REMOTE_UPDATE_MODE is handled
	    	 * equally.
	    	 *
	    	 * If the model is in LOCAL_UPDATE_MODE, a object is
	    	 * created locally and should be added as such.
	    	 *
	    	 * A commit() or rollback() will remove all locally
	    	 * added and deleted objects.
	    	 *
	    	 * ======================================================== */

	        // update internal lists
	        switch (MsoModelImpl.getInstance().getUpdateMode())
	        {
	            case LOOPBACK_UPDATE_MODE:
	            case REMOTE_UPDATE_MODE:
	            {

	            	/* ===========================================================
	            	 * Update to SERVER state
	            	 *
			    	 * If the model is in LOOPBACK_UPDATE_MODE or REMOTE_UPDATE_MODE,
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
	            	 * ================================================================
	            	 * IMPORTANT 2
	            	 * ================================================================
	            	 *
	            	 * The LOOPBACK mode is by definition a violation of the SARA
	            	 * Protocol. The use of postProcessCommit() and LOOPBACK mode
	            	 * is therefore discarded.
	            	 *
	            	 * =========================================================== */

	            	// valid operation?
	                if (!isRemote(anObject))
	                {

		            	// get key
		            	String id = anObject.getObjectId();

		            	// assume not loop back
		            	boolean isLoopback = false;

		                // delete local existence?
		                if(isLocal(anObject))
		                {
		                	// remove from lists
		                	m_pending.remove(id);
		                	m_deleted.remove(id);
		                	// this is a loopback if exists locally
		                	isLoopback = (m_added.remove(id)!=null);
		                }

		                // add to REMOTE state list
		                m_items.put(id, anObject);

		                // register?
		                if(!isLoopback)
		                	registerAddedReference(anObject,true);

		            	// update
		                incrementChangeCount();

		                // success
		                return true;

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
		                m_added.put(id, anObject);

		                // register
		            	registerAddedReference(anObject,true);

		            	// update
		                incrementChangeCount();

		                // success
		                return true;

	                }
	            }
	        }
	    }

        // failure
        return false;
    }

    private void registerAddedReference(M anObject, boolean add)
    {
        if (anObject != null)
        {
        	if(add) ((AbstractMsoObject) anObject).addDeleteListener(this);
        	((AbstractMsoObject) anObject).registerAddedReference(this);
        }
        if (m_owner != null)
        {
            ((AbstractMsoObject) m_owner).registerAddedReference(this);
        }

    }

    public boolean remove(M anObject)
    {

        if (anObject == null)
        {
            throw new MsoRuntimeException(getName() + ": Cannot remove null object");
        }

    	/* ========================================================
    	 * Successfully removing a object is dependent on the
    	 * update mode of the MSO model.
    	 *
    	 * If the model is in LOOPBACK_UPDATE_MODE, the removed object
    	 * does not exist remotely and should be removed from the
    	 * list completely. The same applies for REMOTE_UPDATE_MODE.
    	 * Hence, REMOVE operations can not produce a conflict.
    	 *
    	 * If the model is in LOCAL_UPDATE_MODE, a object is
    	 * deleted locally and should be removed as such.
    	 *
    	 * A commit() or rollback() will remove all locally
    	 * added and deleted objects.
    	 *
    	 * ======================================================== */

        // initialize
    	boolean bFlag = false;

    	// remove from list
        if (isMain())
        {
            bFlag = anObject.delete();
        } else
        {
            bFlag = doDeleteReference(anObject);
        }

        // increment change count?
        if(bFlag) incrementChangeCount();

        // finished
        return bFlag;

    }

    /**
     * Can always execute doDeleteReference().
     *
     * @param anObject See implemented method {@link IMsoObjectHolderIf#canDeleteReference(IMsoObjectIf)}.
     */
    public boolean canDeleteReference(M anObject)
    {
    	return true;
    }

    public boolean doDeleteReference(M anObject)
    {
    	MsoModelImpl model = MsoModelImpl.getInstance();
        boolean isLocalUpdate = model.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);
        boolean updateServer = isLocalUpdate;

        /* ================================================================
         * If client updates are suspended, deleted references must
         * be kept until resumeClientUpdate() is called. If not,
         * the client will never be notified because deleted references
         * are not present in m_added or m_items any more. m_pending
         * is used to store deleted references until resumeClientUpdate()
         * is called.
         * ================================================================ */

        boolean isUpdateSuspended = model.isUpdateSuspended();

        String key = anObject.getObjectId();

        M refObj;
        refObj = m_items.remove(key);
        if (refObj != null)
        {
            if (isLocalUpdate)
            {
                m_deleted.put(key, anObject);
            }
            else {
            	m_deleted.remove(key);
            }
        } else
        {
        	// remove locally added
            refObj = m_added.remove(key);
            updateServer = false;
        }
        if (refObj != null)
        {
            String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
            System.out.println("Delete reference from " + s + " to " + anObject);
            ((AbstractMsoObject) refObj).removeDeleteListener(this);
            if(isUpdateSuspended)
            {
            	m_pending.put(key, refObj);
            }
            if (m_owner != null)
            {
                ((AbstractMsoObject) m_owner).registerRemovedReference(this,updateServer);
            }
            return true;
        }
        return false;
    }

    public void clear()
    {
    	incrementChangeCount();
    	clearList(m_items, true);
        clearList(m_added, false);
        clearDeleted(m_deleted);
    }

    private void clearList(HashMap<String, M> aList, boolean updateServer)
    {
        /*
         * Copy the list and clear the original before any
         * events are sent around, since the events are checking
         * the original list
         *
         */
        if (aList.size() == 0)
        {
            return;
        }

        if (m_owner != null)
        {
            ((AbstractMsoObject) m_owner).registerRemovedReference(this,updateServer);
        }

        Collection<String> tmpList = aList.keySet();
        while (tmpList.size() > 0)
        {
            M refObj = aList.get(tmpList.iterator().next());
            AbstractMsoObject abstrObj = (AbstractMsoObject) refObj;
            if (abstrObj != null)
            {
                if (m_isMain)
                {
                    abstrObj.delete();
                } else
                {
                	doDeleteReference(refObj);
                }
            }
        }
    }

    private void clearDeleted(HashMap<String, M> aList)
    {
    	clearPending(aList);
        aList.clear();
    }

    public int size()
    {
        return m_items.size() + m_added.size();
    }

    public void print()
    {
        System.out.println("List:    " + this.m_name);
        for (M o : m_items.values())
        {
            System.out.println("Item:    " + o.toString());
        }
        for (M o : m_added.values())
        {
            System.out.println("Added:   " + o.toString());
        }
        for (M o : m_deleted.values())
        {
            System.out.println("Deleted: " + o.toString());
        }
    }



    /**
     * Re-insert an object in items.
     *
     * @param anObject The object to undelete.
     */
    protected void reInsert(M anObject)
    {
        if (anObject != null)
        {
            m_items.put(anObject.getObjectId(), anObject);
            ((AbstractMsoObject) anObject).addDeleteListener(this);
            ((AbstractMsoObject) anObject).registerCreatedObject(this);
        }
    }

    /**
     * Undelete all deleted objects
     */
    private void undeleteAll()
    {

    	clearPending(m_deleted);

    	for (M anObject : m_deleted.values())
        {
    		reInsert(anObject);
        }

        if (m_owner != null && m_deleted.size() > 0)
        {
            ((AbstractMsoObject) m_owner).registerAddedReference(this);
        }

        m_deleted.clear();
    }


    /**
     * Delete all objects
     * <p/>
     * todo Can be optimized, but has probably very little effect, as the list normally will be quite short.
     */
    public void deleteAll()
    {
        M refObj = getItem();
        while (refObj != null)
        {
            remove(refObj);
            refObj = getItem();
        }
    }

    public boolean rollback()
    {
        boolean retVal = m_added.size() > 0 || m_deleted.size() > 0;
        clearList(m_added, false);
        undeleteAll();
        if (m_isMain)
        {
            for (M object : m_items.values())
            {
                ((AbstractMsoObject) object).rollback();
            }
        }
        return retVal;
    }

    /**
     * Move from m_added to m_items without changing any listeners etc
     */
    private void commitAddedLocal()
    {
        m_items.putAll(m_added);
        clearPending(m_added);
        m_added.clear();
    }

    private void clearPending(HashMap<String, M> aList) {
    	// only clear if client update is resumed (active)
    	if(!MsoModelImpl.getInstance().isUpdateSuspended()) {
	        for(M it : aList.values()) {
	        	m_pending.remove(it.getObjectId());
	        }
    	}
    }

    boolean postProcessCommit()
    {
        boolean retVal = m_added.size() > 0;
        clearDeleted(m_deleted);
        commitAddedLocal();
        if (m_isMain)
        {
            for (M object : m_items.values())
            {
                ((AbstractMsoObject) object).postProcessCommit();
            }
        }
        return retVal;
    }


    public void resumeClientUpdate(boolean all)
    {
        if (m_isMain)
        {
            for (M object : getItems())
            {
                object.resumeClientUpdate(all);
            }
            for (M object : m_pending.values())
            {
                object.resumeClientUpdate(all);
            }
            m_pending.clear();
        }
    }

    public int getCardinality()
    {
        return m_cardinality;
    }

    public Object validate() {
    	if(m_cardinality>0) {
    		return (size()<m_cardinality);
    	}
    	for(IMsoObjectIf it : getItems()) {
    		Object retVal = it.validate();
    		if(!isTrue(retVal)) return retVal;
    	}
    	return true;
    }

    private boolean isTrue(Object value) {
    	if(value instanceof Boolean)
    		return (Boolean)value;
    	return false;
    }

    public Set<M> selectItems(Selector<M> aSelector)
    {
        return selectItemsInCollection(aSelector, getItems());
    }

    public List<M> selectItems(Selector<M> aSelector, Comparator<M> aComparator)
    {
        return selectItemsInCollection(aSelector, aComparator, getItems());
    }

    public M selectSingleItem(Selector<M> aSelector)
    {
        return selectSingleItem(aSelector, getItems());
    }

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

    protected IMsoObjectIf.IObjectIdIf makeUniqueId()
    {
        IMsoObjectIf.IObjectIdIf retVal;
        do
        {
            retVal = MsoModelImpl.getInstance().getModelDriver().makeObjectId();
        }
        while (m_items.get(retVal.getId()) != null || m_added.get(retVal.getId()) != null || m_deleted.get(retVal.getId()) != null);
        return retVal;
    }

    public Collection<CommittableImpl.CommitReference> getCommittableRelations()
    {
        Vector<CommittableImpl.CommitReference> retVal = new Vector<CommittableImpl.CommitReference>();
        for (M item : m_added.values())
        {
            retVal.add(new CommittableImpl.CommitReference(m_name, m_owner, item, CommitManager.CommitType.COMMIT_CREATED));
        }
        for (M item : m_deleted.values())
        {
            if (!item.hasBeenDeleted())
            {
                retVal.add(new CommittableImpl.CommitReference(m_name, m_owner, item, CommitManager.CommitType.COMMIT_DELETED));
            }
        }
        return retVal;
    }

    protected int makeSerialNumber()
    {
        int max = 0;
        for (M item : getItems())
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
        for (M item : getItems())
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
        if (m_items != null ? !m_items.equals(list.m_items) : list.m_items != null)
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

    public IMsoListIf<M> getClone()
    {
        MsoListImpl<M> retVal = new MsoListImpl<M>(getItemClass(), getOwner(), getName(), isMain(), getCardinality(), size());
        for (M item : m_items.values())
        {
            retVal.m_items.put(item.getObjectId(), item);
        }
        for (M item : m_added.values())
        {
            retVal.m_added.put(item.getObjectId(), item);
        }
        for (M item : m_deleted.values())
        {
            retVal.m_deleted.put(item.getObjectId(), item);
        }
        for (M item : m_pending.values())
        {
            retVal.m_pending.put(item.getObjectId(), item);
        }
        return retVal;
    }

    /**
     * Get the item class
     */
    public Class<M> getItemClass() {
    	return m_itemClass;
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
	    	for(IMsoObjectIf it: m_items.values()) {
	    		if(it instanceof ISerialNumberedIf) {
	    			if(((ISerialNumberedIf)it).getNumber()==s0.getNumber())
	    				return true;
	    		}
	    	}
    	}
    	return false;
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

	        MsoModelImpl.getInstance().setLocalUpdateMode();
	        int nextNumber = -1;
	        for (M item : candidates)
	        {
	            if (item instanceof ISerialNumberedIf)
	            {
	                ISerialNumberedIf numberedItem = (ISerialNumberedIf) item;
	                if (numberedItem.getNumberState() != ModificationState.STATE_SERVER)
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
	        MsoModelImpl.getInstance().restoreUpdateMode();
        }

        // finished
        return renumbered;

    }

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
}