package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public interface ISearchAreaListIf extends IMsoListIf<ISearchAreaIf>
{
    public ISearchAreaIf createSearchArea();

    public ISearchAreaIf createSearchArea(IMsoObjectIf.IObjectIdIf anObjectId);
}