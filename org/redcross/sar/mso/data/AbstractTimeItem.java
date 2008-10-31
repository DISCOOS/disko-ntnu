package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoModelIf;

import java.util.Calendar;

public abstract class AbstractTimeItem extends AbstractMsoObject implements ITimeItemIf
{
    protected final AttributeImpl.MsoCalendar m_timeStamp = new AttributeImpl.MsoCalendar(this, "TimeStamp");
    protected final AttributeImpl.MsoBoolean m_visible = new AttributeImpl.MsoBoolean(this, "Visible");

    public AbstractTimeItem(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        this(theMsoModel, anObjectId, null, true);
    }

    public AbstractTimeItem(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, Calendar aCalendar)
    {
        this(theMsoModel, anObjectId, aCalendar, true);
    }

    public AbstractTimeItem(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, Calendar aCalendar, boolean aVisible)
    {
        super(theMsoModel, anObjectId);
        m_timeStamp.setValue(aCalendar);
        setVisible(aVisible);
    }

    protected void defineAttributes()
    {
        addAttribute(m_timeStamp);
        addAttribute(m_visible);
    }

    protected void defineLists()
    {
    }

    protected void defineReferences()
    {
    }

    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return true;
    }

    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return true;
    }

    public void setTimeStamp(Calendar aDTG)
    {
        m_timeStamp.setValue(aDTG);
    }

    public Calendar getTimeStamp()
    {
        return m_timeStamp.getCalendar();
    }

    public IMsoModelIf.ModificationState getTimeStampState()
    {
        return m_timeStamp.getState();
    }

    public IAttributeIf.IMsoCalendarIf getTimeStampAttribute()
    {
        return m_timeStamp;
    }

    /**
     * Compare time with another ITimeItemIf object.
     * The following rule applies: A <code>null</code>-reference element is considered to be greater (later) than all other objects,
     * two <code>null</code>-reference elements are equal.
     *
     * @param aTimeObject The object to compare.
     * @return As {@link Comparable#compareTo(Object)}
     */
    public int compareTo(IData data)
    {
    	if(data instanceof ITimeItemIf)
        {
    		ITimeItemIf aTimeObject = (ITimeItemIf)data;
            if (getTimeStamp() != null && aTimeObject.getTimeStamp() != null)
            {
                return getTimeStamp().compareTo(aTimeObject.getTimeStamp());
            } else if (getTimeStamp() == null && aTimeObject.getTimeStamp() == null)
            {
                return 0;
            } else if (aTimeObject.getTimeStamp() == null)
            {
                return -1;
            } else
            {
                return 1;
            }
        }
        return 1;
    }

    public void setVisible(boolean aVisible)
    {
        m_visible.setValue(aVisible);
    }

    public boolean isVisible()
    {
        return m_visible.booleanValue();
    }

//    public String toString()
//    {
//        return "Timeitem " + DTG.CalToDTG(getTimeStamp()) ;
//    }

    public IMsoModelIf.ModificationState getVisibleState()
    {
        return m_visible.getState();
    }

    public IAttributeIf.IMsoBooleanIf getVisibleAttribute()
    {
        return m_visible;
    }
}