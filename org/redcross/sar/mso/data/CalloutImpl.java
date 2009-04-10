package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.MsoCastException;
import org.redcross.sar.util.except.MsoRuntimeException;

import java.util.Calendar;
import java.util.Collection;

public class CalloutImpl extends AbstractMsoObject implements ICalloutIf
{
	// Attributes
	private final AttributeImpl.MsoString m_title = new AttributeImpl.MsoString(this, "Title");
	private final AttributeImpl.MsoCalendar m_created = new AttributeImpl.MsoCalendar(this, "Created");
	private final AttributeImpl.MsoString m_organization = new AttributeImpl.MsoString(this, "Organization");
	private final AttributeImpl.MsoString m_department = new AttributeImpl.MsoString(this, "Department");

	// Lists
    private final PersonnelListImpl m_personnel = new PersonnelListImpl(this, "CalloutPersonnel", false);

    public CalloutImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    protected void defineAttributes()
    {
    	addAttribute(m_title);
    	addAttribute(m_created);
    	addAttribute(m_organization);
    	addAttribute(m_department);
    }

    protected void defineLists()
    {
        addList(m_personnel);
    }

    protected void defineReferences()
    {
    }

    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        if (anObject instanceof IPersonnelIf)
        {
            m_personnel.add((IPersonnelIf) anObject);
            return true;
        }
        return false;
    }

    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        if (anObject instanceof IPersonnelIf)
        {
            return m_personnel.remove((IPersonnelIf) anObject);
        }
        return false;
    }

    public static CalloutImpl implementationOf(ICalloutIf anInterface) throws MsoCastException
    {
        try
        {
            return (CalloutImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to Callout");
        }
    }

    public IPersonnelIf createPersonnel(IMsoObjectIf.IObjectIdIf anObjectId, IPersonnelListIf aPesonnelList)
    {
        IPersonnelIf retVal = aPesonnelList.createPersonnel(anObjectId);
        if (addPersonel(retVal))
        {
            return retVal;
        }
        throw new DuplicateIdException("Duplicated id in Callout: " + anObjectId);
    }

    public boolean addPersonel(IPersonnelIf aPersonnel)
    {
        try
        {
            m_personnel.add(aPersonnel);
            return true;
        }
        catch (MsoRuntimeException e)
        {
            return false;
        }
    }

	public IPersonnelListIf getPersonnelList()
	{
		return m_personnel;
	}

	public Collection<IPersonnelIf> getPersonnelListItems()
	{
		return m_personnel.getItems();
	}

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_CALLOUT;
    }

	public void setTitle(String title)
	{
		m_title.setValue(title);
	}

	public String getTitle()
	{
		return m_title.getString();
	}

	public IMsoModelIf.ModificationState getTitleState()
	{
		return m_title.getState();
	}

    public IAttributeIf.IMsoStringIf getTitleAttribute()
    {
    	return m_title;
    }

    public void setCreated(Calendar created)
    {
    	m_created.setValue(created);
    }

	public Calendar getCreated()
	{
		return m_created.getCalendar();
	}

	public IMsoModelIf.ModificationState getCreatedState()
	{
		return m_created.getState();
	}

    public IAttributeIf.IMsoCalendarIf getCreatedAttribute()
    {
    	return m_created;
    }

    public void setOrganization(String organization)
    {
    	m_organization.setValue(organization);
    }

	public String getOrganization()
	{
		return m_organization.getString();
	}

	public IMsoModelIf.ModificationState getOrganizationState()
	{
		return m_organization.getState();
	}

    public IAttributeIf.IMsoStringIf getOrganizationAttribute()
    {
    	return m_organization;
    }


	public void setDepartment(String department)
	{
		m_department.setValue(department);
	}

	public String getDepartment()
	{
		return m_department.getString();
	}

	public IMsoModelIf.ModificationState getDepartmentState()
	{
		return m_department.getState();
	}

    public IAttributeIf.IMsoStringIf getDepartmentAttribute()
    {
    	return m_department;
    }

}