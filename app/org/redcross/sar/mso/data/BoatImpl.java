package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.MsoCastException;


/**
 * Boat unit
 */
public class BoatImpl extends AbstractTransportUnit implements IBoatIf
{
    private final AttributeImpl.MsoInteger m_capacity = new AttributeImpl.MsoInteger(this, "Capacity");
    private final AttributeImpl.MsoInteger m_depth = new AttributeImpl.MsoInteger(this, "Depth");
    private final AttributeImpl.MsoInteger m_freeboard = new AttributeImpl.MsoInteger(this, "Freeboard");
    private final AttributeImpl.MsoInteger m_height = new AttributeImpl.MsoInteger(this, "Height");
    private final AttributeImpl.MsoInteger m_length = new AttributeImpl.MsoInteger(this, "Length");
    private final AttributeImpl.MsoEnum<BoatSubType> m_subType = new AttributeImpl.MsoEnum<BoatSubType>(this, "SubType", 1, BoatSubType.SEARCH_AND_RESCUE);


    public BoatImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(theMsoModel, anObjectId, aNumber);
    }


    public BoatImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber, String anIdentifier)
    {
        super(theMsoModel, anObjectId, aNumber, anIdentifier);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_capacity);
        addAttribute(m_depth);
        addAttribute(m_freeboard);
        addAttribute(m_height);
        addAttribute(m_length);
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
        return IUnitIf.UnitType.BOAT;
    }

    public static BoatImpl implementationOf(IBoatIf anInterface) throws MsoCastException
    {
        try
        {
            return (BoatImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to CmdPostImpl");
        }
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setSubType(BoatSubType aSubType)
    {
        m_subType.set(aSubType);
    }

    public void setSubType(String aSubType)
    {
        m_subType.set(aSubType);
    }

    public BoatSubType getSubType()
    {
        return m_subType.get();
    }

    public IData.DataOrigin getSubTypeState()
    {
        return m_subType.getOrigin();
    }

    public IMsoAttributeIf.IMsoEnumIf<BoatSubType> getSubTypeAttribute()
    {
        return m_subType;
    }

    public String getSubTypeName()
    {
        return Internationalization.translate(m_subType.get());
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setCapacity(int aCapacity)
    {
        m_capacity.setValue(aCapacity);
    }

    public int getCapacity()
    {
        return m_capacity.intValue();
    }

    public IData.DataOrigin getCapacityState()
    {
        return m_capacity.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getCapacityAttribute()
    {
        return m_capacity;
    }

    public void setDepth(int aDepth)
    {
        m_depth.setValue(aDepth);
    }

    public int getDepth()
    {
        return m_depth.intValue();
    }

    public IData.DataOrigin getDepthState()
    {
        return m_depth.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getDepthAttribute()
    {
        return m_depth;
    }

    public void setFreeboard(int aFreeboard)
    {
        m_freeboard.setValue(aFreeboard);
    }

    public int getFreeboard()
    {
        return m_freeboard.intValue();
    }

    public IData.DataOrigin getFreeboardState()
    {
        return m_freeboard.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getFreeboardAttribute()
    {
        return m_freeboard;
    }

    public void setHeight(int aHeight)
    {
        m_height.setValue(aHeight);
    }

    public int getHeight()
    {
        return m_height.intValue();
    }

    public IData.DataOrigin getHeightState()
    {
        return m_height.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getHeightAttribute()
    {
        return m_height;
    }

    public void setLength(int aLength)
    {
        m_length.setValue(aLength);
    }

    public int getLength()
    {
        return m_length.intValue();
    }

    public IData.DataOrigin getLengthState()
    {
        return m_length.getOrigin();
    }

    public IMsoAttributeIf.IMsoIntegerIf getLengthAttribute()
    {
        return m_length;
    }

    /*-------------------------------------------------------------------------------------------
    * Other methods
    *-------------------------------------------------------------------------------------------*/

    public String toString()
    {
        return super.toString();
    }
}