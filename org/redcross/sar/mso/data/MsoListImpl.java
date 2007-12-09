package org.redcross.sar.mso.data;

import org.redcross.sar.mso.CommitManager;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.committer.CommittableImpl;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.mso.Selector;

import java.util.*;

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
    protected final boolean m_isMain;

    public MsoListImpl(IMsoObjectIf anOwner)
    {
        this(anOwner, "");
    }

    public MsoListImpl(IMsoObjectIf anOwner, String theName)
    {
        this(anOwner, theName, false);
    }

    public MsoListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        this(anOwner, theName, isMain, 50);
    }

    public MsoListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        m_owner = anOwner;
        m_name = theName;
        m_isMain = isMain;
        m_items = new LinkedHashMap<String, M>(aSize);
        m_added = new LinkedHashMap<String, M>();
        m_deleted = new LinkedHashMap<String, M>();
    }

    protected void setName(String aName)
    {
        m_name = aName.toLowerCase();
    }

    public String getName()
    {
        return m_name;
    }

    public void checkCreateOp()
    {
        verifyMainOperation("Cannot create object in a non-main list");
    }

    public void verifyMainOperation(String aMessage)
    {
        if (!m_isMain)
        {
            //throw new MsoRuntimeException(aMessage);
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

    public void add(M anObject)
    {
        if (anObject == null)
        {
            //throw new MsoRuntimeException(getName() + ": Cannot add null object");
        }
        String objectId = anObject.getObjectId();
        MsoModelImpl.UpdateMode updateMode = MsoModelImpl.getInstance().getUpdateMode();
        if ((m_items.containsKey(objectId) || m_added.containsKey(objectId)) && updateMode == IMsoModelIf.UpdateMode.LOCAL_UPDATE_MODE)
        {
            throw new DuplicateIdException("ObjectId already added to list");
        }
        if (!((AbstractMsoObject) anObject).isSetup())
        {
            //throw new MsoRuntimeException(getName() + ": Cannot add uninitialized object");
        }


        if (updateMode == MsoModelImpl.UpdateMode.LOCAL_UPDATE_MODE)
        {
            m_added.put(anObject.getObjectId(), anObject);
        } else
        {
            m_items.put(anObject.getObjectId(), anObject);
        }
        ((AbstractMsoObject) anObject).registerAddedReference();
        ((AbstractMsoObject) anObject).addDeleteListener(this);
        if (m_owner != null)
        {
            ((AbstractMsoObject) m_owner).registerAddedReference();
        }
        //System.out.println("Added reference from " + m_owner + " to " + anObject);
    }

    public void clear()
    {
        clearList(m_items, true);
        clearList(m_added, false);
        clearDeleted(m_deleted);
    }

    private void clearList(HashMap<String, M> aList, boolean updateServer)      // todo Denne er feil, slettete elementer blir ikke dereferert.
    {
        // Copy the list and clear the original before any events are sent around, since the events are checking the original list
        if (aList.size() == 0)
        {
            return;
        }

        if (m_owner != null)
        {
            ((AbstractMsoObject) m_owner).registerRemovedReference(updateServer);
        }

        Collection<String> tmpList = aList.keySet();
        while (tmpList.size() > 0)
        {
            String key = tmpList.iterator().next();
            AbstractMsoObject abstrObj = (AbstractMsoObject) aList.remove(key);
            if (abstrObj != null)
            {
                abstrObj.removeDeleteListener(this);
                if (m_isMain)
                {
                    abstrObj.doDelete();
                } else
                {
                    abstrObj.registerRemovedReference(updateServer);
                }
            }
        }
    }

    private void clearDeleted(HashMap<String, M> aList)
    {
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
     * Can always deleteObject.
     *
     * @param anObject See implemented method {@link IMsoObjectHolderIf#canDeleteReference(IMsoObjectIf)}.
     */
    public void canDeleteReference(M anObject)
    {
    }

    public boolean doDeleteReference(M anObject)
    {
        boolean localUpdateMode = MsoModelImpl.getInstance().getUpdateMode() == MsoModelImpl.UpdateMode.LOCAL_UPDATE_MODE;
        boolean updateServer = localUpdateMode;

        String key = anObject.getObjectId();

        M refObj;
        refObj = m_items.remove(key);
        if (refObj != null)
        {
            if (localUpdateMode)
            {
                m_deleted.put(key, anObject);
            }
        } else
        {
            refObj = m_added.remove(key);
            updateServer = false;
        }
        if (refObj != null)
        {
            String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
            System.out.println("Delete reference from " + s + " to " + anObject);
            ((AbstractMsoObject) refObj).removeDeleteListener(this);
            if (m_owner != null)
            {
                ((AbstractMsoObject) m_owner).registerRemovedReference(updateServer);
            }
            return true;
        }
        return false;
    }

    public boolean removeReference(M anObject)
    {
        if (isMain())
        {
            return anObject.deleteObject();
        } else
        {
            return doDeleteReference(anObject);
        }
    }

    /**
     * Re-insert an object in items.
     *
     * @param refObj The object to undelete.
     */
    protected void reInsert(M refObj)
    {
        if (refObj != null)
        {
            m_items.put(refObj.getObjectId(), refObj);
            ((AbstractMsoObject) refObj).addDeleteListener(this);
            ((AbstractMsoObject) refObj).registerCreatedObject();
        }
    }

    /**
     * Undelete all deleted objects
     */
    private void undeleteAll()
    {
        for (M refObj : m_deleted.values())
        {
            reInsert(refObj);
        }

        if (m_owner != null && m_deleted.size() > 0)
        {
            ((AbstractMsoObject) m_owner).registerAddedReference();
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
            removeReference(refObj);
            refObj = getItem();
        }
    }

    public IMsoModelIf.ModificationState getState(M aReference)
    {
        if (m_items.containsKey(aReference.getObjectId()))
        {
            return IMsoModelIf.ModificationState.STATE_SERVER;
        }
        if (m_added.containsKey(aReference.getObjectId()))
        {
            return IMsoModelIf.ModificationState.STATE_LOCAL;
        }
        return IMsoModelIf.ModificationState.STATE_UNDEFINED;
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
        m_added.clear();
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


    public void resumeClientUpdates()
    {
        if (m_isMain)
        {
            for (M object : getItems())
            {
                object.resumeClientUpdates();
            }
        }
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
    	MsoModelImpl.getInstance().suspendClientUpdate();
    	((AbstractMsoObject) anObject).setupReferences();
        ((AbstractMsoObject) anObject).setOwningMainList(this);
        add(anObject);
    	MsoModelImpl.getInstance().resumeClientUpdate();
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
        int retVal = 0;
        for (M item : getItems())
        {
            try
            {
                ISerialNumberedIf serialItem = (ISerialNumberedIf) item;
                if (serialItem.getNumber() > retVal)
                {
                    retVal = serialItem.getNumber();
                }
            }
            catch (ClassCastException e)
            {
                //throw new MsoRuntimeException("Object " + item + " is not implementing ISerialNumberedIf");
            }
        }
        return retVal + 1;
    }

    void rearrangeSerialNumber()
    {
        // todo make code
        for (M item : getItems())
        {
            try
            {
                ISerialNumberedIf serialItem = (ISerialNumberedIf) item;
            }
            catch (ClassCastException e)
            {
                //throw new MsoRuntimeException("Object " + item + " is not implementing ISerialNumberedIf");
            }
        }
    }

    public boolean contains(M anObject)
    {
        return (m_items.containsKey(anObject.getObjectId()) || m_added.containsKey(anObject.getObjectId()));
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
            list = (MsoListImpl<M>) o;
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
        MsoListImpl<M> retVal = new MsoListImpl<M>(getOwner(), getName(), isMain(), size());
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
        return retVal;
    }

    public void renumberItems(M anItem)
    {
        List<M> candidates = selectCandidates(getRenumberSelector(anItem));
        if (candidates.size() != 0)
        {
            renumberCandidates(candidates);
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
    private void renumberCandidates(List<M> candidates)
    {
        MsoModelImpl.getInstance().setLocalUpdateMode();
        int nextNumber = -1;
        for (M item : candidates)
        {
            if (item instanceof ISerialNumberedIf)
            {
                ISerialNumberedIf numberedItem = (ISerialNumberedIf) item;
                if (numberedItem.getNumberState() != IMsoModelIf.ModificationState.STATE_SERVER)
                {
                    if (nextNumber < 0)
                    {
                        nextNumber = numberedItem.getNumber() + 1;
                    }
                    int tmpNumber = numberedItem.getNumber();
                    numberedItem.setNumber(nextNumber);
                    nextNumber = tmpNumber;
                }
            }
        }
        MsoModelImpl.getInstance().restoreUpdateMode();
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