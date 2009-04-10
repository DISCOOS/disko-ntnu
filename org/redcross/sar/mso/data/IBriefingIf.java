package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.DuplicateIdException;

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

    public IAttributeIf.IMsoBooleanIf getActiveAttribute();

    public void setChannel1(String aChannel1);

    public String getChannel1();

    public IMsoModelIf.ModificationState getChannel1State();

    public IAttributeIf.IMsoStringIf getChannel1Attribute();

    public void setChannel2(String aChannel2);

    public String getChannel2();

    public IMsoModelIf.ModificationState getChannel2State();

    public IAttributeIf.IMsoStringIf getChannel2Attribute();

    public void setClosure(Calendar aClosure);

    public Calendar getClosure();

    public IMsoModelIf.ModificationState getClosureState();

    public IAttributeIf.IMsoCalendarIf getClosureAttribute();

    public void setCommsProcedure(String aCommsProcedure);

    public String getCommsProcedure();

    public IMsoModelIf.ModificationState getCommsProcedureState();

    public IAttributeIf.IMsoStringIf getCommsProcedureAttribute();

    public void setFindingsProcedure(String aFindingsProcedure);

    public String getFindingsProcedure();

    public IMsoModelIf.ModificationState getFindingsProcedureState();

    public IAttributeIf.IMsoStringIf getFindingsProcedureAttribute();

    public void setImportantClues(String aImportantClues);

    public String getImportantClues();

    public IMsoModelIf.ModificationState getImportantCluesState();

    public IAttributeIf.IMsoStringIf getImportantCluesAttribute();

    public void setMediaProcedure(String aMediaProcedure);

    public String getMediaProcedure();

    public IMsoModelIf.ModificationState getMediaProcedureState();

    public IAttributeIf.IMsoStringIf getMediaProcedureAttribute();

    public void setOther(String aOther);

    public String getOther();

    public IMsoModelIf.ModificationState getOtherState();

    public IAttributeIf.IMsoStringIf getOtherAttribute();

    public void setOthers(String aOthers);

    public String getOthers();

    public IMsoModelIf.ModificationState getOthersState();

    public IAttributeIf.IMsoStringIf getOthersAttribute();

    public void setOverallStrategy(String aOverallStrategy);

    public String getOverallStrategy();

    public IMsoModelIf.ModificationState getOverallStrategyState();

    public IAttributeIf.IMsoStringIf getOverallStrategyAttribute();

    public void setRepeaters(String aRepeaters);

    public String getRepeaters();

    public IMsoModelIf.ModificationState getRepeatersState();

    public IAttributeIf.IMsoStringIf getRepeatersAttribute();

    public void setSupplies(String aSupplies);

    public String getSupplies();

    public IMsoModelIf.ModificationState getSuppliesState();

    public IAttributeIf.IMsoStringIf getSuppliesAttribute();

    public void setTelephones(String aTelephones);

    public String getTelephones();

    public IMsoModelIf.ModificationState getTelephonesState();

    public IAttributeIf.IMsoStringIf getTelephonesAttribute();

    public void setTransportProcedure(String aTransportProcedure);

    public String getTransportProcedure();

    public IMsoModelIf.ModificationState getTransportProcedureState();

    public IAttributeIf.IMsoStringIf getTransportProcedureAttribute();

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