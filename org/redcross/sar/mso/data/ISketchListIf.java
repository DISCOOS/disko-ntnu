package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public interface ISketchListIf extends IMsoListIf<ISketchIf>
{
    public ISketchIf createSketch();

    public ISketchIf createSketch(IMsoObjectIf.IObjectIdIf anObjectId);
}