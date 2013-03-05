package org.redcross.sar.mso.data;

import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;

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

	public IData.DataOrigin getTitleState();

    public IMsoAttributeIf.IMsoStringIf getTitleAttribute();


    public void setCreated(Calendar created);

	public Calendar getCreated();

	public IData.DataOrigin getCreatedState();

    public IMsoAttributeIf.IMsoCalendarIf getCreatedAttribute();


    public void setOrganization(String organization);

	public String getOrganization();

	public IData.DataOrigin getOrganizationState();

    public IMsoAttributeIf.IMsoStringIf getOrganizationAttribute();


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