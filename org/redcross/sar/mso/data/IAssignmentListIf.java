package org.redcross.sar.mso.data;

import org.redcross.sar.mso.data.ISearchIf.SearchSubType;

/**
 *
 */
public interface IAssignmentListIf extends IMsoListIf<IAssignmentIf>
{
    public IAssignmentIf createAssignment();

    public IAssignmentIf createAssignment(IMsoObjectIf.IObjectIdIf anObjectId);

    public ISearchIf createSearch();

    public ISearchIf createSearch(SearchSubType type);

    public ISearchIf createSearch(IMsoObjectIf.IObjectIdIf anObjectId);

    public IAssistanceIf createAssistance();

    public IAssistanceIf createAssistance(IMsoObjectIf.IObjectIdIf anObjectId);

    public int makeSerialNumber();

    public int makeSerialNumber(Enum<?> type);

}
