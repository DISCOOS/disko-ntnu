package org.redcross.sar.mso.data;

import org.redcross.sar.util.except.DuplicateIdException;

public class CheckpointListImpl extends MsoListImpl<ICheckpointIf> implements ICheckpointListIf
{

    public CheckpointListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(anOwner, theName, isMain);
    }

    public CheckpointListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, 0, aSize);
    }

    public ICheckpointIf createCheckpoint()
    {
        checkCreateOp();
        return createdUniqueItem(new CheckpointImpl(makeUniqueId()));
    }

    public ICheckpointIf createCheckpoint(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ICheckpointIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new CheckpointImpl(anObjectId));
    }


}