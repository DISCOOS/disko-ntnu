package org.redcross.sar.mso.data;

public interface ISubjectListIf extends IMsoListIf<ISubjectIf>
{
    public ISubjectIf createSubject();

    public ISubjectIf createSubject(IMsoObjectIf.IObjectIdIf anObjectId);

}