package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;

public class CheckpointImpl extends AbstractMsoObject implements ICheckpointIf
{
    private final MsoReferenceImpl<ITaskIf> m_checkpointTask = new MsoReferenceImpl<ITaskIf>(this, "CheckpointTask", 0, true);

    private final AttributeImpl.MsoBoolean m_checked = new AttributeImpl.MsoBoolean(this, "Checked");
    private final AttributeImpl.MsoString m_description = new AttributeImpl.MsoString(this, "Description");
    private final AttributeImpl.MsoString m_name = new AttributeImpl.MsoString(this, "Name");

    public CheckpointImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    protected void defineAttributes()
    {
        addAttribute(m_checked);
        addAttribute(m_description);
        addAttribute(m_name);
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
        addObject(m_checkpointTask);
    }

    public static CheckpointImpl implementationOf(ICheckpointIf anInterface) throws MsoCastException
    {
        try
        {
            return (CheckpointImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to CheckpointImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_CHECKPOINT;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setChecked(boolean aChecked)
    {
        m_checked.setValue(aChecked);
    }

    public boolean isChecked()
    {
        return m_checked.booleanValue();
    }

    public IMsoModelIf.ModificationState getCheckedState()
    {
        return m_checked.getState();
    }

    public IAttributeIf.IMsoBooleanIf getCheckedAttribute()
    {
        return m_checked;
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

    public void setName(String aName)
    {
        m_name.setValue(aName);
    }

    public String getName()
    {
        return m_name.getString();
    }

    public IMsoModelIf.ModificationState getNameState()
    {
        return m_name.getState();
    }

    public IAttributeIf.IMsoStringIf getNameAttribute()
    {
        return m_name;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setCheckpointTask(ITaskIf aTask)
    {
        m_checkpointTask.setReference(aTask);
    }

    public ITaskIf getCheckpointTask()
    {
        return m_checkpointTask.getReference();
    }

    public IMsoModelIf.ModificationState getCheckpointTaskState()
    {
        return m_checkpointTask.getState();
    }

    public IMsoReferenceIf<ITaskIf> getCheckpointTaskAttribute()
    {
        return m_checkpointTask;
    }
}