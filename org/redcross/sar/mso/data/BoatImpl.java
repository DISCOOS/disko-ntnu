package org.redcross.sar.mso.data;

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
        m_subType.setValue(aSubType);
    }

    public void setSubType(String aSubType)
    {
        m_subType.setValue(aSubType);
    }

    public BoatSubType getSubType()
    {
        return m_subType.getValue();
    }

    public IMsoModelIf.ModificationState getSubTypeState()
    {
        return m_subType.getState();
    }

    public IAttributeIf.IMsoEnumIf<BoatSubType> getSubTypeAttribute()
    {
        return m_subType;
    }

    public String getSubTypeName()
    {
        return Internationalization.translate(m_subType.getValue());
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

    public IMsoModelIf.ModificationState getCapacityState()
    {
        return m_capacity.getState();
    }

    public IAttributeIf.IMsoIntegerIf getCapacityAttribute()
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

    public IMsoModelIf.ModificationState getDepthState()
    {
        return m_depth.getState();
    }

    public IAttributeIf.IMsoIntegerIf getDepthAttribute()
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

    public IMsoModelIf.ModificationState getFreeboardState()
    {
        return m_freeboard.getState();
    }

    public IAttributeIf.IMsoIntegerIf getFreeboardAttribute()
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

    public IMsoModelIf.ModificationState getHeightState()
    {
        return m_height.getState();
    }

    public IAttributeIf.IMsoIntegerIf getHeightAttribute()
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

    public IMsoModelIf.ModificationState getLengthState()
    {
        return m_length.getState();
    }

    public IAttributeIf.IMsoIntegerIf getLengthAttribute()
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