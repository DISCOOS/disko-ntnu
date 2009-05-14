package org.redcross.sar.mso.data;

import java.util.Calendar;

public interface IEventLogIf extends IMsoListIf<IEventIf>
{
    public IEventIf createEvent(Calendar aCalendar);

    public IEventIf createEvent(IMsoObjectIf.IObjectIdIf anObjectId);
}