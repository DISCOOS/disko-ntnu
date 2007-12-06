package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;

import java.util.Calendar;

public class EnvironmentImpl extends AbstractTimeItem implements IEnvironmentIf
{
    private final AttributeImpl.MsoString m_someText = new AttributeImpl.MsoString(this, "someText");

    public EnvironmentImpl(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(anObjectId);
    }

    public EnvironmentImpl(IMsoObjectIf.IObjectIdIf anObjectId,Calendar aCalendar, String aText)
    {
        super(anObjectId,aCalendar);
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
    protected void defineReferences()
    {
        super.defineReferences();
    }

    @Override
    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return super.addObjectReference(anObject,aReferenceName);
    }

    @Override
    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return super.removeObjectReference(anObject,aReferenceName);
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

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_ENVIRONMENT;
    }


}