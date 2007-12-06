package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public class BriefingListImpl extends MsoListImpl<IBriefingIf> implements IBriefingListIf
{

    public BriefingListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public BriefingListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, aSize);
    }

    public IBriefingIf createBriefing()
    {
        checkCreateOp();
        return createdUniqueItem(new BriefingImpl(makeUniqueId()));
    }

    public IBriefingIf createBriefing(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IBriefingIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new BriefingImpl(anObjectId));
    }
}