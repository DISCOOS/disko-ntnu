package org.redcross.sar.mso.data;

public interface IAreaListIf extends IMsoListIf<IAreaIf>
{

    public IAreaIf createArea(boolean hostile);

    public IAreaIf createArea(IMsoObjectIf.IObjectIdIf anObjectId,boolean hostile);

}