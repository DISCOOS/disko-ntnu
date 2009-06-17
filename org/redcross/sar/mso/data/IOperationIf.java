package org.redcross.sar.mso.data;


import java.util.Collection;

import org.redcross.sar.data.IData;

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

    public IData.DataOrigin getOpNumberState();

    public IMsoAttributeIf.IMsoStringIf getOpNumberAttribute();

    public void setOpNumberPrefix(String aNumberPrefix);

    public String getOpNumberPrefix();

    public IData.DataOrigin getOpNumberPrefixState();

    public IMsoAttributeIf.IMsoStringIf getOpNumberPrefixAttribute();

    /*-------------------------------------------------------------------------------------------
     * Methods for references
     *-------------------------------------------------------------------------------------------*/
    
    public void setSystem(ISystemIf aSystem);

    public ISystemIf getSystem();

    public IData.DataOrigin getSystemState();

    public IMsoRelationIf<ISystemIf> getSystemAttribute();
    
    
    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addCmdPost(ICmdPostIf anICmdPostIf);

    public ICmdPostListIf getCmdPostList();

    public IData.DataOrigin getCmdPostListState(ICmdPostIf anICmdPostIf);

    public Collection<ICmdPostIf> getCmdPostListItems();

    /*-------------------------------------------------------------------------------------------
    * Other specified functions
    *-------------------------------------------------------------------------------------------*/

    public String getOperationNumber();
}
