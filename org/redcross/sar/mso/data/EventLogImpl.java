package org.redcross.sar.mso.data;

import java.util.Calendar;

public class EventLogImpl extends MsoListImpl<IEventIf> implements IEventLogIf
{

    public EventLogImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IEventIf.class, anOwner, theName, isMain);
    }

    public EventLogImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IEventIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IEventIf createEvent(Calendar aCalendar)
    {
        checkCreateOp();
        return createdUniqueItem(new EventImpl(getOwner().getModel(), makeUniqueId(), makeSerialNumber(), aCalendar));
    }

    public IEventIf createEvent(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IEventIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new EventImpl(getOwner().getModel(), anObjectId, -1));
    }
}