package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.mso.Position;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public interface IPOIIf extends IMsoObjectIf
{
    public static final String bundleName  = "org.redcross.sar.mso.data.properties.POI";

    public static final EnumSet<POIType> AREA_SET = EnumSet.of(POIType.START,POIType.VIA,POIType.STOP);

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

    public IMsoModelIf.ModificationState getTypeState();

    public IAttributeIf.IMsoEnumIf<POIType> getTypeAttribute();

    public String getInternationalTypeName();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setName(String aName);

    public String getName();

    public IMsoModelIf.ModificationState getNameState();

    public IAttributeIf.IMsoStringIf getNameAttribute();

    public void setPosition(Position aPosition);

    public Position getPosition();

    public IMsoModelIf.ModificationState getPositionState();

    public IAttributeIf.IMsoPositionIf getPositionAttribute();

    public void setRemarks(String aRemarks);

    public String getRemarks();

    public IMsoModelIf.ModificationState getRemarksState();

    public IAttributeIf.IMsoStringIf getRemarksAttribute();

    public void setAreaSequenceNumber(int aNumber);

    public int getAreaSequenceNumber();

    public IMsoModelIf.ModificationState getAreaSequenceNumberState();

    public IAttributeIf.IMsoIntegerIf getAreaSequenceNumberAttribute();
    
    /*-------------------------------------------------------------------------------------------
     * Other Methods
     *-------------------------------------------------------------------------------------------*/
    
	public String getDefaultName();

    public Set<IMessageLineIf> getReferringMessageLines();

    public Set<IMessageLineIf> getReferringMessageLines(Collection<IMessageLineIf> aCollection);
}