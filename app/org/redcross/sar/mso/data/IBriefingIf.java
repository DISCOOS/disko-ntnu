package org.redcross.sar.mso.data;

import java.util.Calendar;
import java.util.Collection;

import org.redcross.sar.data.IData;

public interface IBriefingIf extends IMsoObjectIf
{
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setActive(boolean anActive);

    public boolean isActive();

    public IData.DataOrigin getActiveState();

    public IMsoAttributeIf.IMsoBooleanIf getActiveAttribute();

    public void setChannel1(String aChannel1);

    public String getChannel1();

    public IData.DataOrigin getChannel1State();

    public IMsoAttributeIf.IMsoStringIf getChannel1Attribute();

    public void setChannel2(String aChannel2);

    public String getChannel2();

    public IData.DataOrigin getChannel2State();

    public IMsoAttributeIf.IMsoStringIf getChannel2Attribute();

    public void setClosure(Calendar aClosure);

    public Calendar getClosure();

    public IData.DataOrigin getClosureState();

    public IMsoAttributeIf.IMsoCalendarIf getClosureAttribute();

    public void setCommsProcedure(String aCommsProcedure);

    public String getCommsProcedure();

    public IData.DataOrigin getCommsProcedureState();

    public IMsoAttributeIf.IMsoStringIf getCommsProcedureAttribute();

    public void setFindingsProcedure(String aFindingsProcedure);

    public String getFindingsProcedure();

    public IData.DataOrigin getFindingsProcedureState();

    public IMsoAttributeIf.IMsoStringIf getFindingsProcedureAttribute();

    public void setImportantClues(String aImportantClues);

    public String getImportantClues();

    public IData.DataOrigin getImportantCluesState();

    public IMsoAttributeIf.IMsoStringIf getImportantCluesAttribute();

    public void setMediaProcedure(String aMediaProcedure);

    public String getMediaProcedure();

    public IData.DataOrigin getMediaProcedureState();

    public IMsoAttributeIf.IMsoStringIf getMediaProcedureAttribute();

    public void setOther(String aOther);

    public String getOther();

    public IData.DataOrigin getOtherState();

    public IMsoAttributeIf.IMsoStringIf getOtherAttribute();

    public void setOthers(String aOthers);

    public String getOthers();

    public IData.DataOrigin getOthersState();

    public IMsoAttributeIf.IMsoStringIf getOthersAttribute();

    public void setOverallStrategy(String aOverallStrategy);

    public String getOverallStrategy();

    public IData.DataOrigin getOverallStrategyState();

    public IMsoAttributeIf.IMsoStringIf getOverallStrategyAttribute();

    public void setRepeaters(String aRepeaters);

    public String getRepeaters();

    public IData.DataOrigin getRepeatersState();

    public IMsoAttributeIf.IMsoStringIf getRepeatersAttribute();

    public void setSupplies(String aSupplies);

    public String getSupplies();

    public IData.DataOrigin getSuppliesState();

    public IMsoAttributeIf.IMsoStringIf getSuppliesAttribute();

    public void setTelephones(String aTelephones);

    public String getTelephones();

    public IData.DataOrigin getTelephonesState();

    public IMsoAttributeIf.IMsoStringIf getTelephonesAttribute();

    public void setTransportProcedure(String aTransportProcedure);

    public String getTransportProcedure();

    public IData.DataOrigin getTransportProcedureState();

    public IMsoAttributeIf.IMsoStringIf getTransportProcedureAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addBriefingForecast(IForecastIf anIForecastIf);

    public IForecastListIf getBriefingForecasts();

    public IData.DataOrigin getBriefingForecastsState(IForecastIf anIForecastIf);

    public Collection<IForecastIf> getBriefingForecastsItems();

    public void addBriefingEnvironment(IEnvironmentIf anIEnvironmentIf);

    public IEnvironmentListIf getBriefingEnvironments();

    public IData.DataOrigin getBriefingEnvironmentsState(IEnvironmentIf anIEnvironmentIf);

    public Collection<IEnvironmentIf> getBriefingEnvironmentsItems();

    public void addBriefingSubject(ISubjectIf anISubjectIf);

    public ISubjectListIf getBriefingSubjects();

    public IData.DataOrigin getBriefingSubjectsState(ISubjectIf anISubjectIf);

    public Collection<ISubjectIf> getBriefingSubjectsItems();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setBriefingHypothesis(IHypothesisIf aHypothesis);

    public IHypothesisIf getBriefingHypothesis();

    public IData.DataOrigin getBriefingHypothesisState();

    public IMsoRelationIf<IHypothesisIf> getBriefingHypothesisAttribute();

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public void Report();


}