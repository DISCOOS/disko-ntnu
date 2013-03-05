package org.redcross.sar.mso.data;

public class IntelligenceListImpl extends MsoListImpl<IIntelligenceIf> implements IIntelligenceListIf
{

    public IntelligenceListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IIntelligenceIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public IIntelligenceIf createIntelligence()
    {
        checkCreateOp();
        return createdUniqueItem(new IntelligenceImpl(getOwner().getModel(), createUniqueId()));
    }

    public IIntelligenceIf createIntelligence(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IIntelligenceIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new IntelligenceImpl(getOwner().getModel(), anObjectId));
    }

}