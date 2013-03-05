package org.redcross.sar.mso.data;

/**
 *
 */
public interface IPersonnelListIf extends IMsoListIf<IPersonnelIf>
{
    public IPersonnelIf createPersonnel();

    public IPersonnelIf createPersonnel(IMsoObjectIf.IObjectIdIf anObjectId);
}
