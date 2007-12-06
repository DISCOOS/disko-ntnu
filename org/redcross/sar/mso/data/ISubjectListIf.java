package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public interface ISubjectListIf extends IMsoListIf<ISubjectIf>
{
    public ISubjectIf createSubject();

    public ISubjectIf createSubject(IMsoObjectIf.IObjectIdIf anObjectId);

}