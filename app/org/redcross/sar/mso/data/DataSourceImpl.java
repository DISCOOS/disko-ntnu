package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;

public class DataSourceImpl extends AbstractMsoObject implements IDataSourceIf
{
    public DataSourceImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    protected void defineAttributes()
    {
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_DATASOURCE;
    }


}