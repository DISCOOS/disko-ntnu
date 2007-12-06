package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

/**
 * Aircraft unit
 */
public class AircraftImpl extends AbstractTransportUnit implements IAircraftIf
{
    private final AttributeImpl.MsoInteger m_averageSpeed = new AttributeImpl.MsoInteger(this, "AverageSpeed");
    private final AttributeImpl.MsoInteger m_flightTime = new AttributeImpl.MsoInteger(this, "FlightTime");
    private final AttributeImpl.MsoBoolean m_infrared = new AttributeImpl.MsoBoolean(this, "Infrared");
    private final AttributeImpl.MsoInteger m_maxSpeed = new AttributeImpl.MsoInteger(this, "MaxSpeed");
    private final AttributeImpl.MsoBoolean m_nightvision = new AttributeImpl.MsoBoolean(this, "Nightvision");
    private final AttributeImpl.MsoBoolean m_photo = new AttributeImpl.MsoBoolean(this, "Photo");
    private final AttributeImpl.MsoInteger m_range = new AttributeImpl.MsoInteger(this, "Range");
    private final AttributeImpl.MsoInteger m_seats = new AttributeImpl.MsoInteger(this, "Seats");
    private final AttributeImpl.MsoBoolean m_video = new AttributeImpl.MsoBoolean(this, "Video");
    private final AttributeImpl.MsoInteger m_visibility = new AttributeImpl.MsoInteger(this, "Visibility");
    private final AttributeImpl.MsoEnum<AircraftSubType> m_subType = new AttributeImpl.MsoEnum<AircraftSubType>(this, "SubType", AircraftSubType.LIGHT_AIRCRAFT);

    public AircraftImpl(IMsoObjectIf.IObjectIdIf anObjectId, int aNumber, String anIdentifier)
    {
        super(anObjectId, aNumber, anIdentifier);
    }

    public AircraftImpl(IMsoObjectIf.IObjectIdIf objectId, int number)
    {
    	super(objectId, number);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_averageSpeed);
        addAttribute(m_flightTime);
        addAttribute(m_infrared);
        addAttribute(m_maxSpeed);
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
    protected void defineReferences()
    {
        super.defineReferences();
    }

    @Override
    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return super.addObjectReference(anObject, aReferenceName);
    }

    @Override
    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName)
    {
        return super.removeObjectReference(anObject, aReferenceName);
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

    public IMsoModelIf.ModificationState getSubTypeState()
    {
        return m_subType.getState();
    }

    public IAttributeIf.IMsoEnumIf<AircraftSubType> getSubTypeAttribute()
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

    public void setAverageSpeed(int anAverageSpeed)
    {
        m_averageSpeed.setValue(anAverageSpeed);
    }

    public int getAverageSpeed()
    {
        return m_averageSpeed.intValue();
    }

    public IMsoModelIf.ModificationState getAverageSpeedState()
    {
        return m_averageSpeed.getState();
    }

    public IAttributeIf.IMsoIntegerIf getAverageSpeedAttribute()
    {
        return m_averageSpeed;
    }

    public void setFlightTime(int aFlightTime)
    {
        m_flightTime.setValue(aFlightTime);
    }

    public int getFlightTime()
    {
        return m_flightTime.intValue();
    }

    public IMsoModelIf.ModificationState getFlightTimeState()
    {
        return m_flightTime.getState();
    }

    public IAttributeIf.IMsoIntegerIf getFlightTimeAttribute()
    {
        return m_flightTime;
    }

    public void setInfrared(boolean hasInfrared)
    {
        m_infrared.setValue(hasInfrared);
    }

    public boolean hasInfrared()
    {
        return m_infrared.booleanValue();
    }

    public IMsoModelIf.ModificationState getInfraredState()
    {
        return m_infrared.getState();
    }

    public IAttributeIf.IMsoBooleanIf getInfraredAttribute()
    {
        return m_infrared;
    }

    public void setMaxSpeed(int aMaxSpeed)
    {
        m_maxSpeed.setValue(aMaxSpeed);
    }

    public int getMaxSpeed()
    {
        return m_maxSpeed.intValue();
    }

    public IMsoModelIf.ModificationState getMaxSpeedState()
    {
        return m_maxSpeed.getState();
    }

    public IAttributeIf.IMsoIntegerIf getMaxSpeedAttribute()
    {
        return m_maxSpeed;
    }

    public void setNightvision(boolean hasNightvision)
    {
        m_nightvision.setValue(hasNightvision);
    }

    public boolean hasNightvision()
    {
        return m_nightvision.booleanValue();
    }

    public IMsoModelIf.ModificationState getNightvisionState()
    {
        return m_nightvision.getState();
    }

    public IAttributeIf.IMsoBooleanIf getNightvisionAttribute()
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

    public IMsoModelIf.ModificationState getPhotoState()
    {
        return m_photo.getState();
    }

    public IAttributeIf.IMsoBooleanIf getPhotoAttribute()
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

    public IMsoModelIf.ModificationState getRangeState()
    {
        return m_range.getState();
    }

    public IAttributeIf.IMsoIntegerIf getRangeAttribute()
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

    public IMsoModelIf.ModificationState getSeatsState()
    {
        return m_seats.getState();
    }

    public IAttributeIf.IMsoIntegerIf getSeatsAttribute()
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

    public IMsoModelIf.ModificationState getVideoState()
    {
        return m_video.getState();
    }

    public IAttributeIf.IMsoBooleanIf getVideoAttribute()
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

    public IMsoModelIf.ModificationState getVisibilityState()
    {
        return m_visibility.getState();
    }

    public IAttributeIf.IMsoIntegerIf getVisibilityAttribute()
    {
        return m_visibility;
    }
}