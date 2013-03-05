package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

public interface ITrackIf extends IMsoObjectIf
{
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setGeodata(Track aGeodata);

    public Track getGeodata();

    public IData.DataOrigin getGeodataState();

    public IMsoAttributeIf.IMsoTrackIf getGeodataAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IData.DataOrigin getRemarksState();

    public IMsoAttributeIf.IMsoStringIf getRemarksAttribute();

    public void setAreaSequenceNumber(int aNumber);

    public int getAreaSequenceNumber();

    public IData.DataOrigin getAreaSequenceNumberState();

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