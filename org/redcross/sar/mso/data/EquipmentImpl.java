package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.except.MsoCastException;

public class EquipmentImpl extends AbstractMsoObject implements IEquipmentIf
{

    public EquipmentImpl(IMsoModelIf theMsoModel, IMsoObjectIf.IObjectIdIf anObjectId)
    {
        super(theMsoModel, anObjectId);
    }

    protected void defineAttributes()
    {
    }

    protected void defineLists()
    {
    }

    protected void defineObjects()
    {
    }

    public static EquipmentImpl implementationOf(IEquipmentIf anInterface) throws MsoCastException
    {
        try
        {
            return (EquipmentImpl) anInterface;
        }
        catch (ClassCastException e)
        {
            throw new MsoCastException("Illegal cast to EquipmentImpl");
        }
    }

    public IMsoManagerIf.MsoClassCode getMsoClassCode()
    {
        return IMsoManagerIf.MsoClassCode.CLASSCODE_EQUIPMENT;
    }

}