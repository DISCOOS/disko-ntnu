package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

import java.util.Collection;

/**
 *
 */
public interface IOperationIf extends IMsoObjectIf
{
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setOpNumber(String aNumber);

    public String getNumber();

    public IMsoModelIf.ModificationState getOpNumberState();

    public IAttributeIf.IMsoStringIf getOpNumberAttribute();

    public void setOpNumberPrefix(String aNumberPrefix);

    public String getOpNumberPrefix();

    public IMsoModelIf.ModificationState getOpNumberPrefixState();

    public IAttributeIf.IMsoStringIf getOpNumberPrefixAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addCmdPostList(ICmdPostIf anICmdPostIf);

    public ICmdPostListIf getCmdPostList();

    public IMsoModelIf.ModificationState getCmdPostListState(ICmdPostIf anICmdPostIf);

    public Collection<ICmdPostIf> getCmdPostListItems();

    /*-------------------------------------------------------------------------------------------
    * Other specified functions
    *-------------------------------------------------------------------------------------------*/

    public String getOperationNumber();
}
