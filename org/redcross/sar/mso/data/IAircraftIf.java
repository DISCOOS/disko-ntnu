package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

/**
 *
 */
public interface IAircraftIf extends ITransportIf
{
    /**
     * Aircraft type enum
     */
    public enum AircraftSubType
    {
        LIGHT_AIRCRAFT,
        AIRPLANE,
        AIRLINER,
        HELICOPTER,
        MICROPLANE,
        HANGGLIDER,
        AIRBALLOON,
        OTHER
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setSubType(AircraftSubType aSubType);

    public void setSubType(String aSubType);

    public AircraftSubType getSubType();

    public IMsoModelIf.ModificationState getSubTypeState();

    public IAttributeIf.IMsoEnumIf<AircraftSubType> getSubTypeAttribute();

    public String getSubTypeName();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setInfrared(boolean hasInfrared);

    public boolean hasInfrared();

    public IMsoModelIf.ModificationState getInfraredState();

    public IAttributeIf.IMsoBooleanIf getInfraredAttribute();

    public void setNightvision(boolean hasNightvision);

    public boolean hasNightvision();

    public IMsoModelIf.ModificationState getNightvisionState();

    public IAttributeIf.IMsoBooleanIf getNightvisionAttribute();

    public void setPhoto(boolean hasPhoto);

    public boolean hasPhoto();

    public IMsoModelIf.ModificationState getPhotoState();

    public IAttributeIf.IMsoBooleanIf getPhotoAttribute();

    public void setRange(int aRange);

    public int getRange();

    public IMsoModelIf.ModificationState getRangeState();

    public IAttributeIf.IMsoIntegerIf getRangeAttribute();

    public void setSeats(int aSeats);

    public int getSeats();

    public IMsoModelIf.ModificationState getSeatsState();

    public IAttributeIf.IMsoIntegerIf getSeatsAttribute();

    public void setVideo(boolean hasVideo);

    public boolean hasVideo();

    public IMsoModelIf.ModificationState getVideoState();

    public IAttributeIf.IMsoBooleanIf getVideoAttribute();

    public void setVisibility(int aVisibility);

    public int getVisibility();

    public IMsoModelIf.ModificationState getVisibilityState();

    public IAttributeIf.IMsoIntegerIf getVisibilityAttribute();

}
