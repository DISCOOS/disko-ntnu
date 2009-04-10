package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.mso.Polygon;

public interface ISearchAreaIf extends IMsoObjectIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.SearchArea";

    public enum SearchAreaStatus
    {
        PROCESSING,
        ABORTED,
        FINISHED
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(SearchAreaStatus aStatus);

    public void setStatus(String aStatus);

    public SearchAreaStatus getStatus();

    public String getStatusText();

    public IMsoModelIf.ModificationState getStatusState();

    public IAttributeIf.IMsoEnumIf<SearchAreaStatus> getStatusAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setGeodata(Polygon aGeodata);

    public Polygon getGeodata();

    public IMsoModelIf.ModificationState getGeodataState();

    public IAttributeIf.IMsoPolygonIf getGeodataAttribute();

    public void setPriority(int aPriority);

    public int getPriority();

    public IMsoModelIf.ModificationState getPriorityState();

    public IAttributeIf.IMsoIntegerIf getPriorityAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IMsoModelIf.ModificationState getRemarksState();

    public IAttributeIf.IMsoStringIf getRemarksAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for references
    *-------------------------------------------------------------------------------------------*/

    public void setSearchAreaHypothesis(IHypothesisIf aHypothesis);

    public IHypothesisIf getSearchAreaHypothesis();

    public IMsoModelIf.ModificationState getSearchAreaHypothesisState();

    public IMsoReferenceIf<IHypothesisIf> getSearchAreaHypothesisAttribute();

}