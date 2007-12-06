package org.redcross.sar.mso.data;

import java.util.Calendar;

public interface IForecastListIf extends IMsoListIf<IForecastIf>
{
    public IForecastIf createForecast(Calendar aCalendar, String aText);

    public IForecastIf createForecast(IMsoObjectIf.IObjectIdIf anObjectId);
}