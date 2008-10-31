package org.redcross.sar.mso.data;

public class HypothesisListImpl extends MsoListImpl<IHypothesisIf> implements IHypothesisListIf
{

    public HypothesisListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IHypothesisIf.class, anOwner, theName, isMain);
    }

    public HypothesisListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IHypothesisIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IHypothesisIf createHypothesis()
    {
        checkCreateOp();
        return createdUniqueItem(new HypothesisImpl(getOwner().getModel(), makeUniqueId(), makeSerialNumber()));
    }

    public IHypothesisIf createHypothesis(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IHypothesisIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new HypothesisImpl(getOwner().getModel(), anObjectId, -1));
    }
}