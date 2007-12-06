package org.redcross.sar.mso.data;

import java.util.Calendar;

public class EnvironmentListImpl extends MsoListImpl<IEnvironmentIf> implements IEnvironmentListIf
{

    public EnvironmentListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public EnvironmentListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, aSize);
    }

    public IEnvironmentIf createEnvironment(Calendar aCalendar, String aText)
    {
        checkCreateOp();
        return createdUniqueItem(new EnvironmentImpl(makeUniqueId(), aCalendar, aText));
    }


    public IEnvironmentIf createEnvironment(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IEnvironmentIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new EnvironmentImpl(anObjectId));
    }

}