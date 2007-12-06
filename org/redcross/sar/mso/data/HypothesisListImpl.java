package org.redcross.sar.mso.data;

public class HypothesisListImpl extends MsoListImpl<IHypothesisIf> implements IHypothesisListIf
{

    public HypothesisListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public HypothesisListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, aSize);
    }

    public IHypothesisIf createHypothesis()
    {
        checkCreateOp();
        return createdUniqueItem(new HypothesisImpl(makeUniqueId(), makeSerialNumber()));
    }

    public IHypothesisIf createHypothesis(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IHypothesisIf retVal = getItem(anObjectId);
        return retVal != null ? retVal : createdItem(new HypothesisImpl(anObjectId, -1));
    }
}