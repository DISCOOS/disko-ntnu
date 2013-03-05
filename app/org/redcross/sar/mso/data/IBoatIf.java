package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;


/**
 *
 */
public interface IBoatIf extends ITransportIf
{
    public enum BoatSubType
    {
        SEARCH_AND_RESCUE,
        MOTOR_BOAT,
        SPEED_BOAT,
        SAILBOAT,
        FREIGHTER,
        CRUISER,
        OTHER
    }

    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setSubType(BoatSubType aSubType);

    public void setSubType(String aSubType);

    public BoatSubType getSubType();

    public IData.DataOrigin getSubTypeState();

    public IMsoAttributeIf.IMsoEnumIf<BoatSubType> getSubTypeAttribute();

    public String getSubTypeName();

    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/

    public void setCapacity(int aCapacity);

    public int getCapacity();

    public IData.DataOrigin getCapacityState();

    public IMsoAttributeIf.IMsoIntegerIf getCapacityAttribute();

    public void setDepth(int aDepth);

    public int getDepth();

    public IData.DataOrigin getDepthState();

    public IMsoAttributeIf.IMsoIntegerIf getDepthAttribute();

    public void setFreeboard(int aFreeboard);

    public int getFreeboard();

    public IData.DataOrigin getFreeboardState();

    public IMsoAttributeIf.IMsoIntegerIf getFreeboardAttribute();

    public void setHeight(int aHeight);

    public int getHeight();

    public IData.DataOrigin getHeightState();

    public IMsoAttributeIf.IMsoIntegerIf getHeightAttribute();

    public void setLength(int aLength);

    public int getLength();

    public IData.DataOrigin getLengthState();

    public IMsoAttributeIf.IMsoIntegerIf getLengthAttribute();

}
