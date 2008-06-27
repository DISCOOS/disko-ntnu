package org.redcross.sar.mso.data;

public class AreaListImpl extends MsoListImpl<IAreaIf> implements IAreaListIf
{
    public AreaListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public AreaListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, 0, aSize);
    }

    public IAreaIf createArea(boolean hostile)
    {
        checkCreateOp();
        return createdUniqueItem(new AreaImpl(makeUniqueId(),true));
    }

    public IAreaIf createArea(IMsoObjectIf.IObjectIdIf anObjectId,boolean hostile)
    {
        checkCreateOp();
        IAreaIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new AreaImpl(anObjectId,true));
    }
}