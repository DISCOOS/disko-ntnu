package org.redcross.sar.mso.data;

import java.util.Calendar;

public interface IEnvironmentListIf extends IMsoListIf<IEnvironmentIf>
{
    public IEnvironmentIf createEnvironment(Calendar aCalendar, String aText);

    public IEnvironmentIf createEnvironment(IMsoObjectIf.IObjectIdIf anObjectId);
}