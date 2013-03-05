package org.redcross.sar.mso.data;

public class DataSourceListImpl extends MsoListImpl<IDataSourceIf> implements IDataSourceListIf
{

    public DataSourceListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IDataSourceIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IDataSourceIf createDataSource()
    {
        checkCreateOp();
        return createdUniqueItem(new DataSourceImpl(getOwner().getModel(), createUniqueId()));
    }

    public IDataSourceIf createDataSource(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IDataSourceIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new DataSourceImpl(getOwner().getModel(), anObjectId));
    }
}