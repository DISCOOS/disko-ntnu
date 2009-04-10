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