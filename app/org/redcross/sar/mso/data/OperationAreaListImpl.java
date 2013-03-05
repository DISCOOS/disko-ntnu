package org.redcross.sar.mso.data;

public class OperationAreaListImpl extends MsoListImpl<IOperationAreaIf> implements IOperationAreaListIf
{

    public OperationAreaListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IOperationAreaIf.class, anOwner, theName, isMain);
    }

    public OperationAreaListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IOperationAreaIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IOperationAreaIf createOperationArea()
    {
        checkCreateOp();
        return createdUniqueItem(new OperationAreaImpl(getOwner().getModel(), createUniqueId()));
    }

    public IOperationAreaIf createOperationArea(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IOperationAreaIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new OperationAreaImpl(getOwner().getModel(), anObjectId));
    }


}