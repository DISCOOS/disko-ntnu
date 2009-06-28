package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public class CmdPostListImpl extends MsoListImpl<ICmdPostIf> implements ICmdPostListIf
{

    public CmdPostListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(ICmdPostIf.class, anOwner, theName, isMain);
    }

    public ICmdPostIf createCmdPost()
    {
        if (size() > 0)
        {
            throw new DuplicateIdException("Duplicate id for cmd post");
        }
        checkCreateOp();
        return createdUniqueItem(new CmdPostImpl(getOwner().getModel(), createUniqueId()));
    }

    public ICmdPostIf createCmdPost(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ICmdPostIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new CmdPostImpl(getOwner().getModel(), anObjectId));
    }


}