package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.wp.IDiskoWp;
import org.redcross.sar.wp.IDiskoWpModule;

public interface IDiskoWpUnit extends IDiskoWpModule, IDiskoWp
{
	/*
	 * View related
	 */
	public static final String PERSONNEL_DETAILS_VIEW_ID = "PERSONNEL_DETAILS_VIEW";
	public static final String PERSONNEL_ADDITIONAL_VIEW_ID = "PERSONNEL_ADDITIONAL_VIEW";
	public static final String UNIT_VIEW_ID = "UNIT_VIEW";
	public static final String CALLOUT_VIEW_ID = "CALLOUT_VIEW";
	public static final String MESSAGE_VIEW_ID = "MESSAGE_VIEW";

    public final static String bundleName = "org.redcross.sar.wp.unit.unit";

    public void setPersonnelLeft(IPersonnelIf personnel);
	public void setPersonnelBottom(IPersonnelIf personnel);
	public void setUnit(IUnitIf unit);
	public void setOverviewPanel(int index);
	public void setLeftView(String viewId);
	public void setBottomView(String viewId);

	/*
	 * Editor objects
	 */
	public IPersonnelIf getEditingPersonnel();
	public IUnitIf getEditingUnit();

	/*
	 * Getters and setters
	 */
	public boolean getNewPersonnel();
	public void setNewPersonnel(boolean newPersonnel);
	public boolean getNewUnit();
	public void setNewUnit(boolean newUnit);
	public boolean getNewCallOut();
	public void setNewCallOut(boolean newCallOut);
	
}
