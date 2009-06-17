package org.redcross.sar.mso.data;


import java.util.Collection;

import org.redcross.sar.data.IData;

/**
 *
 */
public interface ISystemIf extends IMsoObjectIf
{
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/
	
    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public IDataSourceListIf getDataSourceList();

    public IData.DataOrigin getDataSourceListState(IDataSourceIf anIDataSourceIf);

    public Collection<IDataSourceIf> getDataSourceListItems();

    /*-------------------------------------------------------------------------------------------
    * Other specified functions
    *-------------------------------------------------------------------------------------------*/

}
