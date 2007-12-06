package org.redcross.sar.mso.data;

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

    private final AttributeImpl.MsoEnum<SearchSubType> m_subType = new AttributeImpl.MsoEnum<SearchSubType>(this,"SubType", SearchSubType.PATROL);

    public static String getSubTypeText(ISearchIf.SearchSubType aType)
    {
        return getText("SearchSubType." + aType.name() + ".text");
    }

    public SearchImpl(IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(anObjectId, aNumber);
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
    protected void defineReferences()
    {
        super.defineReferences();
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

    @Override
    protected AssignmentType getTypeBySubclass()
    {
        return AssignmentType.SEARCH;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setSubType(SearchSubType aSubType)
    {
        m_subType.setValue(aSubType);
    }

    public void setSubType(String aSubType)
    {
        m_subType.setValue(aSubType);
    }

    public SearchSubType getSubType()
    {
        return m_subType.getValue();
    }

    public String getSubTypeText()
    {
        return m_subType.getInternationalName();

    }
    public IMsoModelIf.ModificationState getSubTypeState()
    {
        return m_subType.getState();
    }

    public IAttributeIf.IMsoEnumIf<SearchSubType> getSubTypeAttribute()
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

    public IMsoModelIf.ModificationState getPlannedAccuracyState()
    {
        return m_plannedAccuracy.getState();
    }

    public IAttributeIf.IMsoIntegerIf getPlannedAccuracyAttribute()
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

    public IMsoModelIf.ModificationState getPlannedCoverageState()
    {
        return m_plannedCoverage.getState();
    }

    public IAttributeIf.IMsoIntegerIf getPlannedCoverageAttribute()
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

    public IMsoModelIf.ModificationState getPlannedPersonnelState()
    {
        return m_plannedPersonnel.getState();
    }

    public IAttributeIf.IMsoIntegerIf getPlannedPersonnelAttribute()
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

    public IMsoModelIf.ModificationState getPlannedProgressState()
    {
        return m_plannedProgress.getState();
    }

    public IAttributeIf.IMsoIntegerIf getPlannedProgressAttribute()
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

    public IMsoModelIf.ModificationState getPlannedSearchMethodState()
    {
        return m_plannedSearchMethod.getState();
    }

    public IAttributeIf.IMsoStringIf getPlannedSearchMethodAttribute()
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

    public IMsoModelIf.ModificationState getReportedAccuracyState()
    {
        return m_reportedAccuracy.getState();
    }

    public IAttributeIf.IMsoIntegerIf getReportedAccuracyAttribute()
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

    public IMsoModelIf.ModificationState getReportedCoverageState()
    {
        return m_reportedCoverage.getState();
    }

    public IAttributeIf.IMsoIntegerIf getReportedCoverageAttribute()
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

    public IMsoModelIf.ModificationState getReportedPersonnelState()
    {
        return m_reportedPersonnel.getState();
    }

    public IAttributeIf.IMsoIntegerIf getReportedPersonnelAttribute()
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

    public IMsoModelIf.ModificationState getReportedProgressState()
    {
        return m_reportedProgress.getState();
    }

    public IAttributeIf.IMsoIntegerIf getReportedProgressAttribute()
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

    public IMsoModelIf.ModificationState getReportedSearchMethodState()
    {
        return m_reportedSearchMethod.getState();
    }

    public IAttributeIf.IMsoIntegerIf getReportedSearchMethodAttribute()
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

    public IMsoModelIf.ModificationState getStartState()
    {
        return m_start.getState();
    }

    public IAttributeIf.IMsoCalendarIf getStartAttribute()
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

    public IMsoModelIf.ModificationState getStopState()
    {
        return m_stop.getState();
    }

    public IAttributeIf.IMsoCalendarIf getStopAttribute()
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
    public int getTypenr()
    {
        return 2;
    }

    @Override
    public String getTypeText()
    {
        return getSubTypeText(getSubType());
    }



}
