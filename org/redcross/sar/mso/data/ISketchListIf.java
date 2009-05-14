package org.redcross.sar.mso.data;

public interface ISketchListIf extends IMsoListIf<ISketchIf>
{
    public ISketchIf createSketch();

    public ISketchIf createSketch(IMsoObjectIf.IObjectIdIf anObjectId);
}