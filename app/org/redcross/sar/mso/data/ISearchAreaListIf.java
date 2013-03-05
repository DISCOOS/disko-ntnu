package org.redcross.sar.mso.data;

public interface ISearchAreaListIf extends IMsoListIf<ISearchAreaIf>
{
    public ISearchAreaIf createSearchArea();

    public ISearchAreaIf createSearchArea(IMsoObjectIf.IObjectIdIf anObjectId);
}