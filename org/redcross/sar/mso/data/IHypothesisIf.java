package org.redcross.sar.mso.data;

import java.util.Comparator;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;

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

    /**
     * Often used selectors
     */
    public static Selector<IHypothesisIf> ALL_SELECTOR = new Selector<IHypothesisIf>() {

		public boolean select(IHypothesisIf anObject) {
			return true;
		}

    };

    /**
     * Often used comparators
     */
    public static Comparator<IHypothesisIf> NUMBER_COMPARATOR = new Comparator<IHypothesisIf>() {

		public int compare(IHypothesisIf o1, IHypothesisIf o2) {
			return o1.getNumber() - o2.getNumber();
		}

    };

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(HypothesisStatus aStatus);

    public void setStatus(String aStatus);

    public HypothesisStatus getStatus();

    public String getStatusText();

    public IData.DataOrigin getStatusState();

    public IMsoAttributeIf.IMsoEnumIf<HypothesisStatus> getStatusAttribute();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setDescription(String aDescription);

    public String getDescription();

    public IData.DataOrigin getDescriptionState();

    public IMsoAttributeIf.IMsoStringIf getDescriptionAttribute();

    public void setPriorityIndex(int aPriority);

    public int getPriorityIndex();

    public IData.DataOrigin getPriorityIndexState();

    public IMsoAttributeIf.IMsoIntegerIf getPriorityIndexAttribute();
}
