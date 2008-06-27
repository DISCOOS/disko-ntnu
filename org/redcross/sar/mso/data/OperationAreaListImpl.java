package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public class OperationAreaListImpl extends MsoListImpl<IOperationAreaIf> implements IOperationAreaListIf
{

    public OperationAreaListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public OperationAreaListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, 0, aSize);
    }

    public IOperationAreaIf createOperationArea()
    {
        checkCreateOp();
        return createdUniqueItem(new OperationAreaImpl(makeUniqueId()));
    }

    public IOperationAreaIf createOperationArea(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IOperationAreaIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new OperationAreaImpl(anObjectId));
    }


}