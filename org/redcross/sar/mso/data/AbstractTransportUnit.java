package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

public abstract class AbstractTransportUnit extends AbstractUnit implements ITransportIf
{
    public void setIdentifier(String aIdentifier)
    {
        m_identifier.setValue(aIdentifier);
    }

    private AttributeImpl.MsoString m_identifier = new AttributeImpl.MsoString(this, "Identifier");


    public AbstractTransportUnit(IMsoModelIf theMsoModel,IMsoObjectIf.IObjectIdIf anObjectId, int aNumber)
    {
        super(theMsoModel, anObjectId, aNumber);
    }

    public AbstractTransportUnit(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId, int aNumber, String anIdentifier)
    {
        super(theMsoModel, anObjectId, aNumber);
        m_identifier.setValue(anIdentifier);
    }

    protected void defineAttributes()
    {
        super.defineAttributes();
        addAttribute(m_identifier);
    }

    protected void defineLists()
    {
        super.defineLists();
    }

    protected void defineReferences()
    {
        super.defineReferences();
    }

//    public String toString()
//    {
//        String id = m_identifier == null ? "" : m_identifier.getString();
//        return super.toString() + " " + id;
//    }
}