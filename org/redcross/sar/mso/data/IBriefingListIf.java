package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public interface IBriefingListIf extends IMsoListIf<IBriefingIf>
{
    public IBriefingIf createBriefing();

    public IBriefingIf createBriefing(IMsoObjectIf.IObjectIdIf anObjectId);
}