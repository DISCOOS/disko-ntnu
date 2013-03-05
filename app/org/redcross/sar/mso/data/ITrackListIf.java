package org.redcross.sar.mso.data;

import org.redcross.sar.util.mso.Track;

public interface ITrackListIf extends IMsoListIf<ITrackIf>
{
    public ITrackIf createTrack();

    public ITrackIf createTrack(IMsoObjectIf.IObjectIdIf anObjectId);

    public ITrackIf createTrack(Track aTrack);

    public ITrackIf createTrack(IMsoObjectIf.IObjectIdIf anObjectId, Track aTrack);

}