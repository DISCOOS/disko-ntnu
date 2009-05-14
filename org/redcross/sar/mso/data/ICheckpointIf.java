package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

public interface ICheckpointIf extends IMsoObjectIf
{
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/
    public void setChecked(boolean aChecked);

    public boolean isChecked();

    public IMsoModelIf.ModificationState getCheckedState();

    public IMsoAttributeIf.IMsoBooleanIf getCheckedAttribute();

    public void setDescription(String aDescription);

    public String getDescription();

    public IMsoModelIf.ModificationState getDescriptionState();

    public IMsoAttributeIf.IMsoStringIf getDescriptionAttribute();

    public void setName(String aName);

    public String getName();

    public IMsoModelIf.ModificationState getNameState();

    public IMsoAttributeIf.IMsoStringIf getNameAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setCheckpointTask(ITaskIf aTask);

    public ITaskIf getCheckpointTask();

    public IMsoModelIf.ModificationState getCheckpointTaskState();

    public IMsoReferenceIf<ITaskIf> getCheckpointTaskAttribute();
}