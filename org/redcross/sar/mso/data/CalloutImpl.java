package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
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
	private final AttributeImpl.MsoString m_division = new AttributeImpl.MsoString(this, "Division");	
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
    	addAttribute(m_division);
    	addAttribute(m_department);
    }

    protected void defineLists()
    {
        addList(m_personnel);
    }

    protected void defineObjects()
    {
    }

    public void addListRelation(IMsoObjectIf anObject, String aReferenceListName)
    {
        if (anObject instanceof IPersonnelIf)
        {
            m_personnel.add((IPersonnelIf) anObject);
        }
    }

    public void removeListRelation(IMsoObjectIf anObject, String aReferenceListName)
    {
        if (anObject instanceof IPersonnelIf)
        {
            m_personnel.remove((IPersonnelIf) anObject);
        }
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
		return m_personnel.getObjects();
	}

    public IMsoManagerIf.MsoClassCode getClassCode()
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

	public IData.DataOrigin getTitleState()
	{
		return m_title.getOrigin();
	}

    public IMsoAttributeIf.IMsoStringIf getTitleAttribute()
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

	public IData.DataOrigin getCreatedState()
	{
		return m_created.getOrigin();
	}

    public IMsoAttributeIf.IMsoCalendarIf getCreatedAttribute()
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

	public IData.DataOrigin getOrganizationState()
	{
		return m_organization.getOrigin();
	}

    public IMsoAttributeIf.IMsoStringIf getOrganizationAttribute()
    {
    	return m_organization;
    }

    public void setDivision(String division)
    {
    	m_division.setValue(division);
    }

	public String getDivision()
	{
		return m_division.getString();
	}

	public IData.DataOrigin getDivisionState()
	{
		return m_division.getOrigin();
	}

    public IMsoAttributeIf.IMsoStringIf getDivisionAttribute()
    {
    	return m_division;
    }
    
	public void setDepartment(String department)
	{
		m_department.setValue(department);
	}

	public String getDepartment()
	{
		return m_department.getString();
	}

	public IData.DataOrigin getDepartmentState()
	{
		return m_department.getOrigin();
	}

    public IMsoAttributeIf.IMsoStringIf getDepartmentAttribute()
    {
    	return m_department;
    }

}