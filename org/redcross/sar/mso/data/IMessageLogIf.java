package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

import java.util.Calendar;

public interface IMessageLogIf extends IMsoListIf<IMessageIf>
{
    public IMessageIf createMessage();

    public IMessageIf createMessage(Calendar aCalendar);

    public IMessageIf createMessage(IMsoObjectIf.IObjectIdIf anObjectId);

}