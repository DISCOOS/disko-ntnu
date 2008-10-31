package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoModelIf.ModificationState;
import org.redcross.sar.mso.data.IAttributeIf.IMsoEnumIf;
import org.redcross.sar.mso.data.IAttributeIf.IMsoIntegerIf;
import org.redcross.sar.mso.data.IAttributeIf.IMsoStringIf;
import org.redcross.sar.util.except.MsoCastException;

import java.util.Calendar;
import java.util.Collection;

@SuppressWarnings("unchecked")
public class EventImpl extends AbstractTimeItem implements IEventIf
{

	private final AttributeImpl.MsoEnum<EventStatus> m_status = new AttributeImpl.MsoEnum<EventStatus>(this, "Status", 1, EventStatus.UNCONFIRMED);

    private final AttributeImpl.MsoInteger m_number = new AttributeImpl.MsoInteger(this, "Number",true);
    private final AttributeImpl.MsoString m_name = new AttributeImpl.MsoString(this, "Name");
    private final AttributeImpl.MsoString m_description = new AttributeImpl.MsoString(this, "Description");
    private final AttributeImpl.MsoInteger m_level = new AttributeImpl.MsoInteger(this, "Level");
    private final AttributeImpl.MsoInteger m_priority = new AttributeImpl.MsoInteger(this, "Priority");

    private final TaskListImpl m_eventTasks = new TaskListImpl(this, "EventTasks", false);

    /*-------------------------------------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------------------------------------*/

    public EventImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(theMsoModel, anObjectId);
        setNumber(aNumber);
    }

    public EventImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber, Calendar aCalendar)
    {
        super(theMsoModel, anObjectId, aCalendar);
        setNumber(aNumber);
    }

    /*-------------------------------------------------------------------------------------------
     * Methods that must be implemented
     *-------------------------------------------------------------------------------------------*/

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_status);
        addAttribute(m_number);
        addAttribute(m_name);
        addAttribute(m_description);
        addAttribute(m_level);
        addAttribute(m_priority);

    }

    @Override
    protected void defineLists()
    {
        super.defineLists();
        addList(m_eventTasks);
    }

    @Override
    protected void defineReferences()
    {
        super.defineReferences();
    }


    @Override
    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        if (anObject instanceof ITaskIf)
        {
            m_eventTasks.add((ITaskIf)anObject);
            return true;
        }
        return super.addObjectReference(anObject,aReferenceName);

    }

    @Override
    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        if (anObject instanceof ITaskIf)
        {
            return m_eventTasks.remove((ITaskIf) anObject);
        }
        return super.removeObjectReference(anObject,aReferenceName);
    }

    public static EventImpl implementationOf(IEventIf anInterface) throws MsoCastException
    {
        try
        {
            return (EventImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to EventImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_EVENT;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for enums
    *-------------------------------------------------------------------------------------------*/

	public EventStatus getStatus() {
		return m_status.getAttrValue();
	}

	public void setStatus(EventStatus status) {
		m_status.setAttrValue(status);
	}

	public void setStatus(String status) {
		m_status.setValue(status);

	}

	public IMsoEnumIf<EventStatus> getStatusAttribute() {
		return m_status;
	}

	public ModificationState getStatusState() {
		return m_status.getState();
	}


    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

	// From ISerialNumberedIf
    public void setNumber(int aNumber)
    {
    	setNumber(m_number,aNumber);    }

    public int getNumber()
    {
        return m_number.intValue();
    }

    public IMsoModelIf.ModificationState getNumberState()
    {
        return m_number.getState();
    }

    public IAttributeIf.IMsoIntegerIf getNumberAttribute()
    {
        return m_number;
    }

	public String getName() {
		return m_name.getAttrValue();
	}

	public IMsoStringIf getNameAttribute() {
		return m_name;
	}

	public ModificationState getNameState() {
		return m_name.getState();
	}

	public void setName(String text) {
		m_name.setAttrValue(text);
	}

	public String getDescription() {
		return m_description.getAttrValue();
	}

	public IMsoStringIf getDescriptionAttribute() {
		return m_description;
	}

	public ModificationState getDescriptionState() {
		return m_description.getState();
	}

	public void setDescription(String text) {
		m_description.setAttrValue(text);
	}

	public int getLevel() {
		return m_level.getAttrValue();
	}

	public IMsoIntegerIf getLevelAttribute() {
		return m_level;
	}

	public ModificationState getLevelState() {
		return m_level.getState();
	}

	public void setLevel(int level) {
		m_level.setAttrValue(level);
	}

	public int getPriority() {
		return m_priority.getAttrValue();
	}

	public IMsoIntegerIf getPriorityAttribute() {
		return m_priority;
	}

	public ModificationState getPriorityState() {
		return m_priority.getState();
	}

	public void setPriority(int priority) {
		m_priority.setAttrValue(priority);
	}

    /*-------------------------------------------------------------------------------------------
     * Methods for references
     *-------------------------------------------------------------------------------------------*/



    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

	public void addEventTask(ITaskIf anITaskIf)
    {
        m_eventTasks.add(anITaskIf);
    }

    public ITaskListIf getEventTasks()
    {
        return m_eventTasks;
    }

    public IMsoModelIf.ModificationState getEventTasksState(ITaskIf anITaskIf)
    {
        return m_eventTasks.getState(anITaskIf);
    }

    public Collection<ITaskIf> getEventTasksItems()
    {
        return m_eventTasks.getItems();
    }

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

}