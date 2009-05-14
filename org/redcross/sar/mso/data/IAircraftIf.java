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

    public IMsoAttributeIf.IMsoEnumIf<AircraftSubType> getSubTypeAttribute();

    public String getSubTypeName();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setInfrared(boolean hasInfrared);

    public boolean hasInfrared();

    public IMsoModelIf.ModificationState getInfraredState();

    public IMsoAttributeIf.IMsoBooleanIf getInfraredAttribute();

    public void setNightvision(boolean hasNightvision);

    public boolean hasNightvision();

    public IMsoModelIf.ModificationState getNightvisionState();

    public IMsoAttributeIf.IMsoBooleanIf getNightvisionAttribute();

    public void setPhoto(boolean hasPhoto);

    public boolean hasPhoto();

    public IMsoModelIf.ModificationState getPhotoState();

    public IMsoAttributeIf.IMsoBooleanIf getPhotoAttribute();

    public void setRange(int aRange);

    public int getRange();

    public IMsoModelIf.ModificationState getRangeState();

    public IMsoAttributeIf.IMsoIntegerIf getRangeAttribute();

    public void setSeats(int aSeats);

    public int getSeats();

    public IMsoModelIf.ModificationState getSeatsState();

    public IMsoAttributeIf.IMsoIntegerIf getSeatsAttribute();

    public void setVideo(boolean hasVideo);

    public boolean hasVideo();

    public IMsoModelIf.ModificationState getVideoState();

    public IMsoAttributeIf.IMsoBooleanIf getVideoAttribute();

    public void setVisibility(int aVisibility);

    public int getVisibility();

    public IMsoModelIf.ModificationState getVisibilityState();

    public IMsoAttributeIf.IMsoIntegerIf getVisibilityAttribute();

}
