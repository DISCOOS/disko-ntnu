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

    public IAttributeIf.IMsoTrackIf getGeodataAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IMsoModelIf.ModificationState getRemarksState();

    public IAttributeIf.IMsoStringIf getRemarksAttribute();

    public void setAreaSequenceNumber(int aNumber);

    public int getAreaSequenceNumber();

    public IMsoModelIf.ModificationState getAreaSequenceNumberState();

    public IAttributeIf.IMsoIntegerIf getAreaSequenceNumberAttribute();

    public void addTrackPoint(TimePos aTimePos);
    
    public void removeTrackPoint(TimePos aTimePos);
 
}