package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

/**
 *
 */
public class DogImpl extends AbstractUnit implements IDogIf
{
    public DogImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(theMsoModel, anObjectId, aNumber);
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
        return IUnitIf.UnitType.DOG;
    }

    public Enum getSubType()
    {
        return IUnitIf.UnitType.DOG; // todo expand
    }

    public String getSubTypeName()
    {
        return "CAR"; // todo expand
    }

    public String toString()
    {
        return super.toString();
    }

}
