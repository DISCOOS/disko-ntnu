package org.redcross.sar.mso.data;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.event.IMsoCoChangeListenerIf;
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
 * @see IMsoCoChangeListenerIf for more information about 
 * derived (co) update events.
 */
public abstract class AbstractCoList<M extends IMsoObjectIf> implements IMsoCoListIf<M>, IMsoCoChangeListenerIf
{
	/**
	 * The co-list items 
	 */
    protected final HashMap<String, M> m_items;

    /**
     * The co-list change count 
     */
    protected int m_changeCount;

    /**
	 * The MSO model owning the objects.
	 */
	protected final IMsoModelIf m_model;

	/**
	 * Constructor
	 */
    public AbstractCoList(IMsoModelIf aMsoModel)
    {
        this(aMsoModel,50);
    }

    public AbstractCoList(IMsoModelIf aMsoModel, int aSize)
    {
        m_items = new LinkedHashMap<String, M>(aSize);
        m_model = aMsoModel;
        m_model.getEventManager().addCoChangeListener(this);        
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


    public void handleMsoCoChangeEvent(MsoEvent.CoChange e)
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
