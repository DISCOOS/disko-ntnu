package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.util.except.MsoCastException;

import java.util.Calendar;

public class PersonnelImpl extends AbstractPerson implements IPersonnelIf
{

    private final AttributeImpl.MsoCalendar m_arrived = new AttributeImpl.MsoCalendar(this, "Arrived");
    private final AttributeImpl.MsoCalendar m_callOut = new AttributeImpl.MsoCalendar(this, "CallOut");
    private final AttributeImpl.MsoString m_dataSourceID = new AttributeImpl.MsoString(this, "DataSourceID");
    private final AttributeImpl.MsoString m_dataSourceName = new AttributeImpl.MsoString(this, "DataSourceName");
    private final AttributeImpl.MsoCalendar m_estimatedArrival = new AttributeImpl.MsoCalendar(this, "EstimatedArrival");
    private final AttributeImpl.MsoCalendar m_released = new AttributeImpl.MsoCalendar(this, "Released");
    private final AttributeImpl.MsoString m_remarks = new AttributeImpl.MsoString(this, "Remarks");
    private final AttributeImpl.MsoString m_department = new AttributeImpl.MsoString(this, "Department");
    private final AttributeImpl.MsoString m_organization = new AttributeImpl.MsoString(this, "Organization");

    private final AttributeImpl.MsoEnum<PersonnelStatus> m_status = new AttributeImpl.MsoEnum<PersonnelStatus>(this, "Status", 1, PersonnelStatus.IDLE);
    private final AttributeImpl.MsoEnum<PersonnelType> m_type = new AttributeImpl.MsoEnum<PersonnelType>(this, "Type", 1, PersonnelType.VOLUNTEER);
    private final AttributeImpl.MsoEnum<PersonnelImportStatus> m_importStatus = new AttributeImpl.MsoEnum<PersonnelImportStatus>(this, "ImportStatus", 1, PersonnelImportStatus.UPDATED);

    private final MsoReferenceImpl<IPersonnelIf> m_nextOccurence = new MsoReferenceImpl<IPersonnelIf>(this,"NextOccurence", 0, true);

    public PersonnelImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_arrived);
        addAttribute(m_callOut);
        addAttribute(m_dataSourceID);
        addAttribute(m_dataSourceName);
        addAttribute(m_estimatedArrival);
        addAttribute(m_released);
        addAttribute(m_remarks);
        addAttribute(m_department);
        addAttribute(m_organization);
        addAttribute(m_status);
        addAttribute(m_type);
        addAttribute(m_importStatus);
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
        addReference(m_nextOccurence);
    }

    @Override
    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return super.addObjectReference(anObject, aReferenceName);
    }

    @Override
    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return super.removeObjectReference(anObject, aReferenceName);
    }

    public static PersonnelImpl implementationOf(IPersonnelIf anInterface) throws MsoCastException
    {
        try
        {
            return (PersonnelImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to PersonnelImpl");
        }
    }

    public void setArrived(Calendar anArrived)
    {
        m_arrived.setValue(anArrived);
    }

    public Calendar getArrived()
    {
        return m_arrived.getCalendar();
    }

    public IMsoModelIf.ModificationState getArrivedState()
    {
        return m_arrived.getState();
    }

    public IAttributeIf.IMsoCalendarIf getArrivedAttribute()
    {
        return m_arrived;
    }

    public void setCallOut(Calendar aCallOut)
    {
        m_callOut.setValue(aCallOut);
    }

    public Calendar getCallOut()
    {
        return m_callOut.getCalendar();
    }

    public IMsoModelIf.ModificationState getCallOutState()
    {
        return m_callOut.getState();
    }

    public IAttributeIf.IMsoCalendarIf getCallOutAttribute()
    {
        return m_callOut;
    }

    public void setDataSourceID(String aDataSourceID)
    {
        m_dataSourceID.setValue(aDataSourceID);
    }

    public String getDataSourceID()
    {
        return m_dataSourceID.getString();
    }

    public IMsoModelIf.ModificationState getDataSourceIDState()
    {
        return m_dataSourceID.getState();
    }

    public IAttributeIf.IMsoStringIf getDataSourceIDAttribute()
    {
        return m_dataSourceID;
    }

    public void setDataSourceName(String aDataSourceName)
    {
        m_dataSourceName.setValue(aDataSourceName);
    }

    public String getDataSourceName()
    {
        return m_dataSourceName.getString();
    }

    public IMsoModelIf.ModificationState getDataSourceNameState()
    {
        return m_dataSourceName.getState();
    }

    public IAttributeIf.IMsoStringIf getDataSourceNameAttribute()
    {
        return m_dataSourceName;
    }

    public void setEstimatedArrival(Calendar anEstimatedArrival)
    {
        m_estimatedArrival.setValue(anEstimatedArrival);
    }

    public Calendar getEstimatedArrival()
    {
        return m_estimatedArrival.getCalendar();
    }

    public IMsoModelIf.ModificationState getEstimatedArrivalState()
    {
        return m_estimatedArrival.getState();
    }

    public IAttributeIf.IMsoCalendarIf getEstimatedArrivalAttribute()
    {
        return m_estimatedArrival;
    }

    public void setReleased(Calendar aReleased)
    {
        m_released.setValue(aReleased);
    }

    public Calendar getReleased()
    {
        return m_released.getCalendar();
    }

    public IMsoModelIf.ModificationState getReleasedState()
    {
        return m_released.getState();
    }

    public IAttributeIf.IMsoCalendarIf getReleasedAttribute()
    {
        return m_released;
    }

    public void setRemarks(String aRemarks)
    {
        m_remarks.setValue(aRemarks);
    }

    public String getRemarks()
    {
        return m_remarks.getString();
    }

    public IMsoModelIf.ModificationState getRemarksState()
    {
        return m_remarks.getState();
    }

    public IAttributeIf.IMsoStringIf getRemarksAttribute()
    {
        return m_remarks;
    }

    public void setDepartment(String aDepartment)
    {
        m_department.setValue(aDepartment);
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

    public void setOrganization(String anOrganization)
    {
        m_organization.setValue(anOrganization);
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

    public void setStatus(PersonnelStatus aStatus)
    {
        m_status.setValue(aStatus);
    }

    public void setStatus(String aStatus)
    {
        m_status.setValue(aStatus);
    }

    public PersonnelStatus getStatus()
    {
        return m_status.getValue();
    }

    public IMsoModelIf.ModificationState getStatusState()
    {
        return m_status.getState();
    }

    public IAttributeIf.IMsoEnumIf<PersonnelStatus> getStatusAttribute()
    {
        return m_status;
    }

    public String getStatusText()
    {
        return m_status.getInternationalName();
    }

    public void setType(PersonnelType aType)
    {
        m_type.setValue(aType);
    }

    public void setType(String aType)
    {
        m_type.setValue(aType);
    }

    public PersonnelType getType()
    {
        return m_type.getValue();
    }

    public IMsoModelIf.ModificationState getTypeState()
    {
        return m_type.getState();
    }

    public IAttributeIf.IMsoEnumIf<PersonnelType> getTypeAttribute()
    {
        return m_type;
    }

    public String getTypeName()
    {
        return m_type.getName();
    }

    public String getInternationalTypeName()
    {
        return m_type.getInternationalName();
    }

    public void setImportStatus(PersonnelImportStatus status)
    {
    	m_importStatus.setValue(status);
    }

    public void setImportStatus(String status)
    {
    	m_importStatus.setValue(PersonnelImportStatus.valueOf(status));
    }

    public PersonnelImportStatus getImportStatus()
    {
    	return m_importStatus.getValue();
    }

    public String getImportStatusText()
    {
        return m_importStatus.getInternationalName();
    }

    public IMsoModelIf.ModificationState getImportStatusState()
    {
    	return m_importStatus.getState();
    }

    public IAttributeIf.IMsoEnumIf<PersonnelImportStatus> getImportStatusAttribute()
    {
    	return m_importStatus;
    }


    public void setNextOccurence(IPersonnelIf aPersonnel)
    {
        m_nextOccurence.setReference(aPersonnel);
    }

    public IPersonnelIf getNextOccurence()
    {
        return m_nextOccurence.getReference();
    }

    public IMsoModelIf.ModificationState getNextOccurenceState()
    {
        return m_nextOccurence.getState();
    }

    public IMsoReferenceIf<IPersonnelIf> getNextOccurenceAttribute()
    {
        return m_nextOccurence;
    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_PERSONNEL;
    }

	public String getDefaultName()
	{
		return (getFirstName() + " " + getLastName());
	}

    public IUnitIf getOwningUnit()
    {
        owningUnitSelector.setSelfObject(this);
        ICmdPostIf cmdPost = MsoModelImpl.getInstance().getMsoManager().getCmdPost();
        return cmdPost != null ? cmdPost.getUnitList().selectSingleItem(owningUnitSelector) : null;
    }

    private final static SelfSelector<IPersonnelIf, IUnitIf> owningUnitSelector = new SelfSelector<IPersonnelIf, IUnitIf>()
    {
        public boolean select(IUnitIf anObject)
        {
        	if(IUnitIf.ACTIVE_UNIT_SELECTOR.select(anObject))
        		return anObject.getUnitPersonnel().equals(m_object);
        	return false;
        }
    };

}