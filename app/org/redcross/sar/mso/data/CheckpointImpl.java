package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;

public class CheckpointImpl extends AbstractMsoObject implements ICheckpointIf
{
    private final MsoRelationImpl<ITaskIf> m_checkpointTask = new MsoRelationImpl<ITaskIf>(this, "CheckpointTask", 0, true, null);

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

    public IMsoManagerIf.MsoClassCode getClassCode()
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

    public IData.DataOrigin getCheckedState()
    {
        return m_checked.getOrigin();
    }

    public IMsoAttributeIf.IMsoBooleanIf getCheckedAttribute()
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

    public IData.DataOrigin getDescriptionState()
    {
        return m_description.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getDescriptionAttribute()
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

    public IData.DataOrigin getNameState()
    {
        return m_name.getOrigin();
    }

    public IMsoAttributeIf.IMsoStringIf getNameAttribute()
    {
        return m_name;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setCheckpointTask(ITaskIf aTask)
    {
        m_checkpointTask.set(aTask);
    }

    public ITaskIf getCheckpointTask()
    {
        return m_checkpointTask.get();
    }

    public IData.DataOrigin getCheckpointTaskState()
    {
        return m_checkpointTask.getOrigin();
    }

    public IMsoRelationIf<ITaskIf> getCheckpointTaskAttribute()
    {
        return m_checkpointTask;
    }
}