package org.redcross.sar.mso.data;

import java.util.Calendar;

public class EnvironmentListImpl extends MsoListImpl<IEnvironmentIf> implements IEnvironmentListIf
{

    public EnvironmentListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IEnvironmentIf.class, anOwner, theName, isMain);
    }

    public EnvironmentListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IEnvironmentIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IEnvironmentIf createEnvironment(Calendar aCalendar, String aText)
    {
        checkCreateOp();
        return createdUniqueItem(new EnvironmentImpl(getOwner().getModel(), createUniqueId(), aCalendar, aText));
    }


    public IEnvironmentIf createEnvironment(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IEnvironmentIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new EnvironmentImpl(getOwner().getModel(), anObjectId));
    }

}