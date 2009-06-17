package org.redcross.sar.mso.data;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.redcross.sar.Application;
import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.event.IMsoCoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;

/**
 * This abstract class is a template that enables implementation
 * of parallel book holding of items (a derived or "co-list") by 
 * listening to create, delete and modify events using the
 * interface IMsoCoUpdateListenerIf. 
 * 
 * @author vinjar, kenneth
 *
 * @param <M> - the item type
 * 
 * @see IMsoCoUpdateListenerIf for more information about 
 * derived (co) update events.
 */
public abstract class AbstractCoList<M extends IMsoObjectIf> implements IMsoCoListIf<M>, IMsoCoUpdateListenerIf
{
    protected final HashMap<String, M> m_items;
    
    protected int m_changeCount;

    public AbstractCoList()
    {
        this(50);
    }

    public AbstractCoList(int aSize)
    {
        m_items = new LinkedHashMap<String, M>(aSize);
        Application.getInstance().getMsoModel().getEventManager().addCoUpdateListener(this);
    }

    public Collection<M> getItems()
    {
        return m_items.values();
    }

    public int size()
    {
        return m_items.size();
    }

    public List<M> selectItems(Selector<M> aSelector, Comparator<M> aComparator)
    {
        return MsoListImpl.selectItemsInCollection(aSelector,aComparator,getItems());
    }

    public M selectSingleItem(Selector<M> aSelector)
    {
        return MsoListImpl.selectSingleItem(aSelector,getItems());
    }

    static final int mask = MsoEvent.MsoEventType.CREATED_OBJECT_EVENT.maskValue()
                | MsoEvent.MsoEventType.DELETED_OBJECT_EVENT.maskValue()
                | MsoEvent.MsoEventType.MODIFIED_DATA_EVENT.maskValue();


    public void handleMsoCoUpdateEvent(MsoEvent.CoChange e)
    {
        if (!hasInterestIn(e.getSource()))
        {
            return;
        }

        if ((e.getMask() & mask) != 0)
        {
            if (e.isCreateObjectEvent())
            {
            	incrementChangeCount();
                handleItemCreate(e.getSource());
            } else if (e.isDeleteObjectEvent())
            {
            	incrementChangeCount();
                handleItemDelete(e.getSource());
            } else if (e.isModifyObjectEvent())
            {
            	incrementChangeCount();
                handleItemModify(e.getSource());
            }
        }
    }

    public int getChangeCount()
    {
        return m_changeCount;
    }
    
    protected void incrementChangeCount() {
    	m_changeCount++;
    }    
    
    public abstract boolean hasInterestIn(Object anObject);

    public abstract void handleItemCreate(Object anObject);

    public abstract void handleItemDelete(Object anObject);

    public abstract void handleItemModify(Object anObject);

}
