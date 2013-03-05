package org.redcross.sar.mso.data;

public interface ICheckpointListIf extends IMsoListIf<ICheckpointIf>
{
    public ICheckpointIf createCheckpoint();

    public ICheckpointIf createCheckpoint(IMsoObjectIf.IObjectIdIf anObjectId);
}