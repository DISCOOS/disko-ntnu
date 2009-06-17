package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

public class TeamImpl extends AbstractUnit implements ITeamIf
{
    private final AttributeImpl.MsoInteger m_speed = new AttributeImpl.MsoInteger(this, "speed");

    public TeamImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(theMsoModel, anObjectId, aNumber);
    }

    @Override
    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_speed);
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
        return UnitType.TEAM;
    }

    public UnitType getSubType()
    {
        return UnitType.TEAM; // todo expand
    }

    public String getSubTypeName()
    {
        return "TEAM"; // todo expand
    }

    public String toString()
    {
        return super.toString();
    }
}