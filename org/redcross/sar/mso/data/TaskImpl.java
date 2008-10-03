package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoManagerImpl;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.except.MsoCastException;

import java.util.Calendar;

public class TaskImpl extends AbstractTimeItem implements ITaskIf
{
    private final AttributeImpl.MsoString m_description = new AttributeImpl.MsoString(this, "Description");
    private final AttributeImpl.MsoInteger m_number = new AttributeImpl.MsoInteger(this, "Number",true);
    private final AttributeImpl.MsoInteger m_progress = new AttributeImpl.MsoInteger(this, "Progress");
    private final AttributeImpl.MsoString m_responsibleRole = new AttributeImpl.MsoString(this, "ResponsibleRole");
    private final AttributeImpl.MsoString m_taskText = new AttributeImpl.MsoString(this, "TaskText");
    private final AttributeImpl.MsoCalendar m_alert = new AttributeImpl.MsoCalendar(this, "Alert");
    private final AttributeImpl.MsoCalendar m_created = new AttributeImpl.MsoCalendar(this, "Created");
    private final AttributeImpl.MsoString m_creatingWorkProcess = new AttributeImpl.MsoString(this,"CreatingWorkProcess");

    private final AttributeImpl.MsoEnum<TaskStatus> m_status = new AttributeImpl.MsoEnum<TaskStatus>(this, "Status", 1, TaskStatus.UNPROCESSED);
    private final AttributeImpl.MsoEnum<TaskPriority> m_priority = new AttributeImpl.MsoEnum<TaskPriority>(this, "Priority", 1, TaskPriority.LOW);
    private final AttributeImpl.MsoEnum<TaskType> m_type = new AttributeImpl.MsoEnum<TaskType>(this, "Type", 1, TaskType.TRANSPORT);
    private final AttributeImpl.MsoEnum<IMsoManagerIf.MsoClassCode> m_sourceClass = new AttributeImpl.MsoEnum<IMsoManagerIf.MsoClassCode>(this, "SourceClass", 1, IMsoManagerIf.MsoClassCode.CLASSCODE_AREA);

    private final MsoReferenceImpl<IEventIf> m_createdEvent = new MsoReferenceImpl<IEventIf>(this, "CreatedEvent", 0, true);
    private final MsoReferenceImpl<IMsoObjectIf> m_dependentObject = new MsoReferenceImpl<IMsoObjectIf>(this, "DependentObject", 0, true);

    public static String getText(String aKey)
    {
        return Internationalization.getString(Internationalization.getBundle(ITaskIf.class), aKey);
    }

    public static String getEnumText(Enum<?> anEnum)
    {
        return getText(anEnum.getClass().getSimpleName() + "." + anEnum.name() + ".text");
    }

    public TaskImpl(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(anObjectId);
    }

    public TaskImpl(IMsoObjectIf.IObjectIdIf anObjectId, int aSerialNumber, Calendar aCalendar)
    {
        super(anObjectId, aCalendar);
        setNumber(aSerialNumber);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_description);
        addAttribute(m_number);
        addAttribute(m_progress);
        addAttribute(m_responsibleRole);
        addAttribute(m_taskText);
        addAttribute(m_alert);
        addAttribute(m_created);
        addAttribute(m_priority);
        addAttribute(m_status);
        addAttribute(m_type);
        addAttribute(m_sourceClass);
        addAttribute(m_creatingWorkProcess);
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
        addReference(m_createdEvent);
        addReference(m_dependentObject);
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

    public static TaskImpl implementationOf(ITaskIf anInterface) throws MsoCastException
    {
        try
        {
            return (TaskImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to TaskImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_TASK;
    }

    public void setDescription(String aDescription)
    {
        m_description.setValue(aDescription);
    }

    public String getDescription()
    {
        return m_description.getString();
    }

    public IMsoModelIf.ModificationState getDescriptionState()
    {
        return m_description.getState();
    }

    public IAttributeIf.IMsoStringIf getDescriptionAttribute()
    {
        return m_description;
    }

    public void setNumber(int aNumber)
    {
    	setNumber(m_number,aNumber);
    }

    public int getNumber()
    {
        return m_number.intValue();
    }

    public IMsoModelIf.ModificationState getNumberState()
    {
        return m_number.getState();
    }

    public IAttributeIf.IMsoIntegerIf getNumberAttribute()
    {
        return m_number;
    }

    public void setCreatingWorkProcess(String aCreatingWorkProcess)
    {
        m_creatingWorkProcess.setValue(aCreatingWorkProcess);
    }

    public String getCreatingWorkProcess()
    {
        return m_creatingWorkProcess.getString();
    }

    public IMsoModelIf.ModificationState getCreatingWorkProcessState()
    {
        return m_creatingWorkProcess.getState();
    }

    public IAttributeIf.IMsoStringIf getCreatingWorkProcessAttribute()
    {
        return m_creatingWorkProcess;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for enums
    *-------------------------------------------------------------------------------------------*/

    public void setType(TaskType aType)
    {
        m_type.setValue(aType);
    }

    public void setType(String aType)
    {
        m_type.setValue(aType);
    }

    public TaskType getType()
    {
        return m_type.getValue();
    }

    public IMsoModelIf.ModificationState getTypeState()
    {
        return m_type.getState();
    }

    public IAttributeIf.IMsoEnumIf<TaskType> getTypeAttribute()
    {
        return m_type;
    }

    public String getTypeName()
    {
        return m_type.getName();
   }
    
    public String getInternationalTypeName()
    {
        return m_type.getInternationalName();
   }

    public void setProgress(int aProgress)
    {
        m_progress.setValue(aProgress);
    }

    public int getProgress()
    {
        return m_progress.intValue();
    }

    public IMsoModelIf.ModificationState getProgressState()
    {
        return m_progress.getState();
    }

    public IAttributeIf.IMsoIntegerIf getProgressAttribute()
    {
        return m_progress;
    }

    public void setResponsibleRole(String aResponsibleRole)
    {
        m_responsibleRole.setValue(aResponsibleRole);
    }

    public String getResponsibleRole()
    {
        return m_responsibleRole.getString();
    }

    public IMsoModelIf.ModificationState getResponsibleRoleState()
    {
        return m_responsibleRole.getState();
    }

    public IAttributeIf.IMsoStringIf getResponsibleRoleAttribute()
    {
        return m_responsibleRole;
    }

    public void setTaskText(String aTaskText)
    {
        m_taskText.setValue(aTaskText);
    }

    public String getTaskText()
    {
        return m_taskText.getString();
    }

    public IMsoModelIf.ModificationState getTaskTextState()
    {
        return m_taskText.getState();
    }

    public IAttributeIf.IMsoStringIf getTaskTextAttribute()
    {
        return m_taskText;
    }

    public void setAlert(Calendar aAlert)
    {
        m_alert.setValue(aAlert);
    }

    public Calendar getAlert()
    {
        return m_alert.getCalendar();
    }

    public IMsoModelIf.ModificationState getAlertState()
    {
        return m_alert.getState();
    }

    public IAttributeIf.IMsoCalendarIf getAlertAttribute()
    {
        return m_alert;
    }

    public void setCreated(Calendar aCreated)
    {
        m_created.setValue(aCreated);
    }

    public Calendar getCreated()
    {
        return m_created.getCalendar();
    }

    public IMsoModelIf.ModificationState getCreatedState()
    {
        return m_created.getState();
    }

    public IAttributeIf.IMsoCalendarIf getCreatedAttribute()
    {
        return m_created;
    }

    public void setPriority(TaskPriority aPriority)
    {
        m_priority.setValue(aPriority);
    }

    public void setPriority(String aPriority)
    {
        m_priority.setValue(aPriority);
    }

    public TaskPriority getPriority()
    {
        return m_priority.getValue();
    }

    public IMsoModelIf.ModificationState getPriorityState()
    {
        return m_priority.getState();
    }

    public IAttributeIf.IMsoEnumIf<TaskPriority> getPriorityAttribute()
    {
        return m_priority;
    }

    public String getPriorityText()
    {
        return m_priority.getInternationalName();
    }

    public int comparePriorityTo(IEnumPriorityHolder<TaskPriority> anObject)
    {
        return getPriority().compareTo(anObject.getPriority());
    }

    public void setStatus(TaskStatus aStatus)
    {
        m_status.setValue(aStatus);
    }

    public void setStatus(String aStatus) throws IllegalOperationException
    {
        m_status.setValue(aStatus);
    }

    public TaskStatus getStatus()
    {
        return m_status.getValue();
    }

    public String getStatusText()
    {
        return m_status.getInternationalName();
    }

    public IMsoModelIf.ModificationState getStatusState()
    {
        return m_status.getState();
    }

    public IAttributeIf.IMsoEnumIf<TaskStatus> getStatusAttribute()
    {
        return m_status;
    }

    public void setSourceClass(IMsoManagerIf.MsoClassCode aSourceClass)
    {
        m_sourceClass.setValue(aSourceClass);
    }

    public void setSourceClass(String aSourceClass)
    {
        m_sourceClass.setValue(aSourceClass);
    }

    public IMsoManagerIf.MsoClassCode getSourceClass()
    {
        return m_sourceClass.getValue();
    }

    public String getSourceClassText()
    {
        return MsoManagerImpl.getClasscodeText(m_sourceClass.getValue());
    }

    public IMsoModelIf.ModificationState getSourceClassState()
    {
        return m_sourceClass.getState();
    }

    public IAttributeIf.IMsoEnumIf<IMsoManagerIf.MsoClassCode> getSourceClassAttribute()
    {
        return m_sourceClass;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setCreatedEvent(IEventIf aEvent)
    {
        m_createdEvent.setReference(aEvent);
    }

    public IEventIf getCreatedEvent()
    {
        return m_createdEvent.getReference();
    }

    public IMsoModelIf.ModificationState getCreatedEventState()
    {
        return m_createdEvent.getState();
    }

    public IMsoReferenceIf<IEventIf> getCreatedEventAttribute()
    {
        return m_createdEvent;
    }

    public void setDependentObject(IMsoObjectIf anAbstractMsoObject)
    {
        m_dependentObject.setReference(anAbstractMsoObject);
    }

    public IMsoObjectIf getDependentObject()
    {
        return m_dependentObject.getReference();
    }

    public IMsoModelIf.ModificationState getDependentObjectState()
    {
        return m_dependentObject.getState();
    }

    public IMsoReferenceIf<IMsoObjectIf> getDependentObjectAttribute()
    {
        return m_dependentObject;
    }

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

    public String getDefaultName() {
    	return getInternationalTypeName();
    }
    
    public Calendar getDueTime()
    {
        return getTimeStamp();
    }

    public void setDueTime(Calendar aCalendar)
    {
        setTimeStamp(aCalendar);
    }

    public IMsoModelIf.ModificationState getDueTimeState()
    {
        return getTimeStampState();
    }

    public IMsoObjectIf getSourceObject()
    {
        switch (getSourceClass())
        {
            case CLASSCODE_MESSAGE:
                return getOwningMessage();
            default: // todo supply with other class codes
                return null;
        }
    }

    private final static SelfSelector<ITaskIf, IMessageIf> referringMessageSelector = new SelfSelector<ITaskIf, IMessageIf>()
    {
        public boolean select(IMessageIf anObject)
        {
            return anObject.getMessageTasks().exists(m_object);
        }
    };

    private IMessageIf getOwningMessage()
    {
        referringMessageSelector.setSelfObject(this);
        return  MsoModelImpl.getInstance().getMsoManager().getCmdPost().getMessageLog().selectSingleItem(referringMessageSelector);
    }
}