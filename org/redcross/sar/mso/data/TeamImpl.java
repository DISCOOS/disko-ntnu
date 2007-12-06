package org.redcross.sar.mso.data;

public class TeamImpl extends AbstractUnit implements ITeamIf
{
    private final AttributeImpl.MsoInteger m_speed = new AttributeImpl.MsoInteger(this, "speed");

    public TeamImpl(IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(anObjectId, aNumber);
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
        return IUnitIf.UnitType.TEAM;
    }

    public Enum getSubType()
    {
        return IUnitIf.UnitType.TEAM; // todo expand
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