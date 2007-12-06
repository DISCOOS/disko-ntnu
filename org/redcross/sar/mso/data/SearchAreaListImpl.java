package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public class SearchAreaListImpl extends MsoListImpl<ISearchAreaIf> implements ISearchAreaListIf
{

    public SearchAreaListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public SearchAreaListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, aSize);
    }

    public ISearchAreaIf createSearchArea()
    {
        checkCreateOp();
        return createdUniqueItem(new SearchAreaImpl(makeUniqueId()));
    }

    public ISearchAreaIf createSearchArea(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ISearchAreaIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new SearchAreaImpl(anObjectId));
    }


}