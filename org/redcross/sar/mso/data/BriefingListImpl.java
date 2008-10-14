package org.redcross.sar.mso.data;

public class BriefingListImpl extends MsoListImpl<IBriefingIf> implements IBriefingListIf
{

    public BriefingListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IBriefingIf.class, anOwner, theName, isMain);
    }

    public BriefingListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IBriefingIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IBriefingIf createBriefing()
    {
        checkCreateOp();
        return createdUniqueItem(new BriefingImpl(makeUniqueId()));
    }

    public IBriefingIf createBriefing(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IBriefingIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new BriefingImpl(anObjectId));
    }
}