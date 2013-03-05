package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("unchecked")
public class PersonnelImpl extends AbstractPerson implements IPersonnelIf
{

    private final AttributeImpl.MsoCalendar m_arrived = new AttributeImpl.MsoCalendar(this, "Arrived");
    private final AttributeImpl.MsoCalendar m_callOut = new AttributeImpl.MsoCalendar(this, "CallOut");
    private final AttributeImpl.MsoString m_dataSourceID = new AttributeImpl.MsoString(this, "DataSourceID");
    private final AttributeImpl.MsoString m_dataSourceName = new AttributeImpl.MsoString(this, "DataSourceName");
    private final AttributeImpl.MsoCalendar m_estimatedArrival = new AttributeImpl.MsoCalendar(this, "EstimatedArrival");
    private final AttributeImpl.MsoCalendar m_released = new AttributeImpl.MsoCalendar(this, "Released");
    private final AttributeImpl.MsoString m_remarks = new AttributeImpl.MsoString(this, "Remarks");
    private final AttributeImpl.MsoString m_organization = new AttributeImpl.MsoString(this, "Organization");
    private final AttributeImpl.MsoString m_division = new AttributeImpl.MsoString(this, "Division");
    private final AttributeImpl.MsoString m_department = new AttributeImpl.MsoString(this, "Department");    

    private final AttributeImpl.MsoEnum<PersonnelStatus> m_status = new AttributeImpl.MsoEnum<PersonnelStatus>(this, "Status", 1, PersonnelStatus.IDLE);
    private final AttributeImpl.MsoEnum<PersonnelType> m_type = new AttributeImpl.MsoEnum<PersonnelType>(this, "Type", 1, PersonnelType.VOLUNTEER);
    private final AttributeImpl.MsoEnum<PersonnelImportStatus> m_importStatus = new AttributeImpl.MsoEnum<PersonnelImportStatus>(this, "ImportStatus", 1, PersonnelImportStatus.UPDATED);

    private final MsoRelationImpl<IPersonnelIf> m_nextOccurrence = new MsoRelationImpl<IPersonnelIf>(this,"NextOccurence", 0, true, null);

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
        addAttribute(m_organization);
        addAttribute(m_division);
        addAttribute(m_department);
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
    protected void defineObjects()
    {
        super.defineObjects();
        addObject(m_nextOccurrence);
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
        m_arrived.set(anArrived);
    }

    public Calendar getArrived()
    {
        return m_arrived.getCalendar();
    }

    public IData.DataOrigin getArrivedState()
    {
        return m_arrived.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getArrivedAttribute()
    {
        return m_arrived;
    }

    public void setCallOut(Calendar aCallOut)
    {
        m_callOut.set(aCallOut);
    }

    public Calendar getCallOut()
    {
        return m_callOut.getCalendar();
    }

    public IData.DataOrigin getCallOutState()
    {
        return m_callOut.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getCallOutAttribute()
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

    public IData.DataOrigin getDataSourceIDState()
    {
        return m_dataSourceID.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getDataSourceIDAttribute()
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

    public IData.DataOrigin getDataSourceNameState()
    {
        return m_dataSourceName.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getDataSourceNameAttribute()
    {
        return m_dataSourceName;
    }

    public void setEstimatedArrival(Calendar anEstimatedArrival)
    {
        m_estimatedArrival.set(anEstimatedArrival);
    }

    public Calendar getEstimatedArrival()
    {
        return m_estimatedArrival.getCalendar();
    }

    public IData.DataOrigin getEstimatedArrivalState()
    {
        return m_estimatedArrival.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getEstimatedArrivalAttribute()
    {
        return m_estimatedArrival;
    }

    public void setReleased(Calendar aReleased)
    {
        m_released.set(aReleased);
    }

    public Calendar getReleased()
    {
        return m_released.getCalendar();
    }

    public IData.DataOrigin getReleasedState()
    {
        return m_released.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getReleasedAttribute()
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

    public IData.DataOrigin getRemarksState()
    {
        return m_remarks.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getRemarksAttribute()
    {
        return m_remarks;
    }

    public void setOrganization(String anOrganization)
    {
        m_organization.setValue(anOrganization);
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
    
    public void setDepartment(String aDepartment)
    {
        m_department.setValue(aDepartment);
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
    
    public void setStatus(PersonnelStatus aStatus)
    {
        m_status.set(aStatus);
    }

    public void setStatus(String aStatus)
    {
        m_status.set(aStatus);
    }

    public PersonnelStatus getStatus()
    {
        return m_status.get();
    }

    public IData.DataOrigin getStatusState()
    {
        return m_status.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<PersonnelStatus> getStatusAttribute()
    {
        return m_status;
    }

    public String getStatusText()
    {
        return m_status.getInternationalName();
    }

    public void setType(PersonnelType aType)
    {
        m_type.set(aType);
    }

    public void setType(String aType)
    {
        m_type.set(aType);
    }

    public PersonnelType getType()
    {
        return m_type.get();
    }

    public IData.DataOrigin getTypeState()
    {
        return m_type.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<PersonnelType> getTypeAttribute()
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
    	m_importStatus.set(status);
    }

    public void setImportStatus(String status)
    {
    	m_importStatus.set(PersonnelImportStatus.valueOf(status));
    }

    public PersonnelImportStatus getImportStatus()
    {
    	return m_importStatus.get();
    }

    public String getImportStatusText()
    {
        return m_importStatus.getInternationalName();
    }

    public IData.DataOrigin getImportStatusState()
    {
    	return m_importStatus.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<PersonnelImportStatus> getImportStatusAttribute()
    {
    	return m_importStatus;
    }

	public IPersonnelIf getFirstOccurrence() {
		List<IPersonnelIf> list = getPreviousOccurrences();
		return (list.size()>0?list.get(0):this);
	}
	
	public IPersonnelIf getPreviousOccurrence() {
    	return (IPersonnelIf)m_mainList.selectSingleItem(new OccurrenceSelector(this,true));
    }
    
	public List<IPersonnelIf> getPreviousOccurrences()
    {    	
        return new Vector<IPersonnelIf>(m_mainList.selectItems(new OccurrenceSelector(this,true)));
    }

    public void setNextOccurrence(IPersonnelIf aPersonnel)
    {
        m_nextOccurrence.set(aPersonnel);
    }

    public IPersonnelIf getNextOccurrence()
    {
        return m_nextOccurrence.get();
    }

    public IData.DataOrigin getNextOccurrenceState()
    {
        return m_nextOccurrence.getOrigin();
    }

    public IMsoRelationIf<IPersonnelIf> getNextOccurrenceAttribute()
    {
        return m_nextOccurrence;
    }

    @SuppressWarnings("unchecked")
	public List<IPersonnelIf> getNextOccurrences()
    {    	
        return new Vector<IPersonnelIf>(m_mainList.selectItems(new OccurrenceSelector(this,false)));
    }
    
	public IPersonnelIf getLastOccurrence() {
		List<IPersonnelIf> list = getNextOccurrences();
		return (list.size()>0?list.get(list.size()-1):this);
	}
	
    public IMsoManagerIf.MsoClassCode getClassCode()
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
        ICmdPostIf cmdPost = m_model.getMsoManager().getCmdPost();
        return cmdPost != null ? cmdPost.getUnitList().selectSingleItem(owningUnitSelector) : null;
    }

    private final static SelfSelector<IPersonnelIf, IUnitIf> owningUnitSelector = new SelfSelector<IPersonnelIf, IUnitIf>()
    {
        public boolean select(IUnitIf anObject)
        {
        	if(IUnitIf.OWNER_SELECTOR.select(anObject))
        		return anObject.getUnitPersonnel().exists(m_object);
        	return false;
        }
    };
    
    public static class OccurrenceSelector implements Selector<IPersonnelIf> {

    	IPersonnelIf pivot;
    	boolean backwards;
    	
    	public OccurrenceSelector(IPersonnelIf pivot, boolean backwards) {
    		this.pivot = pivot;
    		this.backwards = backwards;
    	}
    	
		@Override
		public boolean select(IPersonnelIf anObject) {
			// find root
			if(backwards) {
				/* ========================================
				 * search backwards (previous occurrences)
				 * ======================================== */ 
				if(pivot!=null && pivot.equals(anObject.getNextOccurrence())) {
					pivot = anObject;
					return true;
				}
			} else {
				/* ========================================
				 * search forward (next occurrences)
				 * ======================================== */ 
				if(pivot!=null && pivot.equals(anObject)) {
					pivot = anObject.getNextOccurrence();
				}
			}
			return false;
		}
    	
    };
    

}