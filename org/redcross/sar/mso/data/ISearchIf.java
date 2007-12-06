package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.IllegalOperationException;

import java.util.Calendar;

/**
 *
 */
public interface ISearchIf extends IAssignmentIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.Search";

    public enum SearchSubType
    {
        PATROL,
        URBAN,
        LINE,
        SHORELINE,
        MARINE,
        AIR,
        DOG
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setSubType(SearchSubType aSubType);

    public void setSubType(String aSubType);

    public SearchSubType getSubType();

    public String getSubTypeText();

    public IMsoModelIf.ModificationState getSubTypeState();

    public IAttributeIf.IMsoEnumIf<SearchSubType> getSubTypeAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setPlannedAccuracy(int aPlannedAccuracy);

    public int getPlannedAccuracy();

    public IMsoModelIf.ModificationState getPlannedAccuracyState();

    public IAttributeIf.IMsoIntegerIf getPlannedAccuracyAttribute();

    public void setPlannedCoverage(int aPlannedCoverage);

    public int getPlannedCoverage();

    public IMsoModelIf.ModificationState getPlannedCoverageState();

    public IAttributeIf.IMsoIntegerIf getPlannedCoverageAttribute();

    public void setPlannedPersonnel(int aPlannedPersonnel);

    public int getPlannedPersonnel();

    public IMsoModelIf.ModificationState getPlannedPersonnelState();

    public IAttributeIf.IMsoIntegerIf getPlannedPersonnelAttribute();

    public void setPlannedProgress(int aPlannedProgress);

    public int getPlannedProgress();

    public IMsoModelIf.ModificationState getPlannedProgressState();

    public IAttributeIf.IMsoIntegerIf getPlannedProgressAttribute();

    public void setPlannedSearchMethod(String aPlannedSearchMethod);

    public String getPlannedSearchMethod();

    public IMsoModelIf.ModificationState getPlannedSearchMethodState();

    public IAttributeIf.IMsoStringIf getPlannedSearchMethodAttribute();

    public void setReportedAccuracy(int aReportedAccuracy);

    public int getReportedAccuracy();

    public IMsoModelIf.ModificationState getReportedAccuracyState();

    public IAttributeIf.IMsoIntegerIf getReportedAccuracyAttribute();

    public void setReportedCoverage(int aReportedCoverage);

    public int getReportedCoverage();

    public IMsoModelIf.ModificationState getReportedCoverageState();

    public IAttributeIf.IMsoIntegerIf getReportedCoverageAttribute();

    public void setReportedPersonnel(int aReportedPersonnel);

    public int getReportedPersonnel();

    public IMsoModelIf.ModificationState getReportedPersonnelState();

    public IAttributeIf.IMsoIntegerIf getReportedPersonnelAttribute();

    public void setReportedProgress(int aReportedProgress);

    public int getReportedProgress();

    public IMsoModelIf.ModificationState getReportedProgressState();

    public IAttributeIf.IMsoIntegerIf getReportedProgressAttribute();

    public void setReportedSearchMethod(int aReportedSearchMethod);

    public int getReportedSearchMethod();

    public IMsoModelIf.ModificationState getReportedSearchMethodState();

    public IAttributeIf.IMsoIntegerIf getReportedSearchMethodAttribute();

    public void setStart(Calendar aStart);

    public Calendar getStart();

    public IMsoModelIf.ModificationState getStartState();

    public IAttributeIf.IMsoCalendarIf getStartAttribute();

    public void setStop(Calendar aStop);

    public Calendar getStop();

    public IMsoModelIf.ModificationState getStopState();

    public IAttributeIf.IMsoCalendarIf getStopAttribute();

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public IAreaIf getPlannedSearchArea();

    public IAreaIf getReportedSearchArea();

    public void setPlannedSearchArea(IAreaIf anArea) throws IllegalOperationException;

    public void setReportedSearchArea(IAreaIf anArea) throws IllegalOperationException;
}
