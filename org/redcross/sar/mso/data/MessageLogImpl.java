package org.redcross.sar.mso.data;

import java.util.Calendar;

public class MessageLogImpl extends MsoListImpl<IMessageIf> implements IMessageLogIf
{

    public MessageLogImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public MessageLogImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, 0, aSize);
    }

    public IMessageIf createMessage()
    {
        checkCreateOp();
        return createdUniqueItem(new MessageImpl(makeUniqueId(), makeSerialNumber()));
    }

    public IMessageIf createMessage(Calendar aCalendar)
    {
        checkCreateOp();
        return createdUniqueItem(new MessageImpl(makeUniqueId(), makeSerialNumber(), aCalendar));
    }

    public IMessageIf createMessage(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IMessageIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new MessageImpl(anObjectId, -1));
    }
}