package org.redcross.sar.mso.data;

import org.redcross.sar.mso.data.IAssignmentIf.AssignmentType;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;

public class AssignmentListImpl extends MsoListImpl<IAssignmentIf> implements IAssignmentListIf
{

    public AssignmentListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IAssignmentIf.class, anOwner, theName, isMain);
    }

    public AssignmentListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IAssignmentIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IAssignmentIf createAssignment()
    {
        checkCreateOp();
        return createdUniqueItem(new AssignmentImpl(makeUniqueId(), makeSerialNumber(AssignmentType.GENERAL)));
    }

    public IAssignmentIf createAssignment(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IAssignmentIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new AssignmentImpl(anObjectId, -1));
    }

    public ISearchIf createSearch()
    {
        checkCreateOp();
        return (ISearchIf) createdUniqueItem(new SearchImpl(makeUniqueId(), makeSerialNumber(SearchSubType.PATROL)));
    }

    public ISearchIf createSearch(SearchSubType type)
    {
        checkCreateOp();
        return (ISearchIf) createdUniqueItem(new SearchImpl(makeUniqueId(), makeSerialNumber(type), type));
    }

    public ISearchIf createSearch(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ISearchIf retVal = (ISearchIf) getLoopback(anObjectId);
        return retVal != null ? retVal : (ISearchIf) createdItem(new SearchImpl(anObjectId, -1));
    }

    public IAssistanceIf createAssistance()
    {
        checkCreateOp();
        return (IAssistanceIf) createdUniqueItem(new AssistanceImpl(makeUniqueId(), makeSerialNumber(AssignmentType.ASSISTANCE)));
    }

    public IAssistanceIf createAssistance(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IAssistanceIf retVal = (IAssistanceIf) getLoopback(anObjectId);
        return retVal != null ? retVal : (IAssistanceIf) createdItem(new AssistanceImpl(anObjectId, -1));
    }

    public int makeSerialNumber()
    {
        return super.makeSerialNumber();
    }

    public int makeSerialNumber(Enum<?> type)
    {
    	return super.makeSerialNumber(type);
    }




}