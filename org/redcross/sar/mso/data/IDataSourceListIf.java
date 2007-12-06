package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

/**
 *
 */
public interface IDataSourceListIf extends IMsoListIf<IDataSourceIf>
{
    public IDataSourceIf createDataSource();

    public IDataSourceIf createDataSource(IMsoObjectIf.IObjectIdIf anObjectId);
}
