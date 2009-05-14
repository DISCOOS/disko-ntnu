package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

public interface ITrackIf extends IMsoObjectIf
{
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setGeodata(Track aGeodata);

    public Track getGeodata();

    public IMsoModelIf.ModificationState getGeodataState();

    public IMsoAttributeIf.IMsoTrackIf getGeodataAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IMsoModelIf.ModificationState getRemarksState();

    public IMsoAttributeIf.IMsoStringIf getRemarksAttribute();

    public void setAreaSequenceNumber(int aNumber);

    public int getAreaSequenceNumber();

    public IMsoModelIf.ModificationState getAreaSequenceNumberState();

    public IMsoAttributeIf.IMsoIntegerIf getAreaSequenceNumberAttribute();

    /*-------------------------------------------------------------------------------------------
     * Public methods
     *-------------------------------------------------------------------------------------------*/
    
    public void addTrackPoint(TimePos aTimePos);
    
    public int getTrackPointCount();
    
    public TimePos getTrackPoint(int index);
    
    public TimePos getTrackStartPoint();

    public TimePos getTrackStopPoint();
    
    public void removeTrackPoint(TimePos aTimePos);
 
}