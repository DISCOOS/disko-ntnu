package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

import java.util.Calendar;

public interface ITimeItemIf extends IMsoObjectIf
{
    public Calendar getTimeStamp();

    public void setTimeStamp(Calendar aDTG);

    public IMsoModelIf.ModificationState getTimeStampState();

    public IAttributeIf.IMsoCalendarIf getTimeStampAttribute();

    public String toString();

    public void setVisible(boolean aVisible);

    public boolean isVisible();

    public IMsoModelIf.ModificationState getVisibleState();

    public IAttributeIf.IMsoBooleanIf getVisibleAttribute();
}