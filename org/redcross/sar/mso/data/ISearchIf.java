package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
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

    public String getInternationalSubTypeName();

    public IData.DataOrigin getSubTypeState();

    public IMsoAttributeIf.IMsoEnumIf<SearchSubType> getSubTypeAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setPlannedAccuracy(int aPlannedAccuracy);

    public int getPlannedAccuracy();

    public IData.DataOrigin getPlannedAccuracyState();

    public IMsoAttributeIf.IMsoIntegerIf getPlannedAccuracyAttribute();

    public void setPlannedCoverage(int aPlannedCoverage);

    public int getPlannedCoverage();

    public IData.DataOrigin getPlannedCoverageState();

    public IMsoAttributeIf.IMsoIntegerIf getPlannedCoverageAttribute();

    public void setPlannedPersonnel(int aPlannedPersonnel);

    public int getPlannedPersonnel();

    public IData.DataOrigin getPlannedPersonnelState();

    public IMsoAttributeIf.IMsoIntegerIf getPlannedPersonnelAttribute();

    public void setPlannedProgress(int aPlannedProgress);

    public int getPlannedProgress();

    public IData.DataOrigin getPlannedProgressState();

    public IMsoAttributeIf.IMsoIntegerIf getPlannedProgressAttribute();

    public void setPlannedSearchMethod(String aPlannedSearchMethod);

    public String getPlannedSearchMethod();

    public IData.DataOrigin getPlannedSearchMethodState();

    public IMsoAttributeIf.IMsoStringIf getPlannedSearchMethodAttribute();

    public void setReportedAccuracy(int aReportedAccuracy);

    public int getReportedAccuracy();

    public IData.DataOrigin getReportedAccuracyState();

    public IMsoAttributeIf.IMsoIntegerIf getReportedAccuracyAttribute();

    public void setReportedCoverage(int aReportedCoverage);

    public int getReportedCoverage();

    public IData.DataOrigin getReportedCoverageState();

    public IMsoAttributeIf.IMsoIntegerIf getReportedCoverageAttribute();

    public void setReportedPersonnel(int aReportedPersonnel);

    public int getReportedPersonnel();

    public IData.DataOrigin getReportedPersonnelState();

    public IMsoAttributeIf.IMsoIntegerIf getReportedPersonnelAttribute();

    public void setReportedProgress(int aReportedProgress);

    public int getReportedProgress();

    public IData.DataOrigin getReportedProgressState();

    public IMsoAttributeIf.IMsoIntegerIf getReportedProgressAttribute();

    public void setReportedSearchMethod(int aReportedSearchMethod);

    public int getReportedSearchMethod();

    public IData.DataOrigin getReportedSearchMethodState();

    public IMsoAttributeIf.IMsoIntegerIf getReportedSearchMethodAttribute();

    public void setStart(Calendar aStart);

    public Calendar getStart();

    public IData.DataOrigin getStartState();

    public IMsoAttributeIf.IMsoCalendarIf getStartAttribute();

    public void setStop(Calendar aStop);

    public Calendar getStop();

    public IData.DataOrigin getStopState();

    public IMsoAttributeIf.IMsoCalendarIf getStopAttribute();

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public IAreaIf getPlannedSearchArea();

    public IAreaIf getReportedSearchArea();

    public void setPlannedSearchArea(IAreaIf anArea) throws IllegalOperationException;

    public void setReportedSearchArea(IAreaIf anArea) throws IllegalOperationException;
}
