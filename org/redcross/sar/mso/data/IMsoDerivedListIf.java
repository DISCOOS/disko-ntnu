package org.redcross.sar.mso.data;

import org.redcross.sar.util.mso.Selector;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public interface IMsoDerivedListIf<M extends IMsoObjectIf>
{
    /**
     * Get a java.util.Collection of the items in the list.
     *
     * @return All items (not marked for deletion in the list)
     */
    public Collection<M> getItems();

    /**
     * Get the number of items in the list.
     *
     * @return The number of items.
     */
    public int size();


    /**
     * Generate an List of selected items from the list.
     *
     * Selects
     * @param aSelector A {@link org.redcross.sar.util.mso.Selector} that is used for selecting items.
     * @param aComparator {@link java.util.Comparator} that is used for determining the ordering the items, if <code>null</code>, no ordering will be done.
     * @return The generated list
     */
    public List<M> selectItems(Selector<M> aSelector, Comparator<M> aComparator);

    /**
     * Find an item in the list.
     *
     * @param aSelector A {@link org.redcross.sar.util.mso.Selector} that is used for selecting items.
     * @return The found item, or null if not found;
     */
    public M selectSingleItem(Selector<M> aSelector);
    
    public int getChangeCount();
    
}
