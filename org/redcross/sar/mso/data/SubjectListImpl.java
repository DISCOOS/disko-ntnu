package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public class SubjectListImpl extends MsoListImpl<ISubjectIf> implements ISubjectListIf
{

    public SubjectListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public SubjectListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, 0, aSize);
    }

    public ISubjectIf createSubject()
    {
        checkCreateOp();
        return createdUniqueItem(new SubjectImpl(makeUniqueId()));
    }

    public ISubjectIf createSubject(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ISubjectIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new SubjectImpl(anObjectId));
    }


}