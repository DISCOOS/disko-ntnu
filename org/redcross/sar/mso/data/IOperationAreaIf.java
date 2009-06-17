package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.util.mso.*;

public interface IOperationAreaIf extends IMsoObjectIf
{

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setAssignment(String anAssignment);

    public String getAssignment();

    public IData.DataOrigin getAssignmentState();

    public IMsoAttributeIf.IMsoStringIf getAssignmentAttribute();

    public void setGeodata(Polygon aGeodata);

    public Polygon getGeodata();

    public IData.DataOrigin getGeodataState();

    public IMsoAttributeIf.IMsoPolygonIf getGeodataAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IData.DataOrigin getRemarksState();

    public IMsoAttributeIf.IMsoStringIf getRemarksAttribute();

}