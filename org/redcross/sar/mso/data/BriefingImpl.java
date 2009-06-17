package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;

import java.util.Calendar;
import java.util.Collection;

public class BriefingImpl extends AbstractMsoObject implements IBriefingIf
{
    private final ForecastListImpl m_briefingForecasts = new ForecastListImpl(this, "BriefingForecasts", false);
    private final EnvironmentListImpl m_briefingEnvironments = new EnvironmentListImpl(this, "BriefingEnvironments", false);
    private final SubjectListImpl m_briefingSubjects = new SubjectListImpl(this, "BriefingSubjects", false);

    private final MsoRelationImpl<IHypothesisIf> m_briefingHypothesis = new MsoRelationImpl<IHypothesisIf>(this, "BriefingHypothesis", 0, true, null);

    private final AttributeImpl.MsoBoolean m_active = new AttributeImpl.MsoBoolean(this, "Active");
    private final AttributeImpl.MsoString m_channel1 = new AttributeImpl.MsoString(this, "Channel1");
    private final AttributeImpl.MsoString m_channel2 = new AttributeImpl.MsoString(this, "Channel2");
    private final AttributeImpl.MsoCalendar m_closure = new AttributeImpl.MsoCalendar(this, "Closure");
    private final AttributeImpl.MsoString m_commsProcedure = new AttributeImpl.MsoString(this, "CommsProcedure");
    private final AttributeImpl.MsoString m_findingsProcedure = new AttributeImpl.MsoString(this, "FindingsProcedure");
    private final AttributeImpl.MsoString m_importantClues = new AttributeImpl.MsoString(this, "ImportantClues");
    private final AttributeImpl.MsoString m_mediaProcedure = new AttributeImpl.MsoString(this, "MediaProcedure");
    private final AttributeImpl.MsoString m_other = new AttributeImpl.MsoString(this, "Other");
    private final AttributeImpl.MsoString m_others = new AttributeImpl.MsoString(this, "Others");
    private final AttributeImpl.MsoString m_overallStrategy = new AttributeImpl.MsoString(this, "OverallStrategy");
    private final AttributeImpl.MsoString m_repeaters = new AttributeImpl.MsoString(this, "Repeaters");
    private final AttributeImpl.MsoString m_supplies = new AttributeImpl.MsoString(this, "Supplies");
    private final AttributeImpl.MsoString m_telephones = new AttributeImpl.MsoString(this, "Telephones");
    private final AttributeImpl.MsoString m_transportProcedure = new AttributeImpl.MsoString(this, "TransportProcedure");

    public BriefingImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    protected void defineAttributes()
    {
        addAttribute(m_active);
        addAttribute(m_channel1);
        addAttribute(m_channel2);
        addAttribute(m_closure);
        addAttribute(m_commsProcedure);
        addAttribute(m_findingsProcedure);
        addAttribute(m_importantClues);
        addAttribute(m_mediaProcedure);
        addAttribute(m_other);
        addAttribute(m_others);
        addAttribute(m_overallStrategy);
        addAttribute(m_repeaters);
        addAttribute(m_supplies);
        addAttribute(m_telephones);
        addAttribute(m_transportProcedure);
    }

    protected void defineLists()
    {
        addList(m_briefingForecasts);
        addList(m_briefingEnvironments);
        addList(m_briefingSubjects);
    }

    protected void defineObjects()
    {
        addObject(m_briefingHypothesis);
    }

    public void addListRelation(IMsoObjectIf anObject, String aReferenceListName)
    {
        if (anObject instanceof IForecastIf)
        {
            m_briefingForecasts.add((IForecastIf) anObject);
        }
        if (anObject instanceof IEnvironmentIf)
        {
            m_briefingEnvironments.add((IEnvironmentIf) anObject);
        }
        if (anObject instanceof ISubjectIf)
        {
            m_briefingSubjects.add((ISubjectIf) anObject);
        }
    }

    public void removeListRelation(IMsoObjectIf anObject, String aReferenceListName)
    {
        if (anObject instanceof IForecastIf)
        {
            m_briefingForecasts.remove((IForecastIf) anObject);
        }
        if (anObject instanceof IEquipmentIf)
        {
            m_briefingEnvironments.remove((IEnvironmentIf) anObject);
        }
        if (anObject instanceof ISubjectIf)
        {
            m_briefingSubjects.remove((ISubjectIf) anObject);
        }
    }

    public static BriefingImpl implementationOf(IBriefingIf anInterface) throws MsoCastException
    {
        try
        {
            return (BriefingImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to BriefingImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_BRIEFING;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setActive(boolean anActive)
    {
        m_active.setValue(anActive);
    }

    public boolean isActive()
    {
        return m_active.booleanValue();
    }

    public IData.DataOrigin getActiveState()
    {
        return m_active.getOrigin();
    }

    public IMsoAttributeIf.IMsoBooleanIf getActiveAttribute()
    {
        return m_active;
    }

    public void setChannel1(String aChannel1)
    {
        m_channel1.setValue(aChannel1);
    }

    public String getChannel1()
    {
        return m_channel1.getString();
    }

    public IData.DataOrigin getChannel1State()
    {
        return m_channel1.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getChannel1Attribute()
    {
        return m_channel1;
    }

    public void setChannel2(String aChannel2)
    {
        m_channel2.setValue(aChannel2);
    }

    public String getChannel2()
    {
        return m_channel2.getString();
    }

    public IData.DataOrigin getChannel2State()
    {
        return m_channel2.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getChannel2Attribute()
    {
        return m_channel2;
    }

    public void setClosure(Calendar aClosure)
    {
        m_closure.setValue(aClosure);
    }

    public Calendar getClosure()
    {
        return m_closure.getCalendar();
    }

    public IData.DataOrigin getClosureState()
    {
        return m_closure.getOrigin();
    }

    public IMsoAttributeIf.IMsoCalendarIf getClosureAttribute()
    {
        return m_closure;
    }

    public void setCommsProcedure(String aCommsProcedure)
    {
        m_commsProcedure.setValue(aCommsProcedure);
    }

    public String getCommsProcedure()
    {
        return m_commsProcedure.getString();
    }

    public IData.DataOrigin getCommsProcedureState()
    {
        return m_commsProcedure.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getCommsProcedureAttribute()
    {
        return m_commsProcedure;
    }

    public void setFindingsProcedure(String aFindingsProcedure)
    {
        m_findingsProcedure.setValue(aFindingsProcedure);
    }

    public String getFindingsProcedure()
    {
        return m_findingsProcedure.getString();
    }

    public IData.DataOrigin getFindingsProcedureState()
    {
        return m_findingsProcedure.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getFindingsProcedureAttribute()
    {
        return m_findingsProcedure;
    }

    public void setImportantClues(String aImportantClues)
    {
        m_importantClues.setValue(aImportantClues);
    }

    public String getImportantClues()
    {
        return m_importantClues.getString();
    }

    public IData.DataOrigin getImportantCluesState()
    {
        return m_importantClues.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getImportantCluesAttribute()
    {
        return m_importantClues;
    }

    public void setMediaProcedure(String aMediaProcedure)
    {
        m_mediaProcedure.setValue(aMediaProcedure);
    }

    public String getMediaProcedure()
    {
        return m_mediaProcedure.getString();
    }

    public IData.DataOrigin getMediaProcedureState()
    {
        return m_mediaProcedure.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getMediaProcedureAttribute()
    {
        return m_mediaProcedure;
    }

    public void setOther(String aOther)
    {
        m_other.setValue(aOther);
    }

    public String getOther()
    {
        return m_other.getString();
    }

    public IData.DataOrigin getOtherState()
    {
        return m_other.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getOtherAttribute()
    {
        return m_other;
    }

    public void setOthers(String aOthers)
    {
        m_others.setValue(aOthers);
    }

    public String getOthers()
    {
        return m_others.getString();
    }

    public IData.DataOrigin getOthersState()
    {
        return m_others.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getOthersAttribute()
    {
        return m_others;
    }

    public void setOverallStrategy(String aOverallStrategy)
    {
        m_overallStrategy.setValue(aOverallStrategy);
    }

    public String getOverallStrategy()
    {
        return m_overallStrategy.getString();
    }

    public IData.DataOrigin getOverallStrategyState()
    {
        return m_overallStrategy.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getOverallStrategyAttribute()
    {
        return m_overallStrategy;
    }

    public void setRepeaters(String aRepeaters)
    {
        m_repeaters.setValue(aRepeaters);
    }

    public String getRepeaters()
    {
        return m_repeaters.getString();
    }

    public IData.DataOrigin getRepeatersState()
    {
        return m_repeaters.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getRepeatersAttribute()
    {
        return m_repeaters;
    }

    public void setSupplies(String aSupplies)
    {
        m_supplies.setValue(aSupplies);
    }

    public String getSupplies()
    {
        return m_supplies.getString();
    }

    public IData.DataOrigin getSuppliesState()
    {
        return m_supplies.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getSuppliesAttribute()
    {
        return m_supplies;
    }

    public void setTelephones(String aTelephones)
    {
        m_telephones.setValue(aTelephones);
    }

    public String getTelephones()
    {
        return m_telephones.getString();
    }

    public IData.DataOrigin getTelephonesState()
    {
        return m_telephones.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getTelephonesAttribute()
    {
        return m_telephones;
    }

    public void setTransportProcedure(String aTransportProcedure)
    {
        m_transportProcedure.setValue(aTransportProcedure);
    }

    public String getTransportProcedure()
    {
        return m_transportProcedure.getString();
    }

    public IData.DataOrigin getTransportProcedureState()
    {
        return m_transportProcedure.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getTransportProcedureAttribute()
    {
        return m_transportProcedure;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addBriefingForecast(IForecastIf anIForecastIf)
    {
        m_briefingForecasts.add(anIForecastIf);
    }

    public IForecastListIf getBriefingForecasts()
    {
        return m_briefingForecasts;
    }

    public IData.DataOrigin getBriefingForecastsState(IForecastIf anIForecastIf)
    {
        return m_briefingForecasts.getOrigin(anIForecastIf);
    }

    public Collection<IForecastIf> getBriefingForecastsItems()
    {
        return m_briefingForecasts.getObjects();
    }

    public void addBriefingEnvironment(IEnvironmentIf anIEnvironmentIf)
    {
        m_briefingEnvironments.add(anIEnvironmentIf);
    }

    public IEnvironmentListIf getBriefingEnvironments()
    {
        return m_briefingEnvironments;
    }

    public IData.DataOrigin getBriefingEnvironmentsState(IEnvironmentIf anIEnvironmentIf)
    {
        return m_briefingEnvironments.getOrigin(anIEnvironmentIf);
    }

    public Collection<IEnvironmentIf> getBriefingEnvironmentsItems()
    {
        return m_briefingEnvironments.getObjects();
    }

    public void addBriefingSubject(ISubjectIf anISubjectIf)
    {
        m_briefingSubjects.add(anISubjectIf);
    }

    public ISubjectListIf getBriefingSubjects()
    {
        return m_briefingSubjects;
    }

    public IData.DataOrigin getBriefingSubjectsState(ISubjectIf anISubjectIf)
    {
        return m_briefingSubjects.getOrigin(anISubjectIf);
    }

    public Collection<ISubjectIf> getBriefingSubjectsItems()
    {
        return m_briefingSubjects.getObjects();
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setBriefingHypothesis(IHypothesisIf aHypothesis)
    {
        m_briefingHypothesis.set(aHypothesis);
    }

    public IHypothesisIf getBriefingHypothesis()
    {
        return m_briefingHypothesis.get();
    }

    public IData.DataOrigin getBriefingHypothesisState()
    {
        return m_briefingHypothesis.getOrigin();
    }

    public IMsoRelationIf<IHypothesisIf> getBriefingHypothesisAttribute()
    {
        return m_briefingHypothesis;
    }

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public void Report()
    {
    }


}