package org.redcross.sar.mso.data;

import org.redcross.sar.util.mso.Track;

public class TrackListImpl extends MsoListImpl<ITrackIf> implements ITrackListIf
{

    public TrackListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(ITrackIf.class, anOwner, theName, isMain, 0, aSize);
    }

    public ITrackIf createTrack()
    {
        checkCreateOp();
        return createdUniqueItem(new TrackImpl(getOwner().getModel(), makeUniqueId()));
    }

    public ITrackIf createTrack(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        ITrackIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new TrackImpl(getOwner().getModel(), anObjectId));
    }

    public ITrackIf createTrack(Track aTrack)
    {
        checkCreateOp();
        return createdUniqueItem(new TrackImpl(getOwner().getModel(), makeUniqueId(), aTrack));
    }

    public ITrackIf createTrack(IMsoObjectIf.IObjectIdIf anObjectId, Track aTrack)
    {
        checkCreateOp();
        ITrackIf retVal = getLoopback(anObjectId);
        return retVal != null ? retVal : createdItem(new TrackImpl(getOwner().getModel(), anObjectId, aTrack));
    }

}