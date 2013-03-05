package org.redcross.sar.mso.data;

public interface IBriefingListIf extends IMsoListIf<IBriefingIf>
{
    public IBriefingIf createBriefing();

    public IBriefingIf createBriefing(IMsoObjectIf.IObjectIdIf anObjectId);
}