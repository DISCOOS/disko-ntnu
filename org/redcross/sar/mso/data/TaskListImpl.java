package org.redcross.sar.mso.data;

import java.util.Calendar;

public class TaskListImpl extends MsoListImpl<ITaskIf> implements ITaskListIf
{

    public TaskListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(ITaskIf.class, anOwner, theName, isMain);
    }

    public TaskListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(ITaskIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public ITaskIf createTask(Calendar aCalendar)
    {
        checkCreateOp();
        return createdUniqueItem(new TaskImpl(getOwner().getModel(), makeUniqueId(), makeSerialNumber(), aCalendar));
    }

    public ITaskIf createTask(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ITaskIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new TaskImpl(getOwner().getModel(), anObjectId));
    }
}