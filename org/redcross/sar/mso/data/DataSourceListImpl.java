package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public class DataSourceListImpl extends MsoListImpl<IDataSourceIf> implements IDataSourceListIf
{

    public DataSourceListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, 0, aSize);
    }

    public IDataSourceIf createDataSource()
    {
        checkCreateOp();
        return createdUniqueItem(new DataSourceImpl(makeUniqueId()));
    }

    public IDataSourceIf createDataSource(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IDataSourceIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new DataSourceImpl(anObjectId));
    }
}