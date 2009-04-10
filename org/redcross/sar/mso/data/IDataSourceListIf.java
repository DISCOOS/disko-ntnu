package org.redcross.sar.mso.data;

/**
 *
 */
public interface IDataSourceListIf extends IMsoListIf<IDataSourceIf>
{
    public IDataSourceIf createDataSource();

    public IDataSourceIf createDataSource(IMsoObjectIf.IObjectIdIf anObjectId);
}
