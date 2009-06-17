package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.IllegalOperationException;

import java.util.Calendar;

/**
 * Subject search assignment
 */
public class SearchImpl extends AssignmentImpl implements ISearchIf
{
    private final AttributeImpl.MsoInteger m_plannedAccuracy = new AttributeImpl.MsoInteger(this, "PlannedAccuracy",2);
    private final AttributeImpl.MsoInteger m_plannedCoverage = new AttributeImpl.MsoInteger(this, "PlannedCoverage");
    private final AttributeImpl.MsoInteger m_plannedPersonnel = new AttributeImpl.MsoInteger(this, "PlannedPersonnel",3);
    private final AttributeImpl.MsoInteger m_plannedProgress = new AttributeImpl.MsoInteger(this, "PlannedProgress");
    private final AttributeImpl.MsoString m_plannedSearchMethod = new AttributeImpl.MsoString(this, "PlannedSearchMethod");
    private final AttributeImpl.MsoInteger m_reportedAccuracy = new AttributeImpl.MsoInteger(this, "ReportedAccuracy");
    private final AttributeImpl.MsoInteger m_reportedCoverage = new AttributeImpl.MsoInteger(this, "ReportedCoverage");
    private final AttributeImpl.MsoInteger m_reportedPersonnel = new AttributeImpl.MsoInteger(this, "ReportedPersonnel");
    private final AttributeImpl.MsoInteger m_reportedProgress = new AttributeImpl.MsoInteger(this, "ReportedProgress");
    private final AttributeImpl.MsoInteger m_reportedSearchMethod = new AttributeImpl.MsoInteger(this, "ReportedSearchMethod");
    private final AttributeImpl.MsoCalendar m_start = new AttributeImpl.MsoCalendar(this, "Start");
    private final AttributeImpl.MsoCalendar m_stop = new AttributeImpl.MsoCalendar(this, "Stop");

    private final AttributeImpl.MsoEnum<SearchSubType> m_subType = new AttributeImpl.MsoEnum<SearchSubType>(this,"SubType", 1, SearchSubType.PATROL);

    public static String getSubTypeText(ISearchIf.SearchSubType aType)
    {
        return getText("SearchSubType." + aType.name() + ".text");
    }

    public SearchImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(theMsoModel,anObjectId, aNumber);
    }

    public SearchImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber, SearchSubType aSubType)
    {
        super(theMsoModel, anObjectId, aNumber);
        m_subType.setValue(aSubType);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_plannedAccuracy);
        addAttribute(m_plannedCoverage);
        addAttribute(m_plannedPersonnel);
        addAttribute(m_plannedProgress);
        addAttribute(m_plannedSearchMethod);
        addAttribute(m_reportedAccuracy);
        addAttribute(m_reportedCoverage);
        addAttribute(m_reportedPersonnel);
        addAttribute(m_reportedProgress);
        addAttribute(m_reportedSearchMethod);
        addAttribute(m_start);
        addAttribute(m_stop);
        addAttribute(m_subType);
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
    }

    @Override
    protected AssignmentType getTypeBySubclass()
    {
        return AssignmentType.SEARCH;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    @SuppressWarnings("unchecked")
	public void setSubType(SearchSubType aSubType)
    {
    	// update number?
        if(m_mainList!=null) {
        	setNumber(m_mainList.makeSerialNumber(aSubType));
        }
        m_subType.setValue(aSubType);
    }

    @SuppressWarnings("unchecked")
	public void setSubType(String aSubType)
    {
    	// update number?
        if(m_mainList!=null) {
        	setNumber(m_mainList.makeSerialNumber(SearchSubType.valueOf(aSubType)));
        }
        m_subType.setValue(aSubType);
    }

    public SearchSubType getSubType()
    {
        return m_subType.getValue();
    }

    public String getInternationalSubTypeName()
    {
        return m_subType.getInternationalName();

    }
    public IData.DataOrigin getSubTypeState()
    {
        return m_subType.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<SearchSubType> getSubTypeAttribute()
    {
        return m_subType;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setPlannedAccuracy(int aPlannedAccuracy)
    {
        m_plannedAccuracy.setValue(aPlannedAccuracy);
    }

    public int getPlannedAccuracy()
    {
        return m_plannedAccuracy.intValue();
    }

    public IData.DataOrigin getPlannedAccuracyState()
    {
        return m_plannedAccuracy.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getPlannedAccuracyAttribute()
    {
        return m_plannedAccuracy;
    }

    public void setPlannedCoverage(int aPlannedCoverage)
    {
        m_plannedCoverage.setValue(aPlannedCoverage);
    }

    public int getPlannedCoverage()
    {
        return m_plannedCoverage.intValue();
    }

    public IData.DataOrigin getPlannedCoverageState()
    {
        return m_plannedCoverage.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getPlannedCoverageAttribute()
    {
        return m_plannedCoverage;
    }

    public void setPlannedPersonnel(int aPlannedPersonnel)
    {
        m_plannedPersonnel.setValue(aPlannedPersonnel);
    }

    public int getPlannedPersonnel()
    {
        return m_plannedPersonnel.intValue();
    }

    public IData.DataOrigin getPlannedPersonnelState()
    {
        return m_plannedPersonnel.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getPlannedPersonnelAttribute()
    {
        return m_plannedPersonnel;
    }

    public void setPlannedProgress(int aPlannedProgress)
    {
        m_plannedProgress.setValue(aPlannedProgress);
    }

    public int getPlannedProgress()
    {
        return m_plannedProgress.intValue();
    }

    public IData.DataOrigin getPlannedProgressState()
    {
        return m_plannedProgress.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getPlannedProgressAttribute()
    {
        return m_plannedProgress;
    }

    public void setPlannedSearchMethod(String aPlannedSearchMethod)
    {
        m_plannedSearchMethod.setValue(aPlannedSearchMethod);
    }

    public String getPlannedSearchMethod()
    {
        return m_plannedSearchMethod.getString();
    }

    public IData.DataOrigin getPlannedSearchMethodState()
    {
        return m_plannedSearchMethod.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getPlannedSearchMethodAttribute()
    {
        return m_plannedSearchMethod;
    }

    public void setReportedAccuracy(int aReportedAccuracy)
    {
        m_reportedAccuracy.setValue(aReportedAccuracy);
    }

    public int getReportedAccuracy()
    {
        return m_reportedAccuracy.intValue();
    }

    public IData.DataOrigin getReportedAccuracyState()
    {
        return m_reportedAccuracy.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getReportedAccuracyAttribute()
    {
        return m_reportedAccuracy;
    }

    public void setReportedCoverage(int aReportedCoverage)
    {
        m_reportedCoverage.setValue(aReportedCoverage);
    }

    public int getReportedCoverage()
    {
        return m_reportedCoverage.intValue();
    }

    public IData.DataOrigin getReportedCoverageState()
    {
        return m_reportedCoverage.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getReportedCoverageAttribute()
    {
        return m_reportedCoverage;
    }

    public void setReportedPersonnel(int aReportedPersonnel)
    {
        m_reportedPersonnel.setValue(aReportedPersonnel);
    }

    public int getReportedPersonnel()
    {
        return m_reportedPersonnel.intValue();
    }

    public IData.DataOrigin getReportedPersonnelState()
    {
        return m_reportedPersonnel.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getReportedPersonnelAttribute()
    {
        return m_reportedPersonnel;
    }

    public void setReportedProgress(int aReportedProgress)
    {
        m_reportedProgress.setValue(aReportedProgress);
    }

    public int getReportedProgress()
    {
        return m_reportedProgress.intValue();
    }

    public IData.DataOrigin getReportedProgressState()
    {
        return m_reportedProgress.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getReportedProgressAttribute()
    {
        return m_reportedProgress;
    }

    public void setReportedSearchMethod(int aReportedSearchMethod)
    {
        m_reportedSearchMethod.setValue(aReportedSearchMethod);
    }

    public int getReportedSearchMethod()
    {
        return m_reportedSearchMethod.intValue();
    }

    public IData.DataOrigin getReportedSearchMethodState()
    {
        return m_reportedSearchMethod.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getReportedSearchMethodAttribute()
    {
        return m_reportedSearchMethod;
    }

    public void setStart(Calendar aStart)
    {
        m_start.setValue(aStart);
    }

    public Calendar getStart()
    {
        return m_start.getCalendar();
    }

    public IData.DataOrigin getStartState()
    {
        return m_start.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getStartAttribute()
    {
        return m_start;
    }

    public void setStop(Calendar aStop)
    {
        m_stop.setValue(aStop);
    }

    public Calendar getStop()
    {
        return m_stop.getCalendar();
    }

    public IData.DataOrigin getStopState()
    {
        return m_stop.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getStopAttribute()
    {
        return m_stop;
    }

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public IAreaIf getPlannedSearchArea()
    {
        return getPlannedArea();
    }

    public IAreaIf getReportedSearchArea()
    {
        return getReportedArea();
    }

    public void setPlannedSearchArea(IAreaIf anArea) throws IllegalOperationException
    {
        setPlannedArea(anArea);
    }

    public void setReportedSearchArea(IAreaIf anArea) throws IllegalOperationException
    {
        setReportedArea(anArea);
    }

    @Override
    public String getDefaultName()
    {
        return (m_subType.getAttrValue()==null ?
        		getInternationalTypeName() : getInternationalSubTypeName()) + " " + getNumber();
    }



}
