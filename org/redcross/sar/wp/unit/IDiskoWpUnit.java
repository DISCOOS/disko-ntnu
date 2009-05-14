package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.wp.IDiskoWp;
import org.redcross.sar.wp.IDiskoWpModule;

public interface IDiskoWpUnit extends IDiskoWpModule, IDiskoWp
{
    public final static String bundleName = "org.redcross.sar.wp.unit.unit";

	/*
	 * View related
	 */
	public static final String PERSONNEL_DETAILS_VIEW_ID = "PERSONNEL.DETAILS.VIEW";
	public static final String PERSONNEL_ADDITIONAL_VIEW_ID = "PERSONNEL.ADDITIONAL.VIEW";
	public static final String UNIT_DETAILS_VIEW_ID = "UNIT.DETAILS.VIEW";
	public static final String CALLOUT_DETAILS_VIEW_ID = "CALLOUT.DETAILS.VIEW";
	public static final String MESSAGE_VIEW_ID = "MESSAGE.VIEW";

	public void setMainTab(int index);
	public void setBottomView(String viewId);
	public boolean setLeftView(String viewId);
    public boolean setPersonnel(IPersonnelIf personnel);
    public boolean setPersonnelLeft(IPersonnelIf personnel);
	public void setPersonnelBottom(IPersonnelIf personnel);
	public boolean setUnit(IUnitIf unit);
	public boolean setCallout(ICalloutIf callout);

	public IPersonnelIf getEditingPersonnel();
	public IUnitIf getEditingUnit();
	public ICalloutIf getEditingCallout();
	
	public boolean isEditValid();
	public boolean isDataValid();
		
}
