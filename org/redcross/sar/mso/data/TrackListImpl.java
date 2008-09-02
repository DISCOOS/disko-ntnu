package org.redcross.sar.mso.data;

import org.redcross.sar.util.mso.Track;
import org.redcross.sar.util.except.DuplicateIdException;

public class TrackListImpl extends MsoListImpl<ITrackIf> implements ITrackListIf
{

    public TrackListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(anOwner, theName, isMain, 0, aSize);
    }

    public ITrackIf createTrack()
    {
        checkCreateOp();
        return createdUniqueItem(new TrackImpl(makeUniqueId()));
    }

    public ITrackIf createTrack(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ITrackIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new TrackImpl(anObjectId));
    }

    public ITrackIf createTrack(Track aTrack)
    {
        checkCreateOp();
        return createdUniqueItem(new TrackImpl(makeUniqueId(), aTrack));
    }

    public ITrackIf createTrack(IMsoObjectIf.IObjectIdIf anObjectId, Track aTrack)
    {
        checkCreateOp();
        ITrackIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new TrackImpl(anObjectId, aTrack));
    }

}