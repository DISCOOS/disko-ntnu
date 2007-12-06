package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public interface IOperationAreaListIf extends IMsoListIf<IOperationAreaIf>
{
    public IOperationAreaIf createOperationArea();

    public IOperationAreaIf createOperationArea(IMsoObjectIf.IObjectIdIf anObjectId);

}