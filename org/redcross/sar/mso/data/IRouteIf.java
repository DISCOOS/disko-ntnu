package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.util.mso.Route;

public interface IRouteIf extends IMsoObjectIf
{
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/
    public void setGeodata(Route aGeodata);

    public Route getGeodata();

    public IMsoModelIf.ModificationState getGeodataState();

    public IMsoAttributeIf.IMsoRouteIf getGeodataAttribute();

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
    
    public void addRoutePoint(GeoPos aGeoPos);
    
    public int getRoutePointCount();
    
    public GeoPos getRoutePoint(int index);
    
    public GeoPos getRouteStartPoint();

    public GeoPos getRouteStopPoint();
    
    public void removeRoutePoint(GeoPos aGeoPos);    
}