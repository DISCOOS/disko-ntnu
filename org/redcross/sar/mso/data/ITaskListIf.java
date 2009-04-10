package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

import java.util.Calendar;

public interface ITaskListIf extends IMsoListIf<ITaskIf>
{
    public ITaskIf createTask(Calendar aCalendar);

    public ITaskIf createTask(IMsoObjectIf.IObjectIdIf anObjectId);
}