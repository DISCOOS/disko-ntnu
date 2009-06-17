package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;

import java.util.Calendar;

public class ForecastImpl extends AbstractTimeItem implements IForecastIf
{
    private final AttributeImpl.MsoString m_someText = new AttributeImpl.MsoString(this, "someText");

    public ForecastImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    public ForecastImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, Calendar aCalendar, String aText)
    {
        super(theMsoModel, anObjectId, aCalendar);
        setText(aText);
    }

    protected void defineAttributes()
    {
        addAttribute(m_someText);
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
    }

    public void setText(String aText)
    {
        m_someText.setValue(aText);
    }

    public String getText()
    {
        return m_someText.getString();
    }

//    public String toString()
//    {
//        return super.toString() + " Forecast: " + m_someText.getString();
//    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_FORECAST;
    }


}