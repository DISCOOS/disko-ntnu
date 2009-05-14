package org.redcross.sar.mso.data;

import java.util.Calendar;

public interface ITaskListIf extends IMsoListIf<ITaskIf>
{
    public ITaskIf createTask(Calendar aCalendar);

    public ITaskIf createTask(IMsoObjectIf.IObjectIdIf anObjectId);
}