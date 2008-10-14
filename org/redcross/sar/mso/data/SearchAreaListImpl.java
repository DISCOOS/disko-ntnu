package org.redcross.sar.mso.data;

public class SearchAreaListImpl extends MsoListImpl<ISearchAreaIf> implements ISearchAreaListIf
{

    public SearchAreaListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(ISearchAreaIf.class, anOwner, theName, isMain);
    }

    public SearchAreaListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(ISearchAreaIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public ISearchAreaIf createSearchArea()
    {
        checkCreateOp();
        return createdUniqueItem(new SearchAreaImpl(makeUniqueId()));
    }

    public ISearchAreaIf createSearchArea(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ISearchAreaIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new SearchAreaImpl(anObjectId));
    }


}