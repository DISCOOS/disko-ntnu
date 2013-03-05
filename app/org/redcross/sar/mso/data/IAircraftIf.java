package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;


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

    public IData.DataOrigin getSubTypeState();

    public IMsoAttributeIf.IMsoEnumIf<AircraftSubType> getSubTypeAttribute();

    public String getSubTypeName();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setInfrared(boolean hasInfrared);

    public boolean hasInfrared();

    public IData.DataOrigin getInfraredState();

    public IMsoAttributeIf.IMsoBooleanIf getInfraredAttribute();

    public void setNightvision(boolean hasNightvision);

    public boolean hasNightvision();

    public IData.DataOrigin getNightvisionState();

    public IMsoAttributeIf.IMsoBooleanIf getNightvisionAttribute();

    public void setPhoto(boolean hasPhoto);

    public boolean hasPhoto();

    public IData.DataOrigin getPhotoState();

    public IMsoAttributeIf.IMsoBooleanIf getPhotoAttribute();

    public void setRange(int aRange);

    public int getRange();

    public IData.DataOrigin getRangeState();

    public IMsoAttributeIf.IMsoIntegerIf getRangeAttribute();

    public void setSeats(int aSeats);

    public int getSeats();

    public IData.DataOrigin getSeatsState();

    public IMsoAttributeIf.IMsoIntegerIf getSeatsAttribute();

    public void setVideo(boolean hasVideo);

    public boolean hasVideo();

    public IData.DataOrigin getVideoState();

    public IMsoAttributeIf.IMsoBooleanIf getVideoAttribute();

    public void setVisibility(int aVisibility);

    public int getVisibility();

    public IData.DataOrigin getVisibilityState();

    public IMsoAttributeIf.IMsoIntegerIf getVisibilityAttribute();

}
