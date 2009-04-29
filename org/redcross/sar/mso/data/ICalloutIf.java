package org.redcross.sar.mso.data;

import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;

import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoModelIf;

public interface ICalloutIf extends IAssociationIf
{
	public enum CalloutType
	{
		UMS_VB,
		FILE
	}

	/**
	 * Often used selectors
	 */
    public static final Selector<ICalloutIf> ALL_SELECTOR = new Selector<ICalloutIf>()
	{
		public boolean select(ICalloutIf c1)
		{
			return true;
		}
	};

	/**
	 * Often used comparators
	 */
    public static final Comparator<ICalloutIf> CALLOUT_COMPARATOR = new Comparator<ICalloutIf>()
	{
		public int compare(ICalloutIf c1, ICalloutIf c2)
		{
			return c1.getCreated().compareTo(c2.getCreated());
		}
	};

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