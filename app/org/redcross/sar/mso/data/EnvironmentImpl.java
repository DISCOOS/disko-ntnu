package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;

import java.util.Calendar;

public class EnvironmentImpl extends AbstractTimeItem implements IEnvironmentIf
{
    private final AttributeImpl.MsoString m_someText = new AttributeImpl.MsoString(this, "someText");

    public EnvironmentImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    public EnvironmentImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId,Calendar aCalendar, String aText)
    {
        super(theMsoModel, anObjectId,aCalendar);
        setText(aText);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_someText);
    }

    @Override
    protected void defineLists()
    {
        super.defineLists();
    }

    @Override
    protected void defineObjects()
    {
        super.defineObjects();
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
//        return super.toString() + " EnvironmentImpl: " + m_someText.getString();
//    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_ENVIRONMENT;
    }


}