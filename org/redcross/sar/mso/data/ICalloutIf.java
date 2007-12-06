package org.redcross.sar.mso.data;

import java.util.Calendar;
import java.util.Collection;

import org.redcross.sar.mso.IMsoModelIf;

public interface ICalloutIf extends IMsoObjectIf
{
	public enum CalloutType
	{
		USM_VB,
		FILE
	}
	
	/*-------------------------------------------------------------------------------------------
	 * Methods for attributes
	 *-------------------------------------------------------------------------------------------*/    
	public void setTitle(String title);
	
	public String getTitle();
	
	public IMsoModelIf.ModificationState getTitleState();

    public IAttributeIf.IMsoStringIf getTitleAttribute();
    
	
    public void setCreated(Calendar created);
    
	public Calendar getCreated();
	
	public IMsoModelIf.ModificationState getCreatedState();

    public IAttributeIf.IMsoCalendarIf getCreatedAttribute();
	
    
    public void setOrganization(String organization);
    
	public String getOrganization();
	
	public IMsoModelIf.ModificationState getOrganizationState();

    public IAttributeIf.IMsoStringIf getOrganizationAttribute();
    
    
	public void setDepartment(String department);
	
	public String getDepartment();
	
	public IMsoModelIf.ModificationState getDepartmentState();

    public IAttributeIf.IMsoStringIf getDepartmentAttribute();
	
	/*-------------------------------------------------------------------------------------------
	 * Methods for lists
	 *-------------------------------------------------------------------------------------------*/
    /**
     * Add a reference to an excising personnel.
     *
     * @param aPersonnel The personnel to add.
     * @return <code>false</code> if personnel exists in list already, <code>true</code> otherwise.
     */
    public boolean addPersonel(IPersonnelIf aPersonnel);
    
    public IPersonnelListIf getPersonnelList();
    
    public Collection<IPersonnelIf> getPersonnelListItems();
}