package org.redcross.sar.mso.data;

public class SketchListImpl extends MsoListImpl<ISketchIf> implements ISketchListIf
{

    public SketchListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(ISketchIf.class, anOwner, theName, isMain);
    }

    public SketchListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(ISketchIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public ISketchIf createSketch()
    {
        checkCreateOp();
        return createdUniqueItem(new SketchImpl(makeUniqueId()));
    }

    public ISketchIf createSketch(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ISketchIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new SketchImpl(anObjectId));
    }

}