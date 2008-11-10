package org.redcross.sar.mso.data;

import java.util.Comparator;

import org.redcross.sar.data.Selector;
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
