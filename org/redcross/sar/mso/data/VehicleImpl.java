package org.redcross.sar.mso.data;

public class VehicleImpl extends AbstractTransportUnit implements IVehicleIf
{
    public VehicleImpl(IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(anObjectId, aNumber);
    }

    public VehicleImpl(IMsoObjectIf.IObjectIdIf anObjectId, int aNumber, String anIdentifier)
    {
        super(anObjectId, aNumber, anIdentifier);
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