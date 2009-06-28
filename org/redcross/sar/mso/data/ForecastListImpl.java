package org.redcross.sar.mso.data;

import java.util.Calendar;

public class ForecastListImpl extends MsoListImpl<IForecastIf> implements IForecastListIf
{
    public ForecastListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IForecastIf.class, anOwner, theName, isMain);
    }

    public ForecastListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IForecastIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IForecastIf createForecast(Calendar aCalendar, String aText)
    {
        checkCreateOp();
        return createdUniqueItem(new ForecastImpl(getOwner().getModel(), createUniqueId(), aCalendar, aText));
    }

    public IForecastIf createForecast(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IForecastIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new ForecastImpl(getOwner().getModel(), anObjectId));
    }
}