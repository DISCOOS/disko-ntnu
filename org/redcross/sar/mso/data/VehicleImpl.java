package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

public class VehicleImpl extends AbstractTransportUnit implements IVehicleIf
{
    public VehicleImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(theMsoModel, anObjectId, aNumber);
    }

    public VehicleImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber, String anIdentifier)
    {
        super(theMsoModel, anObjectId, aNumber, anIdentifier);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
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
        return IUnitIf.UnitType.VEHICLE;
    }

    public Enum getSubType()
    {
        return IUnitIf.UnitType.VEHICLE; // todo expand
    }

    public String getSubTypeName()
    {
        return "VEHICLE"; // todo expand
    }

    public String toString()
    {
        return super.toString();
    }
}