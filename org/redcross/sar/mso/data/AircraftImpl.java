package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoModelIf;

/**
 * Aircraft unit
 */
public class AircraftImpl extends AbstractTransportUnit implements IAircraftIf
{
    private final AttributeImpl.MsoBoolean m_infrared = new AttributeImpl.MsoBoolean(this, "Infrared");
    private final AttributeImpl.MsoBoolean m_nightvision = new AttributeImpl.MsoBoolean(this, "Nightvision");
    private final AttributeImpl.MsoBoolean m_photo = new AttributeImpl.MsoBoolean(this, "Photo");
    private final AttributeImpl.MsoInteger m_range = new AttributeImpl.MsoInteger(this, "Range");
    private final AttributeImpl.MsoInteger m_seats = new AttributeImpl.MsoInteger(this, "Seats");
    private final AttributeImpl.MsoBoolean m_video = new AttributeImpl.MsoBoolean(this, "Video");
    private final AttributeImpl.MsoInteger m_visibility = new AttributeImpl.MsoInteger(this, "Visibility");
    private final AttributeImpl.MsoEnum<AircraftSubType> m_subType = new AttributeImpl.MsoEnum<AircraftSubType>(this, "SubType", 1, AircraftSubType.LIGHT_AIRCRAFT);

    public AircraftImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber, String anIdentifier)
    {
        super(theMsoModel, anObjectId, aNumber, anIdentifier);
    }

    public AircraftImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf objectId, int number)
    {
    	super(theMsoModel, objectId, number);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_infrared);
        addAttribute(m_nightvision);
        addAttribute(m_photo);
        addAttribute(m_range);
        addAttribute(m_seats);
        addAttribute(m_video);
        addAttribute(m_visibility);
        addAttribute(m_subType);
    }

    @Override
    protected void defineLists()
    {
        super.defineLists();
    }

    @Override
    protected void defineObjects()
    {
        super.defineObjects();
    }

    protected UnitType getTypeBySubclass()
    {
        return IUnitIf.UnitType.AIRCRAFT;
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setSubType(AircraftSubType aSubType)
    {
        m_subType.setValue(aSubType);
    }

    public void setSubType(String aSubType)
    {
        m_subType.setValue(aSubType);
    }

    public AircraftSubType getSubType()
    {
        return m_subType.getValue();
    }

    public IData.DataOrigin getSubTypeState()
    {
        return m_subType.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<AircraftSubType> getSubTypeAttribute()
    {
        return m_subType;
    }

    public String getSubTypeName()
    {
        return m_subType.getInternationalName();
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setInfrared(boolean hasInfrared)
    {
        m_infrared.setValue(hasInfrared);
    }

    public boolean hasInfrared()
    {
        return m_infrared.booleanValue();
    }

    public IData.DataOrigin getInfraredState()
    {
        return m_infrared.getOrigin();
    }

    public IMsoAttributeIf.IMsoBooleanIf getInfraredAttribute()
    {
        return m_infrared;
    }

    public void setNightvision(boolean hasNightvision)
    {
        m_nightvision.setValue(hasNightvision);
    }

    public boolean hasNightvision()
    {
        return m_nightvision.booleanValue();
    }

    public IData.DataOrigin getNightvisionState()
    {
        return m_nightvision.getOrigin();
    }

    public IMsoAttributeIf.IMsoBooleanIf getNightvisionAttribute()
    {
        return m_nightvision;
    }

    public void setPhoto(boolean hasPhoto)
    {
        m_photo.setValue(hasPhoto);
    }

    public boolean hasPhoto()
    {
        return m_photo.booleanValue();
    }

    public IData.DataOrigin getPhotoState()
    {
        return m_photo.getOrigin();
    }

    public IMsoAttributeIf.IMsoBooleanIf getPhotoAttribute()
    {
        return m_photo;
    }

    public void setRange(int aRange)
    {
        m_range.setValue(aRange);
    }

    public int getRange()
    {
        return m_range.intValue();
    }

    public IData.DataOrigin getRangeState()
    {
        return m_range.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getRangeAttribute()
    {
        return m_range;
    }

    public void setSeats(int aSeats)
    {
        m_seats.setValue(aSeats);
    }

    public int getSeats()
    {
        return m_seats.intValue();
    }

    public IData.DataOrigin getSeatsState()
    {
        return m_seats.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getSeatsAttribute()
    {
        return m_seats;
    }

    public void setVideo(boolean hasVideo)
    {
        m_video.setValue(hasVideo);
    }

    public boolean hasVideo()
    {
        return m_video.booleanValue();
    }

    public IData.DataOrigin getVideoState()
    {
        return m_video.getOrigin();
    }

    public IMsoAttributeIf.IMsoBooleanIf getVideoAttribute()
    {
        return m_video;
    }

    public void setVisibility(int aVisibility)
    {
        m_visibility.setValue(aVisibility);
    }

    public int getVisibility()
    {
        return m_visibility.intValue();
    }

    public IData.DataOrigin getVisibilityState()
    {
        return m_visibility.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getVisibilityAttribute()
    {
        return m_visibility;
    }
}