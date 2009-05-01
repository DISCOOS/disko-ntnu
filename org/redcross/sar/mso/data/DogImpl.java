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
    protected void defineObjects()
    {
        super.defineObjects();
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
