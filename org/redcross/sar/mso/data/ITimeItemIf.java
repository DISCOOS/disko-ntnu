package org.redcross.sar.mso.data;


import java.util.Calendar;

import org.redcross.sar.data.IData;

public interface ITimeItemIf extends IMsoObjectIf
{
    public Calendar getTimeStamp();

    public void setTimeStamp(Calendar aDTG);

    public IData.DataOrigin getTimeStampState();

    public IMsoAttributeIf.IMsoCalendarIf getTimeStampAttribute();

    public String toString();

    public void setVisible(boolean aVisible);

    public boolean isVisible();

    public IData.DataOrigin getVisibleState();

    public IMsoAttributeIf.IMsoBooleanIf getVisibleAttribute();
}