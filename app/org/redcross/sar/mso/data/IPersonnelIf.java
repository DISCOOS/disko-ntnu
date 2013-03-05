
package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;

import java.util.Calendar;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 *
 */
public interface IPersonnelIf extends IPersonIf, IAssociationIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.Personnel";

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

    public static final EnumSet<PersonnelStatus> ACTIVE_SET = EnumSet.range(PersonnelStatus.IDLE,PersonnelStatus.ARRIVED);
    public static final EnumSet<PersonnelStatus> HISTORY_SET = EnumSet.of(PersonnelStatus.IDLE,PersonnelStatus.RELEASED);
    
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

	/**
	 * This selector selects every personnel with status other than RELEASED 
	 */
    public static Selector<IPersonnelIf> ACTIVE_SELECTOR = new Selector<IPersonnelIf>()
	{
		public boolean select(IPersonnelIf personnel)
		{
			return personnel.getNextOccurrence() == null && !PersonnelStatus.RELEASED.equals(personnel.getStatus());
		}
	};
	
	/**
	 * This selector selects every personnel with status other than RELEASED and every
	 * personnel that is released but not reinstated (<code>IPersonnel::getNextOccurrence()</code> 
	 * is <code>null</code>). 
	 */
	public static Selector<IPersonnelIf> REINSTATE_SELECTOR = new Selector<IPersonnelIf>()
	{
		public boolean select(IPersonnelIf personnel)
		{
			return personnel.getNextOccurrence() == null;
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

    public void setArrived(Calendar anArrived);

    public Calendar getArrived();

    public IData.DataOrigin getArrivedState();

    public IMsoAttributeIf.IMsoCalendarIf getArrivedAttribute();

    public void setCallOut(Calendar aCallOut);

    public Calendar getCallOut();

    public IData.DataOrigin getCallOutState();

    public IMsoAttributeIf.IMsoCalendarIf getCallOutAttribute();

    public void setDataSourceID(String aDataSourceID);

    public String getDataSourceID();

    public IData.DataOrigin getDataSourceIDState();

    public IMsoAttributeIf.IMsoStringIf getDataSourceIDAttribute();

    public void setDataSourceName(String aDataSourceName);

    public String getDataSourceName();

    public IData.DataOrigin getDataSourceNameState();

    public IMsoAttributeIf.IMsoStringIf getDataSourceNameAttribute();

    public void setEstimatedArrival(Calendar anEstimatedArrival);

    public Calendar getEstimatedArrival();

    public IData.DataOrigin getEstimatedArrivalState();

    public IMsoAttributeIf.IMsoCalendarIf getEstimatedArrivalAttribute();

    public void setReleased(Calendar aReleased);

    public Calendar getReleased();

    public IData.DataOrigin getReleasedState();

    public IMsoAttributeIf.IMsoCalendarIf getReleasedAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IData.DataOrigin getRemarksState();

    public IMsoAttributeIf.IMsoStringIf getRemarksAttribute();

    public void setStatus(PersonnelStatus aStatus);

    public void setStatus(String aStatus);

    public PersonnelStatus getStatus();

    public String getStatusText();

    public IData.DataOrigin getStatusState();

    public IMsoAttributeIf.IMsoEnumIf<PersonnelStatus> getStatusAttribute();

    public void setType(PersonnelType aType);

    public void setType(String aType);

    public PersonnelType getType();

    public IData.DataOrigin getTypeState();

    public IMsoAttributeIf.IMsoEnumIf<PersonnelType> getTypeAttribute();

    public String getTypeName();

    public String getInternationalTypeName();

    public void setImportStatus(PersonnelImportStatus status);

    public void setImportStatus(String status);

    public PersonnelImportStatus getImportStatus();

    public String getImportStatusText();

    public IData.DataOrigin getImportStatusState();

    public IMsoAttributeIf.IMsoEnumIf<PersonnelImportStatus> getImportStatusAttribute();

    /**
     * Get the first occurrence of this personnel. 
     *  
     * @return A IPersonnelIf instance if found, <code>this</code> otherwise.
     */
    public IPersonnelIf getFirstOccurrence();
    
    /**
     * Get the previous occurrence of this personnel. 
     *  
     * @return A IPersonnelIf instance if found, <code>null</code> otherwise.
     */
    public IPersonnelIf getPreviousOccurrence();
    
    /**
     * Get a list of all previous occurrences of this personnel. 
     *  
     * @return A List<IPersonnelIf> instance.
     */
    public List<IPersonnelIf> getPreviousOccurrences();
    
    /**
     * Set the next occurrence of this personnel. 
     */
    public void setNextOccurrence(IPersonnelIf aPersonnel);

    /**
     * Get the next occurrence of this personnel. 
     *  
     * @return A IPersonnelIf instance if found, <code>null</code> otherwise.
     */
    public IPersonnelIf getNextOccurrence();

    public IData.DataOrigin getNextOccurrenceState();

    public IMsoRelationIf<IPersonnelIf> getNextOccurrenceAttribute();

    /**
     * Get a list of the next occurrences of this personnel. 
     *  
     * @return A List<IPersonnelIf> instance.
     */
    public List<IPersonnelIf> getNextOccurrences();
    
    /**
     * Get the last occurrence of this personnel. 
     *  
     * @return A IPersonnelIf instance if found, <code>this</code> otherwise.
     */
    public IPersonnelIf getLastOccurrence();
    
    public IUnitIf getOwningUnit();


}
