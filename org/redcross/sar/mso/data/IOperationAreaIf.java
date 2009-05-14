package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.mso.*;

public interface IOperationAreaIf extends IMsoObjectIf
{

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setAssignment(String anAssignment);

    public String getAssignment();

    public IMsoModelIf.ModificationState getAssignmentState();

    public IMsoAttributeIf.IMsoStringIf getAssignmentAttribute();

    public void setGeodata(Polygon aGeodata);

    public Polygon getGeodata();

    public IMsoModelIf.ModificationState getGeodataState();

    public IMsoAttributeIf.IMsoPolygonIf getGeodataAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IMsoModelIf.ModificationState getRemarksState();

    public IMsoAttributeIf.IMsoStringIf getRemarksAttribute();

}