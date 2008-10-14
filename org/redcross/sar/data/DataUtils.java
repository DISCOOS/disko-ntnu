package org.redcross.sar.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DataUtils {

    public static <T> Set<T> selectItemsInCollection(Selector<? super T> aSelector, Collection<T> theItems)
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

    public static <T> List<T> selectItemsInCollection(Selector<? super T> aSelector, Comparator<? super T> aComparator, Collection<T> theItems)
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

    public static <T> T selectSingleItem(Selector<? super T> aSelector, Collection<T> theItems)
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

    /**
     * Insert an item into a list.
     *
     * @param aList       The list to insert into
     * @param anItem      The item to add
     * @param aComparator A comparator. If null, the item is appended to the list, if not null, used as a comparator to sort the list.
     */
    private static <T> void addSorted(List<T> aList, T anItem, Comparator<? super T> aComparator)
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

}
