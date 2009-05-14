package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import java.util.Calendar;
import java.util.Collection;

public interface IBriefingIf extends IMsoObjectIf
{
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setActive(boolean anActive);

    public boolean isActive();

    public IMsoModelIf.ModificationState getActiveState();

    public IMsoAttributeIf.IMsoBooleanIf getActiveAttribute();

    public void setChannel1(String aChannel1);

    public String getChannel1();

    public IMsoModelIf.ModificationState getChannel1State();

    public IMsoAttributeIf.IMsoStringIf getChannel1Attribute();

    public void setChannel2(String aChannel2);

    public String getChannel2();

    public IMsoModelIf.ModificationState getChannel2State();

    public IMsoAttributeIf.IMsoStringIf getChannel2Attribute();

    public void setClosure(Calendar aClosure);

    public Calendar getClosure();

    public IMsoModelIf.ModificationState getClosureState();

    public IMsoAttributeIf.IMsoCalendarIf getClosureAttribute();

    public void setCommsProcedure(String aCommsProcedure);

    public String getCommsProcedure();

    public IMsoModelIf.ModificationState getCommsProcedureState();

    public IMsoAttributeIf.IMsoStringIf getCommsProcedureAttribute();

    public void setFindingsProcedure(String aFindingsProcedure);

    public String getFindingsProcedure();

    public IMsoModelIf.ModificationState getFindingsProcedureState();

    public IMsoAttributeIf.IMsoStringIf getFindingsProcedureAttribute();

    public void setImportantClues(String aImportantClues);

    public String getImportantClues();

    public IMsoModelIf.ModificationState getImportantCluesState();

    public IMsoAttributeIf.IMsoStringIf getImportantCluesAttribute();

    public void setMediaProcedure(String aMediaProcedure);

    public String getMediaProcedure();

    public IMsoModelIf.ModificationState getMediaProcedureState();

    public IMsoAttributeIf.IMsoStringIf getMediaProcedureAttribute();

    public void setOther(String aOther);

    public String getOther();

    public IMsoModelIf.ModificationState getOtherState();

    public IMsoAttributeIf.IMsoStringIf getOtherAttribute();

    public void setOthers(String aOthers);

    public String getOthers();

    public IMsoModelIf.ModificationState getOthersState();

    public IMsoAttributeIf.IMsoStringIf getOthersAttribute();

    public void setOverallStrategy(String aOverallStrategy);

    public String getOverallStrategy();

    public IMsoModelIf.ModificationState getOverallStrategyState();

    public IMsoAttributeIf.IMsoStringIf getOverallStrategyAttribute();

    public void setRepeaters(String aRepeaters);

    public String getRepeaters();

    public IMsoModelIf.ModificationState getRepeatersState();

    public IMsoAttributeIf.IMsoStringIf getRepeatersAttribute();

    public void setSupplies(String aSupplies);

    public String getSupplies();

    public IMsoModelIf.ModificationState getSuppliesState();

    public IMsoAttributeIf.IMsoStringIf getSuppliesAttribute();

    public void setTelephones(String aTelephones);

    public String getTelephones();

    public IMsoModelIf.ModificationState getTelephonesState();

    public IMsoAttributeIf.IMsoStringIf getTelephonesAttribute();

    public void setTransportProcedure(String aTransportProcedure);

    public String getTransportProcedure();

    public IMsoModelIf.ModificationState getTransportProcedureState();

    public IMsoAttributeIf.IMsoStringIf getTransportProcedureAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addBriefingForecast(IForecastIf anIForecastIf);

    public IForecastListIf getBriefingForecasts();

    public IMsoModelIf.ModificationState getBriefingForecastsState(IForecastIf anIForecastIf);

    public Collection<IForecastIf> getBriefingForecastsItems();

    public void addBriefingEnvironment(IEnvironmentIf anIEnvironmentIf);

    public IEnvironmentListIf getBriefingEnvironments();

    public IMsoModelIf.ModificationState getBriefingEnvironmentsState(IEnvironmentIf anIEnvironmentIf);

    public Collection<IEnvironmentIf> getBriefingEnvironmentsItems();

    public void addBriefingSubject(ISubjectIf anISubjectIf);

    public ISubjectListIf getBriefingSubjects();

    public IMsoModelIf.ModificationState getBriefingSubjectsState(ISubjectIf anISubjectIf);

    public Collection<ISubjectIf> getBriefingSubjectsItems();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setBriefingHypothesis(IHypothesisIf aHypothesis);

    public IHypothesisIf getBriefingHypothesis();

    public IMsoModelIf.ModificationState getBriefingHypothesisState();

    public IMsoReferenceIf<IHypothesisIf> getBriefingHypothesisAttribute();

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public void Report();


}