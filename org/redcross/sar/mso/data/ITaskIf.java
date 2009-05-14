package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;

import java.util.Calendar;
import java.util.Comparator;

public interface ITaskIf extends ITimeItemIf, ISerialNumberedIf, IEnumStatusHolder<ITaskIf.TaskStatus>, IEnumPriorityHolder<ITaskIf.TaskPriority>
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.Task";

    public enum TaskStatus
    {
        UNPROCESSED,
        STARTED,
        POSTPONED,
        FINISHED,
        DELETED
    }

    public enum TaskPriority
    {
        HIGH,
        NORMAL,
        LOW,
        NONE
    }

    public enum TaskType
    {
        TRANSPORT,
        RESOURCE,
        INTELLIGENCE,
        GENERAL
    }

    public static final Comparator<ITaskIf> PRIORITY_COMPARATOR = new Comparator<ITaskIf>()
    {
        public int compare(ITaskIf o1, ITaskIf o2)
        {
            return o1.comparePriorityTo(o2);
        }
    };


    public void setDescription(String aDescription);

    public String getDescription();

    public IMsoModelIf.ModificationState getDescriptionState();

    public IMsoAttributeIf.IMsoStringIf getDescriptionAttribute();

    public void setProgress(int aProgress);

    public int getProgress();

    public IMsoModelIf.ModificationState getProgressState();

    public IMsoAttributeIf.IMsoIntegerIf getProgressAttribute();

    public void setResponsibleRole(String aResponsibleRole);

    public String getResponsibleRole();

    public IMsoModelIf.ModificationState getResponsibleRoleState();

    public IMsoAttributeIf.IMsoStringIf getResponsibleRoleAttribute();

    public void setTaskText(String aTaskText);

    public String getTaskText();

    public IMsoModelIf.ModificationState getTaskTextState();

    public IMsoAttributeIf.IMsoStringIf getTaskTextAttribute();

    public void setAlert(Calendar aAlert);

    public Calendar getAlert();

    public IMsoModelIf.ModificationState getAlertState();

    public IMsoAttributeIf.IMsoCalendarIf getAlertAttribute();

    public void setCreated(Calendar aCreated);

    public Calendar getCreated();

    public IMsoModelIf.ModificationState getCreatedState();

    public IMsoAttributeIf.IMsoCalendarIf getCreatedAttribute();

    public void setCreatingWorkProcess(String aCreatingWorkProcess);

    public String getCreatingWorkProcess();

    public IMsoModelIf.ModificationState getCreatingWorkProcessState();

    public IMsoAttributeIf.IMsoStringIf getCreatingWorkProcessAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setCreatedEvent(IEventIf aEvent);

    public IEventIf getCreatedEvent();

    public IMsoModelIf.ModificationState getCreatedEventState();

    public IMsoReferenceIf<IEventIf> getCreatedEventAttribute();

    public void setDependentObject(IMsoObjectIf anAbstractMsoObject);

    public IMsoObjectIf getDependentObject();

    public IMsoModelIf.ModificationState getDependentObjectState();

    public IMsoReferenceIf<IMsoObjectIf> getDependentObjectAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for enums
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(TaskStatus aStatus);

    public void setType(TaskType aType);

    public void setType(String aType);

    public TaskType getType();

    public IMsoModelIf.ModificationState getTypeState();

    public IMsoAttributeIf.IMsoEnumIf<TaskType> getTypeAttribute();

    public String getTypeName();
    
    public String getInternationalTypeName();

    public void setSourceClass(IMsoManagerIf.MsoClassCode aSourceClass);

    public void setSourceClass(String aSourceClass);

    public IMsoManagerIf.MsoClassCode getSourceClass();

    public IMsoModelIf.ModificationState getSourceClassState();

    public IMsoAttributeIf.IMsoEnumIf<IMsoManagerIf.MsoClassCode> getSourceClassAttribute();

    public String getSourceClassText();

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public String getDefaultName();
    
    public Calendar getDueTime();

    public void setDueTime(Calendar aCalendar);

    public IMsoModelIf.ModificationState getDueTimeState();

}