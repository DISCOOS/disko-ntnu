package org.redcross.sar.mso.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimeLineImpl extends AbstractCoList<ITimeItemIf> implements ITimeLineIf
{
    final List<ITimeItemIf> m_timeItems = new ArrayList<ITimeItemIf>();

    // todo Sorting can be optimized by using a sorting algorithm that takes into account that only one object has been added/changed

    public TimeLineImpl()
    {
        super();
    }

    public List<ITimeItemIf> getTimeItems()
    {
        return m_timeItems;
    }

    public boolean hasInterestIn(Object anObject)
    {
        return anObject instanceof AbstractTimeItem;
    }

    private void arrange(ITimeItemIf anItem)
    {
        arrange(anItem, m_timeItems.indexOf(anItem));
    }

    /**
     * Uses a simple pass from bubblesort in order to rearrange list, when only one item is misplaced.
     * @param anItem The misplaced item.
     * @param anIndex Index of the misplaced item.
     */
    private void arrange(ITimeItemIf anItem, int anIndex)
    {
        int size = m_timeItems.size();
        while (anIndex < size - 1 && anItem.compareTo(m_timeItems.get(anIndex + 1)) > 0)
        {
            Collections.swap(m_timeItems, anIndex, anIndex + 1);
            anIndex++;
        }
        while (anIndex > 0 && anItem.compareTo(m_timeItems.get(anIndex - 1)) < 0)
        {
            Collections.swap(m_timeItems, anIndex - 1, anIndex);
            anIndex--;
        }
    }

    public void handleItemCreate(Object anObject)
    {
        ITimeItemIf item = (ITimeItemIf) anObject;
        m_items.put(item.getObjectId(), item);
        m_timeItems.add(item);
        arrange(item, m_timeItems.size() - 1);
    }

    public void handleItemDelete(Object anObject)
    {
        ITimeItemIf item = (ITimeItemIf) anObject;
        m_items.remove(item.getObjectId());
        m_timeItems.remove(item);
    }

    public void handleItemModify(Object anObject)
    {
        arrange((ITimeItemIf) anObject);
    }

    public void print()
    {
        for (ITimeItemIf to : m_timeItems)
        {
            System.out.println("Time item: " + to.toString());
        }
    }

}