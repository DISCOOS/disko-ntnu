package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public class AssignmentListImpl extends MsoListImpl<IAssignmentIf> implements IAssignmentListIf
{

    public AssignmentListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public AssignmentListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, 0, aSize);
    }

    public IAssignmentIf createAssignment()
    {
        checkCreateOp();
        return createdUniqueItem(new AssignmentImpl(makeUniqueId(), makeSerialNumber()));
    }

    public IAssignmentIf createAssignment(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IAssignmentIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new AssignmentImpl(anObjectId, -1));
    }

    public ISearchIf createSearch()
    {
        checkCreateOp();
        return (ISearchIf) createdUniqueItem(new SearchImpl(makeUniqueId(), makeSerialNumber()));
    }

    public ISearchIf createSearch(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ISearchIf retVal = (ISearchIf) getItem(anObjectId);
        return retVal != null ? retVal : (ISearchIf) createdItem(new SearchImpl(anObjectId, -1));
    }

    public IAssistanceIf createAssistance()
    {
        checkCreateOp();
        return (IAssistanceIf) createdUniqueItem(new AssistanceImpl(makeUniqueId(), makeSerialNumber()));
    }

    public IAssistanceIf createAssistance(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IAssistanceIf retVal = (IAssistanceIf) getItem(anObjectId);
        return retVal != null ? retVal :  (IAssistanceIf) createdItem(new AssistanceImpl(anObjectId, -1));
    }

}