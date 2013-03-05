package org.redcross.sar.mso.data;

/**
 *
 */
public interface ICmdPostListIf extends IMsoListIf<ICmdPostIf>
{
    public ICmdPostIf createCmdPost();

    public ICmdPostIf createCmdPost(IMsoObjectIf.IObjectIdIf anObjectId);
}
