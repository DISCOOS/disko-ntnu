package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.util.except.IllegalOperationException;
/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 03.sep.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 *
 */
public interface IEnumPriorityHolder<E extends Enum<E>> extends IMsoObjectIf
{
    public void setPriority(E aPriority);

    public void setPriority(String aPriority) throws IllegalOperationException;

    public E getPriority();

    public IData.DataOrigin getPriorityState();

    public IMsoAttributeIf.IMsoEnumIf<E> getPriorityAttribute();

    public String getPriorityText();

    public int comparePriorityTo(IEnumPriorityHolder<E> anObject);
}

