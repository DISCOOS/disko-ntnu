package org.redcross.sar.mso.data;

public class CalloutListImpl extends MsoListImpl<ICalloutIf> implements ICalloutListIf
{
    public CalloutListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public CalloutListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, 0, aSize);
    }

    public ICalloutIf createCallout()
    {
        checkCreateOp();
        return createdUniqueItem(new CalloutImpl(makeUniqueId()));
    }

    public ICalloutIf createCallout(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ICalloutIf retVal = (ICalloutIf)getItem(anObjectId);
        return retVal != null ? retVal : (ICalloutIf)createdItem(new CalloutImpl(anObjectId));
    }
}