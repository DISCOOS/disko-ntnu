package org.redcross.sar.mso.data;

import java.util.Calendar;

public class ForecastListImpl extends MsoListImpl<IForecastIf> implements IForecastListIf
{
    public ForecastListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public ForecastListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, aSize);
    }

    public IForecastIf createForecast(Calendar aCalendar, String aText)
    {
        checkCreateOp();
        return createdUniqueItem(new ForecastImpl(makeUniqueId(), aCalendar, aText));
    }

    public IForecastIf createForecast(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IForecastIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new ForecastImpl(anObjectId));
    }
}