package org.redcross.sar.mso.data;

public class SubjectListImpl extends MsoListImpl<ISubjectIf> implements ISubjectListIf
{

    public SubjectListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(ISubjectIf.class, anOwner, theName, isMain);
    }

    public SubjectListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(ISubjectIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public ISubjectIf createSubject()
    {
        checkCreateOp();
        return createdUniqueItem(new SubjectImpl(getOwner().getModel(), makeUniqueId()));
    }

    public ISubjectIf createSubject(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ISubjectIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new SubjectImpl(getOwner().getModel(), anObjectId));
    }


}