package org.redcross.sar.mso.data;

public interface IOperationAreaListIf extends IMsoListIf<IOperationAreaIf>
{
    public IOperationAreaIf createOperationArea();

    public IOperationAreaIf createOperationArea(IMsoObjectIf.IObjectIdIf anObjectId);

}