package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;


public interface ICheckpointIf extends IMsoObjectIf
{
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/
    public void setChecked(boolean aChecked);

    public boolean isChecked();

    public IData.DataOrigin getCheckedState();

    public IMsoAttributeIf.IMsoBooleanIf getCheckedAttribute();

    public void setDescription(String aDescription);

    public String getDescription();

    public IData.DataOrigin getDescriptionState();

    public IMsoAttributeIf.IMsoStringIf getDescriptionAttribute();

    public void setName(String aName);

    public String getName();

    public IData.DataOrigin getNameState();

    public IMsoAttributeIf.IMsoStringIf getNameAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setCheckpointTask(ITaskIf aTask);

    public ITaskIf getCheckpointTask();

    public IData.DataOrigin getCheckpointTaskState();

    public IMsoRelationIf<ITaskIf> getCheckpointTaskAttribute();
}