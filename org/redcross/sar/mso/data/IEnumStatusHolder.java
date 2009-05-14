package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.IllegalOperationException;
/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 18.apr.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 * Interface for classes holding a status Enum attribute
 */
public interface IEnumStatusHolder<E extends Enum<E>> extends IMsoObjectIf
{

    public void setStatus(E aStatus) throws IllegalOperationException;

    public void setStatus(String aStatus) throws IllegalOperationException;

    public E getStatus();

    public IMsoModelIf.ModificationState getStatusState();

    public IMsoAttributeIf.IMsoEnumIf<E> getStatusAttribute();

    public String getStatusText();
}
