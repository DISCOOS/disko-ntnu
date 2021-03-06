package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.util.mso.Position;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

public interface IPOIIf extends IMsoObjectIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.POI";

    /**
     * Often used enum sets
     */
    public static final EnumSet<POIType> AREA_SET = EnumSet.of(POIType.START,POIType.VIA,POIType.STOP);

    /**
     * Often used selectors
     */
    public static final Selector<IPOIIf> AREA_POI_SELECTOR = new Selector<IPOIIf>() {

		@Override
		public boolean select(IPOIIf poi) {
			// compare ordinals
			return AREA_SET.contains(poi.getType());
		}

    };

    public static final Selector<IPOIIf> NON_AREA_POI_SELECTOR = new Selector<IPOIIf>() {

		@Override
		public boolean select(IPOIIf poi) {
			// compare ordinals
			return !AREA_SET.contains(poi.getType());
		}

    };

    ;    /**
     * Often used comparators
     */
    public static final Comparator<IPOIIf> POI_COMPARATOR = new Comparator<IPOIIf>() {

		@Override
		public int compare(IPOIIf p1, IPOIIf p2) {
			if(POIType.START.equals(p1.getType()))
				return Integer.MIN_VALUE;
			if(POIType.STOP.equals(p1.getType()))
				return Integer.MAX_VALUE;
			// is equal?
			if(p1.getType() == p2.getType())
			{
				return p1.getAreaSequenceNumber() - p2.getAreaSequenceNumber();
			}
			// compare ordinals
			return p1.getType().ordinal()-p2.getType().ordinal();
		}

    };

    public enum POIType
    {
        GENERAL,
        FINDING,
        SILENT_WITNESS,
        INTELLIGENCE,
        OBSERVATION,
        START,
        STOP,
        VIA
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setType(POIType aType);

    public void setType(String aType);

    public POIType getType();

    public IData.DataOrigin getTypeState();

    public IMsoAttributeIf.IMsoEnumIf<POIType> getTypeAttribute();

    public String getInternationalTypeName();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setName(String aName);

    public String getName();

    public IData.DataOrigin getNameState();

    public IMsoAttributeIf.IMsoStringIf getNameAttribute();

    public void setPosition(Position aPosition);

    public Position getPosition();

    public IData.DataOrigin getPositionState();

    public IMsoAttributeIf.IMsoPositionIf getPositionAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IData.DataOrigin getRemarksState();

    public IMsoAttributeIf.IMsoStringIf getRemarksAttribute();

    public void setAreaSequenceNumber(int aNumber);

    public int getAreaSequenceNumber();

    public IData.DataOrigin getAreaSequenceNumberState();

    public IMsoAttributeIf.IMsoIntegerIf getAreaSequenceNumberAttribute();

    /*-------------------------------------------------------------------------------------------
     * Other Methods
     *-------------------------------------------------------------------------------------------*/

	public String getDefaultName();

    public Set<IMessageLineIf> getReferringMessageLines();

    public Set<IMessageLineIf> getReferringMessageLines(Collection<IMessageLineIf> aCollection);
}