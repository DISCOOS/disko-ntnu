package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

/**
 *
 */
public interface IHypothesisIf extends IMsoObjectIf, ISerialNumberedIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.Hypothesis";

    /**
     * Status enum
     */
    public enum HypothesisStatus
    {
        ACTIVE,
        PLAUSIBLE,
        REJECTED,
        CHECKED,
        SUCCESS
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(HypothesisStatus aStatus);

    public void setStatus(String aStatus);

    public HypothesisStatus getStatus();

    public String getStatusText();

    public IMsoModelIf.ModificationState getStatusState();

    public IAttributeIf.IMsoEnumIf<HypothesisStatus> getStatusAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setDescription(String aDescription);

    public String getDescription();

    public IMsoModelIf.ModificationState getDescriptionState();

    public IAttributeIf.IMsoStringIf getDescriptionAttribute();

    public void setPriorityIndex(int aPriority);

    public int getPriorityIndex();

    public IMsoModelIf.ModificationState getPriorityIndexState();

    public IAttributeIf.IMsoIntegerIf getPriorityIndexAttribute();
}
