package org.redcross.sar.mso.data;

import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoModelIf;

import java.util.Calendar;
import java.util.Comparator;

/**
 *
 */
public interface IPersonnelIf extends IPersonIf, IAssociationIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.Personnel";

    /**
     * Often used selectors
     */

    public static Selector<IPersonnelIf> ALL_SELECTOR = new Selector<IPersonnelIf>()
	{
		public boolean select(IPersonnelIf personnel)
		{
			return true;
		}
	};

    public static Selector<IPersonnelIf> ACTIVE_SELECTOR = new Selector<IPersonnelIf>()
	{
		public boolean select(IPersonnelIf personnel)
		{
			return personnel.getNextOccurence() == null;
		}
	};

	/**
	 * Often used comparators
	 */
    public static final Comparator<IPersonnelIf> PERSONNEL_NAME_COMPARATOR = new Comparator<IPersonnelIf>()
	{
		public int compare(IPersonnelIf p1, IPersonnelIf p2)
		{
			int res = p1.getFirstName().compareTo(p2.getFirstName());
			return res == 0 ? p1.getLastName().compareTo(p2.getLastName()) : res;
		}
	};

    public enum PersonnelStatus
    {
        IDLE,
        ON_ROUTE,
        ARRIVED,
        RELEASED
    }

    public enum PersonnelType
    {
        VOLUNTEER,
        POLICE,
        MILITARY,
        CIVIL_DEFENSE,
        HEALTH_WORKER,
        FIREFIGHTER,
        OTHER
    }

    public enum PersonnelImportStatus
    {
    	IMPORTED,
    	UPDATED,
    	KEPT
    }

    public void setArrived(Calendar anArrived);

    public Calendar getArrived();

    public IMsoModelIf.ModificationState getArrivedState();

    public IAttributeIf.IMsoCalendarIf getArrivedAttribute();

    public void setCallOut(Calendar aCallOut);

    public Calendar getCallOut();

    public IMsoModelIf.ModificationState getCallOutState();

    public IAttributeIf.IMsoCalendarIf getCallOutAttribute();

    public void setDataSourceID(String aDataSourceID);

    public String getDataSourceID();

    public IMsoModelIf.ModificationState getDataSourceIDState();

    public IAttributeIf.IMsoStringIf getDataSourceIDAttribute();

    public void setDataSourceName(String aDataSourceName);

    public String getDataSourceName();

    public IMsoModelIf.ModificationState getDataSourceNameState();

    public IAttributeIf.IMsoStringIf getDataSourceNameAttribute();

    public void setEstimatedArrival(Calendar anEstimatedArrival);

    public Calendar getEstimatedArrival();

    public IMsoModelIf.ModificationState getEstimatedArrivalState();

    public IAttributeIf.IMsoCalendarIf getEstimatedArrivalAttribute();

    public void setReleased(Calendar aReleased);

    public Calendar getReleased();

    public IMsoModelIf.ModificationState getReleasedState();

    public IAttributeIf.IMsoCalendarIf getReleasedAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IMsoModelIf.ModificationState getRemarksState();

    public IAttributeIf.IMsoStringIf getRemarksAttribute();

    public void setStatus(PersonnelStatus aStatus);

    public void setStatus(String aStatus);

    public PersonnelStatus getStatus();

    public String getStatusText();

    public IMsoModelIf.ModificationState getStatusState();

    public IAttributeIf.IMsoEnumIf<PersonnelStatus> getStatusAttribute();

    public void setType(PersonnelType aType);

    public void setType(String aType);

    public PersonnelType getType();

    public IMsoModelIf.ModificationState getTypeState();

    public IAttributeIf.IMsoEnumIf<PersonnelType> getTypeAttribute();

    public String getTypeName();

    public String getInternationalTypeName();

    public void setImportStatus(PersonnelImportStatus status);

    public void setImportStatus(String status);

    public PersonnelImportStatus getImportStatus();

    public String getImportStatusText();

    public IMsoModelIf.ModificationState getImportStatusState();

    public IAttributeIf.IMsoEnumIf<PersonnelImportStatus> getImportStatusAttribute();


    public void setNextOccurence(IPersonnelIf aPersonnel);

    public IPersonnelIf getNextOccurence();

    public IMsoModelIf.ModificationState getNextOccurenceState();

    public IMsoReferenceIf<IPersonnelIf> getNextOccurenceAttribute();

    public IUnitIf getOwningUnit();


}
